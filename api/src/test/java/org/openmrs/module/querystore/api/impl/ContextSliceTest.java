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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.module.querystore.QueryStoreConstants;
import org.openmrs.module.querystore.backend.BackendCapabilities;
import org.openmrs.module.querystore.backend.BackendStore;
import org.openmrs.module.querystore.backend.BulkWriteResult;
import org.openmrs.module.querystore.backend.DocFailure;
import org.openmrs.module.querystore.backend.HealthStatus;
import org.openmrs.module.querystore.backend.Hit;
import org.openmrs.module.querystore.backend.SchemaSpec;
import org.openmrs.module.querystore.backend.SearchRequest;
import org.openmrs.module.querystore.backend.SearchResult;
import org.openmrs.module.querystore.backend.WriteResult;
import org.openmrs.module.querystore.model.ContextSlice;
import org.openmrs.module.querystore.model.ContextSliceRecord;
import org.openmrs.module.querystore.model.ContextSliceRequest;
import org.openmrs.module.querystore.model.QueryDocument;

/**
 * {@link QueryStoreServiceImpl#getContextSlice} — the tiered record-selection contract (ADR
 * Decision 17). One implementation of the provider-neutral {@code context_policy} selection
 * invariants (dual-provider-conformance.v1), tested through the real composed service path
 * with a scripted backend. Fixture ids from the conformance manifest are cited per test.
 */
public class ContextSliceTest {

	private static final String PATIENT = "pat-1";

	private FakeBackendStore backend;

	private QueryStoreServiceImpl service;

	private static QueryDocument doc(String type, String uuid, String text, LocalDate date) {
		QueryDocument d = new QueryDocument();
		d.setResourceType(type);
		d.setResourceUuid(uuid);
		d.setPatientUuid(PATIENT);
		d.setText(text);
		d.setDate(date);
		return d;
	}

	private static QueryDocument condition(String uuid, String text, LocalDate date, String status) {
		QueryDocument d = doc("condition", uuid, text, date);
		d.putMetadata(QueryStoreConstants.FIELD_CLINICAL_STATUS, status);
		return d;
	}

	@BeforeEach
	public void setUp() {
		backend = new FakeBackendStore();
		service = new QueryStoreServiceImpl();
		service.setBackend(backend);
		// Chart in record_date-desc order (Decision 15 ordering), one of each policy shape.
		backend.chart = new ArrayList<QueryDocument>(Arrays.asList(
		        doc("patient", "p-1", "Patient: Jane Doe", LocalDate.of(2026, 7, 1)),
		        doc("obs", "recent-1", "Pulse: 80 bpm", LocalDate.of(2026, 6, 30)),
		        doc("visit", "visit-1", "Visit: Adult follow-up", LocalDate.of(2026, 6, 29)),
		        doc("drug_order", "med-1", "Drug order: Lisinopril 10 mg", LocalDate.of(2026, 6, 20)),
		        doc("drug_order", "med-2", "Drug order: Metformin 500 mg", LocalDate.of(2026, 6, 10)),
		        condition("cond-active", "Condition: Hypertension", LocalDate.of(2026, 5, 1), "ACTIVE"),
		        condition("cond-old", "Condition: Sprained ankle", LocalDate.of(2024, 5, 1), "INACTIVE"),
		        doc("allergy", "allergy-1", "Allergy: Penicillin. Reaction: rash", LocalDate.of(2024, 4, 1)),
		        doc("obs", "noise-1", "Shoe size: 42", LocalDate.of(2023, 1, 1))));
	}

	private static Map<String, String> tiersByUuid(ContextSlice slice) {
		Map<String, String> tiers = new LinkedHashMap<String, String>();
		for (ContextSliceRecord record : slice.getRecords()) {
			tiers.put(record.getDocument().getResourceUuid(), record.getTier());
		}
		return tiers;
	}

