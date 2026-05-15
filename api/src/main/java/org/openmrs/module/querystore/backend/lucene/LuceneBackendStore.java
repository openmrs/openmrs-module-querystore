/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.backend.lucene;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;
import org.openmrs.module.querystore.QueryStoreConstants;
import org.openmrs.module.querystore.backend.BackendCapabilities;
import org.openmrs.module.querystore.backend.BackendDocs;
import org.openmrs.module.querystore.backend.BackendStore;
import org.openmrs.module.querystore.backend.BulkWriteResult;
import org.openmrs.module.querystore.backend.DocFailure;
import org.openmrs.module.querystore.backend.Filter;
import org.openmrs.module.querystore.backend.HealthStatus;
import org.openmrs.module.querystore.backend.Hit;
import org.openmrs.module.querystore.backend.MetadataCodec;
import org.openmrs.module.querystore.backend.SchemaSpec;
import org.openmrs.module.querystore.backend.SearchRequest;
import org.openmrs.module.querystore.backend.SearchResult;
import org.openmrs.module.querystore.backend.TopKHits;
import org.openmrs.module.querystore.backend.UnsupportedBackendOperationException;
import org.openmrs.module.querystore.backend.WriteResult;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.util.OpenmrsUtil;

/**
 * Embedded-Lucene reference {@link BackendStore} (Decision 3, "medium" tier). One {@code FSDirectory}
 * per resource type under the OpenMRS app-data directory (Decision 4), native BM25 over a
 * {@link StandardAnalyzer} text field, and brute-force in-process cosine kNN over the stored
 * embedding bytes. The Lucene line is pinned to 8.11.2 to match what OpenMRS core ships
 * transitively via Hibernate Search 6.2.4 (see {@code pom.xml} and issue #13) — Lucene 8 has no
 * native HNSW kNN, so the kNN scan trades native HNSW for capability parity with the MySQL tier
 * (sub-linear when a patient filter narrows the candidate set; O(N) on un-filtered cross-patient
 * queries — the same ceiling the tier already advertised as "single-host, bounded by one JVM").
 * Single-host, no replication; index loss requires rebuild from core. Hybrid fusion stays at the
 * service layer per Decision 3's "uniformity is the default" principle.
 */
public class LuceneBackendStore implements BackendStore, Closeable {

	private static final Log log = LogFactory.getLog(LuceneBackendStore.class);

	private static final int RECOMMENDED_MAX_CORPUS = 5_000_000;

	private static final int BATCH_SIZE = 500;

	// Stripe count for the conditional-upsert read-then-update window. MySQL gets this for free
	// from row-level locking in ON DUPLICATE KEY UPDATE; Lucene has to provide its own narrow
	// serialization. 64 stripes is enough that same-UUID writes are reliably blocked (one stripe
	// per UUID hash) without globally bottlenecking unrelated writes the way a single monitor would.
	// String.intern() was considered and rejected: it leaks every UUID seen by the JVM into the
	// string table and contends globally with any other interner consumer.
	private static final int UPSERT_LOCK_STRIPES = 64;

	private final Object[] upsertLocks = new Object[UPSERT_LOCK_STRIPES];

	private final Path indexRoot;

	private final LuceneSchemaManager schemaManager;

	private final StandardAnalyzer analyzer = new StandardAnalyzer();

	/** Spring-friendly constructor: indexes live under {@code <appdata>/querystore/lucene/}. */
	public LuceneBackendStore() {
		this(defaultIndexRoot());
	}

	public LuceneBackendStore(Path indexRoot) {
		this.indexRoot = indexRoot;
		this.schemaManager = new LuceneSchemaManager(indexRoot);
		for (int i = 0; i < UPSERT_LOCK_STRIPES; i++) {
			upsertLocks[i] = new Object();
		}
	}

	private static Path defaultIndexRoot() {
		return Paths.get(OpenmrsUtil.getApplicationDataDirectory(),
		    QueryStoreConstants.MODULE_ID, "lucene");
	}

	@Override
	public void ensureSchema(String resourceType, SchemaSpec spec) {
		schemaManager.ensureWriter(resourceType);
	}

	@Override
	public void deleteSchema(String resourceType) {
		schemaManager.dropWriter(resourceType);
	}

