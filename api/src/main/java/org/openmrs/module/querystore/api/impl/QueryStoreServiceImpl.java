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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
import org.openmrs.util.PrivilegeConstants;

/**
 * Default {@link QueryStoreService}. Delegates writes and search to the configured
 * {@link BackendStore}; hybrid fusion is the SPI's default-method responsibility (BM25 + kNN with
 * rank-based RRF — backends with native fusion may override; see ADR Decision 3 SPI sub-point 2).
 * The {@link EmbeddingProvider} dependency is optional: when null, search degrades to BM25-only.
 */
public class QueryStoreServiceImpl extends BaseOpenmrsService implements QueryStoreService {

	private static final Log log = LogFactory.getLog(QueryStoreServiceImpl.class);

	/**
	 * A phrase of at least two characters inside straight or curly double quotes, or inside single
	 * quotes that open and close at a word edge. The phrase starts and ends on a non-space, so the
	 * closing quote of one phrase and the opening quote of the next never enclose the words between
	 * them. An apostrophe inside a word (patient's) neither opens nor closes a single-quoted phrase,
	 * and punctuation just inside the closing quote ("fever," or "chest pain.") is not part of it.
	 * The single-quote loops are bounded: each repetition of a group with alternatives is a recursive
	 * call in Java's regex engine, so an unbounded one overflows the stack on a long question.
	 */
	private static final Pattern QUOTED_PHRASE = Pattern.compile(
	        "\"([^\"\\s][^\"]*[^\"\\s.,;:!?])[.,;:!?]*\""
	                + "|\\u201C([^\\u201C\\u201D\\s][^\\u201C\\u201D]*[^\\u201C\\u201D\\s.,;:!?])[.,;:!?]*\\u201D"
	                + "|(?<![\\p{L}\\p{N}])'([^'\\s](?:[^']|'(?=[\\p{L}\\p{N}])){0,200}[^'\\s.,;:!?])[.,;:!?]*'(?![\\p{L}\\p{N}])"
	                + "|(?<![\\p{L}\\p{N}])\\u2018([^\\u2018\\u2019\\s](?:[^\\u2018\\u2019]|\\u2019(?=[\\p{L}\\p{N}]))"
	                + "{0,200}[^\\u2018\\u2019\\s.,;:!?])[.,;:!?]*\\u2019(?![\\p{L}\\p{N}])");

	private static final Pattern ISO_DATE = Pattern.compile("\\b\\d{4}-\\d{2}-\\d{2}\\b");

	private static final Pattern UUID = Pattern.compile(
	        "\\b[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\\b",
	        Pattern.CASE_INSENSITIVE);

	private static final Pattern LABELED_IDENTIFIER = Pattern.compile(
	        "\\b(?:mrn|id|identifier|code)(?:\\s+is\\s+|\\s*[:=#]\\s*)((?=[a-z._/-]*\\d)[a-z0-9][a-z0-9._/-]{2,})\\b",
	        Pattern.CASE_INSENSITIVE);

