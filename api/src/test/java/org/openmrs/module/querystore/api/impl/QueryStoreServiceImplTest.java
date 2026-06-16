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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.backend.BackendCapabilities;
import org.openmrs.module.querystore.backend.BackendStore;
import org.openmrs.module.querystore.backend.Filter;
import org.openmrs.module.querystore.backend.BulkWriteResult;
import org.openmrs.module.querystore.backend.HealthStatus;
import org.openmrs.module.querystore.backend.SchemaSpec;
import org.openmrs.module.querystore.backend.SearchRequest;
import org.openmrs.module.querystore.backend.SearchResult;
import org.openmrs.module.querystore.backend.WriteResult;
import org.openmrs.module.querystore.bootstrap.BootstrapService;
import org.openmrs.module.querystore.bootstrap.BootstrapProgress;
import org.openmrs.module.querystore.embedding.EmbeddingProvider;
import org.openmrs.module.querystore.model.QueryDocument;

public class QueryStoreServiceImplTest {

	private QueryStoreServiceImpl service;

	@Before
	public void setUp() {
		service = new QueryStoreServiceImpl();
	}

	@Test
	public void searchByPatient_returnsEmptyByDefault() {
		assertTrue(service.searchByPatient("patient-uuid", "glucose", 10).isEmpty());
	}

	@Test
	public void index_throwsWhenBackendNotWired() {
		// The prior contract silently returned, which let bootstrap credit "writes" that never landed
		// — the silent-data-loss bug fix #2 closes. The new contract surfaces the misconfiguration so
		// TypeBootstrapper.projectOne sees a RuntimeException, logs, and refuses to count the write.
		QueryDocument doc = new QueryDocument();
		doc.setResourceType("obs");
		doc.setResourceUuid("obs-uuid");
		try {
			service.index(doc);
			fail("expected IllegalStateException because backend was not wired");
		}
		catch (IllegalStateException expected) {
			assertTrue("error message should name the offending resource",
			        expected.getMessage().contains("obs/obs-uuid"));
		}
	}

	@Test
	public void bulkIndex_delegatesToBackendInOneRoundTrip() {
		// #1: the batched path is one backend.bulkUpsert call for the whole page, not N upserts —
		// that's the per-doc-commit amortization the bootstrap throughput fix depends on.
		FakeBackendStore backend = new FakeBackendStore(false);
		service.setBackend(backend);

		BulkWriteResult result = service.bulkIndex(Arrays.asList(doc("obs", "u1"), doc("obs", "u2"),
		        doc("condition", "u3")));

		assertEquals("one backend round-trip for the whole batch", 1, backend.bulkUpsertCount.get());
		assertEquals(3, backend.bulkUpserted.size());
		assertEquals(3, result.getSucceeded());
	}

	@Test
	public void bulkIndex_emptyOrNull_returnsEmptyWithoutTouchingBackend() {
		FakeBackendStore backend = new FakeBackendStore(false);
		service.setBackend(backend);

		assertEquals(0, service.bulkIndex(Collections.<QueryDocument> emptyList()).getTotalRequested());
		assertEquals(0, service.bulkIndex(null).getTotalRequested());
		assertEquals("an empty/null batch never reaches the backend", 0, backend.bulkUpsertCount.get());
	}

	@Test
	public void bulkIndex_filtersMalformedDocsIntoFailures_withoutAbortingTheBatch() {
		// A null doc or a uuid-less doc would make backend.bulkUpsert's validate() throw and sink the
		// whole batch; bulkIndex filters them into failures and still writes the valid ones.
		FakeBackendStore backend = new FakeBackendStore(false);
		service.setBackend(backend);
		QueryDocument noUuid = new QueryDocument();
		noUuid.setResourceType("obs");

		BulkWriteResult result = service.bulkIndex(Arrays.asList(doc("obs", "u1"), null, noUuid));

		assertEquals("only the well-formed doc reaches the backend", 1, backend.bulkUpserted.size());
		assertEquals(3, result.getTotalRequested());
		assertEquals(1, result.getSucceeded());
		assertEquals("the null and uuid-less docs are reported as failures", 2, result.getFailures().size());
	}

	@Test
	public void bulkIndex_throwsWhenBackendNotWired() {
		try {
			service.bulkIndex(Arrays.asList(doc("obs", "u1")));
			fail("expected IllegalStateException because backend was not wired");
		}
		catch (IllegalStateException expected) {
			assertTrue(expected.getMessage().contains("bulkIndex"));
		}
	}

