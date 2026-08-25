/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.api.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.querystore.QueryStoreConstants;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.backend.BackendStore;
import org.openmrs.module.querystore.backend.BulkWriteResult;
import org.openmrs.module.querystore.backend.DocFailure;
import org.openmrs.module.querystore.backend.Filter;
import org.openmrs.module.querystore.backend.PatientChartRead;
import org.openmrs.module.querystore.backend.SearchRequest;
import org.openmrs.module.querystore.backend.SearchResult;
import org.openmrs.module.querystore.backend.WriteResult;
import org.openmrs.module.querystore.bootstrap.BootstrapService;
import org.openmrs.module.querystore.bootstrap.BootstrapStatusReport;
import org.openmrs.module.querystore.embedding.EmbeddingProvider;
import org.openmrs.module.querystore.model.ContextSlice;
import org.openmrs.module.querystore.model.ContextSliceRecord;
import org.openmrs.module.querystore.model.ContextSliceRequest;
import org.openmrs.module.querystore.model.PatientChartFingerprint;
import org.openmrs.module.querystore.model.QueryDocument;

/**
 * Default {@link QueryStoreService}. Delegates writes and search to the configured
 * {@link BackendStore}; hybrid fusion is the SPI's default-method responsibility (BM25 + kNN with
 * rank-based RRF — backends with native fusion may override; see ADR Decision 3 SPI sub-point 2).
 * The {@link EmbeddingProvider} dependency is optional: when null, search degrades to BM25-only.
 */
public class QueryStoreServiceImpl extends BaseOpenmrsService implements QueryStoreService {

	private static final Log log = LogFactory.getLog(QueryStoreServiceImpl.class);

	private static final Pattern QUOTED_PHRASE = Pattern.compile("[\\\"]([^\\\"]{2,})[\\\"]|'([^']{2,})'");

	private static final Pattern ISO_DATE = Pattern.compile("\\b\\d{4}-\\d{2}-\\d{2}\\b");

	private static final Pattern UUID = Pattern.compile(
	        "\\b[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\\b",
	        Pattern.CASE_INSENSITIVE);

	private static final Pattern LABELED_IDENTIFIER = Pattern.compile(
	        "\\b(?:mrn|id|identifier|code)(?:\\s+is\\s+|\\s*[:=#]\\s*)([a-z0-9][a-z0-9._/-]{2,})\\b",
	        Pattern.CASE_INSENSITIVE);

	private static final Pattern NAMESPACE_CODE = Pattern.compile(
	        "\\b[a-z][a-z0-9_-]{1,15}:[a-z0-9][a-z0-9._-]{1,31}\\b", Pattern.CASE_INSENSITIVE);

	private BackendStore backend;

	private EmbeddingProvider embeddingProvider;

	// Test seam: when non-null, replaces the Context.getService(BootstrapService.class) lookup so
	// unit tests can pin the auto-index path without a live OpenMRS Spring context. Production
	// wiring leaves this null and resolves through Context — see ensureIndexedSafely. Package-private
	// to match the seam shape used in BootstrapServiceImpl.providersOverride.
	private BootstrapService bootstrapServiceOverride;

	// Query-embedding LRU. The ONNX query encoder dominates per-search latency on the hot path
	// (measured ~40-80 ms warm, ~3-4 s cold on the L6-v2 model). The same query string is reused
	// across patients within a session — UI default questions, repeat searches — so a small LRU
	// keyed by (modelName, query) eliminates the duplicate encode without changing retrieval
	// semantics. Bounded so a runaway caller cannot exhaust heap; access-ordered LinkedHashMap
	// under a synchronized wrapper is sufficient for the few-hundred-entries / few-QPS hot path.
	private static final int QUERY_EMBED_CACHE_MAX = 256;

	private final Map<String, float[]> queryEmbedCache = Collections.synchronizedMap(
	        new LinkedHashMap<String, float[]>(64, 0.75f, true) {
		        private static final long serialVersionUID = 1L;

		        @Override
		        protected boolean removeEldestEntry(Map.Entry<String, float[]> eldest) {
			        return size() > QUERY_EMBED_CACHE_MAX;
		        }
	        });

