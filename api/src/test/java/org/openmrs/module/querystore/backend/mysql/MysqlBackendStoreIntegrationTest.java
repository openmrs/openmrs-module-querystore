/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.backend.mysql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.querystore.backend.BackendCapabilities;
import org.openmrs.module.querystore.backend.Hit;
import org.openmrs.module.querystore.backend.BulkWriteResult;
import org.openmrs.module.querystore.backend.Filter;
import org.openmrs.module.querystore.backend.JdbcSupport;
import org.openmrs.module.querystore.backend.PatientChartRead;
import org.openmrs.module.querystore.backend.SchemaSpec;
import org.openmrs.module.querystore.backend.SearchRequest;
import org.openmrs.module.querystore.backend.SearchResult;
import org.openmrs.module.querystore.backend.WriteResult;
import org.openmrs.module.querystore.model.QueryDocument;
import org.testcontainers.containers.MySQLContainer;

/**
 * Integration test for {@link MysqlBackendStore} against a real MySQL 8 server via Testcontainers.
 * Activated by the {@code integration} Maven profile (run with {@code mvn test -Pintegration} or
 * {@code mvn test -Dintegration}). Skipped by default — H2 cannot fake MySQL FULLTEXT, and the
 * BM25 + idempotency guarantees the SPI commits to need a real engine to validate.
 */
public class MysqlBackendStoreIntegrationTest {

	private static MySQLContainer<?> mysql;

	private static DbSessionFactory sessionFactory;

	private static MysqlBackendStore backend;

	@BeforeClass
	public static void startContainer() {
		mysql = new MySQLContainer<>("mysql:8.0").withDatabaseName("openmrs_test").withUsername("test")
		        .withPassword("test");
		mysql.start();
		sessionFactory = TestSessionFactories.forContainer(mysql);
		backend = new MysqlBackendStore(sessionFactory);
	}

	@AfterClass
	public static void stopContainer() {
		if (sessionFactory != null) {
			sessionFactory.getHibernateSessionFactory().close();
		}
		if (mysql != null) {
			mysql.stop();
		}
	}

	@Before
	public void resetSchemas() {
		for (String type : Arrays.asList("obs", "condition", "drug_order")) {
			backend.deleteSchema(type);
			backend.ensureSchema(type, SchemaSpec.builder(8).build());
		}
	}

	@Test
	public void upsertThenSearchByPatientFindsDocument() {
		QueryDocument doc = doc("obs", "patient-A", "Fasting blood glucose: 11.2 mmol/L", null);
		assertTrue(backend.upsert(doc).isSucceeded());

		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("glucose").limit(10)
		        .filter(Filter.patientScope("patient-A")).build());