	@Override
	public WriteResult upsert(QueryDocument doc) {
		BackendDocs.validate(doc);
		IndexWriter writer = schemaManager.ensureWriter(doc.getResourceType());
		try {
			applyConditionalUpsert(writer, doc);
			writer.commit();
			return WriteResult.success();
		}
		catch (IOException e) {
			log.warn("upsert failed for " + doc.getResourceType() + "/" + doc.getResourceUuid(), e);
			return WriteResult.failed(failure(doc.getResourceType(), doc.getResourceUuid(), e));
		}
	}

	@Override
	public WriteResult delete(String resourceType, String resourceUuid) {
		IndexWriter writer = schemaManager.ensureWriter(resourceType);
		try {
			writer.deleteDocuments(new Term(LuceneFieldNames.RESOURCE_UUID, resourceUuid));
			writer.commit();
			return WriteResult.success();
		}
		catch (IOException e) {
			log.warn("delete failed for " + resourceType + "/" + resourceUuid, e);
			return WriteResult.failed(failure(resourceType, resourceUuid, e));
		}
	}

	/**
	 * Bulk upsert with fail-fast input validation: any document missing required identity fields
	 * raises {@link IllegalArgumentException} before any write reaches a writer, so the call is
	 * effectively all-or-nothing on input validity. Partial failure inside the per-doc loop is
	 * still surfaced through {@link BulkWriteResult} (e.g. a corrupt-index hit on one document
	 * while siblings succeed).
	 */
	@Override
	public BulkWriteResult bulkUpsert(List<QueryDocument> docs) {
		Map<String, List<QueryDocument>> byType = new LinkedHashMap<>();
		for (QueryDocument doc : docs) {
			BackendDocs.validate(doc);
			byType.computeIfAbsent(doc.getResourceType(), k -> new ArrayList<>()).add(doc);
		}
		List<DocFailure> failures = new ArrayList<>();
		int succeeded = 0;
		for (Map.Entry<String, List<QueryDocument>> entry : byType.entrySet()) {
			succeeded += batchUpsert(entry.getKey(), entry.getValue(), failures);
		}
		return new BulkWriteResult(docs.size(), succeeded, failures);
	}

	@Override
	public BulkWriteResult bulkDelete(String resourceType, List<String> resourceUuids) {
		if (resourceUuids.isEmpty()) {
			return new BulkWriteResult(0, 0, Collections.emptyList());
		}
		IndexWriter writer = schemaManager.ensureWriter(resourceType);
		List<DocFailure> failures = new ArrayList<>();
		int succeeded = 0;
		for (int start = 0; start < resourceUuids.size(); start += BATCH_SIZE) {
			int end = Math.min(start + BATCH_SIZE, resourceUuids.size());
			Term[] terms = new Term[end - start];
			for (int i = start; i < end; i++) {
				terms[i - start] = new Term(LuceneFieldNames.RESOURCE_UUID, resourceUuids.get(i));
			}
			try {
				writer.deleteDocuments(terms);
				succeeded += terms.length;
			}
			catch (IOException e) {
				log.warn("bulkDelete chunk failed for " + resourceType, e);
				for (int i = start; i < end; i++) {
					failures.add(failure(resourceType, resourceUuids.get(i), e));
				}
			}
		}
		try {
			writer.commit();
		}
		catch (IOException e) {
			log.warn("bulkDelete commit failed for " + resourceType, e);
			// Per-doc successes already counted are still durable on a retry; surface a single
			// rollup failure so the caller knows the operation didn't fully complete.
			failures.add(failure(resourceType, null, e));
		}
		return new BulkWriteResult(resourceUuids.size(), succeeded, failures);
	}

	@Override
	public BulkWriteResult bulkDeleteByPatient(String patientUuid) {
		Set<String> indexNames = schemaManager.knownIndexNames();
		if (indexNames.isEmpty()) {
			indexNames = schemaManager.listAllIndexes();
		}
		List<DocFailure> failures = new ArrayList<>();
		int totalAttempted = 0;
		int totalDeleted = 0;
		TermQuery patientQuery = new TermQuery(new Term(LuceneFieldNames.PATIENT_UUID, patientUuid));
		for (String indexName : indexNames) {
			String resourceType = BackendDocs.stripPrefix(indexName);
			IndexWriter writer = schemaManager.ensureWriter(resourceType);
			int matchedHere = 0;
			try {
				// Count before delete using a single reader open. before/after counting would
				// over-count under concurrent writes and pays for two NRT reader opens; one
				// pre-delete count gives both the right answer and half the I/O. Attempted is
				// charged here so a failure in deleteDocuments/commit still reports the docs
				// that were targeted in this index; a failure inside countMatching keeps
				// matchedHere at 0 and contributes nothing, which is the honest answer.
				matchedHere = countMatching(writer, patientQuery);
				totalAttempted += matchedHere;
				writer.deleteDocuments(patientQuery);
				writer.commit();
				totalDeleted += matchedHere;
			}
			catch (IOException e) {
				log.warn("bulkDeleteByPatient failed for " + indexName, e);
				failures.add(failure(resourceType, patientUuid, e));
			}
		}
		return new BulkWriteResult(totalAttempted, totalDeleted, failures);
	}

