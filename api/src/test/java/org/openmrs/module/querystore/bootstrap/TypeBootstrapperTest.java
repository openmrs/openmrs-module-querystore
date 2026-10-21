/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.bootstrap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_OBS_GROUP_CONCEPT_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_SYNONYMS;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.embedding.EmbeddingProvider;
import org.openmrs.module.querystore.embedding.EmbeddingProviderClosedException;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;

public class TypeBootstrapperTest {

	private RecordingQueryStoreService service;

	private FakeEmbedder embedder;

	private InMemoryProgressDao progressDao;

	@Before
	public void setUp() {
		service = new RecordingQueryStoreService();
		embedder = new FakeEmbedder();
		progressDao = new InMemoryProgressDao();
	}

	@Test
	public void run_emptyCorpus_completesWithZeroDocuments() {
		FakeBootstrapper b = new FakeBootstrapper();
		BootstrapProgress progress = new BootstrapProgress("test");

		b.run(progress, service, embedder, progressDao);

		assertEquals(BootstrapStatus.COMPLETED, progress.getStatus());
		assertEquals(0, progress.getDocumentsIndexed());
		assertEquals(0, service.indexed.size());
		assertNotNull(progress.getStartedAt());
		assertNotNull(progress.getCompletedAt());
	}

	@Test
	public void run_singlePage_indexesEveryRecord() {
		FakeBootstrapper b = new FakeBootstrapper();
		b.queuePage(entity("a", Instant.parse("2025-01-01T00:00:00Z")),
		        entity("b", Instant.parse("2025-01-02T00:00:00Z")));
		BootstrapProgress progress = new BootstrapProgress("test");

		b.run(progress, service, embedder, progressDao);

		assertEquals(BootstrapStatus.COMPLETED, progress.getStatus());
		assertEquals(2, progress.getDocumentsIndexed());
		assertEquals(2, service.indexed.size());
		assertEquals("b", progress.getCursorUuid());
		assertEquals(Instant.parse("2025-01-02T00:00:00Z"), progress.getCursorDateChanged());
	}

	@Test
	public void run_multiPage_persistsCursorBetweenPages() {
		FakeBootstrapper b = new FakeBootstrapper();
		b.queuePage(entity("a", Instant.parse("2025-01-01T00:00:00Z")));
		b.queuePage(entity("b", Instant.parse("2025-01-02T00:00:00Z")));
		BootstrapProgress progress = new BootstrapProgress("test");

		b.run(progress, service, embedder, progressDao);

		assertEquals(2, progress.getDocumentsIndexed());
		// The DAO is called at least: initial RUNNING save, after-page-1 save, after-page-2 save,
		// and the terminal COMPLETED save. Cursor must reflect each page's last record on the
		// intermediate writes so an interrupted scan resumes correctly.
		assertEquals(4, progressDao.savesByCursor.size());
		assertEquals("a", progressDao.savesByCursor.get(1).getCursorUuid());
		assertEquals("b", progressDao.savesByCursor.get(2).getCursorUuid());
	}

	@Test
	public void run_nullDocumentEntity_advancesCursorButNotCount() {
		FakeBootstrapper b = new FakeBootstrapper();
		b.queuePage(entity("a", Instant.parse("2025-01-01T00:00:00Z")),
		        entity("skip", Instant.parse("2025-01-02T00:00:00Z")),
		        entity("c", Instant.parse("2025-01-03T00:00:00Z")));
		BootstrapProgress progress = new BootstrapProgress("test");

		b.run(progress, service, embedder, progressDao);

		assertEquals("two documents written; the skip entity is null-serialized",
		        2, progress.getDocumentsIndexed());
		assertEquals(2, service.indexed.size());
		assertEquals("c", progress.getCursorUuid());
	}