	@Test
	public void defaultBulkIndex_countsNullWriteResultAsFailure_notSilentDrop() {
		// The interface default loops index(); index() MUST return non-null, so a contract-violating
		// null is counted as a failure rather than silently dropped — succeeded + failures == requested.
		QueryStoreService viaDefault = new QueryStoreService() {

			@Override public WriteResult index(QueryDocument document) { return null; }

			@Override public void delete(String resourceType, String resourceUuid) { }

			@Override public void bulkDeleteByPatient(String patientUuid) { }

			@Override public List<QueryDocument> searchByPatient(String p, String q, int l) { return Collections.emptyList(); }

			@Override public List<QueryDocument> search(String q, int l) { return Collections.emptyList(); }

			@Override public List<QueryDocument> getPatientChart(String p) { return Collections.emptyList(); }

			@Override public void onStartup() { }

			@Override public void onShutdown() { }
		};

		BulkWriteResult result = viaDefault.bulkIndex(Arrays.asList(doc("obs", "u1"), doc("obs", "u2")));

		assertEquals(2, result.getTotalRequested());
		assertEquals(0, result.getSucceeded());
		assertEquals("null results are counted as failures, not silently dropped", 2, result.getFailures().size());
	}

	@Test
	public void backendStore_countByType_defaultsToUnknown() {
		// A backend (or test double) that doesn't implement countByType degrades to -1 ("unknown"), so
		// drift detection reports "not computable" for that tier rather than a false zero.
		assertEquals(-1L, new FakeBackendStore(false).countByType("obs"));
	}

	private static QueryDocument doc(String resourceType, String resourceUuid) {
		QueryDocument d = new QueryDocument();
		d.setResourceType(resourceType);
		d.setResourceUuid(resourceUuid);
		return d;
	}

	@Test
	public void index_returnsFailedResultForNullDocumentOrUuid() {
		// A null guard that returns a typed failure (rather than silently returning) lets the bootstrap
		// dispatcher branch on "did the write land?" uniformly across all per-doc skip reasons.
		WriteResult nullDocResult = service.index(null);
		assertFalse("null document is not a successful write", nullDocResult.isSucceeded());
		assertNotNull(nullDocResult.getFailure());

		QueryDocument noUuid = new QueryDocument();
		noUuid.setResourceType("obs");
		WriteResult noUuidResult = service.index(noUuid);
		assertFalse("document without resource_uuid is not a successful write", noUuidResult.isSucceeded());
		assertEquals("obs", noUuidResult.getFailure().getResourceType());
	}

	@Test
	public void index_returnsBackendResult() {
		// The service must surface the backend's WriteResult so callers (bootstrap counter, sync
		// logging) can react to per-doc failures the backend reports without swallowing.
		FakeBackendStore backend = new FakeBackendStore(false);
		backend.upsertFailure = WriteResult.failed(new org.openmrs.module.querystore.backend.DocFailure(
		        "obs", "obs-uuid", "simulated I/O", true));
		service.setBackend(backend);

		QueryDocument doc = new QueryDocument();
		doc.setResourceType("obs");
		doc.setResourceUuid("obs-uuid");
		WriteResult result = service.index(doc);

		assertFalse("propagated the backend's failure verdict", result.isSucceeded());
		assertEquals("simulated I/O", result.getFailure().getErrorMessage());
	}

	@Test
	public void delete_toleratesNullArguments() {
		service.delete(null, null);
		service.delete("obs", null);
		service.delete(null, "obs-uuid");
	}

	@Test
	public void bulkDeleteByPatient_delegatesToBackend() {
		FakeBackendStore backend = new FakeBackendStore(false);
		service.setBackend(backend);

		service.bulkDeleteByPatient("patient-9");

		assertEquals("service must forward the patient uuid to the backend",
		        1, backend.bulkDeleteByPatientCount.get());
		assertEquals("patient-9", backend.bulkDeleteByPatientUuids.get(0));
	}

	@Test
	public void bulkDeleteByPatient_toleratesNullUuid() {
		// Mirrors delete(): a null caller (probably a misconfigured advice) must not throw —
		// the dispatcher's swallow guard would log it but the per-document loop would still be
		// interrupted. Return silently so subsequent calls in the same after-commit task survive.
		FakeBackendStore backend = new FakeBackendStore(false);
		service.setBackend(backend);

		service.bulkDeleteByPatient(null);

		assertEquals("must not call backend on null uuid",
		        0, backend.bulkDeleteByPatientCount.get());
	}

