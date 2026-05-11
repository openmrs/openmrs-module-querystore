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

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmrs.module.querystore.backend.BackendCapabilities;
import org.openmrs.module.querystore.backend.BulkWriteResult;
import org.openmrs.module.querystore.backend.Filter;
import org.openmrs.module.querystore.backend.SchemaSpec;
import org.openmrs.module.querystore.backend.SearchRequest;
import org.openmrs.module.querystore.backend.SearchResult;
import org.openmrs.module.querystore.backend.WriteResult;
import org.openmrs.module.querystore.model.QueryDocument;
import org.testcontainers.containers.MySQLContainer;

import com.mysql.cj.jdbc.MysqlDataSource;

/**
 * Integration test for {@link MysqlBackendStore} against a real MySQL 8 server via Testcontainers.
 * Activated by the {@code integration} Maven profile (run with {@code mvn test -Pintegration} or
 * {@code mvn test -Dintegration}). Skipped by default — H2 cannot fake MySQL FULLTEXT, and the
 * BM25 + idempotency guarantees the SPI commits to need a real engine to validate.
 */
public class MysqlBackendStoreIntegrationTest {

	private static MySQLContainer<?> mysql;

	private static DataSource dataSource;

	private static MysqlBackendStore backend;

	@BeforeClass
	public static void startContainer() {
		mysql = new MySQLContainer<>("mysql:8.0").withDatabaseName("openmrs_test").withUsername("test")
		        .withPassword("test");
		mysql.start();

		MysqlDataSource ds = new MysqlDataSource();
		ds.setURL(mysql.getJdbcUrl());
		ds.setUser(mysql.getUsername());
		ds.setPassword(mysql.getPassword());
		dataSource = ds;
		backend = new MysqlBackendStore(dataSource);
	}

	@AfterClass
	public static void stopContainer() {
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

	// ---------- helpers ----------

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