	@Test
	public void run_failureDuringFetch_marksFailedAndRethrows() {
		FakeBootstrapper b = new FakeBootstrapper();
		b.queuePage(entity("a", Instant.parse("2025-01-01T00:00:00Z")));
		b.failOnPage(2, new RuntimeException("simulated fetch failure"));
		BootstrapProgress progress = new BootstrapProgress("test");

		try {
			b.run(progress, service, embedder, progressDao);
			fail("expected runtime exception");
		}
		catch (RuntimeException expected) {
			assertEquals("simulated fetch failure", expected.getMessage());
		}

		assertEquals(BootstrapStatus.FAILED, progress.getStatus());
		assertEquals("RuntimeException: simulated fetch failure", progress.getFailureMessage());
		assertEquals("cursor advances through the first successful page even when a later page throws",
		        "a", progress.getCursorUuid());
	}

	@Test
	public void run_resumesFromPersistedCursor() {
		FakeBootstrapper b = new FakeBootstrapper();
		b.queuePage(entity("c", Instant.parse("2025-01-03T00:00:00Z")));
		BootstrapProgress progress = new BootstrapProgress("test");
		progress.setCursorUuid("b");
		progress.setCursorDateChanged(Instant.parse("2025-01-02T00:00:00Z"));
		progress.setDocumentsIndexed(2);
		progress.setStartedAt(Instant.parse("2025-01-01T00:00:00Z"));

		b.run(progress, service, embedder, progressDao);

		assertEquals("started_at preserved across resume", Instant.parse("2025-01-01T00:00:00Z"),
		        progress.getStartedAt());
		assertEquals(3, progress.getDocumentsIndexed());
		assertEquals("c", progress.getCursorUuid());
		// First fetchPage call must carry the persisted cursor — proves resume actually happens.
		assertEquals(Instant.parse("2025-01-02T00:00:00Z"), b.fetchCalls.get(0).afterDateChanged);
		assertEquals("b", b.fetchCalls.get(0).afterUuid);
	}

	@Test
	public void run_serviceReturnsFailedWriteResult_doesNotCreditTheRecord() {
		// Regression for the silent-data-loss bug fix #2 closes. Previously, service.index() returned
		// void; the bootstrap loop treated "no throw" as success and incremented documents_indexed
		// even when the underlying backend silently dropped the write (LuceneBackendStore.upsert
		// catches IOException and returns a failed WriteResult). The new contract surfaces the failure
		// via WriteResult; the counter must only credit confirmed writes.
		FakeBootstrapper b = new FakeBootstrapper();
		b.queuePage(entity("a", Instant.parse("2025-01-01T00:00:00Z")),
		        entity("ghost", Instant.parse("2025-01-02T00:00:00Z")),
		        entity("c", Instant.parse("2025-01-03T00:00:00Z")));
		service.failUuid = "ghost";
		BootstrapProgress progress = new BootstrapProgress("test");

		b.run(progress, service, embedder, progressDao);

		assertEquals(BootstrapStatus.COMPLETED, progress.getStatus());
		assertEquals("only the two confirmed-landed writes are credited; the dropped one is not",
		        2, progress.getDocumentsIndexed());
		assertEquals("cursor still advances past the dropped record so the scan doesn't stall",
		        "c", progress.getCursorUuid());
	}

	@Test
	public void run_indexFailureOnSingleEntity_isLoggedSkippedAndCursorAdvances() {
		// Poison-record protection: an exception from serialize/embed/index on one entity must not
		// permanently stall the bootstrap. The failing entity is skipped (no count increment), the
		// cursor advances past it, and subsequent entities still index normally.
		FakeBootstrapper b = new FakeBootstrapper();
		b.queuePage(entity("a", Instant.parse("2025-01-01T00:00:00Z")),
		        entity("poison", Instant.parse("2025-01-02T00:00:00Z")),
		        entity("c", Instant.parse("2025-01-03T00:00:00Z")));
		b.poisonUuid = "poison";
		BootstrapProgress progress = new BootstrapProgress("test");

		b.run(progress, service, embedder, progressDao);

		assertEquals(BootstrapStatus.COMPLETED, progress.getStatus());
		assertEquals("only the two non-poison entities were indexed", 2, progress.getDocumentsIndexed());
		assertEquals("cursor advanced past every entity including poison", "c", progress.getCursorUuid());
	}

