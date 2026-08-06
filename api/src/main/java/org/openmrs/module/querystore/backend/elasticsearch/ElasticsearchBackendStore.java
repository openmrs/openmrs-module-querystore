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
import org.openmrs.module.querystore.backend.PatientChartRead;
import org.openmrs.module.querystore.backend.SchemaSpec;
import org.openmrs.module.querystore.backend.SearchRequest;
import org.openmrs.module.querystore.backend.SearchResult;
import org.openmrs.module.querystore.backend.WriteResult;
import org.openmrs.module.querystore.model.QueryDocument;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.BulkIndexByScrollFailure;
import co.elastic.clients.elasticsearch._types.Conflicts;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.ErrorCause;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.KnnSearch;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
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
 * <p>Hybrid retrieval inherits the {@link BackendStore#hybrid(SearchRequest)} interface-default
 * RRF over {@link #bm25(SearchRequest)} and {@link #knn(SearchRequest)} the same way every tier
 * does, which keeps the chartsearchai cross-tier eval a fair signal. Native ES RRF via the
 * {@code retriever} API is a known follow-up; the override hook is reachable because the SPI
 * default is now a real call path (was an {@code UnsupportedBackendOperationException} throw in
 * v1's pre-reshape shape — see Decision 3 SPI sub-point 2).
 */
public class ElasticsearchBackendStore implements BackendStore, Closeable {

	private static final Log log = LogFactory.getLog(ElasticsearchBackendStore.class);

	// Per ADR Decision 3, the ES tier targets cross-patient kNN at multi-million-record scale.
	private static final int RECOMMENDED_MAX_CORPUS = 50_000_000;

	private static final int BATCH_SIZE = 500;

	// Wildcard glob targeting every querystore-managed index in this cluster — bulkDeleteByPatient,
	// existsByPatient, findAllByPatient, and the empty-resourceTypes search path all expand to the
	// same shape. Single constant keeps the four call sites from drifting if the prefix or glob
	// syntax ever changes.
	private static final String ALL_INDICES_PATTERN = QueryStoreConstants.INDEX_PREFIX + "*";

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
		ensureIndexFor(doc);
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
			        .index(ALL_INDICES_PATTERN)
			        .allowNoIndices(true)
			        .ignoreUnavailable(true)
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
	public List<QueryDocument> findAllByPatient(String patientUuid) {
		return findPatientChart(patientUuid).getDocuments();
	}

	@Override
	public PatientChartRead findPatientChart(String patientUuid) {
		if (StringUtils.isBlank(patientUuid)) {
			return PatientChartRead.complete(Collections.<QueryDocument> emptyList());
		}
		Query filter = Query.of(q -> q
		        .term(t -> t.field(ElasticsearchFieldNames.PATIENT_UUID).value(patientUuid)));
		try {
			SearchResponse<Map> resp = client().search(s -> s
			        .index(ALL_INDICES_PATTERN)
			        .size(FULL_CHART_MAX_HITS)
			        .trackTotalHits(t -> t.enabled(true))
			        .allowNoIndices(true)
			        .ignoreUnavailable(true)
			        .query(filter)
			        // Drop the dense_vector from the wire payload — readDocument doesn't decode it
			        // for the LLM full-chart consumer (Decision 15) and serializing ~384 JSON floats
			        // per doc would be the dominant cost on a multi-thousand-doc chart. Mirrors the
			        // MySQL findAllByPatient SELECT that omits the embedding column and the Lucene
			        // CHART_LOAD_FIELDS restricted-fetch — three tiers, one excluded payload.
			        .source(src -> src.filter(f -> f.excludes(ElasticsearchFieldNames.EMBEDDING)))
			        // ES-side sort by record_date desc so any truncation at FULL_CHART_MAX_HITS keeps
			        // the most-recent slice — aligning with chartsearchai's recency-cap prompt
			        // convention. Missing dates land last so legacy rows that pre-date the record_date
			        // convention don't poison the head of the chart. A secondary {@code _doc asc}
			        // sort pins the truncation boundary deterministically when many docs share a date
			        // (or all docs lack one — migration scenario for legacy obs without obs_datetime);
			        // ES's per-shard secondary order is otherwise unspecified and would make the
			        // cap's cut-off vary across calls. {@code _doc} (not {@code _id}) because ES 7+
			        // makes {@code _id} unsortable without enabling expensive fielddata; {@code _doc}
			        // gives Lucene-internal order which is stable within a segment and cheap to read.
			        // The Comparator pass below re-applies the full (date, type, uuid) ordering for
			        // byte-identical output with the other backends.
			        .sort(SortOptions.of(so -> so.field(f -> f
			                .field(ElasticsearchFieldNames.RECORD_DATE)
			                .order(SortOrder.Desc)
			                .missing(FieldValue.of("_last")))))
			        .sort(SortOptions.of(so -> so.field(f -> f
			                .field("_doc")
			                .order(SortOrder.Asc)))),
			        Map.class);
			List<co.elastic.clients.elasticsearch.core.search.Hit<Map>> hits = resp.hits().hits();
			long total = resp.hits().total() == null ? hits.size() : resp.hits().total().value();
			boolean truncated = total > hits.size();
			if (truncated) {
				// Hitting the cap is a v1 quirk of the ES tier: single-search size is bounded by
				// max_result_window (default 10k). MySQL and Lucene have no equivalent cap because
				// they stream from JDBC/Lucene directly. PIT+search_after pagination is the v1.1
				// follow-up if a real consumer ever surfaces here. See ADR Decision 15 for the v1
				// contract; the log message itself stays terse for ops dashboards.
				log.warn("findAllByPatient(" + patientUuid + ") returned the ES v1 cap of "
				        + FULL_CHART_MAX_HITS + " hits; older records beyond this slice are not in"
				        + " the result.");
			}
			List<QueryDocument> all = new ArrayList<>(hits.size());
			for (co.elastic.clients.elasticsearch.core.search.Hit<Map> h : hits) {
				all.add(readDocument(h.index(), h.source()));
			}
			all.sort(BackendDocs.CHART_ORDER);
			return new PatientChartRead(all, truncated);
		}
		catch (ElasticsearchException | IOException e) {
			// Preserve the legacy non-throwing read contract, but never describe a backend failure as a
			// complete empty chart. External consumers fail closed on the explicit incompleteness bit.
			log.warn("findAllByPatient failed for " + patientUuid, e);
			return new PatientChartRead(Collections.<QueryDocument> emptyList(), true);
		}
	}

	// Single-search cap for the ES tier — see findAllByPatient. MySQL and Lucene tiers honour ADR
	// Decision 15's no-limit contract natively; this value brackets the ES-specific quirk that v1
	// inherits from the default max_result_window. Package-private so the integration test can
	// assert against the same constant rather than baking 10_000 into the test text.
	static final int FULL_CHART_MAX_HITS = 10_000;

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
			        .index(ALL_INDICES_PATTERN)
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
	public long countByType(String resourceType) {
		// Drift detection (ADR: Sync reliability and reconciliation). allowNoIndices/ignoreUnavailable
		// give "no index yet → 0" consistent with the other backends; a real error → unknown (-1).
		String index = ElasticsearchSchemaManager.indexName(resourceType);
		try {
			co.elastic.clients.elasticsearch.core.CountResponse resp = client().count(c -> c
			        .index(index)
			        .query(Query.of(q -> q.matchAll(m -> m)))
			        .allowNoIndices(true)
			        .ignoreUnavailable(true));
			return resp.count();
		}
		catch (ElasticsearchException e) {
			if (e.status() == 404 || "index_not_found_exception".equals(e.error().type())) {
				return 0L;
			}
			log.warn("countByType failed for " + resourceType, e);
			return -1L;
		}
		catch (IOException e) {
			log.warn("countByType failed for " + resourceType, e);
			return -1L;
		}
	}

	@Override
	public SearchResult bm25(SearchRequest req) {
		if (StringUtils.isBlank(req.getQueryText()) || req.getLimit() <= 0) {
			return SearchResult.empty();
		}
		List<String> indexes = resolveIndexes(req);
		Query filter = ElasticsearchFilterTranslator.toQuery(req.getFilters());
		// multi_match OR-fans across [text, synonyms, description^boost, mapping_names^boost] so
		// an alternate-term or category-word query surfaces docs whose preferred name doesn't
		// carry the matching term (e.g. "kidney" hits Blood urea nitrogen via its description;
		// "kidney disease" hits Chronic kidney insufficiency via its ICD-10 mapping name) per
		// ADR Decision 6. Boosts are shared with the Lucene tier via QueryStoreConstants so the
		// two backends can't silently drift.
		Query bm25 = Query.of(q -> q.multiMatch(m -> m
		        .fields(ElasticsearchFieldNames.TEXT,
		                ElasticsearchFieldNames.SYNONYMS,
		                ElasticsearchFieldNames.DESCRIPTION + "^"
		                        + QueryStoreConstants.BM25_DESCRIPTION_BOOST,
		                ElasticsearchFieldNames.MAPPING_NAMES + "^"
		                        + QueryStoreConstants.BM25_MAPPING_NAMES_BOOST)
		        .query(req.getQueryText())));
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

	// Without this, ES dynamic mapping types patient_uuid as `text` and the bare-field `term` filter silently matches zero.
	private void ensureIndexFor(QueryDocument doc) {
		if (doc.getEmbedding() == null) {
			return;
		}
		schemaManager.ensureIndex(doc.getResourceType(), doc.getEmbedding().length);
	}

	private int executeBulkUpsert(List<QueryDocument> docs, List<DocFailure> failures) {
		List<BulkOperation> ops = new ArrayList<>(docs.size());
		for (QueryDocument doc : docs) {
			ensureIndexFor(doc);
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
			return Collections.singletonList(ALL_INDICES_PATTERN);
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
		Object synonyms = doc.getMetadata().get(QueryStoreConstants.FIELD_SYNONYMS);
		if (synonyms instanceof List) {
			// Top-level text field used by BM25's multi_match per ADR Decision 6; the same list
			// also lives in metadata_json for rehydration. Duplication is intentional — this
			// field exists purely so BM25 can match.
			source.put(ElasticsearchFieldNames.SYNONYMS, synonyms);
		}
		String descriptionBlob = doc.getDescriptionText();
		if (!descriptionBlob.isEmpty()) {
			// Top-level text field used by BM25's multi_match; the same string is also in
			// metadata_json for rehydration. Description is not added to the embedding input —
			// see ConceptNameUtil.getDescription for the rationale.
			source.put(ElasticsearchFieldNames.DESCRIPTION, descriptionBlob);
		}
		String mappingNamesBlob = doc.getMappingNamesText();
		if (!mappingNamesBlob.isEmpty()) {
			// Top-level text field used by BM25's multi_match; the structured list also lives in
			// metadata_json for rehydration. Mapping names are not added to the embedding input —
			// see ConceptNameUtil.getMappingNames for the rationale.
			source.put(ElasticsearchFieldNames.MAPPING_NAMES, mappingNamesBlob);
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