	@Test
	public void bulkDeleteByPatient_unwiredBackend_returnsQuietly() {
		// No backend set (e.g. wireBackend hasn't run yet, or the configured backend bean failed
		// to load). The call must not NPE — log the warn and return so the dispatcher can move on
		// to the next document in the after-commit task.
		service.bulkDeleteByPatient("patient-9");
		// No assertion possible without a logger spy; passing is "no exception thrown."
	}

	@Test
	public void searchByPatient_coldPatient_triggersEnsureIndexedOnce() {
		FakeBackendStore backend = new FakeBackendStore(false);
		RecordingBootstrapService bootstrap = new RecordingBootstrapService();
		service.setBackend(backend);
		service.setBootstrapServiceOverride(bootstrap);

		service.searchByPatient("patient-uuid", "glucose", 10);

		assertEquals(1, bootstrap.ensureIndexedCalls.size());
		assertEquals("patient-uuid", bootstrap.ensureIndexedCalls.get(0));
		assertEquals("backend was probed before triggering auto-index", 1, backend.existsByPatientCount.get());
	}

	@Test
	public void searchByPatient_hotPatient_delegatesToBackendHybridNotBm25KnnSeparately() {
		// Locks in the Decision 3 SPI reshape: the service calls backend.hybrid() (which fuses
		// internally via the interface-default RankFusion path or a native override) rather than
		// fusing bm25 + knn results in the service layer. A revert that fuses externally would
		// silently produce the same empty result on this stub but fail this assertion.
		FakeBackendStore backend = new FakeBackendStore(true);
		RecordingBootstrapService bootstrap = new RecordingBootstrapService();
		service.setBackend(backend);
		service.setBootstrapServiceOverride(bootstrap);

		service.searchByPatient("patient-uuid", "glucose", 10);

		assertEquals("service must delegate fusion to backend.hybrid()", 1, backend.hybridCount.get());
		assertEquals("service must not call bm25() directly", 0, backend.bm25Count.get());
		assertEquals("service must not call knn() directly", 0, backend.knnCount.get());
	}

	@Test
	public void searchByPatient_hotPatient_skipsEnsureIndexed() {
		FakeBackendStore backend = new FakeBackendStore(true);
		RecordingBootstrapService bootstrap = new RecordingBootstrapService();
		service.setBackend(backend);
		service.setBootstrapServiceOverride(bootstrap);

		service.searchByPatient("patient-uuid", "glucose", 10);

		assertTrue("ensureIndexed must not run when backend already has documents",
		        bootstrap.ensureIndexedCalls.isEmpty());
	}

	@Test
	public void searchByPatient_ensureIndexedFailure_doesNotBlockSearch() {
		// Per the ADR: index-failure must not block search. Whatever the backend already has is
		// still returned; empty results are no worse than pre-feature behavior.
		FakeBackendStore backend = new FakeBackendStore(false);
		RecordingBootstrapService bootstrap = new RecordingBootstrapService();
		bootstrap.onEnsureIndexed = uuid -> { throw new RuntimeException("boom"); };
		service.setBackend(backend);
		service.setBootstrapServiceOverride(bootstrap);

		// Does not throw; returns whatever the (empty) backend has.
		List<QueryDocument> result = service.searchByPatient("patient-uuid", "glucose", 10);
		assertTrue(result.isEmpty());
	}

	@Test
	public void searchByPatient_bootstrapServiceUnavailable_skipsAutoIndexQuietly() {
		// Production wiring resolves BootstrapService via Context.getService; tests run without an
		// OpenMRS context. With no override set, Context.getService throws (no service context /
		// service not registered), and the auto-index step is skipped quietly via the catch — the
		// search still runs and returns whatever the backend has.
		FakeBackendStore backend = new FakeBackendStore(false);
		service.setBackend(backend);

		List<QueryDocument> result = service.searchByPatient("patient-uuid", "glucose", 10);
		assertTrue(result.isEmpty());
	}

	// ---------- getPatientChart ----------

	@Test
	public void getPatientChart_returnsEmptyByDefault() {
		// No backend wired (production: activator hasn't finished; tests: setBackend not called).
		// Mirrors searchByPatient_returnsEmptyByDefault — the consumer-facing contract is "empty
		// list, no throw" so a misconfigured deployment doesn't strand an LLM mid-prompt.
		assertTrue(service.getPatientChart("patient-uuid").isEmpty());
	}

