/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.backend.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmrs.module.querystore.QueryStoreConstants;
import org.openmrs.module.querystore.backend.BackendCapabilities;
import org.openmrs.module.querystore.backend.BulkWriteResult;
import org.openmrs.module.querystore.backend.Filter;
import org.openmrs.module.querystore.backend.SchemaSpec;
import org.openmrs.module.querystore.backend.SearchRequest;
import org.openmrs.module.querystore.backend.SearchResult;
import org.openmrs.module.querystore.model.QueryDocument;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

/**
 * Integration test for {@link ElasticsearchBackendStore} against a real ES 8.13 server via
 * Testcontainers. Activated by the {@code integration} Maven profile (run with
 * {@code mvn test -Pintegration} or {@code mvn test -Dintegration}). Skipped by default — the
 * SPI's durable-and-visible + conditional-upsert + native-kNN guarantees need a real cluster to
 * validate.
 */
public class ElasticsearchBackendStoreIntegrationTest {

	private static final String IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:8.13.4";

	private static ElasticsearchContainer es;

	private static ElasticsearchClientFactory clientFactory;

	private static ElasticsearchBackendStore backend;

	@BeforeClass
	public static void startContainer() {
		es = new ElasticsearchContainer(DockerImageName.parse(IMAGE))
		        // Single-node mode + security off: tests don't exercise auth and 8.x defaults to
		        // security on + TLS-on-loopback, which would force the test to either trust a
		        // generated cert or wire credentials. The production backend keeps the security
		        // surface intact via the URI runtime property (a real deployment supplies an HTTPS
		        // URI with credentials embedded). The test bypasses that path on purpose.
		        .withEnv("xpack.security.enabled", "false")
		        .withEnv("discovery.type", "single-node")
		        // Modest heap cap so CI runners with constrained RAM don't OOM the JVM.
		        .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");
		es.start();

		clientFactory = new ElasticsearchClientFactory("http://" + es.getHttpHostAddress());
		backend = new ElasticsearchBackendStore(clientFactory);
	}

	@AfterClass
	public static void stopContainer() {
		if (backend != null) {
			backend.close();
		}
		if (es != null) {
			es.stop();
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

		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("glucose")
		        .limit(10).filter(Filter.patientScope("patient-A")).build());

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
	public void bm25_matchesOnSynonymsField() {
		// ADR Decision 6: synonyms BM25-indexed as a top-level companion of text on ES so an
		// alternate-term query surfaces docs whose stored text uses the preferred name. Without
		// the synonyms field, "HTN" would not find this doc — its citation-clean text is
		// "Hypertension".
		QueryDocument doc = doc("condition", "patient-A", "Hypertension", null);
		doc.putMetadata(org.openmrs.module.querystore.QueryStoreConstants.FIELD_SYNONYMS,
		    Arrays.asList("HTN", "High blood pressure"));
		assertTrue(backend.upsert(doc).isSucceeded());

		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("condition").queryText("HTN")
		        .filter(Filter.patientScope("patient-A")).limit(10).build());

		assertEquals(1, result.getHits().size());
		assertEquals(doc.getResourceUuid(), result.getHits().get(0).getDocument().getResourceUuid());
	}

	@Test
	public void deleteIsIdempotent() {
		String uuid = UUID.randomUUID().toString();
		assertTrue(backend.delete("obs", uuid).isSucceeded());
		assertTrue(backend.delete("obs", uuid).isSucceeded());
	}

