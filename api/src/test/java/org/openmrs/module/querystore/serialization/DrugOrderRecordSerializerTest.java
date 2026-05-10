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

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterType;
import org.openmrs.Order;
import org.openmrs.OrderFrequency;
// Reflection is used to set Order.orderNumber and Order.dateStopped, which core only assigns
// through OrderService when an order is saved or discontinued — no public setters exist.
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.module.querystore.model.QueryDocument;

public class DrugOrderRecordSerializerTest {

	private DrugOrderRecordSerializer serializer;

	@Before
	public void setUp() {
		serializer = new DrugOrderRecordSerializer();
	}

	@Test
	public void serialize_fullDrugOrder_matchesAdrTextShape() {
		DrugOrder order = order(concept("Metformin"), drug("Metformin 500mg", "drug-uuid"));
		order.setDose(1.0);
		order.setDoseUnits(concept("Tablet(s)"));
		order.setRoute(concept("Oral"));
		order.setFrequency(frequency("twice daily", "freq-uuid"));
		order.setDuration(30);
		order.setDurationUnits(concept("Day(s)"));
		order.setQuantity(60.0);
		order.setQuantityUnits(concept("Tablet(s)"));
		order.setAction(Order.Action.NEW);
		order.setUrgency(Order.Urgency.ROUTINE);
		order.setDosingInstructions("Take with food");

		QueryDocument doc = serializer.serialize(order);

		assertEquals("drug_order", doc.getResourceType());
		assertEquals(
				"Drug order: Metformin 500mg. Dose: 1.0 Tablet(s) Oral twice daily."
				        + " Duration: 30 Day(s). Quantity: 60.0 Tablet(s). Action: NEW."
				        + " Urgency: ROUTINE. Take with food",
				doc.getText());
	}

	@Test
	public void serialize_codedFields_populateBothUuidAndName() {
		Concept concept = concept("Metformin");
		concept.setUuid("concept-uuid");
		Concept doseUnits = concept("Tablet(s)");
		doseUnits.setUuid("dose-units-uuid");
		Concept route = concept("Oral");
		route.setUuid("route-uuid");

		DrugOrder order = order(concept, drug("Metformin 500mg", "drug-uuid"));
		order.setDose(1.0);
		order.setDoseUnits(doseUnits);
		order.setRoute(route);
		order.setFrequency(frequency("twice daily", "freq-uuid"));

		QueryDocument doc = serializer.serialize(order);

		assertEquals("concept-uuid", doc.getMetadata().get("concept_uuid"));
		assertEquals("Metformin", doc.getMetadata().get("concept_name"));
		assertEquals("drug-uuid", doc.getMetadata().get("drug_uuid"));
		assertEquals("Metformin 500mg", doc.getMetadata().get("drug_name"));
		assertEquals(1.0, doc.getMetadata().get("dose"));
		assertEquals("dose-units-uuid", doc.getMetadata().get("dose_units_uuid"));
		assertEquals("Tablet(s)", doc.getMetadata().get("dose_units"));
		assertEquals("route-uuid", doc.getMetadata().get("route_uuid"));
		assertEquals("Oral", doc.getMetadata().get("route"));
		assertEquals("freq-uuid", doc.getMetadata().get("frequency_uuid"));
		assertEquals("twice daily", doc.getMetadata().get("frequency"));
	}

	@Test
	public void serialize_actionAndUrgency_useEnumName() {
		DrugOrder order = order(concept("Aspirin"), null);
		order.setAction(Order.Action.REVISE);
		order.setUrgency(Order.Urgency.STAT);

		QueryDocument doc = serializer.serialize(order);

		assertEquals("REVISE", doc.getMetadata().get("action"));
		assertEquals("STAT", doc.getMetadata().get("urgency"));
		assertTrue(doc.getText().contains("Action: REVISE"));
		assertTrue(doc.getText().contains("Urgency: STAT"));
	}