	@Test
	public void run_embeddingProviderClosed_marksFailedWithoutCheckpointingThePage() {
		// A server stop closes the provider while a bootstrap may still be running. Skipping like a
		// poison record would advance the cursor past records never indexed, so the scan must end
		// FAILED (re-runnable) with the cursor still before the page that hit the closed provider.
		FakeBootstrapper b = new FakeBootstrapper();
		b.queuePage(entity("a", Instant.parse("2025-01-01T00:00:00Z")));
		b.queuePage(entity("b", Instant.parse("2025-01-02T00:00:00Z")),
		        entity("c", Instant.parse("2025-01-03T00:00:00Z")));
		EmbeddingProvider closesAfterFirst = new EmbeddingProvider() {
			int calls;

			@Override public int getDimensions() { return 8; }

			@Override
			public float[] embed(String text) {
				if (++calls > 1) {
					throw new EmbeddingProviderClosedException();
				}
				return new float[8];
			}
		};
		BootstrapProgress progress = new BootstrapProgress("test");

		try {
			b.run(progress, service, closesAfterFirst, progressDao);
			fail("expected EmbeddingProviderClosedException");
		}
		catch (EmbeddingProviderClosedException expected) {
		}

		assertEquals(BootstrapStatus.FAILED, progress.getStatus());
		assertEquals("cursor stays at the last fully indexed page", "a", progress.getCursorUuid());
		assertEquals(1, progress.getDocumentsIndexed());
	}

	@Test
	public void run_indexedDocumentsCarrySerializerLastModified() {
		FakeBootstrapper b = new FakeBootstrapper();
		Instant t = Instant.parse("2025-01-01T00:00:00Z");
		b.queuePage(entity("a", t));
		BootstrapProgress progress = new BootstrapProgress("test");

		b.run(progress, service, embedder, progressDao);

		assertEquals("bootstrap-projected docs must carry the entity timestamp so concurrent sync "
		        + "writes can use the Decision 3 conditional-upsert guard",
		        t, service.indexed.get(0).getLastModified());
	}

	@Test
	public void projectOne_embedsEnrichedInputNotPlainText() {
		FakeBootstrapper b = new FakeBootstrapper();
		b.serializer.enrich = true;
		b.queuePage(entity("a", Instant.parse("2025-01-01T00:00:00Z")));
		BootstrapProgress progress = new BootstrapProgress("test");

		b.run(progress, service, embedder, progressDao);

		assertEquals(1, embedder.inputs.size());
		assertEquals(
		    "Vital signs — entity a SBP Systolic BP",
		    embedder.inputs.get(0));
	}

	@Test
	public void run_batchesWritesPerPage_oneBulkIndexCallEach() {
		// #1: the scan writes one bulkIndex per page (amortizing the per-doc commit), not one index()
		// per record. Two pages of (2, 1) records => two bulk calls of sizes [2, 1].
		FakeBootstrapper b = new FakeBootstrapper();
		b.queuePage(entity("a", Instant.parse("2025-01-01T00:00:00Z")),
		        entity("b", Instant.parse("2025-01-02T00:00:00Z")));
		b.queuePage(entity("c", Instant.parse("2025-01-03T00:00:00Z")));
		BootstrapProgress progress = new BootstrapProgress("test");

		b.run(progress, service, embedder, progressDao);

		assertEquals("one bulkIndex per page", Arrays.asList(2, 1), service.bulkBatchSizes);
		assertEquals(3, progress.getDocumentsIndexed());
		assertEquals(3, service.indexed.size());
	}

	@Test
	public void run_pageFetchThrows_withoutRecovery_failsWithOriginalError() {
		// #2: a type with no per-row recovery (the base default) preserves the prior behavior — the
		// original fetch exception fails the whole type, surfaced verbatim in the progress row.
		FakeBootstrapper b = new FakeBootstrapper();
		b.failOnPage(1, new RuntimeException("dangling FK boom"));
		BootstrapProgress progress = new BootstrapProgress("test");

		try {
			b.run(progress, service, embedder, progressDao);
			fail("expected the original fetch error to propagate");
		}
		catch (RuntimeException expected) {
			assertEquals("dangling FK boom", expected.getMessage());
		}

		assertEquals(BootstrapStatus.FAILED, progress.getStatus());
		assertEquals("RuntimeException: dangling FK boom", progress.getFailureMessage());
	}

