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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.openmrs.module.querystore.serialization.ConceptFixtures.concept;
import static org.openmrs.module.querystore.serialization.ConceptFixtures.conceptName;
import static org.openmrs.module.querystore.serialization.ConceptFixtures.preferredName;
import static org.openmrs.module.querystore.serialization.DateFixtures.utcDate;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Concept;
import org.openmrs.Condition;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.module.querystore.model.QueryDocument;

public class ConditionRecordSerializerTest {

	private ConditionRecordSerializer serializer;

	@Before
	public void setUp() {
		serializer = new ConditionRecordSerializer();
	}

	@Test
	public void serialize_codedCondition_populatesTextAndConceptFields() {
		Concept concept = concept("Type 2 Diabetes Mellitus");
		concept.setUuid("concept-uuid");
		Condition condition = condition(coded(concept));
		condition.setClinicalStatus(ConditionClinicalStatus.ACTIVE);
		condition.setVerificationStatus(ConditionVerificationStatus.CONFIRMED);

		QueryDocument doc = serializer.serialize(condition);

		assertEquals("condition", doc.getResourceType());
		assertEquals("Condition: Type 2 Diabetes Mellitus. Status: ACTIVE. Verification: CONFIRMED",
				doc.getText());
		assertEquals("concept-uuid", doc.getMetadata().get("concept_uuid"));
		assertEquals("Type 2 Diabetes Mellitus", doc.getMetadata().get("concept_name"));
		assertEquals("ACTIVE", doc.getMetadata().get("clinical_status"));
		assertEquals("CONFIRMED", doc.getMetadata().get("verification_status"));
		assertNull("non_coded should be absent for coded conditions",
				doc.getMetadata().get("non_coded"));
	}

	@Test
	public void serialize_nonCodedCondition_populatesNonCodedAndUsesAsName() {
		Condition condition = condition(nonCoded("Custom locally-defined condition"));
		condition.setClinicalStatus(ConditionClinicalStatus.ACTIVE);

		QueryDocument doc = serializer.serialize(condition);

		assertEquals("Condition: Custom locally-defined condition. Status: ACTIVE", doc.getText());
		assertEquals("Custom locally-defined condition", doc.getMetadata().get("non_coded"));
		assertNull("concept_uuid should be absent for non-coded conditions",
				doc.getMetadata().get("concept_uuid"));
	}

	@Test
	public void serialize_onsetDate_appearsInTextAndMetadata() {
		Condition condition = condition(coded(concept("Hypertension")));
		condition.setClinicalStatus(ConditionClinicalStatus.ACTIVE);
		condition.setOnsetDate(utcDate(2020, Calendar.MARCH, 15));

		QueryDocument doc = serializer.serialize(condition);

		assertEquals("Condition: Hypertension. Status: ACTIVE. Onset: 2020-03-15", doc.getText());
		assertEquals("2020-03-15", doc.getMetadata().get("onset_date"));
		assertEquals("2020-03-15", doc.getMetadata().get("clinical_date"));
		assertEquals("administrative", doc.getMetadata().get("date_kind"));
	}

	@Test
	public void serialize_endDate_appearsAsResolvedInText() {
		Condition condition = condition(coded(concept("Pneumonia")));
		condition.setClinicalStatus(ConditionClinicalStatus.RESOLVED);
		condition.setEndDate(utcDate(2024, Calendar.AUGUST, 10));

		QueryDocument doc = serializer.serialize(condition);

		assertEquals("Condition: Pneumonia. Status: RESOLVED. Resolved: 2024-08-10", doc.getText());
		assertEquals("2024-08-10", doc.getMetadata().get("end_date"));
	}

	@Test
	public void serialize_onsetAndEndDate_appearInOrder() {
		Condition condition = condition(coded(concept("Sinusitis")));
		condition.setClinicalStatus(ConditionClinicalStatus.RESOLVED);
		condition.setVerificationStatus(ConditionVerificationStatus.CONFIRMED);
		condition.setOnsetDate(utcDate(2024, Calendar.JANUARY, 5));
		condition.setEndDate(utcDate(2024, Calendar.JANUARY, 20));

		QueryDocument doc = serializer.serialize(condition);

		assertEquals(
				"Condition: Sinusitis. Status: RESOLVED. Verification: CONFIRMED."
				        + " Onset: 2024-01-05. Resolved: 2024-01-20",
				doc.getText());
		assertEquals("2024-01-05", doc.getMetadata().get("onset_date"));
		assertEquals("2024-01-20", doc.getMetadata().get("end_date"));
	}

