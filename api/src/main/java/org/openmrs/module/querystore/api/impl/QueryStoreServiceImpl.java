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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
			BootstrapService bootstrap = bootstrapService();
			return BootstrapStatusReport.from(bootstrap.getStatus(),
			        bootstrap.getResourceTypeNames()).isComplete();
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
		        chartRead.isTruncated() || coldTouchFailed, isProjectionComplete());
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
	 * The documents whose type the calling user may read through core. With no user context bound
	 * there is no caller to check, and nothing reaches one: a call through the service proxy is refused
	 * by its {@code @Authorized} gate before it gets here. Each type's privilege is asked once per read.
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
		String key = (modelName == null ? "" : modelName) + ' ' + query;
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
}