	@Test
	public void run_pageFetchThrows_withRecovery_skipsPoisonAdvancesCursorAndCompletes() {
		// #2: when fetchPage throws on a poison page, run() delegates to fetchPageSkippingPoison, indexes
		// the recovered healthy records, advances the cursor past the window, and completes the type —
		// instead of failing it. (The recovery itself is simulated here; the real Hibernate descriptor +
		// per-row recovery executes against a DB in HibernateTypeBootstrapperExecutionTest.)
		FakeBootstrapper b = new FakeBootstrapper();
		b.failOnPage(1, new RuntimeException("FetchNotFoundException: Encounter 999 does not exist"));
		b.recoverPoison = true;
		b.poisonRecoveryPage = b.recoveryPage(entity("c", Instant.parse("2025-01-03T00:00:00Z")));
		BootstrapProgress progress = new BootstrapProgress("test");

		b.run(progress, service, embedder, progressDao);

		assertEquals(BootstrapStatus.COMPLETED, progress.getStatus());
		assertEquals("the recovered healthy record indexed; the poison row skipped",
		        1, progress.getDocumentsIndexed());
		assertEquals("cursor advanced past the recovered poison window", "c", progress.getCursorUuid());
	}

	@Test
	public void run_aTopUpSkipsRecordsStoredAtTheirCurrentVersion() {
		// A restart's top-up walks every record changed since the last checkpoint, most of which
		// steady-state sync already indexed at that very version; re-embedding them is the cost.
		Instant earlier = Instant.parse("2025-01-01T00:00:00Z");
		Instant later = Instant.parse("2025-01-02T00:00:00Z");
		FakeBootstrapper b = new FakeBootstrapper();
		b.queuePage(entity("same", earlier), entity("changed", later), entity("new", later));
		service.storedVersions.put("same", earlier);
		service.storedVersions.put("changed", earlier);
		BootstrapProgress progress = new BootstrapProgress("test");

		b.run(progress, service, embedder, progressDao, true);

		assertEquals(Arrays.asList("changed", "new"), indexedUuids());
		assertEquals(2, embedder.inputs.size());
		assertEquals(BootstrapStatus.COMPLETED, progress.getStatus());
		assertEquals("the cursor still advances past the skipped record's page", "new", progress.getCursorUuid());
	}

	@Test
	public void run_aTopUpReprojectsAVersionOtherThanTheStoredOne() {
		FakeBootstrapper b = new FakeBootstrapper();
		b.queuePage(entity("edited", Instant.parse("2026-10-20T07:03:22Z")));
		service.storedVersions.put("edited", Instant.parse("2026-10-20T07:03:21Z"));

		b.run(new BootstrapProgress("test"), service, embedder, progressDao, true);

		assertEquals(Collections.singletonList("edited"), indexedUuids());
	}

	@Test
	public void run_aTopUpRewritesATypeWhoseVersionDoesNotTrackItsContent() {
		// An order stopped since it was stored keeps its version (dateCreated), so only a rewrite
		// carries its date_stopped.
		Instant created = Instant.parse("2025-01-01T00:00:00Z");
		FakeBootstrapper b = new FakeBootstrapper();
		b.versionTracksContent = false;
		b.queuePage(entity("stopped", created));
		service.storedVersions.put("stopped", created);

		b.run(new BootstrapProgress("test"), service, embedder, progressDao, true);

		assertEquals(Collections.singletonList("stopped"), indexedUuids());
	}

