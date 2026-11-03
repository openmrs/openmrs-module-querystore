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
import static org.junit.Assert.assertTrue;
import static org.openmrs.module.querystore.serialization.ConceptFixtures.concept;
import static org.openmrs.module.querystore.serialization.DateFixtures.utcDate;
import static org.openmrs.module.querystore.serialization.EncounterFixtures.encounterWithProvider;
import static org.openmrs.module.querystore.serialization.ProviderFixtures.providerNamed;

import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.MedicationDispense;
import org.openmrs.OrderFrequency;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.module.querystore.model.QueryDocument;

public class MedicationDispenseRecordSerializerTest {

	private MedicationDispenseRecordSerializer serializer;

	@Before
	public void setUp() {
		serializer = new MedicationDispenseRecordSerializer();
	}

	@Test
	public void serialize_fullDispense_matchesAdrTextShape() {
		MedicationDispense dispense = dispense(concept("Metformin"), drug("Metformin 500mg", "drug-uuid"));
		dispense.setStatus(concept("Completed"));
		dispense.setQuantity(60.0);
		dispense.setQuantityUnits(concept("Tablet(s)"));
		dispense.setDose(1.0);
		dispense.setDoseUnits(concept("Tablet(s)"));
		dispense.setRoute(concept("Oral"));
		dispense.setFrequency(frequency("twice daily", "freq-uuid"));
		dispense.setDateHandedOver(utcDate(2025, Calendar.JANUARY, 10));

		QueryDocument doc = serializer.serialize(dispense);

		assertEquals("medication_dispense", doc.getResourceType());
		assertEquals(
				"Dispensed: Metformin 500mg. Status: Completed. Quantity: 60.0 Tablet(s)."
				        + " Dose: 1.0 Tablet(s) Oral twice daily. Handed over: 2025-01-10",
				doc.getText());
		assertEquals("2025-01-10", doc.getMetadata().get("clinical_date"));
		assertEquals("clinical_event", doc.getMetadata().get("date_kind"));
	}

	@Test
	public void serialize_codedFields_populateBothUuidAndName() {
		Concept concept = concept("Metformin");
		concept.setUuid("concept-uuid");
		Concept doseUnits = concept("Tablet(s)");
		doseUnits.setUuid("dose-units-uuid");

		MedicationDispense dispense = dispense(concept, drug("Metformin 500mg", "drug-uuid"));
		dispense.setDose(1.0);
		dispense.setDoseUnits(doseUnits);

		QueryDocument doc = serializer.serialize(dispense);

		assertEquals("concept-uuid", doc.getMetadata().get("concept_uuid"));
		assertEquals("Metformin", doc.getMetadata().get("concept_name"));
		assertEquals("drug-uuid", doc.getMetadata().get("drug_uuid"));
		assertEquals("Metformin 500mg", doc.getMetadata().get("drug_name"));
		assertEquals(1.0, doc.getMetadata().get("dose"));
		assertEquals("dose-units-uuid", doc.getMetadata().get("dose_units_uuid"));
		assertEquals("Tablet(s)", doc.getMetadata().get("dose_units"));
	}

	@Test
	public void serialize_status_populatedFromConceptName() {
		MedicationDispense dispense = dispense(concept("Metformin"), null);
		dispense.setStatus(concept("In Progress"));

		QueryDocument doc = serializer.serialize(dispense);

		assertEquals("In Progress", doc.getMetadata().get("status"));
		assertTrue(doc.getText().contains("Status: In Progress"));
	}

	@Test
	public void serialize_drugOrder_populatesDrugOrderUuid() {
		DrugOrder originalOrder = new DrugOrder();
		originalOrder.setUuid("drug-order-uuid");

		MedicationDispense dispense = dispense(concept("Metformin"), null);
		dispense.setDrugOrder(originalOrder);

		QueryDocument doc = serializer.serialize(dispense);

		assertEquals("drug-order-uuid", doc.getMetadata().get("drug_order_uuid"));
	}

	@Test
	public void serialize_dateHandedOver_appearsInTextAndMetadata() {
		MedicationDispense dispense = dispense(concept("Metformin"), null);
		dispense.setDateHandedOver(utcDate(2025, Calendar.JANUARY, 10));

		QueryDocument doc = serializer.serialize(dispense);

		assertTrue(doc.getText().contains("Handed over: 2025-01-10"));
		assertEquals("2025-01-10", doc.getMetadata().get("date_handed_over"));
	}