	@Test
	public void mandatoryCore_ridesEverySlice_regardlessOfTypesAndSimilarity() {
		// Fixture context.enumerated-medications-are-complete: mandatory_ids [patient, allergy-1]
		// plus typed_complete_ids [med-1, med-2] for a medication question — with NO similarity
		// signal, so mandatory/typed status is the only way in.
		ContextSliceRequest request = new ContextSliceRequest(
		        new HashSet<String>(Collections.singletonList("drug_order")), false);

		ContextSlice slice = service.getContextSlice(PATIENT, "What medications is the patient on?", request);

		Map<String, String> tiers = tiersByUuid(slice);
		assertEquals(QueryStoreConstants.TIER_MANDATORY, tiers.get("p-1"));
		assertEquals(QueryStoreConstants.TIER_MANDATORY, tiers.get("allergy-1"));
		assertEquals(QueryStoreConstants.TIER_MANDATORY, tiers.get("cond-active"));
		assertEquals(QueryStoreConstants.TIER_TYPED, tiers.get("med-1"));
		assertEquals(QueryStoreConstants.TIER_TYPED, tiers.get("med-2"));
		assertFalse(tiers.containsKey("cond-old"),
		        "an INACTIVE condition is not mandatory core; got " + tiers);
		assertFalse(tiers.containsKey("noise-1"),
		        "records outside every tier stay excluded; got " + tiers);
	}

	@Test
	public void temporalRequest_addsTheRecencyAnchor() {
		// Fixture context.temporal-adds-recency-anchor: recency_anchor_ids [visit-1] must be
		// included for a temporal question even with no types and no similarity signal.
		ContextSliceRequest request = new ContextSliceRequest(Collections.<String> emptySet(), true);
		request.setRecencyAnchorSize(3);

		ContextSlice slice = service.getContextSlice(PATIENT, "When was the most recent visit?", request);

		Map<String, String> tiers = tiersByUuid(slice);
		assertTrue(tiers.containsKey("visit-1"),
		        "the recent visit rides the recency anchor; got " + tiers);
		assertEquals(QueryStoreConstants.TIER_RECENCY_ANCHOR, tiers.get("visit-1"));
		assertEquals(QueryStoreConstants.TIER_RECENCY_ANCHOR, tiers.get("recent-1"));
		// The patient record is inside the anchor window but mandatory wins tier priority.
		assertEquals(QueryStoreConstants.TIER_MANDATORY, tiers.get("p-1"));
	}

	@Test
	public void nonTemporalRequest_addsNoAnchorRecords() {
		// Fixture context.non-temporal-does-not-fill-budget: a non-temporal question must not
		// pull recent-but-unrelated records into the slice just because they are recent.
		ContextSliceRequest request = new ContextSliceRequest(
		        new HashSet<String>(Collections.singletonList("drug_order")), false);

		ContextSlice slice = service.getContextSlice(PATIENT, "What medications is the patient on?", request);

		Map<String, String> tiers = tiersByUuid(slice);
		assertFalse(tiers.containsKey("recent-1"),
		        "no anchor for non-temporal questions — recency is not a fill signal; got " + tiers);
		assertFalse(tiers.containsKey("visit-1"), "unrelated recent visit stays out; got " + tiers);
	}

	@Test
	public void similarityHits_joinTheSlice_andFailureDegradesToPolicyTiers() {
		backend.hits = Collections.singletonList(
		        doc("obs", "noise-1", "Shoe size: 42", LocalDate.of(2023, 1, 1)));
		ContextSliceRequest request = new ContextSliceRequest(Collections.<String> emptySet(), false);

		ContextSlice slice = service.getContextSlice(PATIENT, "anything about shoes?", request);
		assertEquals(QueryStoreConstants.TIER_SIMILARITY, tiersByUuid(slice).get("noise-1"));

		backend.throwOnSearch = true;
		ContextSlice degraded = service.getContextSlice(PATIENT, "anything about shoes?", request);
		Map<String, String> tiers = tiersByUuid(degraded);
		assertFalse(tiers.containsKey("noise-1"), "similarity failure drops only that tier");
		assertTrue(tiers.containsKey("allergy-1"),
		        "policy tiers survive a similarity failure; got " + tiers);
	}

	@Test
	public void blankQuestion_skipsTheSimilaritySearch() {
		ContextSliceRequest request = new ContextSliceRequest(Collections.<String> emptySet(), false);

		service.getContextSlice(PATIENT, "   ", request);

		assertEquals(0, backend.hybridCount.get(), "blank question has no ranking signal — no search RPC");
	}