	@Test
	public void onlyObsConditionsDiagnosesAndDispensesAreTheTypesWhoseVersionTracksTheirContent() {
		assertTrue(new ObsBootstrapper(null, null).versionTracksContent());
		assertTrue(new ConditionBootstrapper(null, null).versionTracksContent());
		assertTrue(new DiagnosisBootstrapper(null, null).versionTracksContent());
		assertTrue(new MedicationDispenseBootstrapper(null, null).versionTracksContent());
		assertFalse(new DrugOrderBootstrapper(null, null).versionTracksContent());
		assertFalse(new TestOrderBootstrapper(null, null).versionTracksContent());
		assertFalse(new ReferralOrderBootstrapper(null, null).versionTracksContent());
		assertFalse(new PatientBootstrapper(null, null).versionTracksContent());
		assertFalse(new VisitBootstrapper(null, null).versionTracksContent());
		assertFalse(new AllergyBootstrapper(null, null).versionTracksContent());
		assertFalse(new PatientProgramBootstrapper(null, null).versionTracksContent());
		assertFalse(new EncounterBootstrapper(null, null).versionTracksContent());
	}

	@Test
	public void run_aForcedReprojectionEmbedsEveryRecordEvenAtItsStoredVersion() {
		// A resync re-projects on purpose (drift, or new fields an upgrade added), so it never skips.
		Instant earlier = Instant.parse("2025-01-01T00:00:00Z");
		FakeBootstrapper b = new FakeBootstrapper();
		b.queuePage(entity("same", earlier));
		service.storedVersions.put("same", earlier);
		BootstrapProgress progress = new BootstrapProgress("test");

		b.run(progress, service, embedder, progressDao, false);

		assertEquals(Collections.singletonList("same"), indexedUuids());
	}

	private List<String> indexedUuids() {
		List<String> uuids = new ArrayList<>();
		for (QueryDocument doc : service.indexed) {
			uuids.add(doc.getResourceUuid());
		}
		return uuids;
	}

	// ---------- fakes ----------

	private static TestEntity entity(String uuid, Instant ts) {
		return new TestEntity(uuid, ts);
	}

	private static final class TestEntity {
		final String uuid;
		final Instant ts;
		TestEntity(String uuid, Instant ts) { this.uuid = uuid; this.ts = ts; }
	}

	/** Indexes documents for every entity except those with uuid "skip" — exercises the null-doc path. */
	private static class TestSerializer implements ClinicalRecordSerializer<TestEntity> {
		/** When true, attach obs_group_concept_name + synonyms so getEmbeddingInput() diverges from getText(). */
		boolean enrich;

		@Override public String getResourceType() { return "test"; }
		@Override public Class<TestEntity> getSupportedType() { return TestEntity.class; }
		@Override
		public QueryDocument serialize(TestEntity record) {
			if ("skip".equals(record.uuid)) {
				return null;
			}
			QueryDocument doc = new QueryDocument();
			doc.setResourceType("test");
			doc.setResourceUuid(record.uuid);
			doc.setPatientUuid("patient-A");
			doc.setText("entity " + record.uuid);
			doc.setLastModified(record.ts);
			if (enrich) {
				doc.putMetadata(FIELD_OBS_GROUP_CONCEPT_NAME, "Vital signs");
				doc.putMetadata(FIELD_SYNONYMS, Arrays.asList("SBP", "Systolic BP"));
			}
			return doc;
		}
	}

	@Test
	public void runForPatient_stopsWhenAPageDoesNotAdvancePastTheCursor() {
		FakeBootstrapper b = new FakeBootstrapper();
		// A failed fetch leaves half-loaded rows in the session; they come back unhydrated, at the same
		// epoch cursor, on every later fetch.
		b.stuckPatientPage = Arrays.asList(new TestEntity("cond-1", Instant.EPOCH));

		try {
			b.runForPatient("patient-A", service, embedder);
			fail("a page that never advances must stop the walk");
		}
		catch (IllegalStateException expected) {
			assertEquals(2, b.patientFetches);
			assertTrue(expected.getMessage(), expected.getMessage().contains("patient-A"));
		}
	}