	@Test
	public void countByType_countsIndexedDocs_andZeroForUnpopulatedType() {
		// #3 drift "indexed" count: live per-type doc count, 0 for a type with no index (distinct from
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
		QueryDocument doc = doc("obs", "patient-A", "Pulse: 72", null);
		backend.upsert(doc);
		backend.delete("obs", doc.getResourceUuid());

		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("Pulse")
		        .filter(Filter.patientScope("patient-A")).limit(10).build());
		assertEquals(0, result.getHits().size());
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
		// ordered by record_date desc with (resource_type, resource_uuid) tie-breaker. The ES
		// backend uses a single wildcard search sorted ES-side by record_date desc with _doc asc as
		// the deterministic secondary key; this test pins both the cross-type completeness and the
		// CHART_ORDER re-sort that aligns the ES tier byte-for-byte with MySQL and Lucene.
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
	public void knnRetrievesStoredEmbedding() {
		float[] vector = new float[] { 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f };
		QueryDocument doc = doc("obs", "patient-A", "stored", vector);
		backend.upsert(doc);

		SearchResult result = backend.knn(SearchRequest.builder().resourceType("obs")
		        .queryVector(vector).limit(1).filter(Filter.patientScope("patient-A")).build());

		assertEquals(1, result.getHits().size());
		float[] roundTripped = result.getHits().get(0).getDocument().getEmbedding();
		assertNotNull(roundTripped);
		assertEquals(vector.length, roundTripped.length);
		for (int i = 0; i < vector.length; i++) {
			assertEquals(vector[i], roundTripped[i], 0.0001f);
		}
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
		// Duplicate-event delivery: the same save event arrives twice and both upserts carry the
		// entity's current dateChanged as last_modified. The external_gte semantics (>= not >) must
		// let the second write apply so the operation is naturally idempotent across redeliveries.
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
	public void wildcardSearchHitsAllRegisteredTypes() {
		backend.upsert(doc("obs", "patient-A", "Temperature: 38.5", null));
		backend.upsert(doc("condition", "patient-A", "Type 2 Diabetes", null));

		// The point is that the call resolves querystore_* without naming a type. Only the condition
		// doc contains the term "diabetes", so the wildcard correctly narrows to one hit.
		SearchResult result = backend.bm25(SearchRequest.builder().queryText("diabetes").limit(10).build());
		assertEquals(1, result.getHits().size());
		assertEquals("condition", result.getHits().get(0).getDocument().getResourceType());
	}

	@Test
	public void bulkUpsertCountsPerDocumentOutcomes() {
		// Embedding intentionally null: ES `similarity: cosine` rejects zero-magnitude vectors at
		// parse time, and this test only exercises the bulk write counter — it does not query kNN.
		// Tests that exercise kNN must supply a non-zero vector.
		List<QueryDocument> batch = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			batch.add(doc("obs", "patient-A", "Sample " + i, null));
		}
		BulkWriteResult res = backend.bulkUpsert(batch);
		assertEquals(5, res.getTotalRequested());
		assertEquals(5, res.getSucceeded());
		assertFalse(res.hasFailures());
	}