	private static final Pattern NAMESPACE_CODE = Pattern.compile(
	        "\\b[a-z][a-z0-9_-]{1,15}:[a-z0-9][a-z0-9._-]{0,31}\\b", Pattern.CASE_INSENSITIVE);

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
		if (needsColdTouch(patientUuid)) {
			ensureIndexedSafely(patientUuid);
		}
		return rankedPatientSearch(patientUuid, query, limit);
	}

	/** The ranked read of one patient's indexed documents, without the cold touch. The context slice
	 *  runs it after its own chart read has touched the patient, so a failed touch is not repeated. */
	private List<QueryDocument> rankedPatientSearch(String patientUuid, String query, int limit) {
		List<QueryDocument> hits = readableByCaller(runHybrid(query, limit, Filter.patientScope(patientUuid)));
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
	 *  and returns whatever the backend has.
	 *
	 *  @return {@code true} when the projection ran; {@code false} when it failed and was
	 *          swallowed, so a caller that claims completeness can disclose the gap. */
	private boolean ensureIndexedSafely(String patientUuid) {
		try {
			bootstrapService().ensureIndexed(patientUuid);
			return true;
		}
		catch (RuntimeException e) {
			// Index-failure must not block search; whatever did get indexed (or what was already
			// present) is still searchable. Empty results are the same outcome as before this
			// feature shipped.
			log.warn("Auto-index for patient " + patientUuid
			        + " failed; serving search with whatever is indexed", e);
			return false;
		}
	}

	/** A patient is cold when the backend holds none of its documents, or when its last projection
	 *  left records out: it then has documents, but reading them as the whole chart would hide the
	 *  gap, so it is projected again. The second question is asked only of a patient with documents. */
	private boolean needsColdTouch(String patientUuid) {
		if (!backend.existsByPatient(patientUuid)) {
			return true;
		}
		try {
			return bootstrapService().isPatientProjectionIncomplete(patientUuid);
		}
		catch (RuntimeException e) {
			log.debug("Could not ask whether patient " + patientUuid + " was projected completely", e);
			return false;
		}
	}

	private BootstrapService bootstrapService() {
		return bootstrapServiceOverride == null
		        ? Context.getService(BootstrapService.class) : bootstrapServiceOverride;
	}

	private boolean isProjectionComplete() {
		try {
			// A type completed before counts during its restart top-up, or every answer would be
			// withheld while a restart re-walks the window since its last checkpoint.
			return bootstrapService().getStatusReport().isProjected();
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
		return readableByCaller(runHybrid(query, limit, null));
	}

	@Override
	public Map<String, Instant> findStoredVersions(String resourceType, List<String> resourceUuids) {
		if (backend == null || resourceUuids == null || resourceUuids.isEmpty()) {
			return Collections.emptyMap();
		}
		try {
			return backend.findLastModified(resourceType, resourceUuids);
		}
		catch (RuntimeException e) {
			// A failed lookup only costs the skip: the caller re-projects every record.
			log.warn("Stored-version lookup failed for " + resourceType + "; re-projecting the page", e);
			return Collections.emptyMap();
		}
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
		// Same cold-bootstrap protocol as searchByPatient: probe whether the patient is cold,
		// lazy-project if so, then read. Decision 15 explicitly mirrors searchByPatient's behaviour here so the
		// first method to touch a never-indexed patient pays the projection cost once. The shared
		// ensureIndexedSafely also keeps the swallow-on-failure semantics consistent — an
		// index-failure must not block the LLM full-chart caller any more than it blocks search.
		// It must be disclosed, though: records this patient has in OpenMRS that were never
		// projected are missing from this read, which is exactly what truncated means. Neither the
		// backend's read-side bit nor the deployment-wide projection state can see a failed touch,
		// and a touch that projected some types leaves documents behind, so needsColdTouch keeps
		// such a patient cold and every read projects it again until nothing is left out.
		boolean coldTouchFailed = false;
		if (needsColdTouch(patientUuid)) {
			coldTouchFailed = !ensureIndexedSafely(patientUuid);
		}
		PatientChartRead chartRead = backend.findPatientChart(patientUuid);
		return new PatientChartRead(readableByCaller(chartRead.getDocuments()),
		        chartRead.isTruncated() || coldTouchFailed, isProjectionComplete(),
		        typesWithheldFromCaller(CORE_READ_PRIVILEGES.keySet()));
	}

	/**
	 * The read privilege core's own {@code get<Type>ByUuid} is {@code @Authorized} with, per core
	 * resource type (verified against openmrs-api 2.9). A caller holding Get Patients but not one of
	 * these is refused that type's records by core, so Decision 14 point 4 refuses them here too.
	 * A module-contributed type has no core privilege and stays behind the Get Patients gate alone.
	 */
	static final Map<String, String> CORE_READ_PRIVILEGES;

	static {
		Map<String, String> privileges = new HashMap<String, String>();
		privileges.put("patient", PrivilegeConstants.GET_PATIENTS);
		privileges.put("obs", PrivilegeConstants.GET_OBS);
		privileges.put("encounter", PrivilegeConstants.GET_ENCOUNTERS);
		privileges.put("visit", PrivilegeConstants.GET_VISITS);
		privileges.put("condition", PrivilegeConstants.GET_CONDITIONS);
		privileges.put("diagnosis", PrivilegeConstants.GET_DIAGNOSES);
		privileges.put("allergy", PrivilegeConstants.GET_ALLERGIES);
		privileges.put("drug_order", PrivilegeConstants.GET_ORDERS);
		privileges.put("test_order", PrivilegeConstants.GET_ORDERS);
		privileges.put("referral_order", PrivilegeConstants.GET_ORDERS);
		privileges.put("program", PrivilegeConstants.GET_PATIENT_PROGRAMS);
		privileges.put("medication_dispense", PrivilegeConstants.GET_MEDICATION_DISPENSE);
		CORE_READ_PRIVILEGES = Collections.unmodifiableMap(privileges);
	}

	/**
	 * The types among {@code types} the calling user may not read through core, in a stable order.
	 * With no user context bound nothing is withheld, as {@link #readableByCaller} refuses nothing.
	 */
	private static Set<String> typesWithheldFromCaller(Set<String> types) {
		Set<String> withheld = new TreeSet<String>();
		if (types.isEmpty() || !Context.isSessionOpen()) {
			return withheld;
		}
		for (String type : types) {
			String privilege = CORE_READ_PRIVILEGES.get(type);
			if (privilege != null && !Context.hasPrivilege(privilege)) {
				withheld.add(type);
			}
		}
		return withheld;
	}

	/**
	 * The documents whose type the calling user may read through core. With no user context bound
	 * there is no caller to check, and nothing reaches one: a call through the service proxy is refused
	 * by its {@code @Authorized} gate before it gets here. Each type's privilege is asked once in this
	 * filtering pass; {@link #typesWithheldFromCaller} asks again to report what was refused.
	 */
	private List<QueryDocument> readableByCaller(List<QueryDocument> documents) {
		if (documents.isEmpty() || !Context.isSessionOpen()) {
			return documents;
		}
		Map<String, Boolean> readable = new HashMap<String, Boolean>();
		List<QueryDocument> kept = new ArrayList<QueryDocument>(documents.size());
		for (QueryDocument document : documents) {
			String privilege = document == null ? null : CORE_READ_PRIVILEGES.get(document.getResourceType());
			if (privilege == null) {
				kept.add(document);
				continue;
			}
			Boolean mayRead = readable.get(privilege);
			if (mayRead == null) {
				mayRead = Context.hasPrivilege(privilege);
				readable.put(privilege, mayRead);
			}
			if (mayRead) {
				kept.add(document);
			}
		}
		return kept.size() == documents.size() ? documents : kept;
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

		Set<String> effectiveTypes = new HashSet<>(request.getTypes());
		boolean temporal = request.isTemporal();
		// Every type the caller cannot read through core is withheld and named, requested or not: in the
		// scope it would come back as an empty typed tier, and in the mandatory tier (which carries
		// every allergy unasked) as no records, either of which reads as a patient without such
		// records, down to the sliceId.
		Set<String> withheldTypes = chartRead.getWithheldTypes();
		effectiveTypes.removeAll(withheldTypes);

		// Ranked-search hits are the semantic catch-all tier. A failure degrades to the policy
		// tiers alone — selection must never block on the ranking layer.
		Map<String, Integer> similarityRanks = new LinkedHashMap<>();
		if (patientUuid != null && StringUtils.isNotBlank(question) && request.getSimilarityLimit() > 0) {
			try {
				// The chart read above already ran any cold touch; a failed one is not run twice.
				for (QueryDocument hit : rankedPatientSearch(patientUuid, question,
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
		Map<String, Boolean> exactSignals = exactSignals(question);
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
		                chartRead.isProjectionComplete()), withheldTypes);
	}

	/**
	 * The exact signals in {@code question}, each with whether it is an identifier: an identifier or
	 * code is bounded by {@link #continuesIdentifier}, so {@code ABC-123} is not found inside
	 * {@code ABC-123-4}, while a quoted phrase or a date is bounded by letters and digits alone, so
	 * {@code "vomiting"} is found in {@code nausea/vomiting} and {@code "COVID"} in {@code COVID-19}.
	 */
	private static Map<String, Boolean> exactSignals(String question) {
		Map<String, Boolean> signals = new HashMap<>();
		if (StringUtils.isBlank(question)) {
			return signals;
		}
		collectMatches(QUOTED_PHRASE, question, signals, true, false);
		collectMatches(ISO_DATE, question, signals, false, false);
		collectMatches(UUID, question, signals, false, true);
		collectMatches(LABELED_IDENTIFIER, question, signals, true, true);
		collectMatches(NAMESPACE_CODE, question, signals, false, true);
		return signals;
	}

	private static void collectMatches(Pattern pattern, String question, Map<String, Boolean> sink,
	        boolean firstNonEmptyGroup, boolean identifier) {
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
				String signal = value.trim().toLowerCase(Locale.ROOT);
				sink.put(signal, Boolean.valueOf(identifier || Boolean.TRUE.equals(sink.get(signal))));
			}
		}
	}

	private static boolean matchesExactSignal(QueryDocument doc, Map<String, Boolean> signals) {
		if (signals.isEmpty()) {
			return false;
		}
		StringBuilder values = new StringBuilder();
		appendValue(values, doc.getResourceUuid());
		appendValue(values, doc.getDate());
		appendValue(values, doc.getText());
		appendValue(values, doc.getMetadata());
		String searchable = values.toString().toLowerCase(Locale.ROOT);
		for (Map.Entry<String, Boolean> signal : signals.entrySet()) {
			if (containsBounded(searchable, signal.getKey(), signal.getValue().booleanValue())) {
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

	/**
	 * Whether {@code signal} occurs in {@code searchable} with no letter or digit on either side,
	 * nor, for an identifier, a connector that continues it ({@link #continuesIdentifier}).
	 * A date signal is also bounded by the {@code t} of an ISO date-time ({@code 2024-03-05t10:15}),
	 * so a question naming the day a visit ended matches its {@code end_date_time}.
	 */
	private static boolean containsBounded(String searchable, String signal, boolean identifier) {
		boolean dateSignal = ISO_DATE.matcher(signal).matches();
		int from = 0;

		while (from <= searchable.length() - signal.length()) {
			int index = searchable.indexOf(signal, from);
			if (index < 0) {
				return false;
			}
			int end = index + signal.length();
			boolean leftBounded = index == 0 || !continues(searchable, index - 1, -1, identifier);
			boolean rightBounded = end == searchable.length()
					|| !continues(searchable, end, 1, identifier)
					|| (dateSignal && searchable.charAt(end) == 't' && end + 1 < searchable.length()
					        && Character.isDigit(searchable.charAt(end + 1)));
			if (leftBounded && rightBounded) {
				return true;
			}
			from = index + 1;
		}
		return false;
	}

	/**
	 * Whether the character at {@code at} continues the signal: for an identifier, as
	 * {@link #continuesIdentifier} says; for a quoted phrase, a letter or digit.
	 */
	private static boolean continues(String searchable, int at, int step, boolean identifier) {
		return identifier ? continuesIdentifier(searchable, at, step)
		        : Character.isLetterOrDigit(searchable.charAt(at));
	}

	/**
	 * Whether the character at {@code at} continues an identifier beyond the signal: a letter or digit,
	 * or a connector such as {@code -} or {@code .} with a letter or digit beyond it, so that
	 * {@code ABC-123} is not found inside {@code ABC-123-4} while {@code CIEL:60.} at a sentence end
	 * still matches.
	 */
	private static boolean continuesIdentifier(String searchable, int at, int step) {
		char c = searchable.charAt(at);
		if (Character.isLetterOrDigit(c)) {
			return true;
		}
		int beyond = at + step;
		return (c == '-' || c == '.' || c == '_' || c == '/' || c == ':') && beyond >= 0
		        && beyond < searchable.length() && Character.isLetterOrDigit(searchable.charAt(beyond));
	}

	/**
	 * The mandatory clinical core (Decision 17 tier 1): the patient demographics record, every
	 * allergy, and every condition/diagnosis whose {@code clinical_status} metadata is ACTIVE,
	 * RECURRENCE or RELAPSE, the statuses core's {@code ConditionService.getActiveConditions} counts
	 * as active since TRUNK-6170. Safety context every slice carries regardless of the caller's typed
	 * scope.
	 */
	private static boolean isMandatoryCore(QueryDocument doc) {
		String type = doc.getResourceType();
		if ("patient".equals(type) || "allergy".equals(type)) {
			return true;
		}
		if ("condition".equals(type) || "diagnosis".equals(type)) {
			// The statuses core's ConditionService.getActiveConditions counts as active (TRUNK-6170).
			Object status = doc.getMetadata().get(QueryStoreConstants.FIELD_CLINICAL_STATUS);
			String clinicalStatus = status == null ? null : status.toString();
			return "ACTIVE".equalsIgnoreCase(clinicalStatus) || "RECURRENCE".equalsIgnoreCase(clinicalStatus)
			        || "RELAPSE".equalsIgnoreCase(clinicalStatus);
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
