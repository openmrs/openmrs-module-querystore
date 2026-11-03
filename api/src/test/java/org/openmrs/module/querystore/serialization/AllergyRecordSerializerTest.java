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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Allergen;
import org.openmrs.AllergenType;
import org.openmrs.Allergy;
import org.openmrs.AllergyReaction;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.module.querystore.model.QueryDocument;

public class AllergyRecordSerializerTest {

	private AllergyRecordSerializer serializer;

	@Before
	public void setUp() {
		serializer = new AllergyRecordSerializer();
	}

	@Test
	public void serialize_codedAllergenWithSeverityAndReactions_matchesAdrExample() {
		Concept penicillin = concept("Penicillin");
		penicillin.setUuid("allergen-uuid");
		Allergy allergy = allergy(allergen(AllergenType.DRUG, penicillin, null));
		allergy.setSeverity(concept("Severe"));
		allergy.setReactions(reactions("Anaphylaxis", "Rash"));

		QueryDocument doc = serializer.serialize(allergy);

		assertEquals("allergy", doc.getResourceType());
		assertEquals("Allergy: Penicillin (drug allergen). Severity: Severe. Reactions: Anaphylaxis, Rash",
		        doc.getText());
		assertEquals("allergen-uuid", doc.getMetadata().get("allergen_uuid"));
		assertEquals("Penicillin", doc.getMetadata().get("allergen_name"));
		assertEquals("DRUG", doc.getMetadata().get("allergen_type"));
		assertEquals("Severe", doc.getMetadata().get("severity"));
		assertEquals(Arrays.asList("Anaphylaxis", "Rash"), doc.getMetadata().get("reactions"));
		assertNull("allergen_non_coded absent for coded allergens",
		        doc.getMetadata().get("allergen_non_coded"));
		assertNull("comment absent when unset", doc.getMetadata().get("comment"));
	}

	@Test
	public void serialize_nonCodedAllergen_populatesNonCodedAndUsesAsName() {
		Allergy allergy = allergy(allergen(AllergenType.FOOD, null, "Locally-grown nut"));

		QueryDocument doc = serializer.serialize(allergy);

		assertEquals("Allergy: Locally-grown nut (food allergen)", doc.getText());
		assertEquals("Locally-grown nut", doc.getMetadata().get("allergen_non_coded"));
		assertEquals("FOOD", doc.getMetadata().get("allergen_type"));
		assertNull("allergen_uuid absent for non-coded allergens",
		        doc.getMetadata().get("allergen_uuid"));
		assertNull("allergen_name absent for non-coded allergens",
		        doc.getMetadata().get("allergen_name"));
	}

	@Test
	public void serialize_nullAllergenType_omittedFromTextAndMetadata() {
		Allergy allergy = allergy(allergen(null, concept("Latex"), null));

		QueryDocument doc = serializer.serialize(allergy);

		assertEquals("Allergy: Latex", doc.getText());
		assertNull(doc.getMetadata().get("allergen_type"));
	}

	@Test
	public void serialize_nullSeverityAndNoReactions_omittedFromTextAndMetadata() {
		Allergy allergy = allergy(allergen(AllergenType.ENVIRONMENT, concept("Pollen"), null));

		QueryDocument doc = serializer.serialize(allergy);

		assertEquals("Allergy: Pollen (environment allergen)", doc.getText());
		assertNull(doc.getMetadata().get("severity"));
		assertNull(doc.getMetadata().get("reactions"));
	}

	@Test
	public void serialize_reactionWithNonCodedFallback_usesNonCodedText() {
		Allergy allergy = allergy(allergen(AllergenType.DRUG, concept("Aspirin"), null));
		AllergyReaction nonCodedReaction = new AllergyReaction();
		nonCodedReaction.setReactionNonCoded("Lip tingling");
		AllergyReaction codedReaction = new AllergyReaction();
		codedReaction.setReaction(concept("Hives"));
		allergy.setReactions(new ArrayList<>(Arrays.asList(codedReaction, nonCodedReaction)));

		QueryDocument doc = serializer.serialize(allergy);

		assertEquals("Allergy: Aspirin (drug allergen). Reactions: Hives, Lip tingling", doc.getText());
		assertEquals(Arrays.asList("Hives", "Lip tingling"), doc.getMetadata().get("reactions"));
	}