	@Test
	public void obsGroupFamilies_completeWholePanels() {
		// Panel-completion invariant (context_policy): when similarity lands the PANEL PARENT,
		// every member obs joins — the values live in members whose text has no panel name.
		QueryDocument parent = doc("obs", "panel-1", "Basic metabolic panel", LocalDate.of(2026, 6, 15));
		QueryDocument member1 = doc("obs", "member-1", "Serum sodium: 140 mmol/L", LocalDate.of(2026, 6, 15));
		member1.putMetadata(QueryStoreConstants.FIELD_OBS_GROUP_UUID, "panel-1");
		QueryDocument member2 = doc("obs", "member-2", "Serum potassium: 4.1 mmol/L", LocalDate.of(2026, 6, 15));
		member2.putMetadata(QueryStoreConstants.FIELD_OBS_GROUP_UUID, "panel-1");
		backend.chart.addAll(Arrays.asList(parent, member1, member2));
		backend.hits = Collections.singletonList(parent);
		ContextSliceRequest request = new ContextSliceRequest(Collections.<String> emptySet(), false);

		ContextSlice slice = service.getContextSlice(PATIENT, "results of the last BMP?", request);

		Map<String, String> tiers = tiersByUuid(slice);
		assertEquals(QueryStoreConstants.TIER_SIMILARITY, tiers.get("panel-1"));
		assertEquals(QueryStoreConstants.TIER_PANEL, tiers.get("member-1"));
		assertEquals(QueryStoreConstants.TIER_PANEL, tiers.get("member-2"));
	}

	@Test
	public void sliceKeepsChartOrder_andEachRecordAppearsOnce() {
		// Stable-ordering invariant (context_policy): chart record_date-desc order, dedup by
		// uuid, tier = highest-priority match (mandatory > anchor > typed > similarity).
		backend.hits = Collections.singletonList(
		        doc("allergy", "allergy-1", "Allergy: Penicillin. Reaction: rash", LocalDate.of(2024, 4, 1)));
		ContextSliceRequest request = new ContextSliceRequest(
		        new HashSet<String>(Collections.singletonList("allergy")), true);
		request.setRecencyAnchorSize(2);

		ContextSlice slice = service.getContextSlice(PATIENT, "most recent allergy reactions?", request);

		List<String> uuids = new ArrayList<String>(tiersByUuid(slice).keySet());
		assertEquals(new HashSet<String>(uuids).size(), uuids.size(), "no duplicates: " + uuids);
		// Chart order: p-1 (7/1) before recent-1 (6/30) before cond-active (5/1) before allergy-1 (4/2).
		assertTrue(uuids.indexOf("p-1") < uuids.indexOf("recent-1"), "chart order kept: " + uuids);
		assertTrue(uuids.indexOf("recent-1") < uuids.indexOf("allergy-1"), "chart order kept: " + uuids);
		// allergy-1 matched mandatory AND typed AND similarity — the highest priority wins.
		assertEquals(QueryStoreConstants.TIER_MANDATORY, tiersByUuid(slice).get("allergy-1"));
	}

	@Test
	public void chartTruncationAtTheBackendCap_isSurfacedNotSilent() {
		// Explicit-overflow invariant: the ES tier caps getPatientChart at its most-recent
		// 10 000 documents — a slice built on a capped chart must say so.
		List<QueryDocument> big = new ArrayList<QueryDocument>();
		for (int i = 0; i < QueryStoreConstants.CONTEXT_CHART_CAP; i++) {
			big.add(doc("obs", "o-" + i, "Obs " + i, LocalDate.of(2026, 1, 1)));
		}
		backend.chart = big;
		ContextSliceRequest request = new ContextSliceRequest(Collections.<String> emptySet(), false);

		ContextSlice slice = service.getContextSlice(PATIENT, "anything?", request);

		assertTrue(slice.isChartTruncated(), "a capped chart must be flagged");
		assertEquals(QueryStoreConstants.CONTEXT_CHART_CAP, slice.getChartSize());

		backend.chart = backend.chart.subList(0, 100);
		assertFalse(service.getContextSlice(PATIENT, "anything?", request).isChartTruncated(),
		        "an uncapped chart is not flagged");
	}

	@Test
	public void interpretMode_derivesTypesAndTemporalServerSide() {
		// ADR Decision 18: with interpretQuestion, querystore derives the typed scope and the
		// temporal flag from the RAW question — both engines stop duplicating cue routing, so
		// interpretation cannot drift between them.
		ContextSliceRequest request = new ContextSliceRequest(Collections.<String> emptySet(), false);
		request.setInterpretQuestion(true);
		request.setRecencyAnchorSize(3);

		ContextSlice slice = service.getContextSlice(PATIENT,
		        "What medications is the patient currently on?", request);

		Map<String, String> tiers = tiersByUuid(slice);
		assertEquals(QueryStoreConstants.TIER_TYPED, tiers.get("med-1"));
		assertEquals(QueryStoreConstants.TIER_TYPED, tiers.get("med-2"));
		assertEquals(QueryStoreConstants.TIER_RECENCY_ANCHOR, tiers.get("recent-1"),
		        "\"currently\" is a temporal cue — the anchor applies; got " + tiers);
		assertTrue(slice.getEffectiveTypes().contains("drug_order"),
		        "the derived interpretation is traced on the slice; got " + slice.getEffectiveTypes());
		assertTrue(slice.isTemporalApplied());
	}

