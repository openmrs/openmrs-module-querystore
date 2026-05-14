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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.querystore.backend.BackendCapabilities;
import org.openmrs.module.querystore.backend.BulkWriteResult;
import org.openmrs.module.querystore.backend.Filter;
import org.openmrs.module.querystore.backend.SchemaSpec;
import org.openmrs.module.querystore.backend.SearchRequest;
import org.openmrs.module.querystore.backend.SearchResult;
import org.openmrs.module.querystore.model.QueryDocument;

/**
 * Unit-level coverage of the embedded {@link LuceneBackendStore}. Lucene is in-JVM so this runs
 * in the default {@code mvn test} pass — unlike the MySQL backend, no external service is needed.
 * Tests cover the three SPI invariants pinned in ADR Decision 3 (idempotency, sub-linear
 * patient-scoped reads, conditional upsert by {@code last_modified}) plus BM25 and HNSW kNN
 * behaviour.
 */
public class LuceneBackendStoreTest {

	private Path indexRoot;

	private LuceneBackendStore backend;

	@Before
	public void setUp() throws IOException {
		indexRoot = Files.createTempDirectory("querystore-lucene-test-");
		backend = new LuceneBackendStore(indexRoot);
		for (String type : Arrays.asList("obs", "condition", "drug_order")) {
			backend.ensureSchema(type, SchemaSpec.builder(8).build());
		}
	}

	@After
	public void tearDown() throws IOException {
		backend.close();
		deleteRecursive(indexRoot);
	}

	@Test
	public void upsertThenSearchByPatientFindsDocument() {
		QueryDocument doc = doc("obs", "patient-A", "Fasting blood glucose 11.2 mmol per L", null);
		assertTrue(backend.upsert(doc).isSucceeded());

		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("glucose").limit(10)
		        .filter(Filter.patientScope("patient-A")).build());