	@Override
	public boolean existsByPatient(String patientUuid) {
		if (StringUtils.isBlank(patientUuid)) {
			return false;
		}
		Set<String> indexNames = schemaManager.knownIndexNames();
		if (indexNames.isEmpty()) {
			indexNames = schemaManager.listAllIndexes();
		}
		if (indexNames.isEmpty()) {
			return false;
		}
		TermQuery patientQuery = new TermQuery(new Term(LuceneFieldNames.PATIENT_UUID, patientUuid));
		for (String indexName : indexNames) {
			String resourceType = BackendDocs.stripPrefix(indexName);
			IndexWriter writer = schemaManager.ensureWriter(resourceType);
			try {
				if (countMatching(writer, patientQuery) > 0) {
					return true;
				}
			}
			catch (IOException e) {
				log.warn("existsByPatient probe failed for " + indexName, e);
			}
		}
		return false;
	}

	@Override
	public SearchResult bm25(SearchRequest req) {
		if (StringUtils.isBlank(req.getQueryText()) || req.getLimit() <= 0) {
			return SearchResult.empty();
		}
		Query filterQuery = LuceneFilterTranslator.toQuery(req.getFilters());
		// Parse the BM25 query once per request; it doesn't vary by resource type, and on wildcard
		// reads (every querystore_* index) reparsing inside the loop fans the cost out by the type
		// count for no behavioural reason.
		Query bm25Query;
		try {
			bm25Query = buildBm25Query(req.getQueryText(), filterQuery);
		}
		catch (ParseException e) {
			throw new IllegalStateException("BM25 query parse failed for: " + req.getQueryText(), e);
		}
		PriorityQueue<Hit> heap = TopKHits.heap(req.getLimit());
		for (String resourceType : resolveResourceTypes(req)) {
			try {
				for (Hit h : searchOneIndex(resourceType, bm25Query, req.getLimit())) {
					TopKHits.offer(heap, h, req.getLimit());
				}
			}
			catch (IOException e) {
				throw new IllegalStateException("BM25 query on " + resourceType + " failed", e);
			}
		}
		return TopKHits.materialise(heap, req.getLimit());
	}

	@Override
	public SearchResult knn(SearchRequest req) {
		if (req.getQueryVector() == null || req.getLimit() <= 0) {
			return SearchResult.empty();
		}
		// Brute-force cosine over the filter-matched candidate set — the inverted index narrows by
		// patient_uuid (and any other structured filters) first, then the scan computes cosine on
		// each survivor's stored embedding bytes. Mirrors the MySQL tier's knnSingleTable: O(k) on
		// a patient-scoped query (k = patient's documents), O(N) only on un-filtered cross-patient
		// queries — matching the tier's "single-host, single JVM" ceiling per Decision 3.
		Query filterQuery = LuceneFilterTranslator.toQuery(req.getFilters());
		Query scanQuery = filterQuery != null ? filterQuery : new MatchAllDocsQuery();
		float[] queryVector = req.getQueryVector();
		double queryNorm = LuceneVectorCodec.norm(queryVector);
		PriorityQueue<Hit> heap = TopKHits.heap(req.getLimit());
		for (String resourceType : resolveResourceTypes(req)) {
			try {
				knnSingleIndex(resourceType, scanQuery, queryVector, queryNorm, heap, req.getLimit());
			}
			catch (IOException e) {
				throw new IllegalStateException("kNN scan on " + resourceType + " failed", e);
			}
		}
		return TopKHits.materialise(heap, req.getLimit());
	}

	@Override
	public SearchResult hybrid(SearchRequest req) {
		throw new UnsupportedBackendOperationException(
		        "Lucene backend has no native hybrid query; service layer must fuse bm25() + knn() ranks");
	}

	@Override
	public BackendCapabilities capabilities() {
		return new BackendCapabilities(
		        true,
		        false,
		        false,
		        RECOMMENDED_MAX_CORPUS,
		        EnumSet.of(Filter.Kind.TERM, Filter.Kind.IN, Filter.Kind.RANGE, Filter.Kind.PATIENT_SCOPE));
	}

