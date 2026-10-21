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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * with a scripted backend. Each test loads its case directly from the local fixture
 * (src/test/resources/conformance), which carries only the cases these tests read, each unchanged
 * from the shared fixture set, and asserts against the fixture's own declared ids and expected
 * tiers, not hand-duplicated literals, so fixture and selector changes remain visible here.
 * Abstaining when the mandatory core overflows a token budget is not tested here: token-budget
 * enforcement is the answer engine's responsibility (see
 * {@link org.openmrs.module.querystore.model.ContextSliceRecord}), not querystore's.
 */
public class ContextSliceTest {

	private static final String PATIENT = "pat-1";

	private static JsonNode fixture;

	private FakeBackendStore backend;

	private QueryStoreServiceImpl service;

	private static synchronized JsonNode fixtureCase(String family, String id) {
		if (fixture == null) {
			try (InputStream is = ContextSliceTest.class
			        .getResourceAsStream("/conformance/dual-provider-conformance.v1.json")) {
				fixture = new ObjectMapper().readTree(is);
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		for (JsonNode candidate : fixture.path(family)) {
			if (id.equals(candidate.path("id").asText())) {
				return candidate;
			}
		}
		throw new IllegalArgumentException("No " + family + " fixture case '" + id + "'");
	}

	private static List<String> idList(JsonNode caseNode, String field) {
		List<String> out = new ArrayList<String>();
		for (JsonNode n : caseNode.path(field)) {
			out.add(n.asText());
		}
		return out;
	}

	private static String resourceTypeOf(List<QueryDocument> chart, String uuid) {
		for (QueryDocument d : chart) {
			if (uuid.equals(d.getResourceUuid())) {
				return d.getResourceType();
			}
		}
		throw new IllegalStateException("No chart document with uuid " + uuid);
	}

	private Set<String> resourceTypesOf(List<String> uuids) {
		Set<String> types = new HashSet<String>();
		for (String uuid : uuids) {
			types.add(resourceTypeOf(backend.chart, uuid));
		}
		return types;
	}

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

	/** The slice is built from the full chart and the similarity read, so it omits what they omit
	 *  for a caller missing a type's core read privilege (Decision 14 point 4), in every tier. */
	@Test
	public void aCallerWithoutATypesCorePrivilegeGetsNoneOfItsRecordsInAnyTier() {
		ContextSliceRequest request = new ContextSliceRequest(
		        new java.util.HashSet<String>(Arrays.asList("drug_order", "allergy")), false);
		org.openmrs.api.context.Context.setUserContext(new org.openmrs.api.context.UserContext(null) {
			@Override
			public boolean hasPrivilege(String privilege) {
				return !org.openmrs.util.PrivilegeConstants.GET_ALLERGIES.equals(privilege);
			}
		});
		ContextSlice slice;
		try {
			slice = service.getContextSlice(PATIENT, "Any drug allergies or medications?", request);
		}
		finally {
			org.openmrs.api.context.Context.clearUserContext();
		}

		Map<String, String> tiers = tiersByUuid(slice);
		assertFalse(tiers.containsKey("allergy-1"), "an allergy the caller cannot read reached the slice: " + tiers);
		assertTrue(tiers.containsKey("med-1"), "a readable typed record must still be served: " + tiers);
	}

	/** Left in the scope, a refused type would come back as an empty typed tier, identical down to
	 *  the sliceId to a patient with no such records; it is withheld and named instead. */
	@Test
	public void aTypeTheCallerCannotReadIsWithheldAndLeavesTheEffectiveScope() {
		ContextSliceRequest request = new ContextSliceRequest(
		        new java.util.HashSet<String>(Arrays.asList("drug_order", "allergy")), false);
		org.openmrs.api.context.Context.setUserContext(new org.openmrs.api.context.UserContext(null) {
			@Override
			public boolean hasPrivilege(String privilege) {
				return !org.openmrs.util.PrivilegeConstants.GET_ALLERGIES.equals(privilege);
			}
		});
		ContextSlice slice;
		try {
			slice = service.getContextSlice(PATIENT, "Any drug allergies or medications?", request);
		}
		finally {
			org.openmrs.api.context.Context.clearUserContext();
		}

		assertEquals(Collections.singleton("allergy"), slice.getWithheldTypes());
		assertEquals(Collections.singleton("drug_order"), slice.getEffectiveTypes());
	}

	/** The mandatory tier carries every allergy unasked, so a refused type must be named even when no
	 *  type was requested, or the slice reads as a patient with no allergies. */
	@Test
	public void aTypeTheCallerCannotReadIsNamedEvenWhenItWasNotRequested() {
		ContextSliceRequest request = new ContextSliceRequest(Collections.<String> emptySet(), false);
		org.openmrs.api.context.Context.setUserContext(new org.openmrs.api.context.UserContext(null) {
			@Override
			public boolean hasPrivilege(String privilege) {
				return !org.openmrs.util.PrivilegeConstants.GET_ALLERGIES.equals(privilege);
			}
		});
		ContextSlice slice;
		try {
			slice = service.getContextSlice(PATIENT, "What medications is she on?", request);
		}
		finally {
			org.openmrs.api.context.Context.clearUserContext();
		}

		assertEquals(Collections.singleton("allergy"), slice.getWithheldTypes());
	}

	/** A consumer reading the chart through another account learns from this what the caller is refused. */
	@Test
	public void theTypesWithheldFromTheCallerAreReportedWithoutAChartRead() {
		org.openmrs.api.context.Context.setUserContext(new org.openmrs.api.context.UserContext(null) {
			@Override
			public boolean hasPrivilege(String privilege) {
				return !org.openmrs.util.PrivilegeConstants.GET_ALLERGIES.equals(privilege)
				        && !org.openmrs.util.PrivilegeConstants.GET_ORDERS.equals(privilege);
			}
		});
		java.util.Set<String> withheld;
		try {
			withheld = service.getTypesWithheldFromCaller();
		}
		finally {
			org.openmrs.api.context.Context.clearUserContext();
		}

		assertEquals(Arrays.asList("allergy", "drug_order", "referral_order", "test_order"),
		    new java.util.ArrayList<String>(withheld));
	}

	/** A recurrence or relapse is active to core (TRUNK-6170), so it rides the mandatory tier too. */
	@Test
	public void mandatoryCore_carriesARecurringOrRelapsedCondition() {
		backend.chart.add(condition("cond-recurrence", "Condition: Tuberculosis", LocalDate.of(2025, 1, 1), "RECURRENCE"));
		backend.chart.add(condition("cond-relapse", "Condition: Malaria", LocalDate.of(2025, 2, 1), "RELAPSE"));

		Map<String, String> tiers = tiersByUuid(service.getContextSlice(PATIENT, "Shoe size?",
		    new ContextSliceRequest(Collections.<String> emptySet(), false)));

		assertEquals(QueryStoreConstants.TIER_MANDATORY, tiers.get("cond-recurrence"), "got " + tiers);
		assertEquals(QueryStoreConstants.TIER_MANDATORY, tiers.get("cond-relapse"), "got " + tiers);
		assertNotEquals(QueryStoreConstants.TIER_MANDATORY, tiers.get("cond-old"), "got " + tiers);
	}

	@Test
	public void mandatoryCore_ridesEverySlice_regardlessOfTypesAndSimilarity() {
		JsonNode fixtureCase = fixtureCase("context_policy", "context.enumerated-medications-are-complete");
		// The fixture's typed_complete_ids has a 3rd medication the shared setUp() chart doesn't
		// carry — add it here so the slice this test drives actually matches the fixture's claim.
		backend.chart.add(5, doc("drug_order", "med-3", "Drug order: Atorvastatin 20 mg", LocalDate.of(2026, 6, 5)));
		List<String> typedIds = idList(fixtureCase, "typed_complete_ids");
		ContextSliceRequest request = new ContextSliceRequest(resourceTypesOf(typedIds), false);

		ContextSlice slice = service.getContextSlice(PATIENT, "What medications is the patient on?", request);

		Map<String, String> tiers = tiersByUuid(slice);
		for (String id : idList(fixtureCase, "mandatory_ids")) {
			assertEquals(QueryStoreConstants.TIER_MANDATORY, tiers.get(id),
			        "fixture mandatory_ids: " + id + " must be mandatory; got " + tiers);
		}
		for (String id : typedIds) {
			assertEquals(QueryStoreConstants.TIER_TYPED, tiers.get(id),
			        "fixture typed_complete_ids: " + id + " must be typed; got " + tiers);
		}
		List<String> expectedIncluded = idList(fixtureCase, "expected_included");
		for (QueryDocument chartDoc : backend.chart) {
			String id = chartDoc.getResourceUuid();
			if (expectedIncluded.contains(id)) {
				assertTrue(tiers.containsKey(id), "fixture expected_included: " + id + " missing; got " + tiers);
			} else {
				assertFalse(tiers.containsKey(id),
				        "fixture expected_included excludes " + id + " but it was selected; got " + tiers);
			}
		}
	}

	@Test
	public void temporalRequest_addsTheRecencyAnchor() {
		JsonNode fixtureCase = fixtureCase("context_policy", "context.temporal-adds-recency-anchor");
		ContextSliceRequest request = new ContextSliceRequest(Collections.<String> emptySet(), true);
		request.setRecencyAnchorSize(3);

		ContextSlice slice = service.getContextSlice(PATIENT, "When was the most recent visit?", request);

		Map<String, String> tiers = tiersByUuid(slice);
		for (String id : idList(fixtureCase, "recency_anchor_ids")) {
			assertTrue(tiers.containsKey(id), "fixture recency_anchor_ids: " + id + " missing; got " + tiers);
			assertEquals(QueryStoreConstants.TIER_RECENCY_ANCHOR, tiers.get(id));
		}
		assertEquals(QueryStoreConstants.TIER_RECENCY_ANCHOR, tiers.get("recent-1"));
		// The patient record is inside the anchor window but mandatory wins tier priority.
		assertEquals(QueryStoreConstants.TIER_MANDATORY, tiers.get("p-1"));
	}

	@Test
	public void nonTemporalRequest_addsNoAnchorRecords() {
		JsonNode fixtureCase = fixtureCase("context_policy", "context.non-temporal-does-not-fill-budget");
		ContextSliceRequest request = new ContextSliceRequest(
		        new HashSet<String>(Collections.singletonList("drug_order")), false);

		ContextSlice slice = service.getContextSlice(PATIENT, "What medications is the patient on?", request);

		Map<String, String> tiers = tiersByUuid(slice);
		for (String id : idList(fixtureCase, "must_exclude_ids")) {
			assertFalse(tiers.containsKey(id),
			        "no anchor for non-temporal questions — recency is not a fill signal; got " + tiers);
		}
	}

	@Test
	public void explicitIdentifiersDatesCodesAndQuotedPhrasesAreRetained() {
		JsonNode fixtureCase = fixtureCase("context_policy", "context.explicit-signals-are-retained");
		QueryDocument identified = doc("visit", "11111111-1111-4111-8111-111111111111",
		        "Visit: historical review", LocalDate.of(2022, 2, 2));
		QueryDocument coded = doc("obs", "coded-1", "Laboratory result", LocalDate.of(2022, 1, 1));
		// As ConceptNameUtil.getMappingCodes writes it for a concept mapped to CIEL 12345.
		coded.putMetadata(QueryStoreConstants.FIELD_MAPPING_CODES,
		        Collections.singletonList("CIEL:12345"));
		backend.chart.add(identified);
		backend.chart.add(coded);
		ContextSliceRequest request = new ContextSliceRequest(Collections.<String> emptySet(), false);

		ContextSlice slice = service.getContextSlice(PATIENT, fixtureCase.path("question").asText(), request);

		Map<String, String> tiers = tiersByUuid(slice);
		for (String id : idList(fixtureCase, "exact_match_ids")) {
			assertEquals(fixtureCase.path("expected_tier").asText(), tiers.get(id),
			        "exact signal must retain " + id);
		}
	}

	@Test
	public void naturalLanguageCodeQuestionDoesNotInventAnExactIdentifier() {
		QueryDocument incidental = doc("obs", "incidental-1",
		        "Instructions for specimen collection", LocalDate.of(2022, 1, 1));
		backend.chart.add(incidental);

		ContextSlice slice = service.getContextSlice(PATIENT,
		        "What is the code for hemoglobin?",
		        new ContextSliceRequest(Collections.<String> emptySet(), false));

		assertFalse(tiersByUuid(slice).containsKey("incidental-1"),
		        "the word after an unlabeled natural-language 'code' question is not an identifier");
	}

	@Test
	public void aOneCharacterNamespacedCodeIsExactEvidence() {
		QueryDocument coded = doc("obs", "code-6", "Mapping codes: CIEL:6", LocalDate.of(2022, 1, 1));
		backend.chart.add(coded);

		ContextSlice slice = service.getContextSlice(PATIENT, "Compare CIEL:6",
		        new ContextSliceRequest(Collections.<String> emptySet(), false));

		assertEquals(QueryStoreConstants.TIER_EXACT, tiersByUuid(slice).get("code-6"));
	}

	/** The identifier boundary is for identifiers and codes: a quoted phrase is prose, and prose
	 *  joins words with slashes and hyphens ("nausea/vomiting", "COVID-19 vaccination"). */
	@Test
	public void aQuotedPhraseIsExactBesideAConnector() {
		backend.chart.add(doc("obs", "symptoms-1", "Symptoms: nausea/vomiting", LocalDate.of(2026, 3, 1)));
		backend.chart.add(doc("obs", "vaccine-1", "Immunization: COVID-19 vaccination", LocalDate.of(2026, 2, 1)));
		ContextSliceRequest request = new ContextSliceRequest(Collections.<String> emptySet(), false);

		Map<String, String> tiers = tiersByUuid(service.getContextSlice(PATIENT,
		        "Was \"vomiting\" or \"COVID\" recorded?", request));

		assertEquals(QueryStoreConstants.TIER_EXACT, tiers.get("symptoms-1"), "got " + tiers);
		assertEquals(QueryStoreConstants.TIER_EXACT, tiers.get("vaccine-1"), "got " + tiers);
	}

	@Test
	public void anIdentifierDoesNotMatchALongerIdentifierItBegins() {
		QueryDocument exact = doc("patient_identifier", "identifier-1", "Patient identifier: ABC-123",
		        LocalDate.of(2022, 1, 1));
		QueryDocument longer = doc("patient_identifier", "identifier-2", "Accession: ABC-123-4",
		        LocalDate.of(2022, 1, 1));
		QueryDocument code = doc("obs", "code-60", "Mapping codes: CIEL:60", LocalDate.of(2022, 1, 1));
		QueryDocument longerCode = doc("obs", "code-60-1", "Mapping codes: CIEL:60-1", LocalDate.of(2022, 1, 1));
		backend.chart.addAll(Arrays.asList(exact, longer, code, longerCode));

		ContextSlice identifiers = service.getContextSlice(PATIENT, "Find MRN: ABC-123",
		        new ContextSliceRequest(Collections.<String> emptySet(), false));
		ContextSlice codes = service.getContextSlice(PATIENT, "Compare CIEL:60",
		        new ContextSliceRequest(Collections.<String> emptySet(), false));

		assertEquals(QueryStoreConstants.TIER_EXACT, tiersByUuid(identifiers).get("identifier-1"));
		assertNotEquals(QueryStoreConstants.TIER_EXACT, tiersByUuid(identifiers).get("identifier-2"));
		assertEquals(QueryStoreConstants.TIER_EXACT, tiersByUuid(codes).get("code-60"));
		assertNotEquals(QueryStoreConstants.TIER_EXACT, tiersByUuid(codes).get("code-60-1"));
	}

	@Test
	public void labeledIdentifiersAreRetainedAsExactEvidence() {
		QueryDocument identified = doc("patient_identifier", "identifier-1",
		        "Patient identifier: ABC-123", LocalDate.of(2022, 1, 1));
		backend.chart.add(identified);

		ContextSlice colon = service.getContextSlice(PATIENT, "Find MRN: ABC-123",
		        new ContextSliceRequest(Collections.<String> emptySet(), false));
		ContextSlice sentence = service.getContextSlice(PATIENT, "The identifier is ABC-123",
		        new ContextSliceRequest(Collections.<String> emptySet(), false));

		assertEquals(QueryStoreConstants.TIER_EXACT, tiersByUuid(colon).get("identifier-1"));
		assertEquals(QueryStoreConstants.TIER_EXACT, tiersByUuid(sentence).get("identifier-1"));
	}

	/** "code is" before an ordinary word names no identifier: "the" would make every record that
	 *  contains it exact, the tier budgeted consumers protect. An identifier carries a digit. */
	@Test
	public void aWordAfterALabelIsNotAnIdentifierUnlessItCarriesADigit() {
		backend.chart.add(doc("obs", "the-1", "Note: the patient is stable", LocalDate.of(2022, 1, 1)));
		backend.chart.add(doc("obs", "loinc-1", "Lab code: 100-8 result", LocalDate.of(2022, 1, 2)));
		backend.chart.add(doc("diagnosis", "icd-1", "Diagnosis: E11.9 Type 2 diabetes", LocalDate.of(2022, 1, 3)));
		ContextSliceRequest none = new ContextSliceRequest(Collections.<String> emptySet(), false);

		assertFalse(tiersByUuid(service.getContextSlice(PATIENT, "What code is the latest diagnosis?", none))
		        .containsKey("the-1"), "\"the\" after \"code is\" is not an identifier");
		assertEquals(QueryStoreConstants.TIER_EXACT,
		        tiersByUuid(service.getContextSlice(PATIENT, "Which result has code: 100-8?", none)).get("loinc-1"));
		assertEquals(QueryStoreConstants.TIER_EXACT,
		        tiersByUuid(service.getContextSlice(PATIENT, "The code is E11.9", none)).get("icd-1"));
	}

	/** An apostrophe inside a word does not open a quoted phrase. */
	@Test
	public void aQuotedPhraseAfterAnApostropheIsStillExact() {
		backend.chart.add(doc("obs", "chest-1", "Chief complaint: chest pain on exertion", LocalDate.of(2022, 1, 1)));

		ContextSlice slice = service.getContextSlice(PATIENT, "Did the patient's notes mention 'chest pain'?",
		        new ContextSliceRequest(Collections.<String> emptySet(), false));

		assertEquals(QueryStoreConstants.TIER_EXACT, tiersByUuid(slice).get("chest-1"));
	}

	/** The slice reads the chart and then searches it. A cold touch that failed on the chart read
	 *  must not be run again by the search: each touch is a full serialize-and-embed pass. */
	@Test
	public void aFailedColdTouchRunsOncePerSlice() {
		backend.hasDocuments = false;
		AtomicInteger touches = new AtomicInteger();
		org.openmrs.module.querystore.bootstrap.BootstrapService bootstrap = org.mockito.Mockito
		        .mock(org.openmrs.module.querystore.bootstrap.BootstrapService.class);
		org.mockito.Mockito.doAnswer(invocation -> {
			touches.incrementAndGet();
			throw new IllegalStateException("embedder unavailable");
		}).when(bootstrap).ensureIndexed(PATIENT);
		service.setBootstrapServiceOverride(bootstrap);

		ContextSlice slice = service.getContextSlice(PATIENT, "Any recent labs?",
		        new ContextSliceRequest(Collections.<String> emptySet(), false));

		assertEquals(1, touches.get(), "the projection ran once for the slice");
		assertTrue(slice.isChartTruncated(), "the failed touch is still disclosed");
		assertEquals(1, backend.hybridCount.get(), "the similarity search still ran");
	}

	/** An opening single quote followed by a long span must not overflow the stack: each repetition
	 *  of an unbounded alternation is a recursive call in Java's regex engine. The slice runs on a
	 *  thread with Linux x64's default 1 MB stack, where 4,000 characters overflowed it. */
	@Test
	public void aLongSpanAfterAnOpeningSingleQuoteDoesNotOverflowTheStack() throws InterruptedException {
		for (String quote : Arrays.asList("'", "\u2018")) {
			assertSliceCompletesOnA1MbStack("Did the notes mention " + quote + "chest pain");
		}
	}

	private void assertSliceCompletesOnA1MbStack(String opening) throws InterruptedException {
		StringBuilder question = new StringBuilder(opening);
		while (question.length() < 4000) {
			question.append(" and more");
		}
		question.append("?");
		AtomicReference<Throwable> failure = new AtomicReference<>();
		AtomicReference<Map<String, String>> tiers = new AtomicReference<>();
		Thread slice = new Thread(null, () -> {
			try {
				tiers.set(tiersByUuid(service.getContextSlice(PATIENT, question.toString(),
				        new ContextSliceRequest(Collections.<String> emptySet(), false))));
			}
			catch (Throwable t) {
				failure.set(t);
			}
		}, "slice-1mb", 1024 * 1024);
		slice.start();
		slice.join();

		assertEquals(null, failure.get(), "the slice completed on a 1 MB stack after " + opening);
		assertFalse(tiers.get().isEmpty(), "the slice still carries its mandatory tier");
	}

	/** A visit's start and end are date-times; the date a question names matches the day part. */
	@Test
	public void aDateInTheQuestionMatchesADateTimeValue() {
		QueryDocument visit = doc("visit", "visit-ended", "Visit: Inpatient admission", LocalDate.of(2024, 3, 1));
		visit.putMetadata(QueryStoreConstants.FIELD_END_DATE_TIME, "2024-03-05T10:15:00");
		backend.chart.add(visit);

		Map<String, String> tiers = tiersByUuid(service.getContextSlice(PATIENT,
		        "What happened at the visit that ended 2024-03-05?",
		        new ContextSliceRequest(Collections.<String> emptySet(), false)));

		assertEquals(QueryStoreConstants.TIER_EXACT, tiers.get("visit-ended"), "got " + tiers);
	}

	/** A one-letter quoted word opens no phrase, and the closing quote of one phrase with the
	 *  opening quote of the next does not make the words between them a phrase: "and" would make
	 *  nearly every record exact, a tier the consumer must not drop. */
	@Test
	public void aQuotedPhraseNeedsTextInsideItsOwnQuotes() {
		backend.chart.add(doc("obs", "and-1", "Note: blood and urine sent", LocalDate.of(2022, 1, 1)));
		backend.chart.add(doc("drug_order", "bid-1", "Drug order: Amoxicillin 500 mg bid", LocalDate.of(2022, 1, 2)));

		Map<String, String> tiers = tiersByUuid(service.getContextSlice(PATIENT, "Did he take \"a\" and \"bid\"?",
		        new ContextSliceRequest(Collections.<String> emptySet(), false)));

		assertFalse(tiers.containsKey("and-1"), "the words between two quoted phrases are not a phrase: " + tiers);
		assertEquals(QueryStoreConstants.TIER_EXACT, tiers.get("bid-1"));
	}

	/** US style puts a comma or period inside the closing quote; it is not part of the phrase. */
	@Test
	public void punctuationInsideTheClosingQuoteIsNotPartOfThePhrase() {
		backend.chart.add(doc("obs", "chest-1", "Chief complaint: chest pain on exertion", LocalDate.of(2022, 1, 1)));
		backend.chart.add(doc("obs", "fever-1", "Symptom: fever", LocalDate.of(2022, 1, 2)));
		backend.chart.add(doc("obs", "cough-1", "Symptom: cough", LocalDate.of(2022, 1, 3)));
		backend.chart.add(doc("obs", "nausea-1", "Symptom: nausea", LocalDate.of(2022, 1, 4)));
		backend.chart.add(doc("obs", "headache-1", "Symptom: headache", LocalDate.of(2022, 1, 5)));

		Map<String, String> tiers = tiersByUuid(service.getContextSlice(PATIENT,
		        "Was \"chest pain,\" \u201Cfever,\u201D 'nausea.' \u2018headache,\u2019 or \"cough\" recorded?",
		        new ContextSliceRequest(Collections.<String> emptySet(), false)));

		assertEquals(QueryStoreConstants.TIER_EXACT, tiers.get("chest-1"), "got " + tiers);
		assertEquals(QueryStoreConstants.TIER_EXACT, tiers.get("fever-1"), "got " + tiers);
		assertEquals(QueryStoreConstants.TIER_EXACT, tiers.get("nausea-1"), "got " + tiers);
		assertEquals(QueryStoreConstants.TIER_EXACT, tiers.get("headache-1"), "got " + tiers);
		assertEquals(QueryStoreConstants.TIER_EXACT, tiers.get("cough-1"), "got " + tiers);
	}

	/** Each exact signal is scanned against every record, so a question's signals are capped at 32. */
	@Test
	public void aQuestionsExactSignalsAreCapped() {
		backend.chart.add(doc("encounter", "visit-32", "Visit on 2026-02-01", LocalDate.of(2026, 2, 1)));
		backend.chart.add(doc("encounter", "visit-33", "Visit on 2026-02-02", LocalDate.of(2026, 2, 2)));
		StringBuilder question = new StringBuilder("Was there a visit on");
		LocalDate date = LocalDate.of(2026, 1, 1);
		for (int i = 0; i < 33; i++) {
			question.append(' ').append(date.plusDays(i));
		}

		Map<String, String> tiers = tiersByUuid(service.getContextSlice(PATIENT, question.toString(),
		        new ContextSliceRequest(Collections.<String> emptySet(), false)));

		assertEquals(QueryStoreConstants.TIER_EXACT, tiers.get("visit-32"), "got " + tiers);
		assertFalse(tiers.containsKey("visit-33"), "the 33rd date is past the cap: " + tiers);
	}

	/** Curly quotes, the default on macOS and iOS keyboards, open a quoted phrase too. */
	@Test
	public void curlyQuotesOpenAQuotedPhrase() {
		backend.chart.add(doc("obs", "chest-1", "Chief complaint: chest pain on exertion", LocalDate.of(2022, 1, 1)));
		backend.chart.add(doc("obs", "sob-1", "Symptom: shortness of breath", LocalDate.of(2022, 1, 2)));

		Map<String, String> tiers = tiersByUuid(service.getContextSlice(PATIENT,
		        "Did the patient\u2019s notes mention \u201Cchest pain\u201D or \u2018shortness of breath\u2019?",
		        new ContextSliceRequest(Collections.<String> emptySet(), false)));

		assertEquals(QueryStoreConstants.TIER_EXACT, tiers.get("chest-1"));
		assertEquals(QueryStoreConstants.TIER_EXACT, tiers.get("sob-1"));
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
	public void similarityRankPreservesSearchOrderWhileSlicePreservesChartOrder() {
		QueryDocument olderFirstHit = backend.chart.get(8); // noise-1
		QueryDocument newerSecondHit = backend.chart.get(6); // cond-old
		backend.hits = Arrays.asList(olderFirstHit, newerSecondHit);

		ContextSlice slice = service.getContextSlice(PATIENT, "ankle and shoe history?",
		        new ContextSliceRequest(Collections.<String> emptySet(), false));

		List<ContextSliceRecord> similarity = new ArrayList<ContextSliceRecord>();
		for (ContextSliceRecord record : slice.getRecords()) {
			if (QueryStoreConstants.TIER_SIMILARITY.equals(record.getTier())) {
				similarity.add(record);
			}
		}
		assertEquals("cond-old", similarity.get(0).getDocument().getResourceUuid(),
		        "slice output stays in chart chronology");
		assertEquals(Integer.valueOf(2), similarity.get(0).getRank());
		assertEquals("noise-1", similarity.get(1).getDocument().getResourceUuid());
		assertEquals(Integer.valueOf(1), similarity.get(1).getRank(),
		        "the original best-hit rank survives the chronology reorder");
	}

	@Test
	public void blankQuestion_skipsTheSimilaritySearch() {
		ContextSliceRequest request = new ContextSliceRequest(Collections.<String> emptySet(), false);

		service.getContextSlice(PATIENT, "   ", request);

		assertEquals(0, backend.hybridCount.get(), "blank question has no ranking signal — no search RPC");
	}

	@Test
	public void obsGroupFamilies_completeWholePanels() {
		JsonNode fixtureCase = fixtureCase("context_policy", "context.panel-completion-includes-all-members");
		String parentId = idList(fixtureCase, "similarity_matched_ids").get(0);
		QueryDocument parent = doc("obs", parentId, "Basic metabolic panel", LocalDate.of(2026, 6, 15));
		backend.chart.add(parent);
		for (JsonNode memberIdNode : fixtureCase.path("panel_members").path(parentId)) {
			QueryDocument member = doc("obs", memberIdNode.asText(), "Serum value: " + memberIdNode.asText(),
			        LocalDate.of(2026, 6, 15));
			member.putMetadata(QueryStoreConstants.FIELD_OBS_GROUP_UUID, parentId);
			backend.chart.add(member);
		}
		backend.hits = Collections.singletonList(parent);
		ContextSliceRequest request = new ContextSliceRequest(Collections.<String> emptySet(), false);

		ContextSlice slice = service.getContextSlice(PATIENT, "results of the last BMP?", request);

		Map<String, String> tiers = tiersByUuid(slice);
		JsonNode expectedTier = fixtureCase.path("expected_tier");
		for (String id : idList(fixtureCase, "expected_included")) {
			assertTrue(tiers.containsKey(id), "fixture expected_included: " + id + " missing; got " + tiers);
			assertEquals(expectedTier.path(id).asText(), tiers.get(id), "tier mismatch for " + id + "; got " + tiers);
		}
	}

	/** A value-less group parent is never indexed (ObsRecordSerializer), so in production the hit
	 *  that opens a family is a member, and the parent is not in the chart at all. */
	@Test
	public void obsGroupFamilies_completeFromAMemberHit_whenTheParentIsNotIndexed() {
		QueryDocument sodium = doc("obs", "bmp-sodium", "Serum sodium: 140 mmol/L", LocalDate.of(2026, 6, 15));
		QueryDocument potassium = doc("obs", "bmp-potassium", "Serum potassium: 4.1 mmol/L",
		        LocalDate.of(2026, 6, 15));
		sodium.putMetadata(QueryStoreConstants.FIELD_OBS_GROUP_UUID, "bmp-parent");
		potassium.putMetadata(QueryStoreConstants.FIELD_OBS_GROUP_UUID, "bmp-parent");
		backend.chart.add(sodium);
		backend.chart.add(potassium);
		backend.hits = Collections.singletonList(sodium);

		ContextSlice slice = service.getContextSlice(PATIENT, "results of the BMP?",
		        new ContextSliceRequest(Collections.<String> emptySet(), false));
		Map<String, String> tiers = tiersByUuid(slice);

		assertEquals(QueryStoreConstants.TIER_PANEL, tiers.get("bmp-sodium"), "the member hit opens its family: " + tiers);
		assertEquals(QueryStoreConstants.TIER_PANEL, tiers.get("bmp-potassium"), "the sibling joins: " + tiers);
	}

	@Test
	public void obsGroupCompletionProtectsTheRecordThatTriggeredTheFamily() {
		QueryDocument parent = doc("obs", "panel-parent", "Basic metabolic panel",
		        LocalDate.of(2026, 6, 15));
		QueryDocument member = doc("obs", "panel-member", "Serum sodium: 140 mmol/L",
		        LocalDate.of(2026, 6, 15));
		member.putMetadata(QueryStoreConstants.FIELD_OBS_GROUP_UUID, "panel-parent");
		backend.chart.add(parent);
		backend.chart.add(member);
		backend.hits = Collections.singletonList(parent);

		ContextSlice slice = service.getContextSlice(PATIENT, "results of the BMP?",
		        new ContextSliceRequest(Collections.<String> emptySet(), false));
		Map<String, String> tiers = tiersByUuid(slice);

		assertEquals(QueryStoreConstants.TIER_PANEL, tiers.get("panel-parent"),
		        "the triggering record must not remain budget-droppable similarity evidence");
		assertEquals(QueryStoreConstants.TIER_PANEL, tiers.get("panel-member"));
		ContextSliceRecord promoted = slice.getRecords().stream()
		        .filter(record -> "panel-parent".equals(record.getDocument().getResourceUuid()))
		        .findFirst().orElseThrow(AssertionError::new);
		assertEquals(Integer.valueOf(1), promoted.getRank(),
		        "a promoted panel record keeps its original similarity rank for traceability");
	}

	@Test
	public void contextSliceCarriesTheSnapshotOfItsCompleteChartRead() {
		ContextSlice slice = service.getContextSlice(PATIENT, "medications?",
		        new ContextSliceRequest(Collections.<String> emptySet(), false));

		assertEquals(org.openmrs.module.querystore.model.PatientChartFingerprint.snapshotId(
		        backend.chart, false, false), slice.getChartSnapshotId());
		assertFalse(slice.isProjectionComplete());
	}

	@Test
	public void typedCompleteTierWinsWhenTheSameRecordIsInsideTheRecencyAnchor() {
		ContextSliceRequest request = new ContextSliceRequest(
		        Collections.singleton("drug_order"), true);
		request.setRecencyAnchorSize(backend.chart.size());

		ContextSlice slice = service.getContextSlice(PATIENT, "current medications?", request);

		assertEquals(QueryStoreConstants.TIER_TYPED, tiersByUuid(slice).get("med-1"));
		assertEquals(QueryStoreConstants.TIER_TYPED, tiersByUuid(slice).get("med-2"));
	}

	@Test
	public void sliceKeepsChartOrder_andEachRecordAppearsOnce() {
		JsonNode fixtureCase = fixtureCase("context_policy",
		        "context.stable-ordering-preserves-chart-order-and-highest-tier-wins");
		String matchedId = idList(fixtureCase, "similarity_matched_ids").get(0);
		backend.hits = Collections.singletonList(
		        doc("allergy", matchedId, "Allergy: Penicillin. Reaction: rash", LocalDate.of(2024, 4, 1)));
		ContextSliceRequest request = new ContextSliceRequest(
		        resourceTypesOf(idList(fixtureCase, "typed_complete_ids")), true);
		request.setRecencyAnchorSize(2);

		ContextSlice slice = service.getContextSlice(PATIENT, "most recent allergy reactions?", request);

		Map<String, String> tiers = tiersByUuid(slice);
		List<String> uuids = new ArrayList<String>(tiers.keySet());
		if (fixtureCase.path("expected_no_duplicates").asBoolean(false)) {
			assertEquals(new HashSet<String>(uuids).size(), uuids.size(), "no duplicates: " + uuids);
		}
		for (JsonNode pair : fixtureCase.path("expected_order_precedes")) {
			String before = pair.get(0).asText();
			String after = pair.get(1).asText();
			assertTrue(uuids.indexOf(before) < uuids.indexOf(after),
			        "chart order kept: " + before + " before " + after + "; got " + uuids);
		}
		JsonNode expectedTier = fixtureCase.path("expected_tier");
		Iterator<String> tierIds = expectedTier.fieldNames();
		while (tierIds.hasNext()) {
			String id = tierIds.next();
			assertEquals(expectedTier.path(id).asText(), tiers.get(id), "tier mismatch for " + id + "; got " + tiers);
		}
	}

	@Test
	public void chartAtTheBackendCap_isNotClaimedTruncatedWithoutBackendEvidence() {
		// A size equal to the ES cap is not itself evidence of lost data: MySQL and Lucene can
		// legitimately return an exact 10 000-record chart. Only a backend that knows it dropped
		// records may set the explicit-overflow flag.
		int backendCapExample = 10_000;
		List<QueryDocument> big = new ArrayList<QueryDocument>();
		for (int i = 0; i < backendCapExample; i++) {
			big.add(doc("obs", "o-" + i, "Obs " + i, LocalDate.of(2026, 1, 1)));
		}
		backend.chart = big;
		ContextSliceRequest request = new ContextSliceRequest(Collections.<String> emptySet(), false);

		ContextSlice slice = service.getContextSlice(PATIENT, "anything?", request);

		assertFalse(slice.isChartTruncated(), "an exact-cap complete chart is not truncated");
		assertEquals(backendCapExample, slice.getChartSize());

		backend.chart = backend.chart.subList(0, 100);
		assertFalse(service.getContextSlice(PATIENT, "anything?", request).isChartTruncated(),
		        "an uncapped chart is not flagged");
	}

	@Test
	public void chartTruncation_comesFromTheBackendReadNotTheReturnedCount() {
		backend.chartTruncated = true;
		ContextSliceRequest request = new ContextSliceRequest(Collections.<String> emptySet(), false);

		ContextSlice slice = service.getContextSlice(PATIENT, "anything?", request);

		assertTrue(slice.isChartTruncated(), "a backend-reported incomplete chart must be surfaced");
	}

	@Test
	public void interpretMode_derivesTypesAndTemporalServerSide() {
		// ADR Decision 18: interpretQuestion makes QueryStore authoritative for the typed scope
		// and temporal flag while preserving the caller's explicit additions.
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
	public void interpretMode_keepsTheCallersTemporalFlag() {
		ContextSliceRequest request = new ContextSliceRequest(Collections.<String> emptySet(), true);
		request.setInterpretQuestion(true);
		request.setRecencyAnchorSize(3);

		ContextSlice slice = service.getContextSlice(PATIENT, "Any eye problems?", request);

		assertTrue(slice.isTemporalApplied());
		assertEquals(QueryStoreConstants.TIER_RECENCY_ANCHOR, tiersByUuid(slice).get("recent-1"));
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
		// Retrieval-quality preprocessing (lab-panel expansion only, natural language preserved)
		// runs HERE, at the owner of the index and embedder — callers send the raw question.
		ContextSliceRequest request = new ContextSliceRequest(Collections.<String> emptySet(), false);

		service.getContextSlice(PATIENT, "results of the last BMP?", request);

		assertTrue(backend.lastQueryText.contains("basic metabolic panel"),
		        "the panel abbreviation must be expanded for retrieval; got " + backend.lastQueryText);
		assertTrue(backend.lastQueryText.contains("results of the last"),
		        "natural language context must be preserved for dense embeddings; got " + backend.lastQueryText);
	}

	@Test
	public void similarityLeg_leavesOutThePatientTheWholeChartIsAbout() {
		// Records saying "Patient reported ..." matched the word itself and filled the similarity tier
		// ahead of the kidney-function labs a kidney question needs.
		ContextSliceRequest request = new ContextSliceRequest(Collections.<String> emptySet(), false);

		service.getContextSlice(PATIENT, "Does the patient have any kidney problems?", request);

		assertEquals("Does have any kidney problems?", backend.lastQueryText);
	}

	/** Scripted backend: canned chart + canned similarity hits, everything else inert. */
	private static final class FakeBackendStore implements BackendStore {

		List<QueryDocument> chart = Collections.emptyList();

		List<QueryDocument> hits = Collections.emptyList();

		boolean throwOnSearch;

		boolean chartTruncated;

		final AtomicInteger hybridCount = new AtomicInteger();

		boolean hasDocuments = true;

		@Override
		public boolean existsByPatient(String patientUuid) {
			return hasDocuments;
		}

		@Override
		public List<QueryDocument> findAllByPatient(String patientUuid) {
			return chart;
		}

		@Override
		public org.openmrs.module.querystore.backend.PatientChartRead findPatientChart(String patientUuid) {
			return new org.openmrs.module.querystore.backend.PatientChartRead(chart, chartTruncated);
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
