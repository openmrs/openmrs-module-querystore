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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openmrs.module.querystore.serialization.ConceptFixtures.concept;
import static org.openmrs.module.querystore.serialization.ConceptFixtures.conceptName;
import static org.openmrs.module.querystore.serialization.ConceptFixtures.preferredName;
import static org.openmrs.module.querystore.serialization.DateFixtures.utcDate;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.module.querystore.model.QueryDocument;

public class ObsRecordSerializerTest {

	private ObsRecordSerializer serializer;

	@Before
	public void setUp() {
		serializer = new ObsRecordSerializer();
	}

	@Test
	public void serialize_numericObs_populatesTextAndMetadata() {
		ConceptNumeric concept = new ConceptNumeric();
		concept.addName(conceptName("Fasting blood glucose"));
		concept.setUnits("mmol/L");
		Obs obs = obs(concept);
		obs.setValueNumeric(11.2);

		QueryDocument doc = serializer.serialize(obs);

		assertEquals("obs", doc.getResourceType());
		assertEquals("Fasting blood glucose: 11.2 mmol/L", doc.getText());
		assertEquals(11.2, (Double) doc.getMetadata().get("value_numeric"), 0.0001);
		assertEquals("mmol/L", doc.getMetadata().get("units"));
		assertEquals("Fasting blood glucose", doc.getMetadata().get("concept_name"));
	}

	@Test
	public void serialize_valueModifier_includedInText() {
		ConceptNumeric concept = new ConceptNumeric();
		concept.addName(conceptName("Viral Load"));
		concept.setUnits("copies/mL");
		Obs obs = obs(concept);
		obs.setValueNumeric(200.0);
		obs.setValueModifier(">");

		QueryDocument doc = serializer.serialize(obs);

		assertEquals("Viral Load: >200.0 copies/mL", doc.getText());
	}

	@Test
	public void serialize_codedObs_populatesValueCodedFields() {
		Concept question = concept("Diagnosis");
		Concept answer = concept("Malaria");
		answer.setUuid("answer-uuid");
		Obs obs = obs(question);
		obs.setValueCoded(answer);

		QueryDocument doc = serializer.serialize(obs);

		assertEquals("Diagnosis: Malaria", doc.getText());
		assertEquals("answer-uuid", doc.getMetadata().get("value_coded_uuid"));
		assertEquals("Malaria", doc.getMetadata().get("value_coded_name"));
	}

	@Test
	public void serialize_textObs_populatesValueText() {
		Obs obs = obs(concept("Clinical notes"));
		obs.setValueText("Patient reports improvement");

		QueryDocument doc = serializer.serialize(obs);

		assertEquals("Clinical notes: Patient reports improvement", doc.getText());
		assertEquals("Patient reports improvement", doc.getMetadata().get("value_text"));
	}

	@Test
	public void serialize_datetimeObs_populatesValueDatetime() {
		Obs obs = obs(concept("Date of symptom onset"));
		obs.setValueDatetime(utcDate(2024, Calendar.FEBRUARY, 10));

		QueryDocument doc = serializer.serialize(obs);

		assertEquals("Date of symptom onset: 2024-02-10", doc.getText());
		assertEquals("2024-02-10", doc.getMetadata().get("value_datetime"));
	}

	// Boolean obs: Obs.setValueBoolean reaches into Context.getConceptService() to resolve
	// the TRUE/FALSE concept; covered by the (future) context-sensitive test rather than a
	// plain unit test.

	@Test
	public void serialize_interpretation_inMetadataNotText() {
		Obs obs = obs(concept("Heart Rate"));
		obs.setValueNumeric(120.0);
		obs.setInterpretation(Obs.Interpretation.HIGH);

		QueryDocument doc = serializer.serialize(obs);

		assertFalse("text should not contain interpretation",
				doc.getText().contains("HIGH"));
		assertEquals("HIGH", doc.getMetadata().get("interpretation"));
	}

	@Test
	public void serialize_comment_inMetadataNotText() {
		Obs obs = obs(concept("BP"));
		obs.setValueNumeric(140.0);
		obs.setComment("Taken after exercise");

		QueryDocument doc = serializer.serialize(obs);

		assertFalse("text should not contain comment",
				doc.getText().contains("Taken after exercise"));
		assertEquals("Taken after exercise", doc.getMetadata().get("comment"));
	}