	@Override
	public HealthStatus health() {
		// Configuration check only — surface a clear failure when the bean was built without an
		// index root (e.g. a test context that bypassed the default-location factory). v1 doesn't
		// touch disk here; per-writer dirty-segment scans on a real corpus would be expensive
		// enough to defeat the purpose of a fast probe.
		if (indexRoot == null) {
			return HealthStatus.unhealthy("indexRoot not configured");
		}
		return HealthStatus.healthy();
	}

	@Override
	public void close() {
		schemaManager.close();
		analyzer.close();
	}

	// ---------- internals ----------

	private void applyConditionalUpsert(IndexWriter writer, QueryDocument doc) throws IOException {
		Term uuidTerm = new Term(LuceneFieldNames.RESOURCE_UUID, doc.getResourceUuid());
		// Conditional-upsert guard: when both sides have last_modified, drop strictly older writes
		// so a slow projection cannot overwrite a fresher document. Equal-or-newer applies so the
		// same write reapplied is idempotent; null on either side falls back to last-write-wins.
		// The lookup-then-write window is serialized per resource_uuid via the stripe lock — see
		// UPSERT_LOCK_STRIPES — so concurrent writes to the same UUID can't both decide "I'm newer".
		synchronized (lockFor(doc.getResourceUuid())) {
			if (doc.getLastModified() != null) {
				Long storedMillis = findStoredLastModified(writer, uuidTerm);
				if (storedMillis != null && storedMillis > doc.getLastModified().toEpochMilli()) {
					return;
				}
			}
			writer.updateDocument(uuidTerm, toLuceneDocument(doc));
		}
	}

	private Object lockFor(String resourceUuid) {
		return upsertLocks[(resourceUuid.hashCode() & Integer.MAX_VALUE) % UPSERT_LOCK_STRIPES];
	}

	private Long findStoredLastModified(IndexWriter writer, Term uuidTerm) throws IOException {
		try (DirectoryReader reader = DirectoryReader.open(writer)) {
			IndexSearcher searcher = new IndexSearcher(reader);
			TopDocs hits = searcher.search(new TermQuery(uuidTerm), 1);
			if (hits.scoreDocs.length == 0) {
				return null;
			}
			ScoreDoc hit = hits.scoreDocs[0];
			int leafIdx = leafContextOf(reader, hit.doc);
			NumericDocValues values = reader.leaves().get(leafIdx).reader()
			        .getNumericDocValues(LuceneFieldNames.LAST_MODIFIED);
			if (values == null) {
				return null;
			}
			int localDocId = hit.doc - reader.leaves().get(leafIdx).docBase;
			if (!values.advanceExact(localDocId)) {
				return null;
			}
			return values.longValue();
		}
	}

	private static int leafContextOf(DirectoryReader reader, int globalDocId) {
		List<org.apache.lucene.index.LeafReaderContext> leaves = reader.leaves();
		for (int i = leaves.size() - 1; i >= 0; i--) {
			if (leaves.get(i).docBase <= globalDocId) {
				return i;
			}
		}
		return 0;
	}

	private int batchUpsert(String resourceType, List<QueryDocument> docs, List<DocFailure> failures) {
		IndexWriter writer = schemaManager.ensureWriter(resourceType);
		List<QueryDocument> applied = new ArrayList<>(docs.size());
		for (QueryDocument doc : docs) {
			try {
				applyConditionalUpsert(writer, doc);
				applied.add(doc);
			}
			catch (IOException e) {
				log.warn("bulkUpsert doc failed for " + resourceType + "/" + doc.getResourceUuid(), e);
				failures.add(failure(resourceType, doc.getResourceUuid(), e));
			}
		}
		try {
			writer.commit();
		}
		catch (IOException e) {
			log.warn("bulkUpsert commit failed for " + resourceType, e);
			// On commit failure none of the applied writes are durable; surface them as failures.
			// Only the docs that actually made it past applyConditionalUpsert get a commit-failure
			// entry — re-appending every doc would double-count UUIDs that already failed per-doc.
			for (QueryDocument doc : applied) {
				failures.add(failure(resourceType, doc.getResourceUuid(), e));
			}
			return 0;
		}
		return applied.size();
	}