	@Test
	public void getPatientChart_returnsEmptyForNullPatientUuid() {
		FakeBackendStore backend = new FakeBackendStore(true);
		service.setBackend(backend);

		assertTrue(service.getPatientChart(null).isEmpty());
		assertEquals("null uuid must not probe the backend", 0, backend.existsByPatientCount.get());
		assertEquals("null uuid must not enumerate the backend", 0, backend.findAllByPatientCount.get());
	}

	@Test
	public void getPatientChart_coldPatient_triggersEnsureIndexedOnce() {
		// Decision 15 mirrors searchByPatient's cold-bootstrap protocol: a never-indexed patient
		// is projected synchronously before the chart enumeration. This is the test that pins it.
		FakeBackendStore backend = new FakeBackendStore(false);
		RecordingBootstrapService bootstrap = new RecordingBootstrapService();
		service.setBackend(backend);
		service.setBootstrapServiceOverride(bootstrap);

		service.getPatientChart("patient-uuid");

		assertEquals(1, bootstrap.ensureIndexedCalls.size());
		assertEquals("patient-uuid", bootstrap.ensureIndexedCalls.get(0));
		assertEquals("backend was probed before triggering auto-index", 1, backend.existsByPatientCount.get());
	}

	@Test
	public void getPatientChart_hotPatient_skipsEnsureIndexed() {
		FakeBackendStore backend = new FakeBackendStore(true);
		RecordingBootstrapService bootstrap = new RecordingBootstrapService();
		service.setBackend(backend);
		service.setBootstrapServiceOverride(bootstrap);

		service.getPatientChart("patient-uuid");

		assertTrue("ensureIndexed must not run when backend already has documents",
		        bootstrap.ensureIndexedCalls.isEmpty());
	}

	@Test
	public void getPatientChart_delegatesToBackendFindAllByPatient() {
		// Locks in the SPI dispatch: the service calls backend.findAllByPatient (the unfiltered
		// enumeration path) rather than backend.hybrid (the ranked search path). A revert that
		// implemented getPatientChart as searchByPatient with a sentinel query string would still
		// return rows from this fake but fail this assertion.
		FakeBackendStore backend = new FakeBackendStore(true);
		RecordingBootstrapService bootstrap = new RecordingBootstrapService();
		service.setBackend(backend);
		service.setBootstrapServiceOverride(bootstrap);

		service.getPatientChart("patient-uuid");

		assertEquals("service must call backend.findAllByPatient", 1, backend.findAllByPatientCount.get());
		assertEquals("patient-uuid", backend.findAllByPatientUuids.get(0));
		assertEquals("service must not call backend.hybrid()", 0, backend.hybridCount.get());
		assertEquals("service must not call backend.bm25()", 0, backend.bm25Count.get());
		assertEquals("service must not call backend.knn()", 0, backend.knnCount.get());
	}

	@Test
	public void getPatientChart_ensureIndexedFailure_doesNotBlockReturn() {
		// Index-failure must not block the LLM full-chart caller any more than it blocks search;
		// whatever the backend has (possibly empty) is returned. Mirrors the searchByPatient
		// equivalent so the failure-recovery contract is uniform across both read methods.
		FakeBackendStore backend = new FakeBackendStore(false);
		RecordingBootstrapService bootstrap = new RecordingBootstrapService();
		bootstrap.onEnsureIndexed = uuid -> { throw new RuntimeException("boom"); };
		service.setBackend(backend);
		service.setBootstrapServiceOverride(bootstrap);

		List<QueryDocument> result = service.getPatientChart("patient-uuid");
		assertTrue(result.isEmpty());
		assertEquals("findAllByPatient must still run after auto-index failure",
		        1, backend.findAllByPatientCount.get());
	}

	@Test
	public void getPatientChart_bootstrapServiceUnavailable_skipsAutoIndexQuietly() {
		// No override and no OpenMRS Context: Context.getService throws, the ensureIndexedSafely
		// catch absorbs the failure, and the chart enumeration still runs against whatever is
		// indexed. Same shape as the searchByPatient equivalent.
		FakeBackendStore backend = new FakeBackendStore(false);
		service.setBackend(backend);

		List<QueryDocument> result = service.getPatientChart("patient-uuid");
		assertTrue(result.isEmpty());
		assertEquals(1, backend.findAllByPatientCount.get());
	}