		assertEquals(1, result.getHits().size());
		assertEquals(doc.getResourceUuid(), result.getHits().get(0).getDocument().getResourceUuid());
		assertEquals(1, result.getHits().get(0).getRank());
	}

	@Test
	public void upsertIsIdempotent() {
		QueryDocument doc = doc("obs", "patient-A", "Hemoglobin A1c 7.5", null);
		assertTrue(backend.upsert(doc).isSucceeded());
		assertTrue(backend.upsert(doc).isSucceeded());

		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("Hemoglobin")
		        .filter(Filter.patientScope("patient-A")).limit(10).build());
		assertEquals("upsert must not duplicate by resource_uuid", 1, result.getHits().size());
	}

	@Test
	public void deleteIsIdempotent() {
		String uuid = UUID.randomUUID().toString();
		assertTrue(backend.delete("obs", uuid).isSucceeded());
		assertTrue(backend.delete("obs", uuid).isSucceeded());
	}

	@Test
	public void deleteRemovesDocument() {
		QueryDocument doc = doc("obs", "patient-A", "Pulse 72 bpm", null);
		backend.upsert(doc);
		assertEquals(1, bm25Count("obs", "Pulse", "patient-A"));

		assertTrue(backend.delete("obs", doc.getResourceUuid()).isSucceeded());
		assertEquals(0, bm25Count("obs", "Pulse", "patient-A"));
	}

	@Test
	public void existsByPatient_returnsFalseForUnknownPatient() {
		assertFalse(backend.existsByPatient("nobody"));
	}

	@Test
	public void existsByPatient_returnsTrueAfterUpsert() {
		backend.upsert(doc("obs", "patient-A", "Temperature 38.5", null));
		assertTrue(backend.existsByPatient("patient-A"));
		assertFalse(backend.existsByPatient("patient-B"));
	}

	@Test
	public void existsByPatient_findsHitInAnyType() {
		// The probe iterates known indices and short-circuits on first hit. With only a condition
		// document, a patient still resolves as "exists" — the SPI contract is presence anywhere.
		backend.upsert(doc("condition", "patient-A", "Type 2 Diabetes", null));
		assertTrue(backend.existsByPatient("patient-A"));
	}

	@Test
	public void existsByPatient_falseForNullOrBlank() {
		assertFalse(backend.existsByPatient(null));
		assertFalse(backend.existsByPatient(""));
	}

	@Test
	public void bulkDeleteByPatientRemovesAcrossTypes() {
		backend.upsert(doc("obs", "patient-A", "Temperature 38.5", null));
		backend.upsert(doc("condition", "patient-A", "Type 2 Diabetes", null));
		backend.upsert(doc("obs", "patient-B", "Temperature 37.0", null));

		BulkWriteResult res = backend.bulkDeleteByPatient("patient-A");
		assertFalse(res.hasFailures());
		assertEquals(2, res.getSucceeded());

		SearchResult remaining = backend.bm25(SearchRequest.builder().queryText("Temperature").limit(10).build());
		assertEquals(1, remaining.getHits().size());
		assertEquals("patient-B", remaining.getHits().get(0).getDocument().getPatientUuid());
	}

	@Test
	public void knnReturnsResultsRankedByCosineSimilarity() {
		QueryDocument near = doc("obs", "patient-A", "near", new float[] { 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f });
		QueryDocument far = doc("obs", "patient-A", "far", new float[] { 0f, 1f, 0f, 0f, 0f, 0f, 0f, 0f });
		backend.upsert(near);
		backend.upsert(far);

		SearchResult result = backend.knn(SearchRequest.builder().resourceType("obs")
		        .queryVector(new float[] { 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f }).limit(2)
		        .filter(Filter.patientScope("patient-A")).build());

		assertEquals(2, result.getHits().size());
		assertEquals(near.getResourceUuid(), result.getHits().get(0).getDocument().getResourceUuid());
		assertEquals(1, result.getHits().get(0).getRank());
		assertEquals(2, result.getHits().get(1).getRank());
	}

	@Test
	public void knnRetrievesStoredEmbedding() {
		float[] vec = new float[] { 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f };
		QueryDocument doc = doc("obs", "patient-A", "vec doc", vec);
		backend.upsert(doc);

		SearchResult result = backend.knn(SearchRequest.builder().resourceType("obs")
		        .queryVector(vec).limit(1).filter(Filter.patientScope("patient-A")).build());

		assertEquals(1, result.getHits().size());
		float[] retrieved = result.getHits().get(0).getDocument().getEmbedding();
		assertNotNull("kNN hit must carry the stored embedding back", retrieved);
		assertEquals(vec.length, retrieved.length);
		for (int i = 0; i < vec.length; i++) {
			assertEquals(vec[i], retrieved[i], 1e-6);
		}
	}

	@Test
	public void upsertWithOlderLastModifiedIsSkipped() {
		String uuid = UUID.randomUUID().toString();
		Instant t1 = Instant.parse("2025-03-15T09:00:00Z");
		Instant t2 = t1.plus(1, ChronoUnit.HOURS);

		QueryDocument fresh = doc("obs", "patient-A", "Glucose 8.1 at ten", null);
		fresh.setResourceUuid(uuid);
		fresh.setLastModified(t2);
		assertTrue(backend.upsert(fresh).isSucceeded());

		QueryDocument stale = doc("obs", "patient-A", "Glucose 5.4 at nine", null);
		stale.setResourceUuid(uuid);
		stale.setLastModified(t1);
		assertTrue("stale upsert reports success (skipped is a no-error outcome consistent with idempotency)",
		        backend.upsert(stale).isSucceeded());

		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("Glucose")
		        .filter(Filter.patientScope("patient-A")).limit(10).build());
		assertEquals(1, result.getHits().size());
		QueryDocument stored = result.getHits().get(0).getDocument();
		assertEquals("fresh version must survive a stale concurrent write", "Glucose 8.1 at ten", stored.getText());
		assertEquals(t2, stored.getLastModified());
	}

	@Test
	public void upsertWithNewerLastModifiedReplacesOlder() {
		String uuid = UUID.randomUUID().toString();
		Instant t1 = Instant.parse("2025-03-15T09:00:00Z");
		Instant t2 = t1.plus(1, ChronoUnit.HOURS);

		QueryDocument first = doc("obs", "patient-A", "Glucose 5.4 at nine", null);
		first.setResourceUuid(uuid);
		first.setLastModified(t1);
		assertTrue(backend.upsert(first).isSucceeded());

		QueryDocument second = doc("obs", "patient-A", "Glucose 8.1 at ten", null);
		second.setResourceUuid(uuid);
		second.setLastModified(t2);
		assertTrue(backend.upsert(second).isSucceeded());

		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("Glucose")
		        .filter(Filter.patientScope("patient-A")).limit(10).build());
		assertEquals(1, result.getHits().size());
		assertEquals("Glucose 8.1 at ten", result.getHits().get(0).getDocument().getText());
	}

	@Test
	public void upsertWithSameLastModifiedAppliesIdempotently() {
		// Duplicate-event delivery: the same save event arrives twice, both upserts carry the
		// entity's current dateChanged. The >= guard (not >) must let the second write apply so
		// the operation is naturally idempotent.
		String uuid = UUID.randomUUID().toString();
		Instant t = Instant.parse("2025-03-15T09:00:00Z");

		QueryDocument first = doc("obs", "patient-A", "Glucose 5.4", null);
		first.setResourceUuid(uuid);
		first.setLastModified(t);
		assertTrue(backend.upsert(first).isSucceeded());
		assertTrue(backend.upsert(first).isSucceeded());

		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("Glucose")
		        .filter(Filter.patientScope("patient-A")).limit(10).build());
		assertEquals(1, result.getHits().size());
		assertEquals(t, result.getHits().get(0).getDocument().getLastModified());
	}

	@Test
	public void upsertWithoutLastModifiedFallsBackToLastWriteWins() {
		String uuid = UUID.randomUUID().toString();

		QueryDocument first = doc("obs", "patient-A", "Pulse 72 bpm", null);
		first.setResourceUuid(uuid);
		assertTrue(backend.upsert(first).isSucceeded());

		QueryDocument second = doc("obs", "patient-A", "Pulse 88 bpm", null);
		second.setResourceUuid(uuid);
		assertTrue(backend.upsert(second).isSucceeded());

		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("Pulse")
		        .filter(Filter.patientScope("patient-A")).limit(10).build());
		assertEquals(1, result.getHits().size());
		assertEquals("Pulse 88 bpm", result.getHits().get(0).getDocument().getText());
	}

	@Test
	public void bulkUpsertRejectsInvalidInputAndCountsCleanBatch() {
		// Two paths exercised here: (1) validate() fails fast on any bad doc, so the batch raises
		// IllegalArgumentException without writing partial state; (2) an all-valid batch reports
		// the full succeeded count and no failures.
		QueryDocument ok = doc("obs", "patient-A", "valid", null);
		QueryDocument invalid = doc("obs", null, "missing patient_uuid", null);
		try {
			backend.bulkUpsert(Arrays.asList(ok, invalid));
			throw new AssertionError("bulkUpsert must reject documents missing patient_uuid");
		}
		catch (IllegalArgumentException expected) {
			// Expected fail-fast.
		}

		BulkWriteResult clean = backend.bulkUpsert(Arrays.asList(
		        doc("obs", "patient-A", "valid 1", null),
		        doc("obs", "patient-A", "valid 2", null)));
		assertEquals(2, clean.getSucceeded());
		assertFalse(clean.hasFailures());
	}

	@Test
	public void bulkDeleteRemovesDocuments() {
		QueryDocument a = doc("obs", "patient-A", "a", null);
		QueryDocument b = doc("obs", "patient-A", "b", null);
		QueryDocument c = doc("obs", "patient-A", "c", null);
		backend.bulkUpsert(Arrays.asList(a, b, c));

		BulkWriteResult res = backend.bulkDelete("obs", Arrays.asList(a.getResourceUuid(), b.getResourceUuid()));
		assertFalse(res.hasFailures());
		assertEquals(2, res.getSucceeded());

		SearchResult remaining = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("c")
		        .filter(Filter.patientScope("patient-A")).limit(10).build());
		assertEquals(1, remaining.getHits().size());
	}

	@Test
	public void capabilitiesReportLuceneTier() {
		BackendCapabilities caps = backend.capabilities();
		assertTrue(caps.supportsKnn());
		assertFalse("Lucene tier fuses at the service layer per Decision 3", caps.supportsHybridNative());
		assertFalse("single-host Lucene cannot do cross-patient kNN at multi-million scale",
		        caps.supportsCrossPatientKnnAtScale());
		assertTrue(caps.getRecommendedMaxCorpusSize() > 0);
		assertTrue(caps.getSupportedFilters().contains(Filter.Kind.PATIENT_SCOPE));
	}

	@Test
	public void patientScopedReadIsSubLinear() {
		// Sanity-check that the PATIENT_SCOPE filter routes through the inverted index instead of
		// scanning the whole corpus. 5000 docs across 50 patients; querying for one patient's slice
		// should complete in well under five seconds even on a slow CI runner.
		// Lucene's HNSW writer asserts a non-zero magnitude under cosine; the embedding payload here
		// is incidental to the patient-filter test, so a tiny constant non-zero vector is enough.
		float[] vec = new float[8];
		Arrays.fill(vec, 0.1f);
		for (int p = 0; p < 50; p++) {
			List<QueryDocument> batch = new ArrayList<>();
			for (int i = 0; i < 100; i++) {
				batch.add(doc("obs", "patient-" + p, "obs body " + i, vec));
			}
			backend.bulkUpsert(batch);
		}

		long start = System.nanoTime();
		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("body")
		        .filter(Filter.patientScope("patient-7")).limit(10).build());
		long elapsedMs = (System.nanoTime() - start) / 1_000_000L;

		assertNotNull(result);
		assertTrue("patient-scoped query took " + elapsedMs + "ms; expected <5000ms", elapsedMs < 5000);
	}

	@Test
	public void wildcardSearchHitsAllRegisteredTypes() {
		backend.upsert(doc("obs", "patient-A", "shared keyword obs", null));
		backend.upsert(doc("condition", "patient-A", "shared keyword condition", null));

		SearchResult result = backend.bm25(SearchRequest.builder()
		        .resourceTypes(Collections.emptyList())
		        .queryText("shared")
		        .filter(Filter.patientScope("patient-A"))
		        .limit(10).build());
		assertEquals(2, result.getHits().size());
		Set<String> hitTypes = new HashSet<>();
		for (org.openmrs.module.querystore.backend.Hit h : result.getHits()) {
			hitTypes.add(h.getDocument().getResourceType());
		}
		assertEquals("wildcard search must return one hit per registered type, not two of the same type",
		        new HashSet<>(Arrays.asList("obs", "condition")), hitTypes);
	}

	@Test
	public void termFilterOnRecordDateMatchesIndexedPoint() {
		// TERM/IN on record_date must hit the LongPoint, not a non-existent indexed string field.
		// Without the special-case in the filter translator this returns zero hits — silently
		// breaking any consumer that does Filter.term("record_date", ...).
		LocalDate target = LocalDate.of(2025, 3, 15);
		QueryDocument onTarget = doc("obs", "patient-A", "on target", null);
		onTarget.setDate(target);
		QueryDocument otherDay = doc("obs", "patient-A", "different day", null);
		otherDay.setDate(target.plusDays(1));
		backend.upsert(onTarget);
		backend.upsert(otherDay);

		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("target")
		        .filter(Filter.patientScope("patient-A"))
		        .filter(Filter.term("record_date", target))
		        .limit(10).build());
		assertEquals(1, result.getHits().size());
		assertEquals(onTarget.getResourceUuid(), result.getHits().get(0).getDocument().getResourceUuid());
	}

	@Test
	public void concurrentUpsertsOnSameUuidLeaveOnlyHighestVersion() throws InterruptedException {
		// Conditional-upsert under contention: N threads race the same resource_uuid with mixed
		// last_modified. Without the per-UUID stripe lock the read-then-update window admits a
		// race where two writers both decide "I'm newer" and the loser overwrites the winner.
		String uuid = UUID.randomUUID().toString();
		int threads = 16;
		int versionsPerThread = 25;
		Instant base = Instant.parse("2025-03-15T09:00:00Z");
		Instant maxVersion = base.plus(versionsPerThread - 1, ChronoUnit.SECONDS);

		ExecutorService pool = Executors.newFixedThreadPool(threads);
		CountDownLatch start = new CountDownLatch(1);
		List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
		for (int t = 0; t < threads; t++) {
			final int threadId = t;
			futures.add(pool.submit(() -> {
				try {
					start.await();
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
				// Each thread walks its versions in a shuffled order so the writers see
				// out-of-order arrivals — the slow-projection scenario the guard protects.
				List<Integer> order = new ArrayList<>();
				for (int i = 0; i < versionsPerThread; i++) {
					order.add(i);
				}
				Collections.shuffle(order, new java.util.Random(threadId));
				for (int i : order) {
					QueryDocument d = doc("obs", "patient-A", "thread " + threadId + " v" + i, null);
					d.setResourceUuid(uuid);
					d.setLastModified(base.plus(i, ChronoUnit.SECONDS));
					backend.upsert(d);
				}
			}));
		}
		start.countDown();
		pool.shutdown();
		assertTrue("upsert workers must finish under timeout", pool.awaitTermination(30, TimeUnit.SECONDS));
		for (java.util.concurrent.Future<?> f : futures) {
			try {
				f.get();
			}
			catch (java.util.concurrent.ExecutionException e) {
				throw new AssertionError("upsert worker threw", e.getCause());
			}
		}

		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("thread")
		        .filter(Filter.patientScope("patient-A")).limit(10).build());
		assertEquals("exactly one document should survive contention on the same UUID",
		        1, result.getHits().size());
		QueryDocument stored = result.getHits().get(0).getDocument();
		assertEquals("surviving version must be the maximum last_modified, not whoever wrote last",
		        maxVersion, stored.getLastModified());
	}

	@Test
	public void deleteSchemaRemovesIndexDirectory() throws IOException {
		backend.upsert(doc("drug_order", "patient-A", "scratch drug order", null));
		Path dir = indexRoot.resolve("querystore_drug_order");
		assertTrue("drug_order directory created on first upsert", Files.isDirectory(dir));

		backend.deleteSchema("drug_order");
		assertFalse("deleteSchema must remove the directory", Files.exists(dir));
	}

	// ---------- helpers ----------

	private int bm25Count(String resourceType, String text, String patientUuid) {
		return backend.bm25(SearchRequest.builder().resourceType(resourceType).queryText(text)
		        .filter(Filter.patientScope(patientUuid)).limit(10).build()).getHits().size();
	}

	private static QueryDocument doc(String resourceType, String patientUuid, String text, float[] embedding) {
		QueryDocument d = new QueryDocument();
		d.setResourceType(resourceType);
		d.setResourceUuid(UUID.randomUUID().toString());
		d.setPatientUuid(patientUuid);
		d.setDate(LocalDate.now());
		d.setText(text);
		d.setEmbedding(embedding);
		return d;
	}

	private static void deleteRecursive(Path dir) throws IOException {
		if (!Files.exists(dir)) {
			return;
		}
		try (DirectoryStream<Path> children = Files.newDirectoryStream(dir)) {
			for (Path child : children) {
				if (Files.isDirectory(child)) {
					deleteRecursive(child);
				} else {
					Files.deleteIfExists(child);
				}
			}
		}
		Files.deleteIfExists(dir);
	}
}