	@Test
	public void serialize_groupMember_populatesGroupFields() {
		Concept parentConcept = concept("Vital signs");
		Obs parent = obs(parentConcept);
		parent.setUuid("parent-uuid");

		Obs member = obs(concept("Systolic blood pressure"));
		member.setValueNumeric(120.0);
		parent.addGroupMember(member);

		QueryDocument doc = serializer.serialize(member);

		assertNotNull(doc);
		assertEquals("Systolic blood pressure: 120.0", doc.getText());
		assertEquals("parent-uuid", doc.getMetadata().get("obs_group_uuid"));
		assertEquals("Vital signs", doc.getMetadata().get("obs_group_concept_name"));
	}

	@Test
	public void serialize_groupParentWithoutOwnValue_returnsNull() {
		Obs parent = obs(concept("Vital signs"));
		parent.addGroupMember(obs(concept("Pulse")));
		QueryDocument doc = serializer.serialize(parent);
		assertNull(doc);
	}

	@Test
	public void serialize_emptyObs_returnsNull() {
		QueryDocument doc = serializer.serialize(obs(concept("Weight")));
		assertNull(doc);
	}

	@Test
	public void serialize_encounterContext_populated() {
		EncounterType type = new EncounterType();
		type.setUuid("etype-uuid");
		type.setName("Adult Outpatient Visit");

		Visit visit = new Visit();
		visit.setUuid("visit-uuid");

		Form form = new Form();
		form.setUuid("form-uuid");
		form.setName("Adult Outpatient Form");

		Location location = new Location();
		location.setUuid("location-uuid");
		location.setName("Kenyatta National Hospital");

		Provider provider = new Provider();
		provider.setUuid("provider-uuid");

		Encounter enc = new Encounter();
		enc.setUuid("enc-uuid");
		enc.setEncounterType(type);
		enc.setVisit(visit);
		enc.setForm(form);
		enc.setLocation(location);
		EncounterProvider ep = new EncounterProvider();
		ep.setProvider(provider);
		ep.setVoided(Boolean.FALSE);
		enc.setEncounterProviders(new HashSet<>(Collections.singletonList(ep)));

		Obs obs = obs(concept("Weight"));
		obs.setValueNumeric(70.0);
		obs.setEncounter(enc);

		QueryDocument doc = serializer.serialize(obs);

		assertEquals("enc-uuid", doc.getMetadata().get("encounter_uuid"));
		assertEquals("etype-uuid", doc.getMetadata().get("encounter_type_uuid"));
		assertEquals("Adult Outpatient Visit", doc.getMetadata().get("encounter_type_name"));
		assertEquals("visit-uuid", doc.getMetadata().get("visit_uuid"));
		assertEquals("form-uuid", doc.getMetadata().get("form_uuid"));
		assertEquals("Adult Outpatient Form", doc.getMetadata().get("form_name"));
		assertEquals("location-uuid", doc.getMetadata().get("location_uuid"));
		assertEquals("Kenyatta National Hospital", doc.getMetadata().get("location_name"));
		assertEquals("provider-uuid", doc.getMetadata().get("provider_uuid"));
	}

	@Test
	public void serialize_complexObs_populatesUriHandlerAndPlaceholderText() {
		ConceptComplex concept = new ConceptComplex();
		concept.setUuid("concept-xray");
		concept.addName(preferredName("Chest X-Ray"));
		ConceptDatatype complex = new ConceptDatatype();
		complex.setUuid(ConceptDatatype.COMPLEX_UUID);
		concept.setDatatype(complex);
		concept.setHandler("ImageHandler");

		Obs obs = obs(concept);
		obs.setValueComplex("xray-handler|/storage/xray/123.png");

		QueryDocument doc = serializer.serialize(obs);

		assertNotNull(doc);
		assertTrue("text should mark complex value", doc.getText().contains("[complex value]"));
		assertEquals("xray-handler|/storage/xray/123.png",
				doc.getMetadata().get("value_complex_uri"));
		assertEquals("ImageHandler", doc.getMetadata().get("value_complex_handler"));
	}