	@Test
	public void runForPatient_stopsWhenAStaleRowOnAPageBoundaryWouldCycleTheWalk() {
		FakeBootstrapper b = new FakeBootstrapper();
		// Page one ends at a real cursor; page two ends on an unhydrated row at the epoch, which sends the
		// next fetch back to page one. Each page differs from the cursor just before it.
		b.cyclingPatientPages = Arrays.asList(
		    Arrays.asList(new TestEntity("cond-1", Instant.parse("2026-01-01T00:00:00Z"))),
		    Arrays.asList(new TestEntity("cond-stale", Instant.EPOCH)));

		try {
			b.runForPatient("patient-A", service, embedder);
			fail("a walk that returns to an earlier page must stop");
		}
		catch (IllegalStateException expected) {
			assertEquals(2, b.patientFetches);
		}
	}

	private static final class FakeBootstrapper extends TypeBootstrapper<TestEntity> {
		private final Deque<List<TestEntity>> pages = new ArrayDeque<>();
		private int pageNumber = 0;
		private int failOnPage = -1;
		private RuntimeException failureCause;
		String poisonUuid;
		private final List<FetchCall> fetchCalls = new ArrayList<>();
		List<TestEntity> stuckPatientPage;
		List<List<TestEntity>> cyclingPatientPages;
		int patientFetches;
		private final TestSerializer serializer = new TestSerializer() {
			@Override
			public org.openmrs.module.querystore.model.QueryDocument serialize(TestEntity record) {
				if (poisonUuid != null && poisonUuid.equals(record.uuid)) {
					throw new RuntimeException("poison entity serialize failure");
				}
				return super.serialize(record);
			}
		};

		void queuePage(TestEntity... entities) {
			List<TestEntity> page = new ArrayList<>();
			Collections.addAll(page, entities);
			pages.add(page);
		}

		void failOnPage(int n, RuntimeException cause) {
			this.failOnPage = n;
			this.failureCause = cause;
		}

		// Poison-page recovery simulation: when recoverPoison is set, fetchPageSkippingPoison returns
		// poisonRecoveryPage once (then null/exhausted) instead of rethrowing the fetch error.
		boolean recoverPoison;
		PageResult<TestEntity> poisonRecoveryPage;
		private boolean fallbackReturned;

		PageResult<TestEntity> recoveryPage(TestEntity... entities) {
			List<TestEntity> list = new ArrayList<>();
			Collections.addAll(list, entities);
			TestEntity last = entities[entities.length - 1];
			return new PageResult<>(list, last.ts, last.uuid);
		}

		@Override public String getResourceType() { return "test"; }
		@Override protected ClinicalRecordSerializer<TestEntity> getSerializer() { return serializer; }
		@Override protected Instant getDateChanged(TestEntity e) { return e.ts; }
		@Override protected String getUuid(TestEntity e) { return e.uuid; }
		boolean versionTracksContent = true;
		@Override protected boolean versionTracksContent() { return versionTracksContent; }

		@Override
		protected List<TestEntity> fetchPage(Instant afterDateChanged, String afterUuid, int pageSize) {
			pageNumber++;
			fetchCalls.add(new FetchCall(afterDateChanged, afterUuid));
			if (pageNumber == failOnPage) {
				throw failureCause;
			}
			return pages.isEmpty() ? Collections.emptyList() : pages.poll();
		}

		@Override
		protected List<TestEntity> fetchPageForPatient(String patientUuid, Instant afterDateChanged, String afterUuid,
		        int pageSize) {
			if (++patientFetches > 4) {
				throw new AssertionError("the per-patient walk fetched " + patientFetches + " pages without stopping");
			}
			if (cyclingPatientPages != null) {
				return cyclingPatientPages.get((patientFetches - 1) % cyclingPatientPages.size());
			}
			return stuckPatientPage;
		}

		@Override
		protected PageResult<TestEntity> fetchPageSkippingPoison(Instant afterDateChanged, String afterUuid,
		        int pageSize, RuntimeException pageFetchError) {
			if (!recoverPoison) {
				return super.fetchPageSkippingPoison(afterDateChanged, afterUuid, pageSize, pageFetchError);
			}
			if (!fallbackReturned) {
				fallbackReturned = true;
				return poisonRecoveryPage;
			}
			return null;
		}
	}