	@Test
	public void serialize_reactionListWithNullAndBlankEntries_skipsThem() {
		Allergy allergy = allergy(allergen(AllergenType.DRUG, concept("Aspirin"), null));
		AllergyReaction good = new AllergyReaction();
		good.setReaction(concept("Hives"));
		AllergyReaction blank = new AllergyReaction();
		blank.setReactionNonCoded("   ");
		AllergyReaction empty = new AllergyReaction();
		allergy.setReactions(new ArrayList<>(Arrays.asList(good, null, blank, empty)));

		QueryDocument doc = serializer.serialize(allergy);

		assertEquals("Allergy: Aspirin (drug allergen). Reactions: Hives", doc.getText());
		assertEquals(Arrays.asList("Hives"), doc.getMetadata().get("reactions"));
	}

	@Test
	public void serialize_severityWithoutReactions_textOmitsReactionsClause() {
		Allergy allergy = allergy(allergen(AllergenType.DRUG, concept("Penicillin"), null));
		allergy.setSeverity(concept("Severe"));

		QueryDocument doc = serializer.serialize(allergy);

		assertEquals("Allergy: Penicillin (drug allergen). Severity: Severe", doc.getText());
		assertNull(doc.getMetadata().get("reactions"));
	}

	@Test
	public void serialize_reactionsWithoutSeverity_textOmitsSeverityClause() {
		Allergy allergy = allergy(allergen(AllergenType.DRUG, concept("Penicillin"), null));
		allergy.setReactions(reactions("Hives"));

		QueryDocument doc = serializer.serialize(allergy);

		assertEquals("Allergy: Penicillin (drug allergen). Reactions: Hives", doc.getText());
		assertNull(doc.getMetadata().get("severity"));
	}

	@Test
	public void serialize_nonCodedAllergenStoredAsSentinelConcept_routedToNonCodedPath() {
		// Core stores non-coded allergens by setting codedAllergen to the "Other (non-coded)"
		// sentinel concept and putting the free text on nonCodedAllergen. Allergen.isCoded()
		// returns false in this state. The serializer must treat this as non-coded.
		Concept sentinel = concept("Other");
		sentinel.setUuid("sentinel-uuid");
		Allergen.setOtherNonCodedConceptUuid("sentinel-uuid");
		try {
			Allergen allergen = new Allergen();
			allergen.setAllergenType(AllergenType.OTHER);
			allergen.setCodedAllergen(sentinel);
			allergen.setNonCodedAllergen("Patient-reported herbal mixture");
			Allergy allergy = allergy(allergen);

			QueryDocument doc = serializer.serialize(allergy);

			assertEquals("Allergy: Patient-reported herbal mixture (other allergen)", doc.getText());
			assertEquals("Patient-reported herbal mixture",
			        doc.getMetadata().get("allergen_non_coded"));
			assertNull("sentinel concept UUID must not leak into allergen_uuid",
			        doc.getMetadata().get("allergen_uuid"));
			assertNull("sentinel concept name must not leak into allergen_name",
			        doc.getMetadata().get("allergen_name"));
		} finally {
			Allergen.setOtherNonCodedConceptUuid(null);
		}
	}

	@Test
	public void serialize_comment_storedAsMetadataNotInText() {
		Allergy allergy = allergy(allergen(AllergenType.DRUG, concept("Penicillin"), null));
		allergy.setComment("  Reported by patient at intake  ");

		QueryDocument doc = serializer.serialize(allergy);

		assertEquals("Allergy: Penicillin (drug allergen)", doc.getText());
		assertEquals("Reported by patient at intake", doc.getMetadata().get("comment"));
	}

	@Test
	public void serialize_synonymsPopulatedFromCodedAllergen() {
		Concept c = new Concept();
		c.addName(preferredName("Penicillin"));
		c.addName(conceptName("Penicillin G"));
		c.addName(conceptName("Benzylpenicillin"));

		Allergy allergy = allergy(allergen(AllergenType.DRUG, c, null));

		QueryDocument doc = serializer.serialize(allergy);

		assertEquals("Penicillin", doc.getMetadata().get("allergen_name"));
		assertEquals(Arrays.asList("Benzylpenicillin", "Penicillin G"),
		        doc.getMetadata().get("synonyms"));
	}

	@Test
	public void serialize_description_populatedInMetadataWhenAllergenIsCoded() {
		// Allergy serializer was retrofitted to call putDescription after the synonyms write so
		// allergy records pick up the same BM25 vocabulary bridge that obs/condition/diagnosis
		// records get. Without this assertion, a future refactor that drops the putDescription
		// call from AllergyRecordSerializer.populate would silently regress description-driven
		// retrieval for allergen-coded records.
		Concept c = new Concept();
		c.addName(preferredName("Penicillin"));
		org.openmrs.ConceptDescription d = new org.openmrs.ConceptDescription();
		d.setDescription("Beta-lactam antibiotic; common cause of anaphylactic hypersensitivity.");
		d.setLocale(java.util.Locale.ENGLISH);
		c.addDescription(d);

		QueryDocument doc = serializer.serialize(allergy(allergen(AllergenType.DRUG, c, null)));

		assertEquals("Beta-lactam antibiotic; common cause of anaphylactic hypersensitivity.",
				doc.getMetadata().get("description"));
	}