	@Test
	public void interpretMode_unionsCallerTypes_withDerivedOnes() {
		// Module-contributed scopes (chartsearchai QueryScopeContributor SPI) remain caller
		// additions — unioned with the derived types, never replaced.
		ContextSliceRequest request = new ContextSliceRequest(
		        new HashSet<String>(Collections.singletonList("billing_record")), false);
		request.setInterpretQuestion(true);

		ContextSlice slice = service.getContextSlice(PATIENT, "What medications is the patient on?", request);

		assertTrue(slice.getEffectiveTypes().contains("billing_record"),
		        "caller types survive; got " + slice.getEffectiveTypes());
		assertTrue(slice.getEffectiveTypes().contains("drug_order"),
		        "derived types join; got " + slice.getEffectiveTypes());
	}

	@Test
	public void similarityLeg_preprocessesTheQuestion_ownedByTheRetrievalStore() {
		// Retrieval-quality preprocessing (lab-panel expansion + stopword stripping) runs HERE,
		// at the owner of the index and embedder — callers send the raw question. Idempotent for
		// a caller that still preprocesses client-side.
		ContextSliceRequest request = new ContextSliceRequest(Collections.<String> emptySet(), false);

		service.getContextSlice(PATIENT, "results of the last BMP?", request);

		assertTrue(backend.lastQueryText.contains("basic metabolic panel"),
		        "the panel abbreviation must be expanded for retrieval; got " + backend.lastQueryText);
		assertFalse(backend.lastQueryText.contains("the "),
		        "stopwords must be stripped; got " + backend.lastQueryText);
	}

	/** Scripted backend: canned chart + canned similarity hits, everything else inert. */
	private static final class FakeBackendStore implements BackendStore {

		List<QueryDocument> chart = Collections.emptyList();

		List<QueryDocument> hits = Collections.emptyList();

		boolean throwOnSearch;

		final AtomicInteger hybridCount = new AtomicInteger();

		@Override
		public boolean existsByPatient(String patientUuid) {
			return true;
		}

		@Override
		public List<QueryDocument> findAllByPatient(String patientUuid) {
			return chart;
		}

		volatile String lastQueryText;

		@Override
		public SearchResult hybrid(SearchRequest req) {
			hybridCount.incrementAndGet();
			lastQueryText = req.getQueryText();
			if (throwOnSearch) {
				throw new RuntimeException("simulated backend search failure");
			}
			List<Hit> out = new ArrayList<Hit>();
			for (int i = 0; i < hits.size(); i++) {
				out.add(new Hit(hits.get(i), 1.0 - i * 0.1, i + 1));
			}
			return new SearchResult(out);
		}

		@Override
		public SearchResult bm25(SearchRequest req) {
			return hybrid(req);
		}

		@Override
		public SearchResult knn(SearchRequest req) {
			return SearchResult.empty();
		}

		@Override
		public void ensureSchema(String resourceType, SchemaSpec spec) {
		}

		@Override
		public void deleteSchema(String resourceType) {
		}

		@Override
		public WriteResult upsert(QueryDocument doc) {
			return WriteResult.success();
		}

		@Override
		public WriteResult delete(String resourceType, String resourceUuid) {
			return WriteResult.success();
		}

		@Override
		public BulkWriteResult bulkUpsert(List<QueryDocument> docs) {
			return new BulkWriteResult(docs.size(), docs.size(), Collections.<DocFailure> emptyList());
		}

		@Override
		public BulkWriteResult bulkDelete(String resourceType, List<String> uuids) {
			return new BulkWriteResult(0, 0, Collections.<DocFailure> emptyList());
		}

		@Override
		public BulkWriteResult bulkDeleteByPatient(String patientUuid) {
			return new BulkWriteResult(0, 0, Collections.<DocFailure> emptyList());
		}

		@Override
		public long countByType(String resourceType) {
			return chart.size();
		}

		@Override
		public BackendCapabilities capabilities() {
			return new BackendCapabilities(false, false, false, 1_000_000,
			        java.util.EnumSet.allOf(org.openmrs.module.querystore.backend.Filter.Kind.class));
		}

		@Override
		public HealthStatus health() {
			return new HealthStatus(HealthStatus.State.HEALTHY, null);
		}
	}
}