	@Test
	public void getPatientChart_coldPatient_bootstrapResultFlowsThroughToReturnedChart() {
		// End-to-end cold-bootstrap re-entry: existsByPatient probe → ensureIndexed projects →
		// findAllByPatient sees the projection → caller receives the projected docs. The five
		// dispatch-only tests above verify the *ordering* of the three calls, but a regression that
		// swapped the probe-then-bootstrap-then-enumerate chain for, say, probe-then-enumerate (no
		// bootstrap) would still satisfy them because they don't tie bootstrap's side effect to the
		// final return. The bootstrap callback's "projection" channel is the fake's
		// findAllByPatientReturn — assigning it from inside the RecordingBootstrapService callback
		// simulates the real bootstrap writing to the index that the next findAllByPatient reads.
		FakeBackendStore backend = new FakeBackendStore(false);
		QueryDocument projected = new QueryDocument();
		projected.setResourceUuid("projected-obs-1");
		RecordingBootstrapService bootstrap = new RecordingBootstrapService();
		bootstrap.onEnsureIndexed = uuid -> backend.findAllByPatientReturn = Collections.singletonList(projected);
		service.setBackend(backend);
		service.setBootstrapServiceOverride(bootstrap);

		List<QueryDocument> chart = service.getPatientChart("patient-uuid");

		assertEquals("chart must contain docs the bootstrap projected", 1, chart.size());
		assertEquals("projected-obs-1", chart.get(0).getResourceUuid());
		assertEquals(1, bootstrap.ensureIndexedCalls.size());
		assertEquals(1, backend.existsByPatientCount.get());
		assertEquals(1, backend.findAllByPatientCount.get());
	}

	// ---------- query-embedding cache ----------

	@Test
	public void searchByPatient_repeatedQuery_reusesCachedEmbedding() {
		// The ONNX query encoder is the dominant cost on the prefilter path (measured 40-80 ms warm,
		// 3-4 s cold). Same query string + same model -> reuse the float[]. A regression that drops
		// the cache would call embedQuery twice and fail this assertion.
		FakeBackendStore backend = new FakeBackendStore(true);
		CountingEmbeddingProvider provider = new CountingEmbeddingProvider("model-A");
		service.setBackend(backend);
		service.setEmbeddingProvider(provider);
		service.setBootstrapServiceOverride(new RecordingBootstrapService());

		service.searchByPatient("patient-uuid", "diabetes", 10);
		service.searchByPatient("patient-uuid", "diabetes", 10);

		assertEquals("identical query must only be encoded once", 1, provider.embedQueryCount.get());
	}

	@Test
	public void searchByPatient_distinctQueries_eachEncodedOnce() {
		FakeBackendStore backend = new FakeBackendStore(true);
		CountingEmbeddingProvider provider = new CountingEmbeddingProvider("model-A");
		service.setBackend(backend);
		service.setEmbeddingProvider(provider);
		service.setBootstrapServiceOverride(new RecordingBootstrapService());

		service.searchByPatient("patient-uuid", "diabetes", 10);
		service.searchByPatient("patient-uuid", "hypertension", 10);

		assertEquals("distinct queries each require a fresh encode", 2, provider.embedQueryCount.get());
	}

	@Test
	public void searchByPatient_modelChangeInvalidatesCache() {
		// Cache key includes the provider's model name so a switch of the active embedding model
		// (querystore.embedding.providerBean GP flip) cannot serve a vector from the previous
		// model's space, which would silently drift kNN similarity comparisons against record
		// embeddings that the new model never saw.
		FakeBackendStore backend = new FakeBackendStore(true);
		CountingEmbeddingProvider provider = new CountingEmbeddingProvider("model-A");
		service.setBackend(backend);
		service.setEmbeddingProvider(provider);
		service.setBootstrapServiceOverride(new RecordingBootstrapService());

		service.searchByPatient("patient-uuid", "diabetes", 10);
		provider.modelName = "model-B";
		service.searchByPatient("patient-uuid", "diabetes", 10);

		assertEquals("model swap must force a fresh encode", 2, provider.embedQueryCount.get());
	}

	// ---------- fakes ----------

	private static final class FakeBackendStore implements BackendStore {
		private final boolean existsByPatientReturn;
		final AtomicInteger existsByPatientCount = new AtomicInteger();
		final AtomicInteger hybridCount = new AtomicInteger();
		final AtomicInteger bm25Count = new AtomicInteger();
		final AtomicInteger knnCount = new AtomicInteger();
		final AtomicInteger bulkDeleteByPatientCount = new AtomicInteger();
		final AtomicInteger findAllByPatientCount = new AtomicInteger();
		final java.util.List<String> bulkDeleteByPatientUuids = new java.util.ArrayList<>();
		final java.util.List<String> findAllByPatientUuids = new java.util.ArrayList<>();