	@Test
	public void bulkUpsertRejectsInvalidInput() {
		// validate() runs over the whole list before any ES call, so an invalid doc raises
		// IllegalArgumentException before any partial write reaches the cluster.
		QueryDocument valid = doc("obs", "patient-A", "valid", null);
		QueryDocument invalid = new QueryDocument();
		invalid.setResourceType("obs");
		// Missing resourceUuid and patientUuid — validate() must trip.
		try {
			backend.bulkUpsert(Arrays.asList(valid, invalid));
			fail("bulkUpsert must reject the whole batch when any doc lacks required identity fields");
		}
		catch (IllegalArgumentException expected) {
			// expected
		}
		// Confirm the valid doc was not written — fail-fast must precede any ES call.
		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("valid")
		        .filter(Filter.patientScope("patient-A")).limit(10).build());
		assertEquals(0, result.getHits().size());
	}

	@Test
	public void bulkUpsertHonoursConditionalUpsertPerItem() {
		// Mixed-version bulk: a stale write inside a bulk must be swallowed (409 → success per SPI),
		// not surface as a per-doc failure. Covers a code path that single-upsert tests don't reach.
		String uuid = UUID.randomUUID().toString();
		Instant t1 = Instant.parse("2025-03-15T09:00:00Z");
		Instant t2 = t1.plus(1, ChronoUnit.HOURS);

		QueryDocument fresh = doc("obs", "patient-A", "Fresh", null);
		fresh.setResourceUuid(uuid);
		fresh.setLastModified(t2);
		assertTrue(backend.upsert(fresh).isSucceeded());

		QueryDocument stale = doc("obs", "patient-A", "Stale", null);
		stale.setResourceUuid(uuid);
		stale.setLastModified(t1);
		QueryDocument other = doc("obs", "patient-A", "Other", null);

		BulkWriteResult res = backend.bulkUpsert(Arrays.asList(stale, other));
		assertEquals(2, res.getTotalRequested());
		assertEquals("409 conflict on the stale doc counts as success per SPI invariant",
		    2, res.getSucceeded());
		assertFalse(res.hasFailures());

		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("Fresh")
		        .filter(Filter.patientScope("patient-A")).limit(10).build());
		assertEquals(1, result.getHits().size());
		assertEquals("fresh version must survive a stale bulk write",
		    "Fresh", result.getHits().get(0).getDocument().getText());
	}

	@Test
	public void searchOnNeverWrittenTypeReturnsZeroHits() {
		// Parity with Lucene/MySQL: querying a type whose index hasn't been created returns an
		// empty result, not an exception. ES achieves this via allow_no_indices + ignore_unavailable
		// on the search request.
		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("visit").queryText("anything")
		        .limit(10).build());
		assertEquals(0, result.getHits().size());
	}

	@Test
	public void bulkDeleteRemovesDocuments() {
		QueryDocument a = doc("obs", "patient-A", "Vital A", null);
		QueryDocument b = doc("obs", "patient-A", "Vital B", null);
		backend.upsert(a);
		backend.upsert(b);

		BulkWriteResult res = backend.bulkDelete("obs", Arrays.asList(a.getResourceUuid(), b.getResourceUuid()));
		assertEquals(2, res.getTotalRequested());
		assertEquals(2, res.getSucceeded());

		SearchResult remaining = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("Vital")
		        .filter(Filter.patientScope("patient-A")).limit(10).build());
		assertEquals(0, remaining.getHits().size());
	}

	@Test
	public void hybridFusesBm25AndKnnViaSpiDefault() {
		// v1 ships the default-method shape: backend.hybrid() runs bm25 + knn + RRF on the JVM
		// side. ES is permitted to override with native RRF when measurable benefit justifies it
		// (Decision 3 SPI sub-point 2). Empty corpus + no matches → empty result, no throw.
		float[] queryVector = new float[8];
		queryVector[0] = 1.0f; // cosine similarity rejects a zero-magnitude query vector
		SearchResult result = backend.hybrid(
		    SearchRequest.builder().queryText("anything").queryVector(queryVector).limit(5).build());
		assertEquals(0, result.getHits().size());
	}

	@Test
	public void capabilitiesReportElasticsearchTier() {
		BackendCapabilities caps = backend.capabilities();
		assertTrue(caps.supportsKnn());
		assertFalse("v1 ships parity with Lucene/MySQL — SPI-default RRF, no native RRF override",
		    caps.supportsHybridNative());
		assertTrue("ES is the only tier with cross-patient kNN at multi-million scale",
		    caps.supportsCrossPatientKnnAtScale());
		assertTrue(caps.getRecommendedMaxCorpusSize() > 0);
		assertTrue(caps.getSupportedFilters().contains(Filter.Kind.PATIENT_SCOPE));
	}

	@Test
	public void deleteSchemaRemovesIndex() throws Exception {
		backend.deleteSchema("obs");
		ElasticsearchClient client = clientFactory.getClient();
		boolean exists = client.indices()
		        .exists(e -> e.index(QueryStoreConstants.INDEX_PREFIX + "obs")).value();
		assertFalse("querystore_obs should not exist after deleteSchema", exists);
	}

	@Test
	public void ensureSchemaWithMismatchedEmbeddingDimensionsThrows() {
		try {
			backend.ensureSchema("obs", SchemaSpec.builder(16).build());
			fail("dim mismatch should be loud — Decision 8 treats model swaps as re-index events");
		}
		catch (IllegalStateException expected) {
			assertTrue("error message should reference dims",
			    expected.getMessage() != null && expected.getMessage().contains("dims="));
		}
	}

	@Test
	public void upsertCreatesIndexWithKeywordPatientUuidWhenSchemaAbsent() {
		// Writing to a never-ensured index lets ES dynamic mapping type patient_uuid as `text`,
		// against which the bare-field `term` filter silently matches zero docs. upsert must
		// install the explicit `keyword` mapping before the first write.
		backend.deleteSchema("obs");

		QueryDocument doc = doc("obs", "patient-A", "Fasting blood glucose: 11.2 mmol/L",
		        new float[] { 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f });
		assertTrue(backend.upsert(doc).isSucceeded());

		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("glucose")
		        .limit(10).filter(Filter.patientScope("patient-A")).build());
		assertEquals("patient_uuid must be a keyword field so the patient-scope term filter matches",
		    1, result.getHits().size());
		assertEquals("patient-A", result.getHits().get(0).getDocument().getPatientUuid());
	}

	@Test
	public void bulkUpsertCreatesIndexWithKeywordPatientUuidWhenSchemaAbsent() {
		// Bulk-write parity for the same dynamic-mapping trap covered by the single-write test:
		// executeBulkUpsert must also install the explicit `keyword` mapping before the first write.
		backend.deleteSchema("obs");

		List<QueryDocument> batch = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			batch.add(doc("obs", "patient-A", "Sample " + i,
			        new float[] { 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f }));
		}
		BulkWriteResult res = backend.bulkUpsert(batch);
		assertEquals(3, res.getSucceeded());

		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("Sample")
		        .limit(10).filter(Filter.patientScope("patient-A")).build());
		assertEquals("bulk-written docs must be reachable by patient-scope filter",
		    3, result.getHits().size());
	}

	@Test
	public void patientScopedReadIsSubLinear() {
		// Embedding null: see bulkUpsertCountsPerDocumentOutcomes — cosine rejects zero magnitude
		// and this test only exercises BM25 patient-scope pushdown.
		for (int p = 0; p < 50; p++) {
			List<QueryDocument> batch = new ArrayList<>();
			for (int i = 0; i < 100; i++) {
				batch.add(doc("obs", "patient-" + p, "obs " + i, null));
			}
			backend.bulkUpsert(batch);
		}

		long start = System.nanoTime();
		SearchResult result = backend.bm25(SearchRequest.builder().resourceType("obs").queryText("obs")
		        .filter(Filter.patientScope("patient-7")).limit(10).build());
		long elapsedMs = (System.nanoTime() - start) / 1_000_000L;

		assertNotNull(result);
		// Loose bound: 5k corpus, single-patient narrow filter; well under five seconds on any
		// reasonable runner. The assertion is "didn't full-scan-blow-up", wall time is the proxy.
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