	private Document toLuceneDocument(QueryDocument source) {
		Document target = new Document();
		target.add(new StringField(LuceneFieldNames.RESOURCE_UUID, source.getResourceUuid(), Field.Store.YES));
		target.add(new StringField(LuceneFieldNames.PATIENT_UUID, source.getPatientUuid(), Field.Store.YES));

		if (source.getDate() != null) {
			long epochDay = source.getDate().toEpochDay();
			target.add(new LongPoint(LuceneFieldNames.RECORD_DATE_POINT, epochDay));
			target.add(new StoredField(LuceneFieldNames.RECORD_DATE, epochDay));
		}

		if (source.getText() != null) {
			target.add(new TextField(LuceneFieldNames.TEXT, source.getText(), Field.Store.YES));
		}

		if (source.getEmbedding() != null) {
			target.add(new StoredField(LuceneFieldNames.EMBEDDING_STORED,
			        LuceneVectorCodec.encode(source.getEmbedding())));
		}

		target.add(new StoredField(LuceneFieldNames.METADATA_JSON,
		        MetadataCodec.encode(source.getMetadata())));
		indexScalarMetadata(target, source.getMetadata());

		if (source.getLastModified() != null) {
			long millis = source.getLastModified().toEpochMilli();
			target.add(new NumericDocValuesField(LuceneFieldNames.LAST_MODIFIED, millis));
			target.add(new StoredField(LuceneFieldNames.LAST_MODIFIED, millis));
		}
		return target;
	}

	private static void indexScalarMetadata(Document target, Map<String, Object> metadata) {
		if (metadata == null) {
			return;
		}
		for (Map.Entry<String, Object> entry : metadata.entrySet()) {
			Object value = entry.getValue();
			if (value == null) {
				continue;
			}
			// Filter pushdown for metadata is term-based per the SPI Filter shape; only flat
			// scalars get indexed companions. Collections / maps stay in the JSON blob and can
			// be read via document retrieval but are not directly filterable in v1.
			if (BackendDocs.isFilterableScalar(value)) {
				target.add(new StringField(LuceneFieldNames.META_PREFIX + entry.getKey(),
				        String.valueOf(value), Field.Store.NO));
			}
		}
	}

	private List<String> resolveResourceTypes(SearchRequest req) {
		if (req.getResourceTypes().isEmpty()) {
			Set<String> known = schemaManager.knownIndexNames();
			if (known.isEmpty()) {
				known = schemaManager.listAllIndexes();
			}
			List<String> types = new ArrayList<>(known.size());
			for (String indexName : known) {
				types.add(BackendDocs.stripPrefix(indexName));
			}
			return types;
		}
		// Touch each named type so a search against a never-written type returns zero hits rather
		// than failing — matches the MySQL backend's ensureTable on the read path.
		for (String type : req.getResourceTypes()) {
			schemaManager.ensureWriter(type);
		}
		return new ArrayList<>(req.getResourceTypes());
	}

	private Query buildBm25Query(String queryText, Query filterQuery) throws ParseException {
		QueryParser parser = new QueryParser(LuceneFieldNames.TEXT, analyzer);
		// Bypass the parser's special-character handling — clinical text from the consumer side
		// is treated as a natural-language phrase, not a Lucene DSL expression.
		Query textQuery = parser.parse(QueryParser.escape(queryText));
		if (filterQuery == null) {
			return textQuery;
		}
		return new BooleanQuery.Builder()
		        .add(textQuery, BooleanClause.Occur.MUST)
		        .add(filterQuery, BooleanClause.Occur.FILTER)
		        .build();
	}