	@Test
	public void serialize_substitutionFields_populated() {
		Concept substitutionType = concept("Therapeutic substitution");
		substitutionType.setUuid("sub-type-uuid");
		Concept substitutionReason = concept("Out of stock");
		substitutionReason.setUuid("sub-reason-uuid");

		MedicationDispense dispense = dispense(concept("Metformin"), null);
		dispense.setWasSubstituted(Boolean.TRUE);
		dispense.setSubstitutionType(substitutionType);
		dispense.setSubstitutionReason(substitutionReason);

		QueryDocument doc = serializer.serialize(dispense);

		assertEquals(Boolean.TRUE, doc.getMetadata().get("was_substituted"));
		assertEquals("sub-type-uuid", doc.getMetadata().get("substitution_type_uuid"));
		assertEquals("Therapeutic substitution", doc.getMetadata().get("substitution_type"));
		assertEquals("sub-reason-uuid", doc.getMetadata().get("substitution_reason_uuid"));
		assertEquals("Out of stock", doc.getMetadata().get("substitution_reason"));
	}

	@Test
	public void serialize_wasSubstitutedFalse_storedAsFalseWithoutTypeOrReason() {
		MedicationDispense dispense = dispense(concept("Metformin"), null);
		dispense.setWasSubstituted(Boolean.FALSE);

		QueryDocument doc = serializer.serialize(dispense);

		assertEquals(Boolean.FALSE, doc.getMetadata().get("was_substituted"));
		assertNull(doc.getMetadata().get("substitution_type_uuid"));
		assertNull(doc.getMetadata().get("substitution_reason_uuid"));
	}

	@Test
	public void serialize_routeAndFrequencyWithoutDose_inMetadataNotText() {
		MedicationDispense dispense = dispense(concept("Metformin"), null);
		dispense.setRoute(concept("Oral"));
		dispense.setFrequency(frequency("twice daily", "freq-uuid"));

		QueryDocument doc = serializer.serialize(dispense);

		assertEquals("Oral", doc.getMetadata().get("route"));
		assertEquals("twice daily", doc.getMetadata().get("frequency"));
		assertFalse("route should not appear in text without a dose",
				doc.getText().contains("Oral"));
		assertFalse("frequency should not appear in text without a dose",
				doc.getText().contains("twice daily"));
	}

	@Test
	public void serialize_substitutionMetadata_notInText() {
		Concept substitutionType = concept("Therapeutic substitution");

		MedicationDispense dispense = dispense(concept("Metformin"), null);
		dispense.setWasSubstituted(Boolean.TRUE);
		dispense.setSubstitutionType(substitutionType);

		QueryDocument doc = serializer.serialize(dispense);

		assertFalse("substitution fields are metadata-only",
				doc.getText().contains("Therapeutic substitution"));
	}

	@Test
	public void serialize_dispenser_isParallelToEncounterProvider() {
		Provider encounterProvider = providerNamed("encounter-provider-uuid", "Dr.", "Ochieng");
		Encounter enc = encounterWithProvider("enc-uuid", encounterProvider);
		Provider dispenser = providerNamed("dispenser-uuid", "Pharm.", "Wanjiku");

		MedicationDispense dispense = dispense(concept("Metformin"), null);
		dispense.setEncounter(enc);
		dispense.setDispenser(dispenser);

		QueryDocument doc = serializer.serialize(dispense);

		assertEquals("encounter-provider-uuid", doc.getMetadata().get("provider_uuid"));
		assertEquals("Dr. Ochieng", doc.getMetadata().get("provider_name"));
		assertEquals("dispenser-uuid", doc.getMetadata().get("dispenser_uuid"));
		assertEquals("Pharm. Wanjiku", doc.getMetadata().get("dispenser_name"));
	}

	@Test
	public void serialize_dispenseLocation_overridesEncounterLocation() {
		Location encounterLocation = new Location();
		encounterLocation.setUuid("enc-location-uuid");
		encounterLocation.setName("Clinic A");
		Encounter enc = new Encounter();
		enc.setUuid("enc-uuid");
		enc.setLocation(encounterLocation);

		Location dispenseLocation = new Location();
		dispenseLocation.setUuid("dispense-location-uuid");
		dispenseLocation.setName("Pharmacy B");

		MedicationDispense dispense = dispense(concept("Metformin"), null);
		dispense.setEncounter(enc);
		dispense.setLocation(dispenseLocation);

		QueryDocument doc = serializer.serialize(dispense);

		assertEquals("dispense-location-uuid", doc.getMetadata().get("location_uuid"));
		assertEquals("Pharmacy B", doc.getMetadata().get("location_name"));
	}