	@Test
	public void serialize_description_populatedFromConcept() {
		// End-to-end integration: a concept with a description flows through
		// ObsRecordSerializer.serialize → AbstractRecordSerializer.putConceptFields →
		// putDescription → metadata['description']. Parity with the analogous
		// mapping-names test below: without this assertion, a refactor that removes the
		// putDescription() call from putConceptFields would silently stop writing the
		// field — every existing test still passes because the description-indexing slice
		// (exp 5) shipped before this hardening discipline was established.
		Concept c = new Concept();
		c.addName(preferredName("Blood urea nitrogen"));
		ConceptDescription d = new ConceptDescription();
		d.setDescription("Lab test reflecting kidney function. Used to assess renal status.");
		d.setLocale(Locale.ENGLISH);
		c.addDescription(d);

		Obs obs = obs(c);
		obs.setValueText("noted");

		QueryDocument doc = serializer.serialize(obs);

		assertEquals("Lab test reflecting kidney function. Used to assess renal status.",
				doc.getMetadata().get("description"));
	}

	@Test
	public void serialize_mappingNames_populatedFromConceptMappings() {
		// End-to-end integration: a concept with reference-term mappings flows through
		// ObsRecordSerializer.serialize → AbstractRecordSerializer.putConceptFields →
		// putMappingNames → metadata['mapping_names']. Without this test, a refactor that
		// removes the putMappingNames() call from putConceptFields would silently lose the
		// slice's contribution — every existing test still passes because they don't assert
		// the field.
		Concept c = new Concept();
		c.addName(preferredName("Chronic kidney insufficiency"));
		LinkedHashSet<ConceptMap> mappings = new LinkedHashSet<>();
		mappings.add(termMapping("Chronic kidney disease, unspecified"));
		mappings.add(termMapping("Chronic kidney disease"));
		c.setConceptMappings(mappings);

		Obs obs = obs(c);
		obs.setValueText("noted");

		QueryDocument doc = serializer.serialize(obs);

		assertEquals(Arrays.asList(
				"Chronic kidney disease",
				"Chronic kidney disease, unspecified"),
				doc.getMetadata().get("mapping_names"));
	}

	private static ConceptMap termMapping(String name) {
		ConceptReferenceTerm t = new ConceptReferenceTerm();
		t.setName(name);
		ConceptMap m = new ConceptMap();
		m.setConceptReferenceTerm(t);
		return m;
	}

	@Test
	public void serialize_synonyms_populatedAsSortedList() {
		Concept c = new Concept();
		c.addName(preferredName("Hypertension"));
		c.addName(conceptName("HTN"));
		c.addName(conceptName("High blood pressure"));

		Obs obs = obs(c);
		obs.setValueText("noted");

		QueryDocument doc = serializer.serialize(obs);

		assertEquals(Arrays.asList("HTN", "High blood pressure"), doc.getMetadata().get("synonyms"));
		assertEquals("Hypertension", doc.getMetadata().get("concept_name"));
	}

	@Test
	public void serialize_drugObs_populatesValueDrugFields() {
		Concept question = concept("Medication given");
		Obs obs = obs(question);
		Drug drug = new Drug();
		drug.setUuid("drug-uuid");
		drug.setName("Metformin 500mg");
		obs.setValueDrug(drug);

		QueryDocument doc = serializer.serialize(obs);

		assertEquals("Medication given: Metformin 500mg", doc.getText());
		assertEquals("drug-uuid", doc.getMetadata().get("value_drug_uuid"));
		assertEquals("Metformin 500mg", doc.getMetadata().get("value_drug_name"));
	}

	@Test
	public void serialize_conceptClass_populated() {
		Concept c = concept("Fasting blood glucose");
		ConceptClass cc = new ConceptClass();
		cc.setName("Test");
		c.setConceptClass(cc);
		Obs obs = obs(c);
		obs.setValueNumeric(11.2);

		QueryDocument doc = serializer.serialize(obs);

		assertEquals("Test", doc.getMetadata().get("concept_class"));
	}

	@Test
	public void serialize_carriesPatientAndResourceUuids() {
		Patient patient = new Patient();
		patient.setUuid("patient-uuid");
		Obs obs = obs(concept("Weight"));
		obs.setUuid("obs-uuid");
		obs.setPerson(patient);
		obs.setValueNumeric(70.0);

		QueryDocument doc = serializer.serialize(obs);

		assertEquals("patient-uuid", doc.getPatientUuid());
		assertEquals("obs-uuid", doc.getResourceUuid());
	}

	private static Obs obs(Concept concept) {
		Obs obs = new Obs();
		obs.setConcept(concept);
		obs.setObsDatetime(new Date());
		return obs;
	}

}