		WriteResult upsertFailure;

		List<QueryDocument> findAllByPatientReturn = Collections.emptyList();

		FakeBackendStore(boolean existsByPatientReturn) {
			this.existsByPatientReturn = existsByPatientReturn;
		}

		@Override public boolean existsByPatient(String patientUuid) {
			existsByPatientCount.incrementAndGet();
			return existsByPatientReturn;
		}

		@Override public List<QueryDocument> findAllByPatient(String patientUuid) {
			findAllByPatientCount.incrementAndGet();
			findAllByPatientUuids.add(patientUuid);
			return findAllByPatientReturn;
		}

		@Override public void ensureSchema(String resourceType, SchemaSpec spec) { }
		@Override public void deleteSchema(String resourceType) { }
		@Override public WriteResult upsert(QueryDocument doc) {
			return upsertFailure != null ? upsertFailure : WriteResult.success();
		}
		@Override public WriteResult delete(String resourceType, String resourceUuid) { return null; }
		final AtomicInteger bulkUpsertCount = new AtomicInteger();
		final java.util.List<QueryDocument> bulkUpserted = new java.util.ArrayList<>();
		@Override public BulkWriteResult bulkUpsert(List<QueryDocument> docs) {
			bulkUpsertCount.incrementAndGet();
			bulkUpserted.addAll(docs);
			return new BulkWriteResult(docs.size(), docs.size(),
			        Collections.<org.openmrs.module.querystore.backend.DocFailure> emptyList());
		}
		@Override public BulkWriteResult bulkDelete(String resourceType, List<String> uuids) { return null; }
		@Override public BulkWriteResult bulkDeleteByPatient(String patientUuid) {
			bulkDeleteByPatientCount.incrementAndGet();
			bulkDeleteByPatientUuids.add(patientUuid);
			return null;
		}
		@Override public SearchResult bm25(SearchRequest req) { bm25Count.incrementAndGet(); return SearchResult.empty(); }
		@Override public SearchResult knn(SearchRequest req) { knnCount.incrementAndGet(); return SearchResult.empty(); }
		@Override public SearchResult hybrid(SearchRequest req) { hybridCount.incrementAndGet(); return SearchResult.empty(); }
		// Non-null capabilities even though this fake currently overrides hybrid(): the
		// BackendStore.hybrid default-method (post-Decision-3 SPI reshape) dereferences
		// capabilities().supportsKnn(), so a future test that forgets to override hybrid()
		// — or inherits this fake — would NPE on the default path if capabilities() returned
		// null. Returning a real object here keeps the fake safe under that drift.
		@Override public BackendCapabilities capabilities() {
			return new BackendCapabilities(true, false, false, 1_000_000, EnumSet.allOf(Filter.Kind.class));
		}
		@Override public HealthStatus health() { return null; }
	}

	private static final class CountingEmbeddingProvider implements EmbeddingProvider {
		final AtomicInteger embedQueryCount = new AtomicInteger();

		String modelName;

		CountingEmbeddingProvider(String modelName) {
			this.modelName = modelName;
		}

		@Override public int getDimensions() { return 4; }

		@Override public float[] embed(String text) {
			return new float[] { 1f, 0f, 0f, 0f };
		}

		@Override public float[] embedQuery(String text) {
			embedQueryCount.incrementAndGet();
			return new float[] { 1f, 0f, 0f, 0f };
		}

		@Override public String getModelName() { return modelName; }
	}

	private static final class RecordingBootstrapService implements BootstrapService {
		final java.util.List<String> ensureIndexedCalls = new java.util.ArrayList<>();

		java.util.function.Consumer<String> onEnsureIndexed;

		@Override public void bootstrap() { }
		@Override public void bootstrap(String resourceType) { }
		@Override public void ensureIndexed(String patientUuid) {
			ensureIndexedCalls.add(patientUuid);
			if (onEnsureIndexed != null) {
				onEnsureIndexed.accept(patientUuid);
			}
		}
		@Override public void reindexPatient(String patientUuid) { }
		@Override public List<BootstrapProgress> getStatus() { return Collections.emptyList(); }
		@Override public BootstrapProgress getStatus(String resourceType) { return null; }
		@Override public org.openmrs.module.querystore.bootstrap.DriftReport getDrift() { return null; }
		@Override public void onStartup() { }
		@Override public void onShutdown() { }
	}
}