	public void setBackend(BackendStore backend) {
		this.backend = backend;
	}

	public void setEmbeddingProvider(EmbeddingProvider embeddingProvider) {
		this.embeddingProvider = embeddingProvider;
	}

	void setBootstrapServiceOverride(BootstrapService bootstrapService) {
		this.bootstrapServiceOverride = bootstrapService;
	}

	@Override
	public WriteResult index(QueryDocument document) {
		if (document == null) {
			return WriteResult.failed(new DocFailure(null, null, "document was null", false));
		}
		if (document.getResourceUuid() == null) {
			return WriteResult.failed(new DocFailure(document.getResourceType(), null,
			        "document resource_uuid was null", false));
		}
		if (backend == null) {
			// Misconfiguration, not a per-doc problem — silently swallowing here is what produced the
			// "bootstrap reports 682 indexed but the index holds 0" failure mode the slice fixes.
			// Throwing lets the bootstrap dispatcher (and any other caller that cares about persistence
			// accuracy) react: TypeBootstrapper.serializeAndEmbed catches RuntimeException to skip the
			// record, so the batch's documents_indexed only reflects confirmed writes.
			throw new IllegalStateException(
			        "No BackendStore wired into QueryStoreServiceImpl; cannot index "
			                + document.getResourceType() + "/" + document.getResourceUuid()
			                + ". Production: check wireBackend() in QueryStoreActivator.started() and "
			                + "the querystore.backend GP value. Tests: call setBackend() before "
			                + "exercising the index path.");
		}
		return backend.upsert(document);
	}

	// Overrides the interface's per-doc default with a single batched backend round-trip — the
	// commit-amortization win the bootstrap needs (ADR Decision 3).
	@Override
	public BulkWriteResult bulkIndex(List<QueryDocument> documents) {
		if (documents == null || documents.isEmpty()) {
			return new BulkWriteResult(0, 0, Collections.<DocFailure> emptyList());
		}
		if (backend == null) {
			// Same misconfiguration guard as index() — throw rather than silently report success.
			throw new IllegalStateException(
			        "No BackendStore wired into QueryStoreServiceImpl; cannot bulkIndex "
			                + documents.size() + " documents. Production: check wireBackend() in "
			                + "QueryStoreActivator.started() and the querystore.backend GP value. "
			                + "Tests: call setBackend() before exercising the index path.");
		}
		// Filter malformed docs into failures here rather than passing them to the backend, where a
		// single invalid doc would abort the whole batch (BackendDocs.validate throws outside the
		// backend's per-batch try/catch). Keeps one poison doc from sinking a good batch — the batched
		// analogue of index()'s null/uuid guards.
		List<QueryDocument> valid = new ArrayList<QueryDocument>(documents.size());
		List<DocFailure> invalid = new ArrayList<DocFailure>();
		for (QueryDocument doc : documents) {
			if (doc == null) {
				invalid.add(new DocFailure(null, null, "document was null", false));
			} else if (doc.getResourceUuid() == null) {
				invalid.add(new DocFailure(doc.getResourceType(), null, "document resource_uuid was null", false));
			} else {
				valid.add(doc);
			}
		}
		if (valid.isEmpty()) {
			return new BulkWriteResult(documents.size(), 0, invalid);
		}
		BulkWriteResult backendResult = backend.bulkUpsert(valid);
		if (invalid.isEmpty()) {
			return backendResult;
		}
		List<DocFailure> merged = new ArrayList<DocFailure>(backendResult.getFailures());
		merged.addAll(invalid);
		return new BulkWriteResult(documents.size(), backendResult.getSucceeded(), merged);
	}

	@Override
	public void delete(String resourceType, String resourceUuid) {
		if (resourceType == null || resourceUuid == null) {
			return;
		}
		if (backend == null) {
			return;
		}
		backend.delete(resourceType, resourceUuid);
	}

	@Override
	public void bulkDeleteByPatient(String patientUuid) {
		if (patientUuid == null) {
			return;
		}
		if (backend == null) {
			log.warn("No BackendStore wired; ignoring bulkDeleteByPatient call for " + patientUuid);
			return;
		}
		backend.bulkDeleteByPatient(patientUuid);
	}