	@Test
	public void serialize_asNeededWithCondition_appearsAsPrnInText() {
		DrugOrder order = order(concept("Ibuprofen"), null);
		order.setDose(200.0);
		order.setAsNeeded(Boolean.TRUE);
		order.setAsNeededCondition("pain");

		QueryDocument doc = serializer.serialize(order);

		assertTrue(doc.getText().contains("PRN for pain"));
		assertEquals(Boolean.TRUE, doc.getMetadata().get("as_needed"));
		assertEquals("pain", doc.getMetadata().get("as_needed_condition"));
	}

	@Test
	public void serialize_asNeededFalse_omitsPrnFromText() {
		DrugOrder order = order(concept("Aspirin"), null);
		order.setAsNeeded(Boolean.FALSE);

		QueryDocument doc = serializer.serialize(order);

		assertFalse("PRN should not appear when as_needed is false", doc.getText().contains("PRN"));
		assertEquals(Boolean.FALSE, doc.getMetadata().get("as_needed"));
	}

	@Test
	public void serialize_dosingInstructions_inMetadataAndText() {
		DrugOrder order = order(concept("Metformin"), null);
		order.setDosingInstructions("Take with food");

		QueryDocument doc = serializer.serialize(order);

		assertEquals("Take with food", doc.getMetadata().get("dosing_instructions"));
		assertTrue(doc.getText().endsWith("Take with food"));
	}

	@Test
	public void serialize_careSetting_usesName() {
		CareSetting outpatient = new CareSetting();
		outpatient.setName("Outpatient");

		DrugOrder order = order(concept("Metformin"), null);
		order.setCareSetting(outpatient);

		QueryDocument doc = serializer.serialize(order);

		assertEquals("Outpatient", doc.getMetadata().get("care_setting"));
	}

	@Test
	public void serialize_previousOrder_populatesUuid() {
		DrugOrder previous = new DrugOrder();
		previous.setUuid("previous-order-uuid");

		DrugOrder order = order(concept("Metformin"), null);
		order.setPreviousOrder(previous);

		QueryDocument doc = serializer.serialize(order);

		assertEquals("previous-order-uuid", doc.getMetadata().get("previous_order_uuid"));
	}

	@Test
	public void serialize_orderNumberAndRefills_populated() {
		DrugOrder order = order(concept("Metformin"), null);
		setOrderField(order, "orderNumber", "ORD-1234");
		order.setNumRefills(3);

		QueryDocument doc = serializer.serialize(order);

		assertEquals("ORD-1234", doc.getMetadata().get("order_number"));
		assertEquals(3, doc.getMetadata().get("num_refills"));
	}

	@Test
	public void serialize_dateStopped_appearsInTextAndMetadata() {
		DrugOrder order = order(concept("Metformin"), null);
		setOrderField(order, "dateStopped", utcDate(2025, Calendar.FEBRUARY, 10));

		QueryDocument doc = serializer.serialize(order);

		assertTrue(doc.getText().contains("Stopped: 2025-02-10"));
		assertEquals("2025-02-10", doc.getMetadata().get("date_stopped"));
	}

	@Test
	public void serialize_autoExpireDate_inMetadataNotText() {
		DrugOrder order = order(concept("Metformin"), null);
		order.setAutoExpireDate(utcDate(2025, Calendar.FEBRUARY, 9));

		QueryDocument doc = serializer.serialize(order);

		assertEquals("2025-02-09", doc.getMetadata().get("auto_expire_date"));
		assertFalse("auto_expire_date is metadata-only, not in text",
				doc.getText().contains("2025-02-09"));
	}

	@Test
	public void serialize_displayNameFallsBackToConceptWhenDrugAbsent() {
		DrugOrder order = order(concept("Metformin"), null);

		QueryDocument doc = serializer.serialize(order);

		assertTrue(doc.getText().startsWith("Drug order: Metformin"));
		assertNull(doc.getMetadata().get("drug_uuid"));
		assertNull(doc.getMetadata().get("drug_name"));
	}