	@Test
	public void serialize_description_absentWhenAllergenIsNonCoded() {
		// Non-coded allergens have no Concept to source a description from. The metadata key
		// must stay absent so toLuceneDocument/toSource skip the TextField/source.put branch.
		QueryDocument doc = serializer.serialize(allergy(allergen(AllergenType.OTHER, null, "Bee stings")));

		assertNull("description key must be absent for non-coded allergens",
				doc.getMetadata().get("description"));
	}

	@Test
	public void serialize_encounterContext_populated() {
		EncounterType type = new EncounterType();
		type.setUuid("etype-uuid");
		type.setName("Adult Outpatient Visit");
		Encounter enc = new Encounter();
		enc.setUuid("enc-uuid");
		enc.setEncounterType(type);

		Allergy allergy = allergy(allergen(AllergenType.FOOD, concept("Peanut"), null));
		allergy.setEncounter(enc);

		QueryDocument doc = serializer.serialize(allergy);

		assertEquals("enc-uuid", doc.getMetadata().get("encounter_uuid"));
		assertEquals("etype-uuid", doc.getMetadata().get("encounter_type_uuid"));
		assertEquals("Adult Outpatient Visit", doc.getMetadata().get("encounter_type_name"));
	}

	@Test
	public void serialize_emptyAllergy_returnsNull() {
		assertNull(serializer.serialize(new Allergy()));
	}

	@Test
	public void serialize_allergenWithNoUsableName_returnsNull() {
		Allergy allergy = allergy(allergen(AllergenType.OTHER, null, "   "));
		assertNull(serializer.serialize(allergy));
	}

	@Test
	public void serialize_carriesPatientAndResourceUuids() {
		Patient patient = new Patient();
		patient.setUuid("patient-uuid");
		Allergy allergy = allergy(allergen(AllergenType.DRUG, concept("Penicillin"), null));
		allergy.setUuid("allergy-uuid");
		allergy.setPatient(patient);

		QueryDocument doc = serializer.serialize(allergy);

		assertEquals("patient-uuid", doc.getPatientUuid());
		assertEquals("allergy-uuid", doc.getResourceUuid());
	}

	@Test
	public void serialize_datesTheRecordByItsCreationAndTheEventByItsEncounter() {
		Allergy allergy = allergy(allergen(AllergenType.DRUG, concept("Penicillin"), null));
		allergy.setDateCreated(java.sql.Timestamp.valueOf("2026-03-04 10:00:00"));
		Encounter encounter = new Encounter();
		encounter.setEncounterDatetime(java.sql.Timestamp.valueOf("2025-11-20 09:30:00"));
		allergy.setEncounter(encounter);

		QueryDocument doc = serializer.serialize(allergy);

		assertEquals(java.time.LocalDate.of(2026, 3, 4), doc.getDate());
		assertEquals("2025-11-20", doc.getMetadata().get(FIELD_CLINICAL_DATE));
		assertEquals(DATE_KIND_ADMINISTRATIVE, doc.getMetadata().get(FIELD_DATE_KIND));
	}

	@Test
	public void serialize_leavesTheEventUndatedWithoutAnEncounter() {
		Allergy allergy = allergy(allergen(AllergenType.DRUG, concept("Penicillin"), null));
		allergy.setDateCreated(java.sql.Timestamp.valueOf("2026-03-04 10:00:00"));

		QueryDocument doc = serializer.serialize(allergy);

		assertEquals(java.time.LocalDate.of(2026, 3, 4), doc.getDate());
		assertNull(doc.getMetadata().get(FIELD_CLINICAL_DATE));
	}

	private static Allergy allergy(Allergen allergen) {
		Allergy a = new Allergy();
		a.setAllergen(allergen);
		a.setDateCreated(new Date());
		return a;
	}

	private static Allergen allergen(AllergenType type, Concept coded, String nonCoded) {
		return new Allergen(type, coded, nonCoded);
	}

	private static List<AllergyReaction> reactions(String... names) {
		List<AllergyReaction> result = new ArrayList<>(names.length);
		for (String name : names) {
			AllergyReaction r = new AllergyReaction();
			r.setReaction(concept(name));
			result.add(r);
		}
		return result;
	}

}