	@Override
	public List<QueryDocument> searchByPatient(String patientUuid, String query, int limit) {
		if (backend == null || patientUuid == null) {
			return Collections.emptyList();
		}
		if (!backend.existsByPatient(patientUuid)) {
			ensureIndexedSafely(patientUuid);
		}
		List<QueryDocument> hits = runHybrid(query, limit, Filter.patientScope(patientUuid));
		emitRetrievalLog(query, hits);
		return hits;
	}

	/**
	 * Retrieval-debug log: one {@code [QSEVAL]} line per hit at DEBUG level so the eval harness
	 * ({@code /tmp/qs_smoke_eval.py}) can grep its rubric against the actual retrieval order
	 * when querystore DEBUG is enabled. DEBUG (not INFO) because the line emits patient record
	 * text — INFO would land PHI in production server logs by default. Operators running an
	 * eval pass against a deployment enable DEBUG on this package via log4j2 config; production
	 * pays nothing because the {@link Log#isDebugEnabled()} guard short-circuits before any
	 * string concatenation.
	 */
	private void emitRetrievalLog(String query, List<QueryDocument> hits) {
		if (!log.isDebugEnabled() || hits == null || hits.isEmpty()) {
			return;
		}
		String safeQuery = query == null ? "" : query.replace('\n', ' ').replace('\r', ' ');
		for (int i = 0; i < hits.size(); i++) {
			QueryDocument d = hits.get(i);
			String text = d == null || d.getText() == null
					? "" : d.getText().replace('\n', ' ').replace('\r', ' ');
			log.debug("[QSEVAL] q=[" + safeQuery + "] rank=" + (i + 1)
					+ " type=" + (d == null ? "null" : d.getResourceType())
					+ " uuid=" + (d == null ? "null" : d.getResourceUuid())
					+ " text=[" + text + "]");
		}
	}

	/** Lazy lookup avoids a Spring circular dependency: {@code BootstrapService} depends on
	 *  {@link QueryStoreService}, so resolving the reverse direction at wiring time would surface
	 *  the cycle in bean construction. Context lookup defers it to call time, when both beans are
	 *  fully wired. An unavailable service (no Spring context in test envs; narrow activation
	 *  window) throws from Context.getService and is absorbed by the catch — the search still runs
	 *  and returns whatever the backend has. */
	private void ensureIndexedSafely(String patientUuid) {
		try {
			bootstrapService().ensureIndexed(patientUuid);
		}
		catch (RuntimeException e) {
			// Index-failure must not block search; whatever did get indexed (or what was already
			// present) is still searchable. Empty results are the same outcome as before this
			// feature shipped.
			log.warn("Auto-index for patient " + patientUuid
			        + " failed; serving search with whatever is indexed", e);
		}
	}

	private BootstrapService bootstrapService() {
		return bootstrapServiceOverride == null
		        ? Context.getService(BootstrapService.class) : bootstrapServiceOverride;
	}

	private boolean isProjectionComplete() {
		try {
			return BootstrapStatusReport.from(bootstrapService().getStatus()).isComplete();
		}
		catch (RuntimeException e) {
			log.warn("Could not verify QueryStore projection completeness; reporting incomplete", e);
			return false;
		}
	}

	@Override
	public List<QueryDocument> search(String query, int limit) {
		if (backend == null) {
			return Collections.emptyList();
		}
		return runHybrid(query, limit, null);
	}

	@Override
	public List<QueryDocument> getPatientChart(String patientUuid) {
		if (backend == null || patientUuid == null) {
			return Collections.emptyList();
		}
		return getPatientChartRead(patientUuid).getDocuments();
	}

