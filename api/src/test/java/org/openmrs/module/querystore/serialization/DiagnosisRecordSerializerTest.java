/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.serialization;

import static org.junit.Assert.assertEquals;
import static org.openmrs.module.querystore.QueryStoreConstants.DATE_KIND_ADMINISTRATIVE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DATE_KIND;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_CLINICAL_DATE;
import static org.junit.Assert.assertNull;
import static org.openmrs.module.querystore.serialization.ConceptFixtures.concept;
import static org.openmrs.module.querystore.serialization.ConceptFixtures.conceptName;
import static org.openmrs.module.querystore.serialization.ConceptFixtures.preferredName;

import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Concept;
import org.openmrs.Condition;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.module.querystore.model.QueryDocument;

public class DiagnosisRecordSerializerTest {

	private DiagnosisRecordSerializer serializer;

	@Before
	public void setUp() {
		serializer = new DiagnosisRecordSerializer();
	}

	@Test
	public void serialize_codedDiagnosis_populatesTextAndConceptFields() {
		Concept concept = concept("Tuberculosis");
		concept.setUuid("concept-uuid");
		Diagnosis diagnosis = diagnosis(coded(concept));
		diagnosis.setCertainty(ConditionVerificationStatus.CONFIRMED);
		diagnosis.setRank(1);

		QueryDocument doc = serializer.serialize(diagnosis);

		assertEquals("diagnosis", doc.getResourceType());
		assertEquals("Diagnosis: Tuberculosis. Certainty: CONFIRMED. Rank: Primary", doc.getText());
		assertEquals("concept-uuid", doc.getMetadata().get("concept_uuid"));
		assertEquals("Tuberculosis", doc.getMetadata().get("concept_name"));
		assertEquals("CONFIRMED", doc.getMetadata().get("certainty"));
		assertEquals("Primary", doc.getMetadata().get("rank"));
		assertNull("non_coded should be absent for coded diagnoses",
		        doc.getMetadata().get("non_coded"));
		assertNull("condition_uuid should be absent when no associated condition",
		        doc.getMetadata().get("condition_uuid"));
	}

	@Test
	public void serialize_nonCodedDiagnosis_populatesNonCodedAndUsesAsName() {
		Diagnosis diagnosis = diagnosis(nonCoded("Locally-defined ailment"));
		diagnosis.setCertainty(ConditionVerificationStatus.PROVISIONAL);

		QueryDocument doc = serializer.serialize(diagnosis);

		assertEquals("Diagnosis: Locally-defined ailment. Certainty: PROVISIONAL", doc.getText());
		assertEquals("Locally-defined ailment", doc.getMetadata().get("non_coded"));
		assertNull("concept_uuid should be absent for non-coded diagnoses",
		        doc.getMetadata().get("concept_uuid"));
	}

	@Test
	public void serialize_secondaryRank_labelledSecondary() {
		Diagnosis diagnosis = diagnosis(coded(concept("Hypertension")));
		diagnosis.setRank(2);

		QueryDocument doc = serializer.serialize(diagnosis);

		assertEquals("Diagnosis: Hypertension. Rank: Secondary", doc.getText());
		assertEquals("Secondary", doc.getMetadata().get("rank"));
	}

	@Test
	public void serialize_higherRank_alsoLabelledSecondary() {
		Diagnosis diagnosis = diagnosis(coded(concept("Asthma")));
		diagnosis.setRank(3);

		QueryDocument doc = serializer.serialize(diagnosis);

		assertEquals("Secondary", doc.getMetadata().get("rank"));
	}

	@Test
	public void serialize_nullRank_omittedFromTextAndMetadata() {
		Diagnosis diagnosis = diagnosis(coded(concept("Asthma")));
		diagnosis.setCertainty(ConditionVerificationStatus.CONFIRMED);

		QueryDocument doc = serializer.serialize(diagnosis);

		assertEquals("Diagnosis: Asthma. Certainty: CONFIRMED", doc.getText());
		assertNull(doc.getMetadata().get("rank"));
	}

	@Test
	public void serialize_condition_populatesConditionUuid() {
		Condition condition = new Condition();
		condition.setUuid("condition-uuid");
		Diagnosis diagnosis = diagnosis(coded(concept("Diabetes")));
		diagnosis.setCondition(condition);

		QueryDocument doc = serializer.serialize(diagnosis);

		assertEquals("condition-uuid", doc.getMetadata().get("condition_uuid"));
	}

