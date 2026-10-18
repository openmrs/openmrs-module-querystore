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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BooleanSupplier;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.querystore.QueryStoreConstants;
import org.openmrs.module.querystore.SafeLog;
import org.openmrs.module.querystore.api.IndexStatus;
import org.openmrs.module.querystore.api.QueryResult;
import org.openmrs.module.querystore.api.QuerySpec;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.backend.BackendStore;
import org.openmrs.module.querystore.backend.BulkWriteResult;
import org.openmrs.module.querystore.backend.DocFailure;
import org.openmrs.module.querystore.backend.Filter;
import org.openmrs.module.querystore.backend.PatientChartRead;
import org.openmrs.module.querystore.backend.SearchRequest;
import org.openmrs.module.querystore.backend.SearchResult;
import org.openmrs.module.querystore.backend.WriteResult;
import org.openmrs.module.querystore.bootstrap.BootstrapLauncher;
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

	/**
	 * Each exact signal is scanned against every chart record, so a question's signals are capped: a
	 * question listing about 550 dates took 3 to 4 s over a 10,000-record chart, against about 50 ms.
	 */
	private static final int MAX_EXACT_SIGNALS = 32;

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

	/** Default for {@code querystore.query.candidatePool}. */
	public static final int DEFAULT_CANDIDATE_POOL = 200;

	/** Largest candidate pool accepted, whatever the global property says: each request holds it in memory. */
	public static final int MAX_CANDIDATE_POOL = 1000;

	/** Default for {@code querystore.projection.syncWaitSeconds}. */
	public static final int DEFAULT_SYNC_PROJECTION_WAIT_SECONDS = 10;

	/** Longest synchronous wait honoured: the waiting request holds its server thread. */
	public static final int MAX_SYNC_PROJECTION_WAIT_SECONDS = 30;

	// Candidate pool for query(): its service-layer collapse, recency sort, facets and paging all run
	// over this many backend hits. Set from querystore.query.candidatePool at module start.
	private volatile int candidatePool = DEFAULT_CANDIDATE_POOL;

	// How long a synchronous cold-touch projection waits for a projection slot before serving what is
	// indexed. Set from querystore.projection.syncWaitSeconds at module start.
	private volatile long syncProjectionWaitMillis = DEFAULT_SYNC_PROJECTION_WAIT_SECONDS * 1000L;

	// [QSEVAL] lines carry clinical text, so they have a logger of their own outside this module's
	// package: DEBUG on org.openmrs.module.querystore, which troubleshooting turns on, leaves them off.
	private static final Log qsevalLog = LogFactory.getLog("querystore.qseval");

	int candidatePool() {
		return candidatePool;
	}

	public void setCandidatePool(int candidatePool) {
		this.candidatePool = Math.max(1, Math.min(MAX_CANDIDATE_POOL, candidatePool));
	}

	public void setSyncProjectionWaitMillis(long millis) {
		this.syncProjectionWaitMillis = Math.max(0, Math.min(MAX_SYNC_PROJECTION_WAIT_SECONDS * 1000L, millis));
	}

	// Optional: without it, query()/warm() can't start a background projection and getIndexStatus()
	// never reports PROJECTING. Wired in moduleApplicationContext.xml.
	private BootstrapLauncher bootstrapLauncher;

	public void setBootstrapLauncher(BootstrapLauncher bootstrapLauncher) {
		this.bootstrapLauncher = bootstrapLauncher;
	}

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
			log.warn("No BackendStore wired; ignoring a bulkDeleteByPatient call");
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
	 * ({@code /tmp/qs_smoke_eval.py}) can grep its rubric against the actual retrieval order. DEBUG
	 * (not INFO) because the line emits patient record text — INFO would land PHI in production
	 * server logs by default. It has its own logger, {@code querystore.qseval}, outside the module's
	 * package, so operators running an eval pass enable DEBUG on that logger by name and DEBUG on the
	 * package does not turn it on; production pays nothing because the
	 * {@link Log#isDebugEnabled()} guard short-circuits before any string concatenation.
	 */
	private void emitRetrievalLog(String query, List<QueryDocument> hits) {
		if (!qsevalLog.isDebugEnabled() || hits == null || hits.isEmpty()) {
			return;
		}
		String safeQuery = query == null ? "" : query.replace('\n', ' ').replace('\r', ' ');
		for (int i = 0; i < hits.size(); i++) {
			QueryDocument d = hits.get(i);
			String text = d == null || d.getText() == null
					? "" : d.getText().replace('\n', ' ').replace('\r', ' ');
			qsevalLog.debug("[QSEVAL] q=[" + safeQuery + "] rank=" + (i + 1)
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
			// Through a projection slot, shared with background warms, so a burst of searches on cold
			// patients cannot run a projection each at once. With no slot free in time the search
			// serves what is indexed, as it does when projection fails, and the read is not complete.
			if (bootstrapLauncher == null) {
				bootstrapService().ensureIndexed(patientUuid);
				return true;
			}
			if (bootstrapLauncher.runWithProjectionSlot(patientUuid,
			    () -> bootstrapService().projectIfNotIndexed(patientUuid), syncProjectionWaitMillis)) {
				// True also when this call only waited for another projection of the patient, which
				// may have failed: ask, rather than take the chart as complete.
				return !bootstrapLauncher.hasFailed(patientUuid)
				        && !bootstrapService().isPatientProjectionIncomplete(patientUuid);
			}
			log.debug("No projection slot for a cold patient in time; serving search with whatever is indexed");
			return false;
		}
		catch (RuntimeException e) {
			// Index-failure must not block search; whatever did get indexed (or what was already
			// present) is still searchable. Empty results are the same outcome as before this
			// feature shipped.
			SafeLog.warn(QueryStoreServiceImpl.class, "Auto-index for a cold patient failed; serving search with whatever is indexed", e);
			log.debug("Auto-index failed for patient " + patientUuid);
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
			SafeLog.warn(QueryStoreServiceImpl.class, "Could not verify QueryStore projection completeness; reporting incomplete", e);
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
			SafeLog.warn(QueryStoreServiceImpl.class, "Stored-version lookup failed for " + resourceType + "; re-projecting the page", e);
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

	@Override
	public Set<String> getTypesWithheldFromCaller() {
		return typesWithheldFromCaller(CORE_READ_PRIVILEGES.keySet());
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

	@Override
	public IndexStatus getIndexStatus(String patientUuid) {
		if (backend == null || patientUuid == null) {
			return IndexStatus.NOT_INDEXED;
		}
		// Warming first: a background projection writes type by type, so existsByPatient turns true
		// long before the chart is complete.
		if (bootstrapLauncher != null && bootstrapLauncher.isWarming(patientUuid)) {
			return IndexStatus.PROJECTING;
		}
		// Failed before indexed: a warm that threw partway leaves a partial chart that exists.
		if (bootstrapLauncher != null && bootstrapLauncher.hasFailed(patientUuid)) {
			return IndexStatus.FAILED;
		}
		return backend.existsByPatient(patientUuid) ? IndexStatus.INDEXED : IndexStatus.NOT_INDEXED;
	}

	/**
	 * Deletes and re-projects the patient under its per-patient lock, inside a projection slot, waiting
	 * (bounded) for one and for any projection of the patient already in flight. A full re-projection,
	 * so success clears a recorded failure.
	 */
	@Override
	public IndexStatus reindex(String patientUuid) {
		if (backend == null || patientUuid == null) {
			return IndexStatus.NOT_INDEXED;
		}
		BooleanSupplier work = () -> {
			bootstrapService().reindexPatient(patientUuid);
			return true;
		};
		try {
			boolean ran = bootstrapLauncher != null
			        ? bootstrapLauncher.runExclusiveProjection(patientUuid, work, syncProjectionWaitMillis)
			        : work.getAsBoolean();
			return ran ? getIndexStatus(patientUuid) : IndexStatus.NOT_INDEXED;
		}
		catch (RuntimeException e) {
			SafeLog.warn(QueryStoreServiceImpl.class, "Patient reindex failed (patient uuid at DEBUG)", e);
			log.debug("Patient reindex failed for " + patientUuid);
			return IndexStatus.FAILED;
		}
	}

	@Override
	public IndexStatus warm(String patientUuid) {
		IndexStatus status = getIndexStatus(patientUuid);
		if (status == IndexStatus.PROJECTING || status == IndexStatus.INDEXED || backend == null
		        || patientUuid == null) {
			return status;
		}
		// A retry re-projects in full: the failed warm may have left a partial chart, which
		// ensureIndexed would take as indexed and skip. It waits out a backoff, so a chart that never
		// projects is not re-run by every keystroke or POST warm.
		boolean retry = status == IndexStatus.FAILED;
		if (retry && bootstrapLauncher != null && bootstrapLauncher.millisUntilRetry(patientUuid) > 0) {
			return status;
		}
		BooleanSupplier work = () -> {
			boolean full;
			if (retry) {
				bootstrapService().reindexPatient(patientUuid);
				full = true;
			} else {
				full = bootstrapService().projectIfNotIndexed(patientUuid);
			}
			// A live patient always projects at least its patient document, so nothing indexed means
			// the projection failed (or the uuid is no live patient's).
			if (!backend.existsByPatient(patientUuid)) {
				throw new IllegalStateException("Warm finished with nothing indexed");
			}
			return full;
		};
		return bootstrapLauncher != null && bootstrapLauncher.launchPatientWarm(patientUuid, work)
		        ? IndexStatus.PROJECTING : status;
	}

	/**
	 * Prototype: collapse, recency sort, facets and paging all run here, over a candidate pool of the
	 * top {@code querystore.query.candidatePool} backend hits (or the whole chart for a recency browse).
	 * Each has a
	 * backend-native form that would scale better, noted where it happens.
	 */
	@Override
	public QueryResult query(QuerySpec spec) {
		if (backend == null) {
			return QueryResult.empty(IndexStatus.NOT_INDEXED, spec);
		}
		requireKnownTypes(spec.getTypes());
		String patientUuid = spec.getPatientUuid();
		IndexStatus status = getIndexStatus(patientUuid);
		if (status == IndexStatus.FAILED) {
			// Retried by a warm once its backoff has run out (the omnibar's searches are the only warms
			// it gets), not on every keystroke. Until it re-projects, its chart may be partial.
			warm(patientUuid);
			status = getIndexStatus(patientUuid);
		}
		if (status != IndexStatus.INDEXED && status != IndexStatus.FAILED) {
			if (spec.isWait()) {
				// Waits (bounded) for a projection of its own, or for the one already in flight.
				ensureIndexedSafely(patientUuid);
			} else if (status == IndexStatus.NOT_INDEXED) {
				warm(patientUuid);
			}
			status = getIndexStatus(patientUuid);
		}
		// A projecting chart is served as far as it has landed, labelled projecting: the caller says
		// "still indexing", since a partial chart answers "no CD4" for obs that haven't landed yet. A
		// failed chart gets no hits (it may be partial, with nothing on its way to fill it), and a
		// chart with nothing indexed has none to give.
		if (status == IndexStatus.FAILED || status == IndexStatus.NOT_INDEXED) {
			return QueryResult.empty(status, spec);
		}
		if (status == IndexStatus.PROJECTING && !spec.isPartialResults()) {
			return QueryResult.statusOnly(status);
		}

		List<QueryResult.Hit> pool = candidates(spec);
		boolean faceted = spec.getFacets().contains(QuerySpec.Facet.RESOURCE_TYPE);
		// Hybrid's vector half returns its nearest documents however far they are, so it has no match
		// boundary: relevance order demotes the far ones, but a date sort or a count would not. Both
		// therefore run over the keyword matches only.
		List<QueryResult.Hit> matches = spec.getMode() == QuerySpec.Mode.HYBRID && spec.getQuery() != null
		        && (spec.getSort() == QuerySpec.Sort.RECENT || faceted) ? keywordMatches(spec, pool) : pool;
		if (spec.getSort() == QuerySpec.Sort.RECENT) {
			// Backend-native: a record_date sort on the search itself (ES "sort", Lucene Sort on the
			// RECORD_DATE doc value) instead of re-sorting a relevance-capped pool, which drops older
			// matches that ranked below the cap.
			pool = new ArrayList<>(matches);
			pool.sort(NEWEST_FIRST);
		}
		Map<String, Integer> typeCounts = faceted ? countByResourceType(matches) : null;

		List<QueryResult.Hit> hits;
		List<QueryResult.ConceptGroup> groups = null;
		if (spec.getCollapse() == QuerySpec.Collapse.CONCEPT) {
			groups = page(collapseByConcept(pool, spec), spec.getOffset(), spec.getLimit());
			hits = Collections.emptyList();
			List<QueryDocument> logged = new ArrayList<>(groups.size());
			groups.forEach(g -> logged.add(g.getLatest().getDocument()));
			emitRetrievalLog(spec.getQuery(), logged);
		} else {
			hits = page(pool, spec.getOffset(), spec.getLimit());
			List<QueryDocument> logged = new ArrayList<>(hits.size());
			hits.forEach(h -> logged.add(h.getDocument()));
			emitRetrievalLog(spec.getQuery(), logged);
		}
		QueryResult result = new QueryResult(status, hits, groups, typeCounts);
		return status == IndexStatus.PROJECTING
		        ? result.withProgress(bootstrapService().getProjectionProgress(patientUuid)) : result;
	}

	/**
	 * Refuses a type no provider registers. The Lucene and MySQL backends create an index or table
	 * for any well-formed type a search names, so an unchecked name would create one.
	 */
	private void requireKnownTypes(List<String> types) {
		if (types.isEmpty()) {
			return;
		}
		List<String> known = bootstrapService().getResourceTypeNames();
		for (String type : types) {
			if (!known.contains(type)) {
				throw new IllegalArgumentException("Unknown resource type: " + type);
			}
		}
	}

	private List<QueryResult.Hit> candidates(QuerySpec spec) {
		List<QueryResult.Hit> pool = new ArrayList<>();
		if (spec.getQuery() == null) {
			// Recency browse: the whole chart, already newest first. Backend-native: a patient-scoped,
			// type-filtered top-N by record_date, instead of materializing the chart to keep N.
			for (QueryDocument d : backend.findAllByPatient(spec.getPatientUuid())) {
				if (spec.getTypes().isEmpty() || spec.getTypes().contains(d.getResourceType())) {
					pool.add(new QueryResult.Hit(d, null));
				}
			}
			return pool;
		}
		SearchRequest.Builder req = searchRequest(spec);
		SearchResult result;
		if (spec.getMode() == QuerySpec.Mode.KEYWORD) {
			result = backend.bm25(req.build());
		} else {
			if (embeddingProvider != null) {
				req.queryVector(embedQueryCached(spec.getQuery()));
			}
			result = backend.hybrid(req.build());
		}
		result.getHits().forEach(h -> pool.add(new QueryResult.Hit(h.getDocument(), h.getRawScore())));
		return pool;
	}

	private SearchRequest.Builder searchRequest(QuerySpec spec) {
		return SearchRequest.builder().queryText(spec.getQuery()).limit(candidatePool).resourceTypes(spec.getTypes())
		        .filter(Filter.patientScope(spec.getPatientUuid()));
	}

	/**
	 * The hybrid pool's hits that also match by keyword, in pool order. Costs a second BM25 pass
	 * (hybrid's own is internal to the backend). Backend-native: return the BM25 match set alongside
	 * the fused ranking, or apply a vector-similarity floor.
	 */
	private List<QueryResult.Hit> keywordMatches(QuerySpec spec, List<QueryResult.Hit> pool) {
		Set<String> matched = new HashSet<>();
		backend.bm25(searchRequest(spec).build()).getHits()
		        .forEach(h -> matched.add(h.getDocument().getResourceUuid()));
		List<QueryResult.Hit> out = new ArrayList<>();
		for (QueryResult.Hit h : pool) {
			if (matched.contains(h.getDocument().getResourceUuid())) {
				out.add(h);
			}
		}
		return out;
	}

	/**
	 * Groups the pool by {@code concept_uuid}, in order of each group's best-ranked member (or newest
	 * first under {@link QuerySpec.Sort#RECENT}). A group's latest, count and recent values come from
	 * the patient's whole chart rather than the pool, because the pool is ranked by relevance: the
	 * newest CD4 need not be among the top matches for "cd4".
	 *
	 * <p>Backend-native: ES field collapsing on {@code concept_uuid} with an {@code inner_hits} sorted
	 * by {@code record_date} (size 5) and a cardinality/terms aggregation for the count; Lucene
	 * grouping ({@code GroupingSearch} on a concept_uuid doc-values field) plus a date-sorted
	 * per-group read. Either removes the full-chart read this does per request.
	 */
	private List<QueryResult.ConceptGroup> collapseByConcept(List<QueryResult.Hit> pool, QuerySpec spec) {
		Map<String, List<QueryResult.Hit>> byKey = new LinkedHashMap<>();
		Map<String, Double> scoreByResource = new HashMap<>();
		boolean anyConcept = false;
		for (QueryResult.Hit h : pool) {
			QueryDocument d = h.getDocument();
			String concept = conceptUuid(d);
			anyConcept |= concept != null;
			String key = concept != null ? "c:" + concept : "d:" + d.getResourceType() + "/" + d.getResourceUuid();
			byKey.computeIfAbsent(key, k -> new ArrayList<>()).add(h);
			if (h.getScore() != null) {
				scoreByResource.put(d.getResourceUuid(), h.getScore());
			}
		}

		Map<String, List<QueryDocument>> chartByConcept = anyConcept ? chartByConcept(spec, pool)
		        : Collections.<String, List<QueryDocument>> emptyMap();
		List<QueryResult.ConceptGroup> groups = new ArrayList<>(byKey.size());
		for (List<QueryResult.Hit> members : byKey.values()) {
			QueryDocument first = members.get(0).getDocument();
			String concept = conceptUuid(first);
			List<QueryDocument> recent = concept == null ? null : chartByConcept.get(concept);
			if (recent == null || recent.isEmpty()) {
				// No concept, or the chart read raced a write: fall back to the matching members.
				recent = new ArrayList<>(members.size());
				for (QueryResult.Hit m : members) {
					recent.add(m.getDocument());
				}
				recent.sort(NEWEST_DOC);
			}
			QueryDocument latest = recent.get(0);
			Double best = null;
			for (QueryResult.Hit m : members) {
				if (m.getScore() != null && (best == null || m.getScore() > best)) {
					best = m.getScore();
				}
			}
			Object name = latest.getMetadata().get(QueryStoreConstants.FIELD_CONCEPT_NAME);
			groups.add(new QueryResult.ConceptGroup(concept, name instanceof String ? (String) name : null,
			        recent.size(), best, new QueryResult.Hit(latest, scoreByResource.get(latest.getResourceUuid())),
			        recent));
		}
		if (spec.getSort() == QuerySpec.Sort.RECENT) {
			groups.sort((a, b) -> NEWEST_FIRST.compare(a.getLatest(), b.getLatest()));
		} else if (spec.getQuery() != null) {
			groups.sort(lookupOrder(spec.getQuery()));
		}
		return groups;
	}

	/**
	 * Relevance order for concept groups, adjusted for lookup: a concept whose name or a synonym is
	 * the query itself ("cd4" is a synonym of CD4 count, not of CD4%; "weight" is "Weight (kg)" less
	 * its units) comes first, then the best member's score. On a tied score, a group with a numeric
	 * value, then one with more readings. Ties stay in pool order (the sort is stable).
	 *
	 * <p>BM25 can't make this distinction: "CD4 count: 350", "CD4%: 21" and "Tests ordered: CD4
	 * panel" all match the one token "cd4" and score the same, and in hybrid mode CD4% is often the
	 * nearer vector. Measured on the O3 demo data, see the ADR's consumer-API open question.
	 */
	private static Comparator<QueryResult.ConceptGroup> lookupOrder(String query) {
		String wanted = normalizeName(query);
		return Comparator
		        .comparing((QueryResult.ConceptGroup g) -> !namesConcept(g.getLatest().getDocument(), wanted))
		        .thenComparing(QueryResult.ConceptGroup::getScore, Comparator.nullsLast(Comparator.reverseOrder()))
		        .thenComparing(g -> g.getLatest().getDocument().getMetadata()
		                .get(QueryStoreConstants.FIELD_VALUE_NUMERIC) == null)
		        .thenComparing(QueryResult.ConceptGroup::getCount, Comparator.reverseOrder());
	}

	/** Whether the document's concept is called {@code wanted}, by its name or a synonym. */
	private static boolean namesConcept(QueryDocument d, String wanted) {
		if (wanted == null || conceptUuid(d) == null) {
			return false;
		}
		Object name = d.getMetadata().get(QueryStoreConstants.FIELD_CONCEPT_NAME);
		if (name instanceof String && wanted.equals(normalizeName((String) name))) {
			return true;
		}
		Object synonyms = d.getMetadata().get(QueryStoreConstants.FIELD_SYNONYMS);
		if (synonyms instanceof List) {
			for (Object synonym : (List<?>) synonyms) {
				if (synonym instanceof String && wanted.equals(normalizeName((String) synonym))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Lower case, single-spaced, without a trailing parenthesized qualifier: CIEL puts units there
	 * ("Weight (kg)", "Temperature (c)"). Other punctuation stays, so "CD4%" is not "CD4".
	 */
	static String normalizeName(String name) {
		if (name == null) {
			return null;
		}
		String n = name.toLowerCase(Locale.ROOT).replaceAll("\\s*\\([^()]*\\)\\s*$", "").replaceAll("\\s+", " ").trim();
		return n.isEmpty() ? null : n;
	}

	/** The patient's chart (within the spec's types), newest first, indexed by concept. */
	private Map<String, List<QueryDocument>> chartByConcept(QuerySpec spec, List<QueryResult.Hit> pool) {
		// A recency browse's pool already is the chart within the spec's types; read it again only for a
		// search, whose pool holds just the matches.
		List<QueryDocument> chart;
		if (spec.getQuery() == null) {
			chart = new ArrayList<>(pool.size());
			pool.forEach(h -> chart.add(h.getDocument()));
		} else {
			chart = backend.findAllByPatient(spec.getPatientUuid());
		}
		Map<String, List<QueryDocument>> out = new HashMap<>();
		for (QueryDocument d : chart) {
			String concept = conceptUuid(d);
			if (concept != null && (spec.getTypes().isEmpty() || spec.getTypes().contains(d.getResourceType()))) {
				out.computeIfAbsent(concept, k -> new ArrayList<>()).add(d);
			}
		}
		out.values().forEach(docs -> docs.sort(NEWEST_DOC));
		return out;
	}

	/**
	 * Backend-native: ES terms aggregation on {@code _index} (or a resource_type keyword), Lucene
	 * per-index {@code IndexSearcher.count} with the same query. Both are exact and uncapped.
	 */
	private static Map<String, Integer> countByResourceType(List<QueryResult.Hit> pool) {
		Map<String, Integer> counts = new TreeMap<>();
		for (QueryResult.Hit h : pool) {
			counts.merge(h.getDocument().getResourceType(), 1, Integer::sum);
		}
		return counts;
	}

	private static String conceptUuid(QueryDocument d) {
		Object value = d.getMetadata().get(QueryStoreConstants.FIELD_CONCEPT_UUID);
		return value instanceof String && !((String) value).isEmpty() ? (String) value : null;
	}

	private static <T> List<T> page(List<T> all, int offset, int limit) {
		if (offset >= all.size()) {
			return Collections.emptyList();
		}
		return new ArrayList<>(all.subList(offset, Math.min(all.size(), offset + limit)));
	}

	// Newest record_date first, undated last. record_date is a day, so two records on the same day
	// (two CD4s entered for one visit date) tie; last_modified, when the record was entered or last
	// changed, breaks that tie, then the uuid so the order is stable.
	private static final Comparator<QueryDocument> NEWEST_DOC = Comparator
	        .comparing(QueryDocument::getDate, Comparator.nullsLast(Comparator.<LocalDate> reverseOrder()))
	        .thenComparing(QueryDocument::getLastModified, Comparator.nullsLast(Comparator.<Instant> reverseOrder()))
	        .thenComparing(QueryDocument::getResourceUuid, Comparator.nullsLast(Comparator.<String> naturalOrder()));

	// Used with a stable sort, so relevance breaks whatever NEWEST_DOC leaves tied.
	private static final Comparator<QueryResult.Hit> NEWEST_FIRST = Comparator.comparing(QueryResult.Hit::getDocument,
	    NEWEST_DOC);

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
		// Every type the caller cannot read through core is withheld and named, requested, derived or
		// neither: in the scope it would come back as an empty typed tier, and in the mandatory tier
		// (which carries every allergy unasked) as no records, either of which reads as a patient
		// without such records, down to the sliceId.
		Set<String> withheldTypes = chartRead.getWithheldTypes();
		effectiveTypes.removeAll(withheldTypes);

		int anchor = temporal ? request.getRecencyAnchorSize() : 0;
		Map<String, Boolean> exactSignals = exactSignals(question);
		Map<String, String> policyTiers = new LinkedHashMap<>();
		for (int i = 0; i < chart.size(); i++) {
			QueryDocument doc = chart.get(i);
			if (doc == null || doc.getResourceUuid() == null || policyTiers.containsKey(doc.getResourceUuid())) {
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
			}
			if (tier != null) {
				policyTiers.put(doc.getResourceUuid(), tier);
			}
		}

		// Ranked-search hits are the semantic catch-all tier. Retrieval preprocessing (the patient
		// left out, lab panels expanded; Decision 18) runs HERE on every slice, interpretation or
		// not, at the owner of the embedder — idempotent for callers that already expand panels. A
		// failure degrades to the policy tiers alone —
		// selection must never block on the ranking layer. Over-fetch candidates so records
		// already claimed by higher-priority policy tiers and identical repeats of one value do not
		// starve other records of similarity slots. A series of distinct values of one concept still
		// takes one slot per value (thirty different BP readings can fill thirty slots).
		Map<String, Integer> similarityRanks = new LinkedHashMap<>();
		if (patientUuid != null && StringUtils.isNotBlank(question) && request.getSimilarityLimit() > 0) {
			try {
				String retrievalQuestion = ContextQuestionInterpreter.preprocess(question);
				// Tripled in long arithmetic: a limit above a third of Integer.MAX_VALUE would wrap.
				int candidateLimit = (int) Math.max(Math.min(request.getSimilarityLimit() * 3L, Integer.MAX_VALUE), 36);
				// The chart read above already ran any cold touch; a failed one is not run twice.
				List<QueryDocument> hits = rankedPatientSearch(patientUuid, retrievalQuestion, candidateLimit);
				similarityRanks = selectDiversifiedSimilarityRanks(
				        hits, policyTiers.keySet(), chartPositions(chart), request.getSimilarityLimit());
			}
			catch (RuntimeException e) {
				SafeLog.warn(QueryStoreServiceImpl.class,
				    "Context-slice ranked search failed (patient uuid at DEBUG); proceeding with policy tiers only", e);
				log.debug("Context-slice ranked search failed for patient " + patientUuid);
			}
		}

		List<ContextSliceRecord> selected = new ArrayList<>();
		Set<String> selectedUuids = new HashSet<>();
		for (int i = 0; i < chart.size(); i++) {
			QueryDocument doc = chart.get(i);
			if (doc == null || doc.getResourceUuid() == null) {
				continue;
			}
			String tier = policyTiers.get(doc.getResourceUuid());
			if (tier == null && similarityRanks.containsKey(doc.getResourceUuid())) {
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
	 * Picks up to {@code limit} similarity records one repeat-group at a time, so identical repeats
	 * of one value share a slot instead of starving other records of slots. A group is a concept and
	 * its rendered value: identical repeats ("Pregnancy status: No" three times) share one slot, while
	 * distinct values of one concept stay separate, so a trend keeps one record per value. Within a
	 * group the newest of the fetched candidates comes first, since rankers never see dates
	 * (Decision 7) and a repeat's slot must report its latest date; a newer repeat ranked below the
	 * candidate pool is not seen. Records a policy tier already claimed are skipped, as is a
	 * hit the chart does not hold (past the Elasticsearch cap): it could never fill the slot it took,
	 * and the next candidate can. Each pick keeps its own 1-based search rank.
	 */
	private static Map<String, Integer> selectDiversifiedSimilarityRanks(List<QueryDocument> hits,
	        Set<String> policyClaimedUuids, Map<String, Integer> chartPositions, int limit) {
		Map<String, Integer> searchRanks = new HashMap<>();
		Map<String, List<String>> groups = new LinkedHashMap<>();
		Set<String> seenUuids = new HashSet<>(policyClaimedUuids);
		for (int i = 0; i < hits.size(); i++) {
			QueryDocument hit = hits.get(i);
			if (hit == null || hit.getResourceUuid() == null || !chartPositions.containsKey(hit.getResourceUuid())
			        || !seenUuids.add(hit.getResourceUuid())) {
				continue;
			}
			searchRanks.put(hit.getResourceUuid(), Integer.valueOf(i + 1));
			String key = repeatKey(hit);
			List<String> group = groups.get(key);
			if (group == null) {
				group = new ArrayList<>();
				groups.put(key, group);
			}
			group.add(hit.getResourceUuid());
		}
		for (List<String> group : groups.values()) {
			group.sort(Comparator.comparingInt(uuid -> chartPositions.getOrDefault(uuid, Integer.MAX_VALUE)));
		}
		Map<String, Integer> ranks = new LinkedHashMap<>();
		int depth = 0;
		boolean addedInPass = true;
		while (ranks.size() < limit && addedInPass) {
			addedInPass = false;
			for (List<String> group : groups.values()) {
				if (depth < group.size()) {
					String uuid = group.get(depth);
					ranks.put(uuid, searchRanks.get(uuid));
					addedInPass = true;
					if (ranks.size() >= limit) {
						break;
					}
				}
			}
			depth++;
		}
		return ranks;
	}

	/** Each chart record's index in the {@code record_date}-desc chart: lower is newer. */
	private static Map<String, Integer> chartPositions(List<QueryDocument> chart) {
		Map<String, Integer> positions = new HashMap<>();
		for (int i = 0; i < chart.size(); i++) {
			QueryDocument doc = chart.get(i);
			if (doc != null && doc.getResourceUuid() != null && !positions.containsKey(doc.getResourceUuid())) {
				positions.put(doc.getResourceUuid(), Integer.valueOf(i));
			}
		}
		return positions;
	}

	/** A record's concept and its rendered value; a record without a concept is its own group. */
	private static String repeatKey(QueryDocument doc) {
		Object conceptUuid = doc.getMetadata().get(QueryStoreConstants.FIELD_CONCEPT_UUID);
		String concept = conceptUuid == null ? "" : conceptUuid.toString().trim();
		if (concept.isEmpty()) {
			return "resource:" + doc.getResourceUuid();
		}
		return "concept:" + concept + "\n" + (doc.getText() == null ? "" : doc.getText().trim());
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
		while (sink.size() < MAX_EXACT_SIGNALS && matcher.find()) {
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
