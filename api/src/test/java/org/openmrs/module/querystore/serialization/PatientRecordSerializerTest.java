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
import static org.openmrs.module.querystore.serialization.DateFixtures.utcDate;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.module.querystore.model.QueryDocument;

public class PatientRecordSerializerTest {

	private PatientRecordSerializer serializer;

	@Before
	public void setUp() {
		serializer = new PatientRecordSerializer();
	}

	@Test
	public void serialize_fullPatient_matchesAdrExample() {
		Patient patient = patient("8a7b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
		        utcDate(2018, Calendar.APRIL, 22));
		patient.addName(name("Achieng", null, "Otieno"));
		patient.setGender("F");
		patient.setBirthdate(utcDate(1982, Calendar.JULY, 14));
		patient.setBirthdateEstimated(false);
		patient.setDead(false);
		patient.addIdentifier(identifier(1,
		        identifierType("a5d38e09-efcb-4d91-a526-50ce1ba5011a", "MRN"),
		        "100023", true, location("a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d")));
		patient.addIdentifier(identifier(2,
		        identifierType("b6e49f1a-fdcc-5e02-b637-61df2ca6022b", "National ID"),
		        "12345678", false, null));
		patient.addAddress(address(1, null, "Kibera", "Nairobi", null, "Kenya", true));
		patient.setAttributes(attributeSet(attribute(1,
		        attributeType("c7f5a02b-0edd-6f13-c748-72e03db7033c", "Telephone"),
		        "+254712345678")));

		QueryDocument doc = serializer.serialize(patient);

		assertEquals("patient", doc.getResourceType());
		assertEquals("8a7b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d", doc.getResourceUuid());
		assertEquals("8a7b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d", doc.getPatientUuid());
		assertEquals("2018-04-22", doc.getDate().toString());
		assertNull("patient creation is administrative, not a clinical event",
		        doc.getMetadata().get("clinical_date"));
		assertEquals("administrative", doc.getMetadata().get("date_kind"));
		assertEquals("Patient: Achieng Otieno. Female. Born 1982-07-14."
		        + " Address: Kibera, Nairobi, Kenya."
		        + " Identifiers: MRN 100023, National ID 12345678", doc.getText());

		assertEquals("Achieng", doc.getMetadata().get("given_name"));
		assertNull(doc.getMetadata().get("middle_name"));
		assertEquals("Otieno", doc.getMetadata().get("family_name"));
		assertEquals("F", doc.getMetadata().get("gender"));
		assertEquals("1982-07-14", doc.getMetadata().get("birthdate"));
		assertEquals(Boolean.FALSE, doc.getMetadata().get("birthdate_estimated"));
		assertNotNull(doc.getMetadata().get("age_years"));
		assertEquals(Boolean.FALSE, doc.getMetadata().get("dead"));
		assertNull(doc.getMetadata().get("death_date"));
		assertNull(doc.getMetadata().get("cause_of_death_uuid"));

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> ids = (List<Map<String, Object>>) doc.getMetadata().get("identifiers");
		assertEquals(2, ids.size());
		Map<String, Object> first = ids.get(0);
		assertEquals("a5d38e09-efcb-4d91-a526-50ce1ba5011a", first.get("type_uuid"));
		assertEquals("MRN", first.get("type_name"));
		assertEquals("100023", first.get("value"));
		assertEquals(Boolean.TRUE, first.get("preferred"));
		assertEquals("a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d", first.get("location_uuid"));
		Map<String, Object> second = ids.get(1);
		assertEquals("National ID", second.get("type_name"));
		assertEquals("12345678", second.get("value"));
		assertEquals(Boolean.FALSE, second.get("preferred"));
		assertFalse("location_uuid omitted when identifier has no location",
		        second.containsKey("location_uuid"));

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> addresses = (List<Map<String, Object>>) doc.getMetadata().get("addresses");
		assertEquals(1, addresses.size());
		Map<String, Object> addr = addresses.get(0);
		assertFalse("address1 omitted when null", addr.containsKey("address1"));
		assertEquals("Kibera", addr.get("city_village"));
		assertEquals("Nairobi", addr.get("state_province"));
		assertFalse("postal_code omitted when null", addr.containsKey("postal_code"));
		assertEquals("Kenya", addr.get("country"));
		assertEquals(Boolean.TRUE, addr.get("preferred"));

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> attrs = (List<Map<String, Object>>) doc.getMetadata().get("attributes");
		assertEquals(1, attrs.size());
		assertEquals("Telephone", attrs.get(0).get("type_name"));
		assertEquals("+254712345678", attrs.get(0).get("value"));
	}

