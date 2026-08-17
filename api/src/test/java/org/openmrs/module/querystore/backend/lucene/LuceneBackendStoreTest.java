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

import org.apache.lucene.search.TermQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.querystore.backend.BackendCapabilities;
import org.openmrs.module.querystore.backend.BulkWriteResult;
import org.openmrs.module.querystore.backend.Filter;
import org.openmrs.module.querystore.backend.PatientChartRead;
import org.openmrs.module.querystore.backend.SchemaSpec;
import org.openmrs.module.querystore.backend.SearchRequest;
import org.openmrs.module.querystore.backend.SearchResult;
import org.openmrs.module.querystore.model.QueryDocument;

/**
 * Unit-level coverage of the embedded {@link LuceneBackendStore}. Lucene is in-JVM so this runs
 * in the default {@code mvn test} pass — unlike the MySQL backend, no external service is needed.
 * Tests cover the three SPI invariants pinned in ADR Decision 3 (idempotency, sub-linear
 * patient-scoped reads, conditional upsert by {@code last_modified}) plus BM25 and brute-force
 * cosine kNN behaviour.
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
	public void bm25_matchesOnSynonymsField() {
		// ADR Decision 6: synonyms are BM25-indexed as a top-level companion of text on the
		// Lucene tier so an alternate-term query surfaces docs whose stored text uses the
		// preferred name. Without the synonyms field, "HTN" would not find this doc — its
		// citation-clean text is "Hypertension".
		QueryDocument doc = doc("condition", "patient-A", "Hypertension", null);
		doc.putMetadata(org.openmrs.module.querystore.QueryStoreConstants.FIELD_SYNONYMS,
		    java.util.Arrays.asList("HTN", "High blood pressure"));
		assertTrue(backend.upsert(doc).isSucceeded());

		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("condition").queryText("HTN")
		        .filter(Filter.patientScope("patient-A")).limit(10).build());

		assertEquals(1, result.getHits().size());
		assertEquals(doc.getResourceUuid(), result.getHits().get(0).getDocument().getResourceUuid());
	}

	@Test
	public void bm25_matchesOnDescriptionField() {
		// Concept descriptions are BM25-indexed as a top-level companion of text so a
		// category-word query surfaces docs whose preferred name doesn't carry the matching
		// term — e.g. "Blood urea nitrogen" doesn't say "kidney" in its name but its concept
		// description does. Without this field, a "kidney" query would miss BUN entirely and
		// the smoke-eval P@5 gain on the kidney query (+0.20) would silently disappear.
		QueryDocument doc = doc("obs", "patient-A", "Blood urea nitrogen: 82.9 mmol/L", null);
		doc.putMetadata(org.openmrs.module.querystore.QueryStoreConstants.FIELD_DESCRIPTION,
				"Measure of urea levels in the blood often used to assess kidney status.");
		assertTrue(backend.upsert(doc).isSucceeded());

		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("kidney")
				.filter(Filter.patientScope("patient-A")).limit(10).build());

		assertEquals("description-only term must surface the doc", 1, result.getHits().size());
		assertEquals(doc.getResourceUuid(), result.getHits().get(0).getDocument().getResourceUuid());
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
	public void countByType_countsIndexedDocs_andZeroForUnpopulatedType() {
		// #3 drift "indexed" count: live per-type doc count, 0 for a type with no docs (distinct from
		// the SPI default's -1 "can't count").
		backend.upsert(doc("obs", "patient-A", "Glucose 5.1", null));
		backend.upsert(doc("obs", "patient-B", "Glucose 6.0", null));
		backend.upsert(doc("condition", "patient-A", "Asthma", null));

		assertEquals(2L, backend.countByType("obs"));
		assertEquals(1L, backend.countByType("condition"));
		assertEquals("a type with no indexed docs counts 0, not -1", 0L, backend.countByType("visit"));
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
	public void findAllByPatient_returnsAllDocsAcrossTypesOrderedByRecordDateDesc() {
		// ADR Decision 15: getPatientChart returns every indexed doc for the patient, no filtering,
		// ordered by record_date desc with (resource_type, resource_uuid) tie-breaker. The Lucene
		// backend.findAllByPatient is the SPI primitive that backs it. Pinned cross-tier with the
		// MySQL integration test so the CHART_ORDER Comparators can't silently drift.
		QueryDocument recentObs = doc("obs", "patient-A", "Glucose 8.1", null);
		recentObs.setDate(LocalDate.parse("2026-04-10"));
		QueryDocument olderObs = doc("obs", "patient-A", "Glucose 7.4", null);
		olderObs.setDate(LocalDate.parse("2025-12-01"));
		QueryDocument condition = doc("condition", "patient-A", "Type 2 Diabetes", null);
		condition.setDate(LocalDate.parse("2025-12-01"));
		QueryDocument otherPatientObs = doc("obs", "patient-B", "Temperature 37.0", null);
		otherPatientObs.setDate(LocalDate.parse("2026-04-10"));

		assertTrue(backend.upsert(recentObs).isSucceeded());
		assertTrue(backend.upsert(olderObs).isSucceeded());
		assertTrue(backend.upsert(condition).isSucceeded());
		assertTrue(backend.upsert(otherPatientObs).isSucceeded());

		List<QueryDocument> chart = backend.findAllByPatient("patient-A");

		assertEquals("must return all of patient-A's docs and no one else's", 3, chart.size());
		assertEquals(recentObs.getResourceUuid(), chart.get(0).getResourceUuid());
		assertEquals("tie-break on record_date must order condition before obs",
		        condition.getResourceUuid(), chart.get(1).getResourceUuid());
		assertEquals(olderObs.getResourceUuid(), chart.get(2).getResourceUuid());
		for (QueryDocument hit : chart) {
			assertEquals("must not leak other patients' docs", "patient-A", hit.getPatientUuid());
		}
	}

	@Test
	public void findAllByPatient_returnsEmptyForUnknownPatient() {
		backend.upsert(doc("obs", "patient-A", "Temperature 38.5", null));

		assertTrue(backend.findAllByPatient("nobody").isEmpty());
	}

	@Test
	public void findAllByPatient_returnsEmptyForNullOrBlankUuid() {
		assertTrue(backend.findAllByPatient(null).isEmpty());
		assertTrue(backend.findAllByPatient("").isEmpty());
	}

	@Test
	public void findPatientChart_marksAHandledPerIndexFailureIncomplete() throws Exception {
		QueryDocument obs = doc("obs", "patient-A", "Retained observation", null);
		QueryDocument condition = doc("condition", "patient-A", "Unavailable condition", null);
		backend.upsert(obs);
		backend.upsert(condition);
		backend.close();

		backend = new LuceneBackendStore(indexRoot) {

			@Override
			protected void collectAllByPatient(String resourceType, TermQuery patientQuery,
			        List<QueryDocument> sink) throws IOException {
				if ("condition".equals(resourceType)) {
					throw new IOException("simulated index read failure");
				}
				super.collectAllByPatient(resourceType, patientQuery, sink);
			}
		};

		PatientChartRead read = backend.findPatientChart("patient-A");

		assertTrue("a handled per-index failure must be disclosed", read.isTruncated());
		assertEquals(1, read.getDocuments().size());
		assertEquals(obs.getResourceUuid(), read.getDocuments().get(0).getResourceUuid());
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
	public void knnWithoutPatientFilterScansAcrossAllPatients() {
		// The un-filtered branch routes through MatchAllDocsQuery — without an explicit test the
		// only thing exercising it is a hypothetical cross-patient call. Two docs, two patients,
		// no filter, query vector closest to the second one: result must include both, ranked.
		QueryDocument a = doc("obs", "patient-A", "a", new float[] { 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f });
		QueryDocument b = doc("obs", "patient-B", "b", new float[] { 0f, 1f, 0f, 0f, 0f, 0f, 0f, 0f });
		backend.upsert(a);
		backend.upsert(b);

		SearchResult result = backend.knn(SearchRequest.builder().resourceType("obs")
		        .queryVector(new float[] { 0f, 1f, 0f, 0f, 0f, 0f, 0f, 0f }).limit(2).build());

		assertEquals(2, result.getHits().size());
		assertEquals(b.getResourceUuid(), result.getHits().get(0).getDocument().getResourceUuid());
		assertEquals(a.getResourceUuid(), result.getHits().get(1).getDocument().getResourceUuid());
	}

	@Test
	public void knnSkipsDocumentsWithoutEmbedding() {
		// The scan's null-embedding skip protects against entries that satisfy the structured
		// filter but were indexed without an embedding (e.g. mid-rollout, or a serializer that
		// chose to skip embedding for a particular record). Without this branch the heap would
		// admit them with score 0, polluting the top-K.
		QueryDocument embedded = doc("obs", "patient-A", "has vector",
		        new float[] { 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f });
		QueryDocument unembedded = doc("obs", "patient-A", "no vector", null);
		backend.upsert(embedded);
		backend.upsert(unembedded);

		SearchResult result = backend.knn(SearchRequest.builder().resourceType("obs")
		        .queryVector(new float[] { 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f }).limit(10)
		        .filter(Filter.patientScope("patient-A")).build());

		assertEquals("kNN must skip documents without an indexed embedding",
		        1, result.getHits().size());
		assertEquals(embedded.getResourceUuid(), result.getHits().get(0).getDocument().getResourceUuid());
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
		assertFalse("Lucene tier inherits the SPI-default RRF per Decision 3 (no native hybrid)",
		        caps.supportsHybridNative());
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
		// The embedding payload is incidental to this BM25 test — any non-null vector works since
		// the brute-force scan only consumes embeddings under knn(), not bm25().
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
	public void existsByPatient_findsOnDiskTypeFromPriorSession() throws IOException {
		// Same regression class as wildcardSearchHitsOnDiskTypesFromPriorSessions: the prior fix
		// only patched resolveResourceTypes, but existsByPatient ran the same short-circuit and
		// would return false for a patient whose only documents lived in an index inherited from
		// a prior JVM. Concrete failure: chartsearchai's lazy-projection probe skips reconciling
		// patients whose data is "invisible" to existsByPatient.
		backend.upsert(doc("obs", "patient-A", "prior session obs", null));
		backend.close();

		LuceneBackendStore secondSession = new LuceneBackendStore(indexRoot);
		try {
			secondSession.ensureSchema("condition", SchemaSpec.builder(8).build());
			// Note: no upsert on patient-A in this session — the obs hit must come from disk only.
			assertTrue("patient-A's on-disk obs index must be visible to existsByPatient",
			        secondSession.existsByPatient("patient-A"));
		}
		finally {
			secondSession.close();
			backend = new LuceneBackendStore(indexRoot);
		}
	}

	@Test
	public void bulkDeleteByPatient_purgesOnDiskTypeFromPriorSession() throws IOException {
		// Same regression class. Concrete failure: a patient-purge run via bulkDeleteByPatient
		// silently leaves PHI in any index inherited from a prior JVM whose writer hasn't been
		// opened this session — every type with no current-session activity stays unpurged.
		//
		// Production trigger: PatientRecordSerializer's bulkDeletePatientUuidFor hook fires this
		// SPI on every core purgePatient call. This test pins the backend contract — that the
		// sweep finds on-disk indexes from prior JVMs as well as the in-memory writers cache.
		backend.upsert(doc("obs", "patient-A", "prior session obs", null));
		backend.close();

		LuceneBackendStore secondSession = new LuceneBackendStore(indexRoot);
		try {
			secondSession.ensureSchema("condition", SchemaSpec.builder(8).build());
			BulkWriteResult result = secondSession.bulkDeleteByPatient("patient-A");
			assertEquals("must attempt the on-disk obs document",
			        1, result.getTotalRequested());
			assertEquals("must delete the on-disk obs document",
			        1, result.getSucceeded());
			assertFalse("patient-A must be gone from all indexes after the purge",
			        secondSession.existsByPatient("patient-A"));
		}
		finally {
			secondSession.close();
			backend = new LuceneBackendStore(indexRoot);
		}
	}

	@Test
	public void wildcardSearchSurvivesPerDirectoryWriterOpenFailure() throws IOException {
		// Phase 2 Pass 3 found that wildcard enumeration now primes writers for every on-disk
		// directory; previously, only ensureWriter(resourceType) opened a writer, so a stale
		// write.lock on one directory only failed type-specific reads. After the cross-session
		// merge, an openWriter failure inside listAllIndexes would take down EVERY cross-type
		// read (bulkDeleteByPatient, existsByPatient, bm25/knn). Pin: per-directory openWriter
		// failure must be swallowed with a WARN, the bad index skipped, and enumeration continues
		// so a single bad dir doesn't cause a global cross-type read outage.
		backend.upsert(doc("obs", "patient-A", "valid obs", null));
		backend.upsert(doc("condition", "patient-A", "valid condition", null));
		backend.close();

		// Hold an exclusive writer on querystore_condition via a SECOND backend pointing at the
		// same root; a THIRD backend's listAllIndexes() will then fail to open the condition
		// directory (lock held) but should still succeed on obs and complete the enumeration.
		LuceneBackendStore lockHolder = new LuceneBackendStore(indexRoot);
		try {
			lockHolder.ensureSchema("condition", SchemaSpec.builder(8).build());
			lockHolder.upsert(doc("condition", "patient-A", "lock-holder condition", null));

			LuceneBackendStore probe = new LuceneBackendStore(indexRoot);
			try {
				// Must NOT throw — the per-directory swallow must keep enumeration alive.
				SearchResult result = probe.bm25(SearchRequest.builder()
				        .resourceTypes(Collections.emptyList())
				        .queryText("valid")
				        .filter(Filter.patientScope("patient-A"))
				        .limit(10).build());
				Set<String> hitTypes = new HashSet<>();
				for (org.openmrs.module.querystore.backend.Hit h : result.getHits()) {
					hitTypes.add(h.getDocument().getResourceType());
				}
				assertTrue("obs must still be reachable when condition's writer can't be opened",
				        hitTypes.contains("obs"));
				// condition may or may not be present (depends on whether the second backend's
				// commit had reached the on-disk segment) — the contract here is "obs is still
				// reachable," not "exactly one type returns."
			}
			finally {
				probe.close();
			}
		}
		finally {
			lockHolder.close();
			backend = new LuceneBackendStore(indexRoot); // restore for @After cleanup
		}
	}

	@Test
	public void wildcardSearchSkipsOnDiskDirectoriesWithInvalidResourceTypeNames() throws IOException {
		// Stale-directory regression guard. The pre-existing cache-only short-circuit hid any
		// querystore_* directory whose stripped name didn't match the resource-type regex (e.g.
		// LegacyObs from a v0.9 release with mixed-case identifiers, or a manually-renamed dir).
		// After the cross-session fix, every querystore_* directory is candidate for inclusion in
		// wildcard reads — but consumers downstream (chartsearchai's QueryStoreChartBuilder)
		// expect resource-type names from the registered ResourceTypeProvider set. The schema
		// manager must filter stale directories out so they don't bleed into search results.
		backend.upsert(doc("obs", "patient-A", "valid obs", null));
		// Plant a stale directory with an invalid name (uppercase fails the [a-z][a-z0-9_]* regex).
		java.nio.file.Files.createDirectories(indexRoot.resolve(
		        org.openmrs.module.querystore.QueryStoreConstants.INDEX_PREFIX + "LegacyObs"));

		SearchResult result = backend.bm25(SearchRequest.builder()
		        .resourceTypes(Collections.emptyList())
		        .queryText("valid")
		        .filter(Filter.patientScope("patient-A"))
		        .limit(10).build());

		Set<String> hitTypes = new HashSet<>();
		for (org.openmrs.module.querystore.backend.Hit h : result.getHits()) {
			hitTypes.add(h.getDocument().getResourceType());
		}
		assertEquals("wildcard must exclude the LegacyObs stray directory",
		        Collections.singleton("obs"), hitTypes);
	}

	@Test
	public void wildcardSearchHitsOnDiskTypesFromPriorSessions() throws IOException {
		// Regression guard: a previous JVM indexed obs (bootstrap completed), the current JVM has
		// only touched condition (e.g. via AOP). resolveResourceTypes used to short-circuit on the
		// first non-empty knownIndexNames() → wildcard search saw only condition, and the obs index
		// on disk was silently invisible. This is exactly the symptom that hid Betty's 6
		// appointment hits behind a single bill hit after the bridge UserContext fix landed.
		backend.upsert(doc("obs", "patient-A", "prior session obs", null));
		backend.close();

		LuceneBackendStore secondSession = new LuceneBackendStore(indexRoot);
		try {
			secondSession.ensureSchema("condition", SchemaSpec.builder(8).build());
			secondSession.upsert(doc("condition", "patient-A", "current session condition", null));

			SearchResult result = secondSession.bm25(SearchRequest.builder()
			        .resourceTypes(Collections.emptyList())
			        .queryText("session")
			        .filter(Filter.patientScope("patient-A"))
			        .limit(10).build());

			Set<String> hitTypes = new HashSet<>();
			for (org.openmrs.module.querystore.backend.Hit h : result.getHits()) {
				hitTypes.add(h.getDocument().getResourceType());
			}
			assertEquals("wildcard must include on-disk types not yet opened this session",
			        new HashSet<>(Arrays.asList("obs", "condition")), hitTypes);
		}
		finally {
			secondSession.close();
			backend = new LuceneBackendStore(indexRoot); // restore for @After cleanup
		}
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
