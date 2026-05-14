/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.backend.elasticsearch;

import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.openmrs.module.querystore.backend.UnsupportedBackendOperationException;
import org.openmrs.module.querystore.backend.WriteResult;
import org.openmrs.module.querystore.model.QueryDocument;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.BulkIndexByScrollFailure;
import co.elastic.clients.elasticsearch._types.Conflicts;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.ErrorCause;
import co.elastic.clients.elasticsearch._types.KnnSearch;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.VersionType;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteByQueryResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;

/**
 * Elasticsearch reference {@link BackendStore} (Decision 3, "large" tier). One ES index per
 * resource type ({@code querystore_<type>} per Decision 4) with native {@code dense_vector} HNSW kNN
 * and BM25 text. The durable+visible contract is satisfied by {@code refresh=wait_for} on every
 * write; the conditional-upsert-by-version invariant is enforced via external versioning
 * ({@code version_type=external_gte} with {@code last_modified} as the version).
 *
 * <p>Hybrid retrieval intentionally throws — service-layer RRF fuses {@link #bm25(SearchRequest)}
 * and {@link #knn(SearchRequest)} the same way every tier does, which keeps the chartsearchai
 * cross-tier eval a fair signal. Native ES RRF via the {@code retriever} API is a known follow-up.
 */
public class ElasticsearchBackendStore implements BackendStore, Closeable {

	private static final Log log = LogFactory.getLog(ElasticsearchBackendStore.class);

	// Per ADR Decision 3, the ES tier targets cross-patient kNN at multi-million-record scale.
	private static final int RECOMMENDED_MAX_CORPUS = 50_000_000;

	private static final int BATCH_SIZE = 500;

	// Mirror Lucene's HNSW candidate-width floor. Wider than k for better recall on small limits.
	private static final long KNN_NUM_CANDIDATES_FLOOR = 100L;

	private final ElasticsearchClientFactory clientFactory;

	private final ElasticsearchSchemaManager schemaManager;

	public ElasticsearchBackendStore() {
		this(new ElasticsearchClientFactory());
	}

	public ElasticsearchBackendStore(ElasticsearchClientFactory clientFactory) {
		this.clientFactory = clientFactory;
		this.schemaManager = new ElasticsearchSchemaManager(clientFactory);
	}

	@Override
	public void ensureSchema(String resourceType, SchemaSpec spec) {
		schemaManager.ensureIndex(resourceType, spec.getEmbeddingDimensions());
	}

	@Override
	public void deleteSchema(String resourceType) {
		schemaManager.deleteIndex(resourceType);
	}

	@Override
	public WriteResult upsert(QueryDocument doc) {
		BackendDocs.validate(doc);
		String index = ElasticsearchSchemaManager.indexName(doc.getResourceType());
		Map<String, Object> source = toSource(doc);
		try {
			client().index(i -> {
				i.index(index).id(doc.getResourceUuid()).refresh(Refresh.WaitFor).document(source);
				if (doc.getLastModified() != null) {
					// External_gte: drop strictly older writes, apply equal-or-newer. Matches the
					// SPI invariant. Null version skips this branch and falls back to last-write-wins.
					i.versionType(VersionType.ExternalGte).version(doc.getLastModified().toEpochMilli());
				}
				return i;
			});
			return WriteResult.success();
		}
		catch (ElasticsearchException e) {
			if (isVersionConflict(e)) {
				// The SPI does not distinguish "applied" from "dropped as stale" — both succeed.
				return WriteResult.success();
			}
			log.warn("upsert failed for " + doc.getResourceType() + "/" + doc.getResourceUuid(), e);
			return WriteResult.failed(failure(doc, e));
		}
		catch (IOException e) {
			// The high-level client wraps most cluster errors in ElasticsearchException, but a
			// version-conflict on the index endpoint can surface as the low-level RestClient's
			// ResponseException (an IOException) carrying the 409 in its message. Check both.
			if (isVersionConflict(e)) {
				return WriteResult.success();
			}
			log.warn("upsert failed for " + doc.getResourceType() + "/" + doc.getResourceUuid(), e);
			return WriteResult.failed(failure(doc, e));
		}
	}