	@Test
	public void serialize_missingAllNameComponents_returnsNull() {
		Patient patient = patient("p-uuid", utcDate(2018, Calendar.APRIL, 22));
		assertNull(serializer.serialize(patient));
	}

	@Test
	public void serialize_onlyFamilyName_textAndMetadataPopulated() {
		Patient patient = patient("p-uuid", utcDate(2018, Calendar.APRIL, 22));
		patient.addName(name(null, null, "Otieno"));

		QueryDocument doc = serializer.serialize(patient);

		assertEquals("Patient: Otieno", doc.getText());
		assertNull(doc.getMetadata().get("given_name"));
		assertEquals("Otieno", doc.getMetadata().get("family_name"));
	}

	@Test
	public void serialize_middleNameIncluded_textRendersAllThree() {
		Patient patient = patient("p-uuid", utcDate(2018, Calendar.APRIL, 22));
		patient.addName(name("Mary", "Wanjiku", "Otieno"));

		QueryDocument doc = serializer.serialize(patient);

		assertEquals("Patient: Mary Wanjiku Otieno", doc.getText());
		assertEquals("Wanjiku", doc.getMetadata().get("middle_name"));
	}

	@Test
	public void serialize_nonStandardGenderCode_passesThroughVerbatim() {
		Patient patient = patient("p-uuid", utcDate(2018, Calendar.APRIL, 22));
		patient.addName(name("Alex", null, "Doe"));
		patient.setGender("nonbinary");

		QueryDocument doc = serializer.serialize(patient);

		assertEquals("Patient: Alex Doe. nonbinary", doc.getText());
		assertEquals("nonbinary", doc.getMetadata().get("gender"));
	}

	@Test
	public void serialize_deadPatientWithCauseOfDeath_populatesMortalityFields() {
		Patient patient = patient("p-uuid", utcDate(2018, Calendar.APRIL, 22));
		patient.addName(name("Achieng", null, "Otieno"));
		patient.setDead(true);
		patient.setDeathDate(utcDate(2024, Calendar.DECEMBER, 15));
		Concept cause = concept("Malaria");
		cause.setUuid("cause-uuid");
		patient.setCauseOfDeath(cause);

		QueryDocument doc = serializer.serialize(patient);

		assertEquals(Boolean.TRUE, doc.getMetadata().get("dead"));
		assertEquals("2024-12-15", doc.getMetadata().get("death_date"));
		assertEquals("cause-uuid", doc.getMetadata().get("cause_of_death_uuid"));
		assertEquals("Malaria", doc.getMetadata().get("cause_of_death_name"));
	}

	@Test
	public void serialize_birthdateAbsent_omitsAgeAndBirthFields() {
		Patient patient = patient("p-uuid", utcDate(2018, Calendar.APRIL, 22));
		patient.addName(name("Alex", null, "Doe"));

		QueryDocument doc = serializer.serialize(patient);

		assertEquals("Patient: Alex Doe", doc.getText());
		assertNull(doc.getMetadata().get("birthdate"));
		assertNull(doc.getMetadata().get("birthdate_estimated"));
		assertNull(doc.getMetadata().get("age_years"));
	}

	@Test
	public void serialize_identifiersSortedByIdAndVoidedFiltered() {
		Patient patient = patient("p-uuid", utcDate(2018, Calendar.APRIL, 22));
		patient.addName(name("Alex", null, "Doe"));
		// Insert in reverse-id order so the sort visibility isn't masked by Set hash order.
		patient.addIdentifier(identifier(2, identifierType("type-2", "National ID"),
		        "B-value", false, null));
		patient.addIdentifier(identifier(1, identifierType("type-1", "MRN"),
		        "A-value", true, null));
		PatientIdentifier voided = identifier(3, identifierType("type-3", "Old"),
		        "C-value", false, null);
		voided.setVoided(true);
		patient.addIdentifier(voided);

		QueryDocument doc = serializer.serialize(patient);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> ids = (List<Map<String, Object>>) doc.getMetadata().get("identifiers");
		assertEquals(2, ids.size());
		assertEquals("type-1", ids.get(0).get("type_uuid"));
		assertEquals("type-2", ids.get(1).get("type_uuid"));
	}