	@Test
	public void serialize_orderer_populatesProviderUuidAndName() {
		Provider orderer = providerNamed("orderer-uuid", "Dr.", "Ochieng");

		DrugOrder order = order(concept("Metformin"), null);
		order.setOrderer(orderer);

		QueryDocument doc = serializer.serialize(order);

		assertEquals("orderer-uuid", doc.getMetadata().get("provider_uuid"));
		assertEquals("Dr. Ochieng", doc.getMetadata().get("provider_name"));
	}

	@Test
	public void serialize_orderer_overridesEncounterProvider() {
		Provider encounterProvider = providerNamed("encounter-provider-uuid", "Nurse", "Akinyi");
		Encounter enc = new Encounter();
		enc.setUuid("enc-uuid");
		EncounterProvider ep = new EncounterProvider();
		ep.setProvider(encounterProvider);
		Set<EncounterProvider> providers = new HashSet<>();
		providers.add(ep);
		enc.setEncounterProviders(providers);

		Provider orderer = providerNamed("orderer-uuid", "Dr.", "Ochieng");

		DrugOrder order = order(concept("Metformin"), null);
		order.setEncounter(enc);
		order.setOrderer(orderer);

		QueryDocument doc = serializer.serialize(order);

		assertEquals("orderer-uuid", doc.getMetadata().get("provider_uuid"));
		assertEquals("Dr. Ochieng", doc.getMetadata().get("provider_name"));
	}

	@Test
	public void serialize_encounterContext_populated() {
		EncounterType type = new EncounterType();
		type.setUuid("etype-uuid");
		type.setName("Adult Outpatient Visit");
		Encounter enc = new Encounter();
		enc.setUuid("enc-uuid");
		enc.setEncounterType(type);

		DrugOrder order = order(concept("Metformin"), null);
		order.setEncounter(enc);

		QueryDocument doc = serializer.serialize(order);

		assertEquals("enc-uuid", doc.getMetadata().get("encounter_uuid"));
		assertEquals("etype-uuid", doc.getMetadata().get("encounter_type_uuid"));
		assertEquals("Adult Outpatient Visit", doc.getMetadata().get("encounter_type_name"));
	}

	@Test
	public void serialize_emptyOrder_returnsNull() {
		DrugOrder order = new DrugOrder();
		assertNull(serializer.serialize(order));
	}

	@Test
	public void serialize_carriesPatientAndResourceUuids() {
		Patient patient = new Patient();
		patient.setUuid("patient-uuid");

		DrugOrder order = order(concept("Metformin"), null);
		order.setUuid("order-uuid");
		order.setPatient(patient);

		QueryDocument doc = serializer.serialize(order);

		assertEquals("patient-uuid", doc.getPatientUuid());
		assertEquals("order-uuid", doc.getResourceUuid());
	}

	private static DrugOrder order(Concept concept, Drug drug) {
		DrugOrder order = new DrugOrder();
		order.setConcept(concept);
		if (drug != null) {
			order.setDrug(drug);
		}
		order.setDateActivated(new Date());
		return order;
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

	private static Concept concept(String name) {
		Concept c = new Concept();
		c.addName(conceptName(name));
		return c;
	}

	private static ConceptName conceptName(String name) {
		ConceptName cn = new ConceptName();
		cn.setName(name);
		cn.setLocale(Locale.ENGLISH);
		return cn;
	}

	private static Provider providerNamed(String uuid, String givenName, String familyName) {
		PersonName personName = new PersonName();
		personName.setGivenName(givenName);
		personName.setFamilyName(familyName);
		Person person = new Person();
		person.addName(personName);
		Provider p = new Provider();
		p.setUuid(uuid);
		p.setPerson(person);
		return p;
	}

	private static void setOrderField(Order order, String fieldName, Object value) {
		try {
			Field f = Order.class.getDeclaredField(fieldName);
			f.setAccessible(true);
			f.set(order, value);
		}
		catch (ReflectiveOperationException e) {
			throw new AssertionError("Failed to set Order." + fieldName, e);
		}
	}

	private static Date utcDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.set(year, month, day, 12, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
}