	private List<Hit> searchOneIndex(String resourceType, Query query, int limit) throws IOException {
		IndexWriter writer = schemaManager.ensureWriter(resourceType);
		try (DirectoryReader reader = DirectoryReader.open(writer)) {
			IndexSearcher searcher = new IndexSearcher(reader);
			TopDocs topDocs = searcher.search(query, limit);
			List<Hit> hits = new ArrayList<>(topDocs.scoreDocs.length);
			for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
				Document stored = searcher.doc(scoreDoc.doc);
				QueryDocument doc = readDocument(resourceType, stored);
				hits.add(new Hit(doc, scoreDoc.score, 0));
			}
			return hits;
		}
	}

	private void knnSingleIndex(String resourceType, Query scanQuery, float[] queryVector,
	        double queryNorm, PriorityQueue<Hit> heap, int limit) throws IOException {
		IndexWriter writer = schemaManager.ensureWriter(resourceType);
		try (DirectoryReader reader = DirectoryReader.open(writer)) {
			IndexSearcher searcher = new IndexSearcher(reader);
			searcher.search(scanQuery, new SimpleCollector() {

				private int docBase;

				@Override
				protected void doSetNextReader(LeafReaderContext context) {
					this.docBase = context.docBase;
				}

				@Override
				public void collect(int doc) throws IOException {
					int globalDoc = docBase + doc;
					// First fetch: only the embedding bytes. Lucene 8 stored fields share a
					// block-compressed blob with text and metadata_json, so a full
					// {@code searcher.doc(globalDoc)} pays for decompressing every stored field
					// per candidate; restricting to {@code EMBEDDING_STORED} confines that work
					// to the scoring step. The MySQL sibling gets the equivalent for free from
					// SQL column projection.
					Document embeddingOnly = searcher.doc(globalDoc,
					    Collections.singleton(LuceneFieldNames.EMBEDDING_STORED));
					BytesRef embeddingBytes = embeddingOnly.getBinaryValue(LuceneFieldNames.EMBEDDING_STORED);
					if (embeddingBytes == null) {
						return;
					}
					double score = LuceneVectorCodec.cosineFromBytes(queryVector, queryNorm,
					    embeddingBytes.bytes, embeddingBytes.offset, embeddingBytes.length);
					if (heap.size() >= limit && score <= heap.peek().getRawScore()) {
						return;
					}
					// Second fetch: full stored fields only once the heap has admitted this
					// candidate. Gating the metadata_json decode and embedding float[] copy
					// behind admission keeps per-candidate cost proportional to top-K rather
					// than to the candidate set.
					Document stored = searcher.doc(globalDoc);
					QueryDocument hitDoc = readDocument(resourceType, stored);
					TopKHits.offer(heap, new Hit(hitDoc, score, 0), limit);
				}

				@Override
				public ScoreMode scoreMode() {
					return ScoreMode.COMPLETE_NO_SCORES;
				}
			});
		}
	}

	private QueryDocument readDocument(String resourceType, Document stored) {
		QueryDocument doc = new QueryDocument();
		doc.setResourceType(resourceType);
		doc.setResourceUuid(stored.get(LuceneFieldNames.RESOURCE_UUID));
		doc.setPatientUuid(stored.get(LuceneFieldNames.PATIENT_UUID));
		IndexableField dateField = stored.getField(LuceneFieldNames.RECORD_DATE);
		if (dateField != null && dateField.numericValue() != null) {
			doc.setDate(LocalDate.ofEpochDay(dateField.numericValue().longValue()));
		}
		doc.setText(stored.get(LuceneFieldNames.TEXT));
		BytesRef embeddingBytes = stored.getBinaryValue(LuceneFieldNames.EMBEDDING_STORED);
		if (embeddingBytes != null) {
			doc.setEmbedding(LuceneVectorCodec.decode(embeddingBytes.bytes, embeddingBytes.offset,
			    embeddingBytes.length));
		}
		String metaJson = stored.get(LuceneFieldNames.METADATA_JSON);
		Map<String, Object> meta = MetadataCodec.decode(metaJson);
		for (Map.Entry<String, Object> entry : meta.entrySet()) {
			doc.putMetadata(entry.getKey(), entry.getValue());
		}
		IndexableField lmField = stored.getField(LuceneFieldNames.LAST_MODIFIED);
		if (lmField != null && lmField.numericValue() != null) {
			doc.setLastModified(Instant.ofEpochMilli(lmField.numericValue().longValue()));
		}
		return doc;
	}

	private static int countMatching(IndexWriter writer, Query query) throws IOException {
		try (DirectoryReader reader = DirectoryReader.open(writer)) {
			return new IndexSearcher(reader).count(query);
		}
	}

	private static DocFailure failure(String resourceType, String resourceUuid, Throwable cause) {
		return new DocFailure(resourceType, resourceUuid, cause.getMessage(), isRetryable(cause));
	}

	private static boolean isRetryable(Throwable cause) {
		// CorruptIndexException is a permanent failure — the segment is broken and re-applying the
		// same write will fail the same way. Argument validation and unsupported-operation throws
		// are caller bugs, also non-retryable. Everything else (general IOException — disk full,
		// transient locks, OS-level errors) is treated as retryable so the sync pipeline can back
		// off and retry rather than dead-letter the doc on a recoverable hiccup.
		if (cause instanceof org.apache.lucene.index.CorruptIndexException) {
			return false;
		}
		if (cause instanceof IllegalArgumentException || cause instanceof UnsupportedOperationException) {
			return false;
		}
		return true;
	}

}