	@Test
	public void serialize_addressesPreferredSortsFirstAndVoidedFiltered() {
		Patient patient = patient("p-uuid", utcDate(2018, Calendar.APRIL, 22));
		patient.addName(name("Alex", null, "Doe"));
		// Non-preferred first by id; preferred added later. Expectation: preferred sorts to slot 0
		// so the citation-baked address clause uses it.
		patient.addAddress(address(1, null, "OtherCity", null, null, "OtherCountry", false));
		patient.addAddress(address(2, null, "Nairobi", null, null, "Kenya", true));
		PersonAddress voided = address(3, null, "Voided", null, null, "Country", false);
		voided.setVoided(true);
		patient.addAddress(voided);

		QueryDocument doc = serializer.serialize(patient);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> addresses = (List<Map<String, Object>>) doc.getMetadata().get("addresses");
		assertEquals(2, addresses.size());
		assertEquals("Nairobi", addresses.get(0).get("city_village"));
		assertEquals(Boolean.TRUE, addresses.get(0).get("preferred"));
		assertEquals("OtherCity", addresses.get(1).get("city_village"));
		assertTrue("text address clause uses preferred address",
		        doc.getText().contains("Address: Nairobi, Kenya"));
	}

	@Test
	public void serialize_addressWithAllStructuredFieldsNull_skipped() {
		Patient patient = patient("p-uuid", utcDate(2018, Calendar.APRIL, 22));
		patient.addName(name("Alex", null, "Doe"));
		patient.addAddress(address(1, null, null, null, null, null, false));

		QueryDocument doc = serializer.serialize(patient);

		assertNull(doc.getMetadata().get("addresses"));
		assertFalse("text omits Address clause when no usable address",
		        doc.getText().contains("Address:"));
	}

	@Test
	public void serialize_attributesUseGetValueNotValueReference() {
		Patient patient = patient("p-uuid", utcDate(2018, Calendar.APRIL, 22));
		patient.addName(name("Alex", null, "Doe"));
		// PersonAttribute predates BaseAttribute and has a plain String value field — the serializer
		// should read it directly, not route through a custom-datatype deserializer.
		patient.setAttributes(attributeSet(
		        attribute(1, attributeType("type-uuid", "Telephone"), "+254712345678")));

		QueryDocument doc = serializer.serialize(patient);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> attrs = (List<Map<String, Object>>) doc.getMetadata().get("attributes");
		assertEquals("+254712345678", attrs.get(0).get("value"));
	}

	@Test
	public void serialize_genderMappedToLabel() {
		Patient male = patient("p-uuid", utcDate(2018, Calendar.APRIL, 22));
		male.addName(name("Bob", null, "Smith"));
		male.setGender("M");

		QueryDocument doc = serializer.serialize(male);

		assertEquals("Patient: Bob Smith. Male", doc.getText());
		assertEquals("M", doc.getMetadata().get("gender"));
	}

	@Test
	public void serialize_identifierClauseSkipsBlankValueIdentifiers() {
		Patient patient = patient("p-uuid", utcDate(2018, Calendar.APRIL, 22));
		patient.addName(name("Alex", null, "Doe"));
		patient.addIdentifier(identifier(1, identifierType("type-uuid", "MRN"),
		        "   ", false, null));

		QueryDocument doc = serializer.serialize(patient);

		assertNull(doc.getMetadata().get("identifiers"));
		assertFalse("text omits Identifiers clause when no usable identifier",
		        doc.getText().contains("Identifiers:"));
	}

	@Test
	public void serialize_identifiersPreferredSortsFirst() {
		Patient patient = patient("p-uuid", utcDate(2018, Calendar.APRIL, 22));
		patient.addName(name("Alex", null, "Doe"));
		// Preferred entry is added second AND has a higher id, so neither insertion order nor
		// id-asc would put it first. Only the preferred-first sort key surfaces it.
		patient.addIdentifier(identifier(1, identifierType("type-other", "National ID"),
		        "B-value", false, null));
		patient.addIdentifier(identifier(2, identifierType("type-mrn", "MRN"),
		        "A-value", true, null));

		QueryDocument doc = serializer.serialize(patient);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> ids = (List<Map<String, Object>>) doc.getMetadata().get("identifiers");
		assertEquals("type-mrn", ids.get(0).get("type_uuid"));
		assertEquals(Boolean.TRUE, ids.get(0).get("preferred"));
		assertTrue("text Identifiers clause leads with the preferred identifier",
		        doc.getText().contains("Identifiers: MRN A-value, National ID B-value"));
	}