	@Test
	public void serialize_noDispenseLocation_keepsEncounterLocation() {
		Location encounterLocation = new Location();
		encounterLocation.setUuid("enc-location-uuid");
		encounterLocation.setName("Clinic A");
		Encounter enc = new Encounter();
		enc.setUuid("enc-uuid");
		enc.setLocation(encounterLocation);

		MedicationDispense dispense = dispense(concept("Metformin"), null);
		dispense.setEncounter(enc);

		QueryDocument doc = serializer.serialize(dispense);

		assertEquals("enc-location-uuid", doc.getMetadata().get("location_uuid"));
		assertEquals("Clinic A", doc.getMetadata().get("location_name"));
	}

	@Test
	public void serialize_dosingInstructions_inMetadataAndText() {
		MedicationDispense dispense = dispense(concept("Metformin"), null);
		dispense.setDosingInstructions("Take with food");

		QueryDocument doc = serializer.serialize(dispense);

		assertEquals("Take with food", doc.getMetadata().get("dosing_instructions"));
		assertTrue(doc.getText().endsWith("Take with food"));
	}

	@Test
	public void serialize_asNeededTrue_appearsAsPrnInText() {
		MedicationDispense dispense = dispense(concept("Ibuprofen"), null);
		dispense.setDose(200.0);
		dispense.setAsNeeded(Boolean.TRUE);

		QueryDocument doc = serializer.serialize(dispense);

		assertTrue(doc.getText().contains("PRN"));
	}

	@Test
	public void serialize_encounterContext_populated() {
		EncounterType type = new EncounterType();
		type.setUuid("etype-uuid");
		type.setName("Pharmacy Dispense");
		Encounter enc = new Encounter();
		enc.setUuid("enc-uuid");
		enc.setEncounterType(type);

		MedicationDispense dispense = dispense(concept("Metformin"), null);
		dispense.setEncounter(enc);

		QueryDocument doc = serializer.serialize(dispense);

		assertEquals("enc-uuid", doc.getMetadata().get("encounter_uuid"));
		assertEquals("Pharmacy Dispense", doc.getMetadata().get("encounter_type_name"));
	}

	@Test
	public void serialize_displayNameFallsBackToConceptWhenDrugAbsent() {
		MedicationDispense dispense = dispense(concept("Metformin"), null);

		QueryDocument doc = serializer.serialize(dispense);

		assertTrue(doc.getText().startsWith("Dispensed: Metformin"));
		assertNull(doc.getMetadata().get("drug_uuid"));
		assertNull(doc.getMetadata().get("drug_name"));
	}

	@Test
	public void serialize_emptyDispense_returnsNull() {
		MedicationDispense dispense = new MedicationDispense();
		assertNull(serializer.serialize(dispense));
	}

	@Test
	public void serialize_carriesPatientAndResourceUuids() {
		Patient patient = new Patient();
		patient.setUuid("patient-uuid");

		MedicationDispense dispense = dispense(concept("Metformin"), null);
		dispense.setUuid("dispense-uuid");
		dispense.setPatient(patient);

		QueryDocument doc = serializer.serialize(dispense);

		assertEquals("patient-uuid", doc.getPatientUuid());
		assertEquals("dispense-uuid", doc.getResourceUuid());
	}

	private static MedicationDispense dispense(Concept concept, Drug drug) {
		MedicationDispense d = new MedicationDispense();
		d.setConcept(concept);
		if (drug != null) {
			d.setDrug(drug);
		}
		d.setDateCreated(new Date());
		return d;
	}

	private static Drug drug(String name, String uuid) {
		Drug d = new Drug();
		d.setName(name);
		d.setUuid(uuid);
		return d;
	}

	private static OrderFrequency frequency(String name, String uuid) {
		OrderFrequency f = new OrderFrequency();
		f.setConcept(concept(name));
		f.setUuid(uuid);
		return f;
	}

}