		assertEquals(1, result.getHits().size());
		assertEquals(doc.getResourceUuid(), result.getHits().get(0).getDocument().getResourceUuid());
		assertEquals(1, result.getHits().get(0).getRank());
	}

	@Test
	public void upsertIsIdempotent() {
		QueryDocument doc = doc("obs", "patient-A", "Hemoglobin A1c: 7.5", null);
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
		// #3 drift "indexed" count: the live per-type row count, with 0 for a type that has no docs
		// (table absent or empty) — distinct from the SPI default's -1 ("can't count").
		backend.upsert(doc("obs", "patient-A", "Glucose 5.1", null));
		backend.upsert(doc("obs", "patient-B", "Glucose 6.0", null));
		backend.upsert(doc("condition", "patient-A", "Asthma", null));

		assertEquals(2L, backend.countByType("obs"));
		assertEquals(1L, backend.countByType("condition"));
		assertEquals("a type with no indexed docs counts 0, not -1", 0L, backend.countByType("visit"));
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
		// ordered by record_date desc with (resource_type, resource_uuid) tie-breaker. The MySQL
		// backend.findAllByPatient is the SPI primitive that backs it.
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
		// Recent first.
		assertEquals(recentObs.getResourceUuid(), chart.get(0).getResourceUuid());
		// Two docs on 2025-12-01: tie-break is (resource_type asc, resource_uuid asc).
		// 'condition' < 'obs' alphabetically, so the condition comes second.
		assertEquals("tie-break on record_date must order condition before obs",
		        condition.getResourceUuid(), chart.get(1).getResourceUuid());
		assertEquals(olderObs.getResourceUuid(), chart.get(2).getResourceUuid());
		for (QueryDocument hit : chart) {
			assertEquals("must not leak other patients' docs", "patient-A", hit.getPatientUuid());
		}
	}

	@Test
	public void findAllByPatient_returnsEmptyForUnknownPatient() {
		// Steady-state "no data" path: backend has tables but nothing for this patient. Mirrors
		// existsByPatient's null-blank/no-data tolerance — the service layer probes existsByPatient
		// first and triggers lazy bootstrap, so empty here means bootstrap also produced nothing.
		backend.upsert(doc("obs", "patient-A", "Temperature 38.5", null));

		assertTrue(backend.findAllByPatient("nobody").isEmpty());
	}

	@Test
	public void findAllByPatient_returnsEmptyForNullOrBlankUuid() {
		assertTrue(backend.findAllByPatient(null).isEmpty());
		assertTrue(backend.findAllByPatient("").isEmpty());
	}

	@Test
	public void findPatientChart_marksAHandledPerTableFailureIncomplete() {
		QueryDocument obs = doc("obs", "patient-A", "Retained observation", null);
		assertTrue(backend.upsert(obs).isSucceeded());
		JdbcSupport.inTransaction(sessionFactory, conn -> {
			try (Statement stmt = conn.createStatement()) {
				stmt.executeUpdate("CREATE TABLE querystore_broken (id INT)");
			}
		});

		try {
			PatientChartRead read = new MysqlBackendStore(sessionFactory).findPatientChart("patient-A");

			assertTrue("a handled per-table failure must be disclosed", read.isTruncated());
			assertEquals(1, read.getDocuments().size());
			assertEquals(obs.getResourceUuid(), read.getDocuments().get(0).getResourceUuid());
		}
		finally {
			JdbcSupport.inTransaction(sessionFactory, conn -> {
				try (Statement stmt = conn.createStatement()) {
					stmt.executeUpdate("DROP TABLE IF EXISTS querystore_broken");
				}
			});
		}
	}

	@Test
	public void bulkDeleteByPatientRemovesAcrossTypes() {
		backend.upsert(doc("obs", "patient-A", "Temperature: 38.5", null));
		backend.upsert(doc("condition", "patient-A", "Type 2 Diabetes", null));
		backend.upsert(doc("obs", "patient-B", "Temperature: 37.0", null));

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
	public void upsertWithOlderLastModifiedIsSkipped() {
		String uuid = UUID.randomUUID().toString();
		Instant t1 = Instant.parse("2025-03-15T09:00:00Z");
		Instant t2 = t1.plus(1, ChronoUnit.HOURS);

		QueryDocument fresh = doc("obs", "patient-A", "Glucose: 8.1 mmol/L (10:00)", null);
		fresh.setResourceUuid(uuid);
		fresh.setLastModified(t2);
		assertTrue(backend.upsert(fresh).isSucceeded());

		QueryDocument stale = doc("obs", "patient-A", "Glucose: 5.4 mmol/L (09:00)", null);
		stale.setResourceUuid(uuid);
		stale.setLastModified(t1);
		assertTrue("stale upsert still reports success (write applied or skipped — both succeed)",
		        backend.upsert(stale).isSucceeded());

		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("Glucose")
		        .filter(Filter.patientScope("patient-A")).limit(10).build());
		assertEquals(1, result.getHits().size());
		QueryDocument stored = result.getHits().get(0).getDocument();
		assertEquals("fresh version must survive a stale concurrent write",
		        "Glucose: 8.1 mmol/L (10:00)", stored.getText());
		assertEquals(t2, stored.getLastModified());
	}

	@Test
	public void upsertWithNewerLastModifiedReplacesOlder() {
		String uuid = UUID.randomUUID().toString();
		Instant t1 = Instant.parse("2025-03-15T09:00:00Z");
		Instant t2 = t1.plus(1, ChronoUnit.HOURS);

		QueryDocument first = doc("obs", "patient-A", "Glucose: 5.4 mmol/L (09:00)", null);
		first.setResourceUuid(uuid);
		first.setLastModified(t1);
		assertTrue(backend.upsert(first).isSucceeded());

		QueryDocument second = doc("obs", "patient-A", "Glucose: 8.1 mmol/L (10:00)", null);
		second.setResourceUuid(uuid);
		second.setLastModified(t2);
		assertTrue(backend.upsert(second).isSucceeded());

		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("Glucose")
		        .filter(Filter.patientScope("patient-A")).limit(10).build());
		assertEquals(1, result.getHits().size());
		assertEquals("Glucose: 8.1 mmol/L (10:00)", result.getHits().get(0).getDocument().getText());
	}

	@Test
	public void upsertWithSameLastModifiedAppliesIdempotently() {
		// Duplicate-event delivery: the same save event arrives twice, handlers re-fetch the
		// entity, both upserts carry the entity's current dateChanged as last_modified. The >=
		// guard (not >) must let the second write apply so the operation is naturally idempotent.
		String uuid = UUID.randomUUID().toString();
		Instant t = Instant.parse("2025-03-15T09:00:00Z");

		QueryDocument first = doc("obs", "patient-A", "Glucose: 5.4 mmol/L", null);
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

		QueryDocument first = doc("obs", "patient-A", "Pulse: 72 bpm", null);
		first.setResourceUuid(uuid);
		assertTrue(backend.upsert(first).isSucceeded());

		QueryDocument second = doc("obs", "patient-A", "Pulse: 88 bpm", null);
		second.setResourceUuid(uuid);
		assertTrue(backend.upsert(second).isSucceeded());

		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("Pulse")
		        .filter(Filter.patientScope("patient-A")).limit(10).build());
		assertEquals(1, result.getHits().size());
		assertEquals("Pulse: 88 bpm", result.getHits().get(0).getDocument().getText());
	}

	@Test
	public void capabilitiesReportMysqlTier() {
		BackendCapabilities caps = backend.capabilities();
		assertTrue(caps.supportsKnn());
		assertFalse(caps.supportsHybridNative());
		assertFalse(caps.supportsCrossPatientKnnAtScale());
		assertTrue(caps.getRecommendedMaxCorpusSize() > 0);
		assertTrue(caps.getSupportedFilters().contains(Filter.Kind.PATIENT_SCOPE));
	}

	@Test
	public void patientScopedReadIsSubLinear() {
		// Sanity-check that PATIENT_SCOPE filter pushes down to the indexed column.
		// 5000 docs across 50 patients; query for one patient's 100 docs should return promptly.
		float[] zeroVec = new float[8];
		for (int p = 0; p < 50; p++) {
			List<QueryDocument> batch = new ArrayList<>();
			for (int i = 0; i < 100; i++) {
				batch.add(doc("obs", "patient-" + p, "obs " + i, zeroVec));
			}
			backend.bulkUpsert(batch);
		}

		long start = System.nanoTime();
		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("obs")
		        .filter(Filter.patientScope("patient-7")).limit(10).build());
		long elapsedMs = (System.nanoTime() - start) / 1_000_000L;

		assertNotNull(result);
		// Loose bound: per-patient query at 5k corpus should complete in well under five seconds
		// even on a slow CI runner. Real assertion is "didn't full-scan" — wall time is the cheapest
		// proxy without parsing EXPLAIN output.
		assertTrue("patient-scoped query took " + elapsedMs + "ms; expected <5000ms", elapsedMs < 5000);
	}

	@Test
	public void wildcardSearchHitsOnDiskTablesFromPriorSessions() {
		// Mysql-side regression guard for the same bug fixed in LuceneBackendStore:
		// "if known is empty, listAllTables" short-circuits silently dropped any type whose
		// table existed on disk but whose `ensureTable` hadn't fired in the current JVM. Once
		// any single type was touched this session, every other on-disk type fell out of
		// wildcard reads. Concrete failure: chartsearchai/QueryStoreChartBuilder.searchByPatient
		// returned 1 hit instead of 7 for a patient with cross-type data, after the bridge
		// UserContext fix wired a single type via AOP.
		QueryDocument priorObs = doc("obs", "patient-cross-session", "shared keyword obs", null);
		assertTrue(backend.upsert(priorObs).isSucceeded());

		// Fresh backend → fresh empty knownTables cache. Simulates "JVM restart with obs already
		// on disk." Touching condition this session populates the cache with only condition; the
		// obs table on disk must still be visible to a wildcard search.
		MysqlBackendStore secondSession = new MysqlBackendStore(sessionFactory);
		secondSession.ensureSchema("condition", SchemaSpec.builder(8).build());
		QueryDocument freshCondition = doc("condition", "patient-cross-session",
		        "shared keyword condition", null);
		assertTrue(secondSession.upsert(freshCondition).isSucceeded());

		SearchResult result = secondSession.bm25(SearchRequest.builder()
		        .queryText("shared")
		        .filter(Filter.patientScope("patient-cross-session"))
		        .limit(10).build());
		Set<String> hitTypes = new HashSet<>();
		for (Hit h : result.getHits()) {
			hitTypes.add(h.getDocument().getResourceType());
		}
		assertEquals("wildcard must include the on-disk obs table not opened this session",
		        new HashSet<>(Arrays.asList("obs", "condition")), hitTypes);
		assertTrue("existsByPatient must also see the on-disk obs row",
		        secondSession.existsByPatient("patient-cross-session"));
	}

	@Test
	public void wildcardSearchSkipsBootstrapStateTable() {
		// Regression for #11. querystore_bootstrap_progress matches the querystore_% metadata probe
		// but lacks resource_uuid / patient_uuid, so a wildcard read that enumerates it and issues
		// per-type-index SQL crashes with SQLSyntaxErrorException. listAllTables() must filter it
		// out. A fresh MysqlBackendStore guarantees an empty knownTables cache, forcing the
		// metadata-probe fallback path the bug surfaces in.
		createBootstrapProgressTable();
		try {
			MysqlBackendStore freshBackend = new MysqlBackendStore(sessionFactory);
			// UUID guarantees the query doesn't match any fixture in this class's shared container,
			// so isEmpty() pins the filter's behavior rather than depending on absent-token luck.
			SearchResult result = freshBackend.bm25(
			    SearchRequest.builder().queryText(UUID.randomUUID().toString()).limit(10).build());
			assertNotNull(result);
			assertTrue("bookkeeping table must be filtered, not silently leaked into wildcard hits",
			    result.getHits().isEmpty());
		}
		finally {
			dropBootstrapProgressTable();
		}
	}

	// ---------- helpers ----------

	// Minimal subset of the real liquibase schema — the table only needs to exist for the
	// metadata probe to enumerate it. Don't extend ad-hoc; if a test ever needs to insert rows,
	// share BootstrapIntegrationTest.PROGRESS_DDL instead.
	private static void createBootstrapProgressTable() {
		JdbcSupport.inTransaction(sessionFactory, conn -> {
			try (Statement stmt = conn.createStatement()) {
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS querystore_bootstrap_progress ("
				        + "  resource_type VARCHAR(64) NOT NULL,"
				        + "  status VARCHAR(16) NOT NULL,"
				        + "  documents_indexed BIGINT NOT NULL DEFAULT 0,"
				        + "  PRIMARY KEY (resource_type)"
				        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
			}
		});
	}

	private static void dropBootstrapProgressTable() {
		JdbcSupport.inTransaction(sessionFactory, conn -> {
			try (Statement stmt = conn.createStatement()) {
				stmt.executeUpdate("DROP TABLE IF EXISTS querystore_bootstrap_progress");
			}
		});
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
}