	@Override
	public WriteResult delete(String resourceType, String resourceUuid) {
		String index = ElasticsearchSchemaManager.indexName(resourceType);
		try {
			client().delete(d -> d.index(index).id(resourceUuid).refresh(Refresh.WaitFor));
			return WriteResult.success();
		}
		catch (ElasticsearchException e) {
			if (e.status() == 404 || "index_not_found_exception".equals(e.error().type())) {
				// Idempotent delete: missing doc or missing index is the second-call no-op outcome.
				return WriteResult.success();
			}
			log.warn("delete failed for " + resourceType + "/" + resourceUuid, e);
			return WriteResult.failed(new DocFailure(resourceType, resourceUuid, e.getMessage(), isRetryable(e)));
		}
		catch (IOException e) {
			log.warn("delete failed for " + resourceType + "/" + resourceUuid, e);
			return WriteResult.failed(new DocFailure(resourceType, resourceUuid, e.getMessage(), isRetryable(e)));
		}
	}

	@Override
	public BulkWriteResult bulkUpsert(List<QueryDocument> docs) {
		for (QueryDocument doc : docs) {
			BackendDocs.validate(doc);
		}
		List<DocFailure> failures = new ArrayList<>();
		int succeeded = 0;
		for (int start = 0; start < docs.size(); start += BATCH_SIZE) {
			int end = Math.min(start + BATCH_SIZE, docs.size());
			succeeded += executeBulkUpsert(docs.subList(start, end), failures);
		}
		return new BulkWriteResult(docs.size(), succeeded, failures);
	}

	@Override
	public BulkWriteResult bulkDelete(String resourceType, List<String> resourceUuids) {
		if (resourceUuids.isEmpty()) {
			return new BulkWriteResult(0, 0, Collections.emptyList());
		}
		String index = ElasticsearchSchemaManager.indexName(resourceType);
		List<DocFailure> failures = new ArrayList<>();
		int succeeded = 0;
		for (int start = 0; start < resourceUuids.size(); start += BATCH_SIZE) {
			int end = Math.min(start + BATCH_SIZE, resourceUuids.size());
			List<String> chunk = resourceUuids.subList(start, end);
			List<BulkOperation> ops = new ArrayList<>(chunk.size());
			for (String uuid : chunk) {
				ops.add(BulkOperation.of(b -> b.delete(d -> d.index(index).id(uuid))));
			}
			try {
				BulkResponse resp = client().bulk(BulkRequest.of(b -> b.refresh(Refresh.WaitFor).operations(ops)));
				succeeded += countBulkSuccess(resp, resourceType, failures);
			}
			catch (ElasticsearchException | IOException e) {
				log.warn("bulkDelete chunk failed for " + resourceType, e);
				for (String uuid : chunk) {
					failures.add(new DocFailure(resourceType, uuid, e.getMessage(), isRetryable(e)));
				}
			}
		}
		return new BulkWriteResult(resourceUuids.size(), succeeded, failures);
	}

	@Override
	public BulkWriteResult bulkDeleteByPatient(String patientUuid) {
		// Single cross-index call — ES handles the querystore_* wildcard natively, no per-type loop.
		// wait_for_completion + refresh=true together satisfy the durable+visible contract.
		List<DocFailure> failures = new ArrayList<>();
		long deleted = 0;
		long matched = 0;
		try {
			DeleteByQueryResponse resp = client().deleteByQuery(d -> d
			        .index(QueryStoreConstants.INDEX_PREFIX + "*")
			        .query(Query.of(q -> q.term(t -> t.field(ElasticsearchFieldNames.PATIENT_UUID).value(patientUuid))))
			        .refresh(true)
			        .waitForCompletion(true)
			        .conflicts(Conflicts.Proceed));
			deleted = resp.deleted() == null ? 0L : resp.deleted();
			matched = resp.total() == null ? deleted : resp.total();
			if (resp.failures() != null) {
				for (BulkIndexByScrollFailure scrollFailure : resp.failures()) {
					String type = scrollFailure.index() == null ? null : BackendDocs.stripPrefix(scrollFailure.index());
					ErrorCause cause = scrollFailure.cause();
					String reason = cause == null ? null : cause.reason();
					failures.add(new DocFailure(type, scrollFailure.id(), reason,
					        isRetryableStatus(scrollFailure.status())));
				}
			}
		}
		catch (ElasticsearchException | IOException e) {
			log.warn("bulkDeleteByPatient failed for " + patientUuid, e);
			// Call-level failure: no per-doc telemetry available, so resourceType/resourceUuid stay
			// null. totalRequested stays 0 — the call didn't get far enough to know.
			failures.add(new DocFailure(null, null, e.getMessage(), isRetryable(e)));
		}
		int deletedInt = (int) Math.min(Integer.MAX_VALUE, deleted);
		int matchedInt = (int) Math.min(Integer.MAX_VALUE, matched);
		return new BulkWriteResult(matchedInt, deletedInt, failures);
	}