	@Test
	public void serialize_synonyms_populatedFromCodedConcept() {
		Concept c = new Concept();
		c.addName(preferredName("Hypertension"));
		c.addName(conceptName("HTN"));
		c.addName(conceptName("High blood pressure"));

		Condition condition = condition(coded(c));

		QueryDocument doc = serializer.serialize(condition);

		assertEquals(Arrays.asList("HTN", "High blood pressure"),
				doc.getMetadata().get("synonyms"));
		assertEquals("Hypertension", doc.getMetadata().get("concept_name"));
	}

	@Test
	public void serialize_description_populatedInMetadataNotInText() {
		// Concept descriptions ride into a metadata field so BM25 can index them as searchable
		// vocabulary without polluting the citation-clean text. Without this assertion, a future
		// refactor could silently drop descriptions and the kidney-query retrieval win would
		// disappear without breaking the existing text/synonyms tests.
		Concept c = new Concept();
		c.addName(preferredName("Blood urea nitrogen"));
		org.openmrs.ConceptDescription d = new org.openmrs.ConceptDescription();
		d.setDescription("Measure of urea levels in the blood often used to assess kidney status.");
		d.setLocale(java.util.Locale.ENGLISH);
		c.addDescription(d);

		Condition condition = condition(coded(c));
		QueryDocument doc = serializer.serialize(condition);

		assertEquals("Measure of urea levels in the blood often used to assess kidney status.",
				doc.getMetadata().get("description"));
		assertFalse("description must not leak into stored text (citation-clean contract)",
				doc.getText().contains("kidney"));
	}

	@Test
	public void serialize_descriptionAbsent_metadataKeyOmitted() {
		// 40-60% of concepts have no description in real OpenMRS data. The metadata key must
		// stay absent (not present-but-empty) so doc.getMetadata().get("description") is null,
		// matching the contract Lucene's toLuceneDocument relies on for the "skip TextField"
		// branch and keeping metadata_json compact.
		Condition condition = condition(coded(concept("Asthma")));
		QueryDocument doc = serializer.serialize(condition);

		assertNull("description metadata key must be absent when concept has no description",
				doc.getMetadata().get("description"));
	}

	@Test
	public void serialize_additionalDetail_inMetadataNotText() {
		Condition condition = condition(coded(concept("Asthma")));
		condition.setAdditionalDetail("Triggered by cold weather");

		QueryDocument doc = serializer.serialize(condition);

		assertFalse("text should not contain additional detail",
				doc.getText().contains("Triggered by cold weather"));
		assertEquals("Triggered by cold weather", doc.getMetadata().get("additional_detail"));
	}

	@Test
	public void serialize_previousVersion_populatesUuid() {
		Condition previous = new Condition();
		previous.setUuid("previous-uuid");
		Condition condition = condition(coded(concept("Anemia")));
		condition.setPreviousVersion(previous);

		QueryDocument doc = serializer.serialize(condition);

		assertEquals("previous-uuid", doc.getMetadata().get("previous_version_uuid"));
	}

	@Test
	public void serialize_encounterContext_populated() {
		EncounterType type = new EncounterType();
		type.setUuid("etype-uuid");
		type.setName("Adult Outpatient Visit");
		Encounter enc = new Encounter();
		enc.setUuid("enc-uuid");
		enc.setEncounterType(type);

		Condition condition = condition(coded(concept("Migraine")));
		condition.setEncounter(enc);

		QueryDocument doc = serializer.serialize(condition);

		assertEquals("enc-uuid", doc.getMetadata().get("encounter_uuid"));
		assertEquals("etype-uuid", doc.getMetadata().get("encounter_type_uuid"));
		assertEquals("Adult Outpatient Visit", doc.getMetadata().get("encounter_type_name"));
	}

	@Test
	public void serialize_emptyCondition_returnsNull() {
		Condition condition = new Condition();
		assertNull(serializer.serialize(condition));
	}

	@Test
	public void serialize_blankNonCoded_returnsNull() {
		Condition condition = condition(nonCoded("   "));
		assertNull(serializer.serialize(condition));
	}

	@Test
	public void serialize_carriesPatientAndResourceUuids() {
		Patient patient = new Patient();
		patient.setUuid("patient-uuid");
		Condition condition = condition(coded(concept("Diabetes")));
		condition.setUuid("condition-uuid");
		condition.setPatient(patient);

		QueryDocument doc = serializer.serialize(condition);

		assertEquals("patient-uuid", doc.getPatientUuid());
		assertEquals("condition-uuid", doc.getResourceUuid());
	}

	private static Condition condition(CodedOrFreeText cft) {
		Condition c = new Condition();
		c.setCondition(cft);
		c.setDateCreated(new Date());
		return c;
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