	@Test
	public void serialize_synonyms_populatedFromCodedConcept() {
		Concept c = new Concept();
		c.addName(preferredName("Tuberculosis"));
		c.addName(conceptName("TB"));
		c.addName(conceptName("Phthisis"));

		Diagnosis diagnosis = diagnosis(coded(c));

		QueryDocument doc = serializer.serialize(diagnosis);

		assertEquals(Arrays.asList("Phthisis", "TB"), doc.getMetadata().get("synonyms"));
		assertEquals("Tuberculosis", doc.getMetadata().get("concept_name"));
	}

	@Test
	public void serialize_encounterContext_populated() {
		EncounterType type = new EncounterType();
		type.setUuid("etype-uuid");
		type.setName("Adult Outpatient Visit");
		Encounter enc = new Encounter();
		enc.setUuid("enc-uuid");
		enc.setEncounterType(type);

		Diagnosis diagnosis = diagnosis(coded(concept("Migraine")));
		diagnosis.setEncounter(enc);

		QueryDocument doc = serializer.serialize(diagnosis);

		assertEquals("enc-uuid", doc.getMetadata().get("encounter_uuid"));
		assertEquals("etype-uuid", doc.getMetadata().get("encounter_type_uuid"));
		assertEquals("Adult Outpatient Visit", doc.getMetadata().get("encounter_type_name"));
	}

	@Test
	public void serialize_emptyDiagnosis_returnsNull() {
		Diagnosis diagnosis = new Diagnosis();
		assertNull(serializer.serialize(diagnosis));
	}

	@Test
	public void serialize_blankNonCoded_returnsNull() {
		Diagnosis diagnosis = diagnosis(nonCoded("   "));
		assertNull(serializer.serialize(diagnosis));
	}

	@Test
	public void serialize_carriesPatientAndResourceUuids() {
		Patient patient = new Patient();
		patient.setUuid("patient-uuid");
		Diagnosis diagnosis = diagnosis(coded(concept("Diabetes")));
		diagnosis.setUuid("diagnosis-uuid");
		diagnosis.setPatient(patient);

		QueryDocument doc = serializer.serialize(diagnosis);

		assertEquals("patient-uuid", doc.getPatientUuid());
		assertEquals("diagnosis-uuid", doc.getResourceUuid());
	}

	@Test
	public void serialize_datesTheRecordByItsCreationAndTheEventByItsEncounter() {
		Diagnosis diagnosis = diagnosis(coded(concept("Tuberculosis")));
		diagnosis.setDateCreated(java.sql.Timestamp.valueOf("2026-03-04 10:00:00"));
		Encounter encounter = new Encounter();
		encounter.setEncounterDatetime(java.sql.Timestamp.valueOf("2025-11-20 09:30:00"));
		diagnosis.setEncounter(encounter);

		QueryDocument doc = serializer.serialize(diagnosis);

		assertEquals(java.time.LocalDate.of(2026, 3, 4), doc.getDate());
		assertEquals("2025-11-20", doc.getMetadata().get(FIELD_CLINICAL_DATE));
		assertEquals(DATE_KIND_ADMINISTRATIVE, doc.getMetadata().get(FIELD_DATE_KIND));
	}

	@Test
	public void serialize_leavesTheEventUndatedWithoutAnEncounter() {
		Diagnosis diagnosis = diagnosis(coded(concept("Tuberculosis")));
		diagnosis.setDateCreated(java.sql.Timestamp.valueOf("2026-03-04 10:00:00"));

		QueryDocument doc = serializer.serialize(diagnosis);

		assertNull(doc.getMetadata().get(FIELD_CLINICAL_DATE));
	}

	private static Diagnosis diagnosis(CodedOrFreeText cft) {
		Diagnosis d = new Diagnosis();
		d.setDiagnosis(cft);
		d.setDateCreated(new Date());
		return d;
	}

	private static CodedOrFreeText coded(Concept concept) {
		CodedOrFreeText cft = new CodedOrFreeText();
		cft.setCoded(concept);
		return cft;
	}

	private static CodedOrFreeText nonCoded(String text) {
		CodedOrFreeText cft = new CodedOrFreeText();
		cft.setNonCoded(text);
		return cft;
	}

}