	@Override
	public PatientChartRead getPatientChartRead(String patientUuid) {
		if (patientUuid == null) {
			return PatientChartRead.complete(Collections.<QueryDocument> emptyList());
		}
		if (backend == null) {
			throw new IllegalStateException(
			        "No BackendStore wired into QueryStoreServiceImpl; cannot determine whether the patient chart is complete");
		}
		// Same cold-bootstrap protocol as searchByPatient: probe existsByPatient, lazy-project on
		// miss, then read. Decision 15 explicitly mirrors searchByPatient's behaviour here so the
		// first method to touch a never-indexed patient pays the projection cost once. The shared
		// ensureIndexedSafely also keeps the swallow-on-failure semantics consistent — an
		// index-failure must not block the LLM full-chart caller any more than it blocks search.
		if (!backend.existsByPatient(patientUuid)) {
			ensureIndexedSafely(patientUuid);
		}
		PatientChartRead chartRead = backend.findPatientChart(patientUuid);
		return new PatientChartRead(chartRead.getDocuments(), chartRead.isTruncated(),
		        isProjectionComplete());
	}

	private List<QueryDocument> runHybrid(String query, int limit, Filter scope) {
		if (StringUtils.isBlank(query) || limit <= 0) {
			return Collections.emptyList();
		}
		SearchRequest.Builder req = SearchRequest.builder().queryText(query).limit(limit);
		if (scope != null) {
			req.filter(scope);
		}
		if (embeddingProvider != null) {
			req.queryVector(embedQueryCached(query));
		}
		return toDocuments(backend.hybrid(req.build()));
	}

	/**
	 * Returns the dense vector for {@code query}, reusing a previously-computed result when the
	 * same string has been embedded by the same model. Cache key includes the model name so an
	 * embedding-provider switch (see {@code querystore.embedding.providerBean} GP and
	 * {@link org.openmrs.module.querystore.embedding.ConfiguredEmbeddingProvider}) does not return
	 * a vector from the previous model. The cache is a small bounded LRU; queries beyond
	 * {@link #QUERY_EMBED_CACHE_MAX} oldest entries fall back to a live ONNX encode.
	 */
	private float[] embedQueryCached(String query) {
		String modelName = embeddingProvider.getModelName();
		String key = (modelName == null ? "" : modelName) + '\u0000' + query;
		float[] cached = queryEmbedCache.get(key);
		if (cached != null) {
			return cached;
		}
		float[] vec = embeddingProvider.embedQuery(query);
		if (vec != null) {
			queryEmbedCache.put(key, vec);
		}
		return vec;
	}

	private static List<QueryDocument> toDocuments(SearchResult result) {
		List<QueryDocument> out = new ArrayList<>(result.getHits().size());
		result.getHits().forEach(h -> out.add(h.getDocument()));
		return out;
	}