	private static final class FetchCall {
		final Instant afterDateChanged;
		final String afterUuid;
		FetchCall(Instant afterDateChanged, String afterUuid) {
			this.afterDateChanged = afterDateChanged;
			this.afterUuid = afterUuid;
		}
	}

	private static final class RecordingQueryStoreService implements QueryStoreService {
		final List<QueryDocument> indexed = new ArrayList<>();
		final List<Integer> bulkBatchSizes = new ArrayList<>();
		final Map<String, Instant> storedVersions = new HashMap<>();
		String failUuid;

		@Override
		public Map<String, Instant> findStoredVersions(String resourceType, List<String> resourceUuids) {
			Map<String, Instant> found = new HashMap<>();
			for (String uuid : resourceUuids) {
				if (storedVersions.containsKey(uuid)) {
					found.put(uuid, storedVersions.get(uuid));
				}
			}
			return found;
		}

		// Record the batch size per call, then route to the interface default (which loops index())
		// so `indexed` still captures every doc — lets a test assert "one bulkIndex per page" while
		// keeping the per-doc recording the other tests rely on.
		@Override
		public org.openmrs.module.querystore.backend.BulkWriteResult bulkIndex(List<QueryDocument> documents) {
			bulkBatchSizes.add(documents.size());
			return QueryStoreService.super.bulkIndex(documents);
		}

		@Override
		public org.openmrs.module.querystore.backend.WriteResult index(QueryDocument document) {
			indexed.add(document);
			if (failUuid != null && failUuid.equals(document.getResourceUuid())) {
				return org.openmrs.module.querystore.backend.WriteResult.failed(
				        new org.openmrs.module.querystore.backend.DocFailure(
				                document.getResourceType(), document.getResourceUuid(),
				                "simulated backend failure", true));
			}
			return org.openmrs.module.querystore.backend.WriteResult.success();
		}
		@Override public void delete(String resourceType, String resourceUuid) { /* unused */ }
		@Override public void bulkDeleteByPatient(String patientUuid) { /* unused */ }
		@Override public List<QueryDocument> searchByPatient(String p, String q, int l) { return Collections.emptyList(); }
		@Override public List<QueryDocument> search(String q, int l) { return Collections.emptyList(); }
		@Override public List<QueryDocument> getPatientChart(String patientUuid) { return Collections.emptyList(); }
		@Override public void onStartup() { /* unused */ }
		@Override public void onShutdown() { /* unused */ }
	}

	private static final class FakeEmbedder implements EmbeddingProvider {
		final List<String> inputs = new ArrayList<>();

		@Override public int getDimensions() { return 8; }
		@Override public float[] embed(String text) { inputs.add(text); return new float[8]; }
	}

	private static final class InMemoryProgressDao extends BootstrapProgressDao {
		final Map<String, BootstrapProgress> store = new HashMap<>();
		final List<BootstrapProgress> savesByCursor = new ArrayList<>();

		InMemoryProgressDao() { super(null); }

		@Override
		public BootstrapProgress find(String resourceType) {
			BootstrapProgress p = store.get(resourceType);
			return p == null ? null : copy(p);
		}

		@Override
		public List<BootstrapProgress> findAll() {
			List<BootstrapProgress> out = new ArrayList<>();
			for (BootstrapProgress p : store.values()) {
				out.add(copy(p));
			}
			return out;
		}

		@Override
		public void save(BootstrapProgress progress) {
			store.put(progress.getResourceType(), copy(progress));
			savesByCursor.add(copy(progress));
		}

		// Defensive copy so the test's recorded saves don't reflect later in-place mutations on the
		// progress object as the bootstrap loop advances.
		private static BootstrapProgress copy(BootstrapProgress p) {
			BootstrapProgress c = new BootstrapProgress(p.getResourceType());
			c.setStatus(p.getStatus());
			c.setCursorDateChanged(p.getCursorDateChanged());
			c.setCursorUuid(p.getCursorUuid());
			c.setDocumentsIndexed(p.getDocumentsIndexed());
			c.setStartedAt(p.getStartedAt());
			c.setCompletedAt(p.getCompletedAt());
			c.setFailureMessage(p.getFailureMessage());
			return c;
		}
	}

}