	@Override
	public boolean existsByPatient(String patientUuid) {
		if (StringUtils.isBlank(patientUuid)) {
			return false;
		}
		try {
			// Single wildcard count short-circuited via terminateAfter=1 — ES stops scanning shards
			// as soon as one document matches. allowNoIndices / ignoreUnavailable preserve the "no
			// indexes yet" semantics consistent with the other backends.
			co.elastic.clients.elasticsearch.core.CountResponse resp = client().count(c -> c
			        .index(QueryStoreConstants.INDEX_PREFIX + "*")
			        .query(Query.of(q -> q.term(t -> t.field(ElasticsearchFieldNames.PATIENT_UUID).value(patientUuid))))
			        .terminateAfter(1L)
			        .allowNoIndices(true)
			        .ignoreUnavailable(true));
			return resp.count() > 0L;
		}
		catch (ElasticsearchException | IOException e) {
			log.warn("existsByPatient failed for " + patientUuid, e);
			return false;
		}
	}

	@Override
	public SearchResult bm25(SearchRequest req) {
		if (StringUtils.isBlank(req.getQueryText()) || req.getLimit() <= 0) {
			return SearchResult.empty();
		}
		List<String> indexes = resolveIndexes(req);
		Query filter = ElasticsearchFilterTranslator.toQuery(req.getFilters());
		Query bm25 = Query.of(q -> q.match(m -> m.field(ElasticsearchFieldNames.TEXT).query(req.getQueryText())));
		Query combined = filter == null ? bm25
		        : Query.of(q -> q.bool(b -> b.must(bm25).filter(filter)));
		return runSearch(req, indexes, sb -> sb.query(combined), null);
	}

	@Override
	public SearchResult knn(SearchRequest req) {
		if (req.getQueryVector() == null || req.getLimit() <= 0) {
			return SearchResult.empty();
		}
		List<String> indexes = resolveIndexes(req);
		Query filter = ElasticsearchFilterTranslator.toQuery(req.getFilters());
		List<Float> vector = toFloatList(req.getQueryVector());
		long numCandidates = Math.max((long) req.getLimit() * 4L, KNN_NUM_CANDIDATES_FLOOR);
		KnnSearch knn = KnnSearch.of(k -> {
			k.field(ElasticsearchFieldNames.EMBEDDING)
			        .queryVector(vector)
			        .k((long) req.getLimit())
			        .numCandidates(numCandidates);
			if (filter != null) {
				k.filter(Collections.singletonList(filter));
			}
			return k;
		});
		return runSearch(req, indexes, sb -> sb, knn);
	}

	@Override
	public SearchResult hybrid(SearchRequest req) {
		throw new UnsupportedBackendOperationException(
		        "Elasticsearch backend does not implement native hybrid in v1; service layer must fuse bm25() + knn() ranks");
	}

	@Override
	public BackendCapabilities capabilities() {
		return new BackendCapabilities(
		        true,
		        false,
		        true,
		        RECOMMENDED_MAX_CORPUS,
		        EnumSet.of(Filter.Kind.TERM, Filter.Kind.IN, Filter.Kind.RANGE, Filter.Kind.PATIENT_SCOPE));
	}

	@Override
	public HealthStatus health() {
		try {
			HealthResponse resp = client().cluster().health(h -> h
			        .waitForStatus(co.elastic.clients.elasticsearch._types.HealthStatus.Yellow)
			        .timeout(Time.of(t -> t.time("5s"))));
			if (resp.status() == co.elastic.clients.elasticsearch._types.HealthStatus.Red) {
				return HealthStatus.unhealthy("cluster status red");
			}
			return HealthStatus.healthy();
		}
		catch (ElasticsearchException | IOException e) {
			return HealthStatus.unhealthy(e.getMessage());
		}
	}

	@Override
	public void close() {
		clientFactory.close();
	}

	// ---------- internals ----------

	private ElasticsearchClient client() {
		return clientFactory.getClient();
	}