	@Override
	public ContextSlice getContextSlice(String patientUuid, String question, ContextSliceRequest request) {
		if (request == null) {
			request = new ContextSliceRequest(Collections.<String> emptySet(), false);
		}
		// Composed over the sibling complete-chart read so cold-bootstrap, ordering, and any
		// backend-documented cap behave identically (Decision 17 §3).
		PatientChartRead chartRead = getPatientChartRead(patientUuid);
		List<QueryDocument> chart = chartRead.getDocuments();

		// Server-side interpretation (ADR Decision 18): derived types UNION the caller's,
		// derived temporal ORs the caller's — module-contributed additions always survive.
		Set<String> effectiveTypes = new HashSet<>(request.getTypes());
		boolean temporal = request.isTemporal();
		if (request.isInterpretQuestion()) {
			ContextQuestionInterpreter.Interpretation interpreted =
			        ContextQuestionInterpreter.interpret(question);
			effectiveTypes.addAll(interpreted.types);
			temporal = temporal || interpreted.temporal;
		}

		// Ranked-search hits are the semantic catch-all tier. Retrieval preprocessing (panel
		// expansion + stopword stripping) runs HERE, at the owner of the embedder — idempotent
		// for callers that still preprocess. A failure degrades to the policy tiers alone —
		// selection must never block on the ranking layer.
		Map<String, Integer> similarityRanks = new LinkedHashMap<>();
		if (StringUtils.isNotBlank(question) && request.getSimilarityLimit() > 0) {
			try {
				String retrievalQuestion = ContextQuestionInterpreter.preprocess(question);
				for (QueryDocument hit : searchByPatient(patientUuid, retrievalQuestion,
				        request.getSimilarityLimit())) {
					if (hit != null && hit.getResourceUuid() != null
					        && !similarityRanks.containsKey(hit.getResourceUuid())) {
						similarityRanks.put(hit.getResourceUuid(),
						        Integer.valueOf(similarityRanks.size() + 1));
					}
				}
			}
			catch (RuntimeException e) {
				log.warn("Context-slice ranked search failed for patient " + patientUuid
				        + " — proceeding with policy tiers only", e);
			}
		}

		int anchor = temporal ? request.getRecencyAnchorSize() : 0;
		Set<String> exactSignals = exactSignals(question);
		List<ContextSliceRecord> selected = new ArrayList<>();
		Set<String> selectedUuids = new HashSet<>();
		for (int i = 0; i < chart.size(); i++) {
			QueryDocument doc = chart.get(i);
			if (doc == null || doc.getResourceUuid() == null) {
				continue;
			}
			String tier = null;
			if (isMandatoryCore(doc)) {
				tier = QueryStoreConstants.TIER_MANDATORY;
			} else if (matchesExactSignal(doc, exactSignals)) {
				tier = QueryStoreConstants.TIER_EXACT;
			} else if (doc.getResourceType() != null && effectiveTypes.contains(doc.getResourceType())) {
				tier = QueryStoreConstants.TIER_TYPED;
			} else if (i < anchor) {
				tier = QueryStoreConstants.TIER_RECENCY_ANCHOR;
			} else if (similarityRanks.containsKey(doc.getResourceUuid())) {
				tier = QueryStoreConstants.TIER_SIMILARITY;
			}
			if (tier != null && selectedUuids.add(doc.getResourceUuid())) {
				Integer rank = QueryStoreConstants.TIER_SIMILARITY.equals(tier)
				        ? similarityRanks.get(doc.getResourceUuid()) : null;
				selected.add(new ContextSliceRecord(doc, tier, rank));
			}
		}
		selected = completePanelFamilies(chart, selected, selectedUuids);

		return new ContextSlice(selected, chart.size(), chartRead.isTruncated(),
		        chartRead.isProjectionComplete(), effectiveTypes, temporal,
		        PatientChartFingerprint.snapshotId(chart, chartRead.isTruncated(),
		                chartRead.isProjectionComplete()));
	}

	private static Set<String> exactSignals(String question) {
		Set<String> signals = new HashSet<>();
		if (StringUtils.isBlank(question)) {
			return signals;
		}
		collectMatches(QUOTED_PHRASE, question, signals, true);
		collectMatches(ISO_DATE, question, signals, false);
		collectMatches(UUID, question, signals, false);
		collectMatches(LABELED_IDENTIFIER, question, signals, true);
		collectMatches(NAMESPACE_CODE, question, signals, false);
		return signals;
	}

	private static void collectMatches(Pattern pattern, String question, Set<String> sink,
	        boolean firstNonEmptyGroup) {
		Matcher matcher = pattern.matcher(question);
		while (matcher.find()) {
			String value = matcher.group();
			if (firstNonEmptyGroup) {
				value = null;
				for (int group = 1; group <= matcher.groupCount(); group++) {
					if (StringUtils.isNotBlank(matcher.group(group))) {
						value = matcher.group(group);
						break;
					}
				}
			}
			if (StringUtils.isNotBlank(value)) {
				sink.add(value.trim().toLowerCase(Locale.ROOT));
			}
		}
	}

	private static boolean matchesExactSignal(QueryDocument doc, Set<String> signals) {
		if (signals.isEmpty()) {
			return false;
		}
		StringBuilder values = new StringBuilder();
		appendValue(values, doc.getResourceUuid());
		appendValue(values, doc.getDate());
		appendValue(values, doc.getText());
		appendValue(values, doc.getMetadata());
		String searchable = values.toString().toLowerCase(Locale.ROOT);
		for (String signal : signals) {
			if (containsBounded(searchable, signal)) {
				return true;
			}
		}
		return false;
	}

	private static void appendValue(StringBuilder sink, Object value) {
		if (value instanceof Map) {
			for (Object child : ((Map<?, ?>) value).values()) {
				appendValue(sink, child);
			}
		} else if (value instanceof Iterable) {
			for (Object child : (Iterable<?>) value) {
				appendValue(sink, child);
			}
		} else if (value != null) {
			sink.append(value).append('\n');
		}
	}