	@Test
	public void serialize_deadWithoutDateOrCause_emitsDeadOnly() {
		Patient patient = patient("p-uuid", utcDate(2018, Calendar.APRIL, 22));
		patient.addName(name("Alex", null, "Doe"));
		patient.setDead(true);

		QueryDocument doc = serializer.serialize(patient);

		assertEquals(Boolean.TRUE, doc.getMetadata().get("dead"));
		assertNull(doc.getMetadata().get("death_date"));
		assertNull(doc.getMetadata().get("cause_of_death_uuid"));
		assertNull(doc.getMetadata().get("cause_of_death_name"));
	}

	@Test
	public void serialize_noIdentifiersOrAddresses_omitsBothFieldsAndTextClauses() {
		Patient patient = patient("p-uuid", utcDate(2018, Calendar.APRIL, 22));
		patient.addName(name("Alex", null, "Doe"));

		QueryDocument doc = serializer.serialize(patient);

		assertEquals("Patient: Alex Doe", doc.getText());
		assertNull(doc.getMetadata().get("identifiers"));
		assertNull(doc.getMetadata().get("addresses"));
	}

	@Test
	public void serialize_personNameSelection_skipsVoidedPrefersNonVoided() {
		Patient patient = patient("p-uuid", utcDate(2018, Calendar.APRIL, 22));
		// Voided preferred name + non-voided non-preferred name: getPersonName should return the
		// non-voided one even though the voided is flagged preferred. Pins the contract that the
		// serializer relies on core's name-selection logic rather than re-implementing it.
		PersonName voidedPreferred = name("Stale", null, "Name");
		voidedPreferred.setPreferred(true);
		voidedPreferred.setVoided(true);
		patient.addName(voidedPreferred);
		patient.addName(name("Current", null, "Name"));

		QueryDocument doc = serializer.serialize(patient);

		assertEquals("Patient: Current Name", doc.getText());
		assertEquals("Current", doc.getMetadata().get("given_name"));
	}

	private static Patient patient(String uuid, Date dateCreated) {
		Patient p = new Patient();
		p.setUuid(uuid);
		p.setDateCreated(dateCreated);
		return p;
	}

	private static PersonName name(String given, String middle, String family) {
		PersonName n = new PersonName();
		n.setGivenName(given);
		n.setMiddleName(middle);
		n.setFamilyName(family);
		return n;
	}

	private static PatientIdentifierType identifierType(String uuid, String name) {
		PatientIdentifierType t = new PatientIdentifierType();
		t.setUuid(uuid);
		t.setName(name);
		return t;
	}

	private static PatientIdentifier identifier(Integer id, PatientIdentifierType type,
	                                            String value, boolean preferred, Location location) {
		PatientIdentifier pi = new PatientIdentifier();
		pi.setPatientIdentifierId(id);
		pi.setIdentifierType(type);
		pi.setIdentifier(value);
		pi.setPreferred(preferred);
		pi.setLocation(location);
		return pi;
	}

	private static Location location(String uuid) {
		Location l = new Location();
		l.setUuid(uuid);
		return l;
	}

	private static PersonAddress address(Integer id, String address1, String city, String state,
	                                     String postal, String country, boolean preferred) {
		PersonAddress a = new PersonAddress();
		a.setPersonAddressId(id);
		a.setAddress1(address1);
		a.setCityVillage(city);
		a.setStateProvince(state);
		a.setPostalCode(postal);
		a.setCountry(country);
		a.setPreferred(preferred);
		return a;
	}

	private static PersonAttributeType attributeType(String uuid, String name) {
		PersonAttributeType t = new PersonAttributeType();
		t.setUuid(uuid);
		t.setName(name);
		return t;
	}

	private static PersonAttribute attribute(Integer id, PersonAttributeType type, String value) {
		PersonAttribute attr = new PersonAttribute();
		attr.setPersonAttributeId(id);
		attr.setAttributeType(type);
		attr.setValue(value);
		return attr;
	}

	private static Set<PersonAttribute> attributeSet(PersonAttribute... attrs) {
		Set<PersonAttribute> set = new HashSet<>();
		for (PersonAttribute a : attrs) {
			set.add(a);
		}
		return set;
	}
}