	private int executeBulkUpsert(List<QueryDocument> docs, List<DocFailure> failures) {
		List<BulkOperation> ops = new ArrayList<>(docs.size());
		for (QueryDocument doc : docs) {
			String index = ElasticsearchSchemaManager.indexName(doc.getResourceType());
			Map<String, Object> source = toSource(doc);
			Long version = doc.getLastModified() == null ? null : doc.getLastModified().toEpochMilli();
			ops.add(BulkOperation.of(b -> b.index(idx -> {
				idx.index(index).id(doc.getResourceUuid()).document(source);
				if (version != null) {
					idx.versionType(VersionType.ExternalGte).version(version);
				}
				return idx;
			})));
		}
		try {
			BulkResponse resp = client().bulk(BulkRequest.of(b -> b.refresh(Refresh.WaitFor).operations(ops)));
			return countBulkSuccess(resp, null, failures);
		}
		catch (ElasticsearchException | IOException e) {
			log.warn("bulkUpsert chunk failed", e);
			for (QueryDocument doc : docs) {
				failures.add(failure(doc, e));
			}
			return 0;
		}
	}

	private int countBulkSuccess(BulkResponse resp, String fallbackType, List<DocFailure> failures) {
		int succeeded = 0;
		for (BulkResponseItem item : resp.items()) {
			if (item.error() == null) {
				succeeded++;
				continue;
			}
			if (item.status() == 409) {
				// Conditional-upsert "skipped as stale" — counts as success per SPI invariant.
				succeeded++;
				continue;
			}
			String type = fallbackType != null ? fallbackType
			        : (item.index() == null ? null : BackendDocs.stripPrefix(item.index()));
			failures.add(new DocFailure(type, item.id(), item.error().reason(),
			        isRetryableStatus(item.status())));
		}
		return succeeded;
	}

	private SearchResult runSearch(SearchRequest req, List<String> indexes,
	        java.util.function.UnaryOperator<co.elastic.clients.elasticsearch.core.SearchRequest.Builder> queryShaper,
	        KnnSearch knn) {
		try {
			SearchResponse<Map> resp = client().search(s -> {
				// allowNoIndices + ignoreUnavailable preserve the Lucene/MySQL "search a never-written
				// type returns zero hits" semantics. Without these, ES throws index_not_found on the
				// explicit named-type path; the wildcard path was already implicitly tolerant.
				s.index(indexes).size(req.getLimit()).trackTotalHits(t -> t.enabled(false))
				        .allowNoIndices(true).ignoreUnavailable(true);
				queryShaper.apply(s);
				if (knn != null) {
					s.knn(knn);
				}
				return s;
			}, Map.class);
			List<Hit> ranked = new ArrayList<>(resp.hits().hits().size());
			int rank = 1;
			for (co.elastic.clients.elasticsearch.core.search.Hit<Map> h : resp.hits().hits()) {
				QueryDocument doc = readDocument(h.index(), h.source());
				double score = h.score() == null ? 0.0 : h.score();
				ranked.add(new Hit(doc, score, rank++));
			}
			return new SearchResult(ranked);
		}
		catch (ElasticsearchException | IOException e) {
			throw new IllegalStateException("ES search failed on " + indexes, e);
		}
	}

	private List<String> resolveIndexes(SearchRequest req) {
		if (req.getResourceTypes().isEmpty()) {
			return Collections.singletonList(QueryStoreConstants.INDEX_PREFIX + "*");
		}
		List<String> indexes = new ArrayList<>(req.getResourceTypes().size());
		for (String type : req.getResourceTypes()) {
			indexes.add(ElasticsearchSchemaManager.indexName(type));
		}
		return indexes;
	}