	private static boolean containsBounded(String searchable, String signal) {
		int from = 0;
		while (from <= searchable.length() - signal.length()) {
			int index = searchable.indexOf(signal, from);
			if (index < 0) {
				return false;
			}
			int end = index + signal.length();
			boolean leftBounded = index == 0 || !Character.isLetterOrDigit(searchable.charAt(index - 1));
			boolean rightBounded = end == searchable.length()
					|| !Character.isLetterOrDigit(searchable.charAt(end));
			if (leftBounded && rightBounded) {
				return true;
			}
			from = index + 1;
		}
		return false;
	}

	/**
	 * The mandatory clinical core (Decision 17 tier 1): the patient demographics record, every
	 * allergy, and every condition/diagnosis whose {@code clinical_status} metadata is ACTIVE.
	 * Safety context every slice carries regardless of the caller's typed scope.
	 */
	private static boolean isMandatoryCore(QueryDocument doc) {
		String type = doc.getResourceType();
		if ("patient".equals(type) || "allergy".equals(type)) {
			return true;
		}
		if ("condition".equals(type) || "diagnosis".equals(type)) {
			Object status = doc.getMetadata().get(QueryStoreConstants.FIELD_CLINICAL_STATUS);
			return status != null && "ACTIVE".equalsIgnoreCase(status.toString());
		}
		return false;
	}

	/**
	 * Obs-group family completion (Decision 17 tier {@code panel}): when a group parent or any
	 * member is already selected, the whole family joins — panel values live in member obs whose
	 * text carries no panel name, so ranking can match the parent and miss every value. Returns a
	 * REBUILT list scanned from the chart so joined members keep chart order instead of being
	 * appended after unrelated newer records.
	 */
	private static List<ContextSliceRecord> completePanelFamilies(List<QueryDocument> chart,
	        List<ContextSliceRecord> selected, Set<String> selectedUuids) {
		Set<String> families = new HashSet<>();
		for (QueryDocument doc : chart) {
			if (doc == null || doc.getResourceUuid() == null) {
				continue;
			}
			Object group = doc.getMetadata().get(QueryStoreConstants.FIELD_OBS_GROUP_UUID);
			if (group == null) {
				continue;
			}
			String groupUuid = group.toString();
			if (selectedUuids.contains(doc.getResourceUuid()) || selectedUuids.contains(groupUuid)) {
				families.add(groupUuid);
			}
		}
		if (families.isEmpty()) {
			return selected;
		}
		Map<String, ContextSliceRecord> byUuid = new LinkedHashMap<>();
		for (ContextSliceRecord record : selected) {
			byUuid.put(record.getDocument().getResourceUuid(), record);
		}
		List<ContextSliceRecord> completed = new ArrayList<>();
		for (QueryDocument doc : chart) {
			if (doc == null || doc.getResourceUuid() == null) {
				continue;
			}
			Object group = doc.getMetadata().get(QueryStoreConstants.FIELD_OBS_GROUP_UUID);
			boolean inFamily = (group != null && families.contains(group.toString()))
			        || families.contains(doc.getResourceUuid());
			ContextSliceRecord already = byUuid.get(doc.getResourceUuid());
			if (already != null) {
				if (inFamily && !isProtectedTier(already.getTier())) {
					completed.add(new ContextSliceRecord(doc, QueryStoreConstants.TIER_PANEL,
					        already.getRank()));
				} else {
					completed.add(already);
				}
				continue;
			}
			if (inFamily) {
				completed.add(new ContextSliceRecord(doc, QueryStoreConstants.TIER_PANEL));
			}
		}
		return completed;
	}

	private static boolean isProtectedTier(String tier) {
		return QueryStoreConstants.TIER_MANDATORY.equals(tier)
		        || QueryStoreConstants.TIER_EXACT.equals(tier)
		        || QueryStoreConstants.TIER_TYPED.equals(tier)
		        || QueryStoreConstants.TIER_PANEL.equals(tier);
	}
}