	private static Map<String, Object> toSource(QueryDocument doc) {
		Map<String, Object> source = new LinkedHashMap<>();
		source.put(ElasticsearchFieldNames.RESOURCE_UUID, doc.getResourceUuid());
		source.put(ElasticsearchFieldNames.PATIENT_UUID, doc.getPatientUuid());
		if (doc.getDate() != null) {
			source.put(ElasticsearchFieldNames.RECORD_DATE, doc.getDate().toString());
		}
		if (doc.getText() != null) {
			source.put(ElasticsearchFieldNames.TEXT, doc.getText());
		}
		if (doc.getEmbedding() != null) {
			// Jackson serializes float[] directly as a JSON number array (no per-element boxing).
			// Converting to List<Float> here would allocate ~N Float wrappers per write; on a
			// 500-doc bulk chunk with 384-dim vectors that's ~190K boxed Floats per chunk.
			source.put(ElasticsearchFieldNames.EMBEDDING, doc.getEmbedding());
		}
		source.put(ElasticsearchFieldNames.METADATA_JSON, MetadataCodec.encode(doc.getMetadata()));
		// Companion per-key meta.* fields populated alongside the JSON blob so structured filters
		// land on the inverted index rather than reparsing the blob. Mirrors Lucene's META_PREFIX.
		Map<String, Object> meta = scalarMetadata(doc.getMetadata());
		if (!meta.isEmpty()) {
			source.put(ElasticsearchFieldNames.META_PARENT, meta);
		}
		if (doc.getLastModified() != null) {
			source.put(ElasticsearchFieldNames.LAST_MODIFIED, doc.getLastModified().toString());
		}
		return source;
	}

	private static Map<String, Object> scalarMetadata(Map<String, Object> metadata) {
		if (metadata == null || metadata.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, Object> out = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : metadata.entrySet()) {
			Object value = entry.getValue();
			if (BackendDocs.isFilterableScalar(value)) {
				out.put(entry.getKey(), String.valueOf(value));
			}
		}
		return out;
	}

	@SuppressWarnings("unchecked")
	private static QueryDocument readDocument(String index, Map<String, Object> source) {
		QueryDocument doc = new QueryDocument();
		doc.setResourceType(BackendDocs.stripPrefix(index));
		if (source == null) {
			return doc;
		}
		doc.setResourceUuid(asString(source.get(ElasticsearchFieldNames.RESOURCE_UUID)));
		doc.setPatientUuid(asString(source.get(ElasticsearchFieldNames.PATIENT_UUID)));
		String recordDate = asString(source.get(ElasticsearchFieldNames.RECORD_DATE));
		if (recordDate != null) {
			doc.setDate(LocalDate.parse(recordDate));
		}
		doc.setText(asString(source.get(ElasticsearchFieldNames.TEXT)));
		Object embedding = source.get(ElasticsearchFieldNames.EMBEDDING);
		if (embedding instanceof List) {
			doc.setEmbedding(toFloatArray((List<Number>) embedding));
		}
		String metaJson = asString(source.get(ElasticsearchFieldNames.METADATA_JSON));
		Map<String, Object> meta = MetadataCodec.decode(metaJson);
		for (Map.Entry<String, Object> entry : meta.entrySet()) {
			doc.putMetadata(entry.getKey(), entry.getValue());
		}
		String lastModified = asString(source.get(ElasticsearchFieldNames.LAST_MODIFIED));
		if (lastModified != null) {
			doc.setLastModified(Instant.parse(lastModified));
		}
		return doc;
	}

	private static String asString(Object value) {
		return value == null ? null : value.toString();
	}

	private static List<Float> toFloatList(float[] vector) {
		if (vector == null) {
			return Collections.emptyList();
		}
		List<Float> list = new ArrayList<>(vector.length);
		for (float v : vector) {
			list.add(v);
		}
		return list;
	}

	private static float[] toFloatArray(List<Number> list) {
		if (list == null) {
			return null;
		}
		float[] out = new float[list.size()];
		for (int i = 0; i < list.size(); i++) {
			out[i] = list.get(i).floatValue();
		}
		return out;
	}

	private static DocFailure failure(QueryDocument doc, Throwable cause) {
		return new DocFailure(doc.getResourceType(), doc.getResourceUuid(), cause.getMessage(),
		        isRetryable(cause));
	}

	private static boolean isVersionConflict(ElasticsearchException e) {
		return e.status() == 409 || "version_conflict_engine_exception".equals(e.error().type());
	}

	private static boolean isVersionConflict(IOException e) {
		String message = e.getMessage();
		return message != null && message.contains("version_conflict_engine_exception");
	}

	private static boolean isRetryable(Throwable cause) {
		if (cause instanceof ElasticsearchException) {
			return isRetryableStatus(((ElasticsearchException) cause).status());
		}
		// IOException / connection failure: typically transient. Argument/usage bugs are not.
		return !(cause instanceof IllegalArgumentException || cause instanceof UnsupportedOperationException);
	}

	private static boolean isRetryableStatus(int status) {
		// 5xx and a small set of throttling-related 4xx are retryable; everything else is caller bug
		// or non-recoverable state.
		return status >= 500 || status == 429;
	}

}
