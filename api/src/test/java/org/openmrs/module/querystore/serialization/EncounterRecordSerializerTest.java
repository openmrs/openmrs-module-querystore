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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openmrs.module.querystore.serialization.DateFixtures.utcDate;
import static org.openmrs.module.querystore.serialization.ProviderFixtures.providerNamed;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.module.querystore.model.QueryDocument;

public class EncounterRecordSerializerTest {

	private EncounterRecordSerializer serializer;

	@Before
	public void setUp() {
		serializer = new EncounterRecordSerializer();
	}

	@Test
	public void serialize_fullEncounter_matchesAdrExample() {
		Encounter encounter = encounter("encounter-uuid",
		        utcDate(2025, Calendar.MARCH, 15),
		        encounterType("encounter-type-uuid", "Adult Outpatient Visit"));
		encounter.setLocation(location("location-uuid", "Kenyatta National Hospital"));
		encounter.setForm(form("form-uuid", "Adult Outpatient Form"));
		encounter.setVisit(visit("visit-uuid"));
		EncounterRole clinician = encounterRole("role-uuid", "Clinician");
		Provider provider = providerNamed("provider-uuid", "Dr.", "Ochieng");
		encounter.setEncounterProviders(providerSet(provider, clinician));

		QueryDocument doc = serializer.serialize(encounter);

		assertEquals("encounter", doc.getResourceType());
		assertEquals("encounter-uuid", doc.getResourceUuid());
		assertEquals("Encounter: Adult Outpatient Visit at Kenyatta National Hospital."
		        + " Provider: Dr. Ochieng (Clinician). Form: Adult Outpatient Form", doc.getText());
		assertEquals("encounter-type-uuid", doc.getMetadata().get("encounter_type_uuid"));
		assertEquals("Adult Outpatient Visit", doc.getMetadata().get("encounter_type_name"));
		assertEquals("visit-uuid", doc.getMetadata().get("visit_uuid"));
		assertEquals("form-uuid", doc.getMetadata().get("form_uuid"));
		assertEquals("Adult Outpatient Form", doc.getMetadata().get("form_name"));
		assertEquals("location-uuid", doc.getMetadata().get("location_uuid"));
		assertEquals("Kenyatta National Hospital", doc.getMetadata().get("location_name"));
		assertNull("encounter document does not duplicate its own uuid under encounter_uuid",
		        doc.getMetadata().get("encounter_uuid"));

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> providers = (List<Map<String, Object>>) doc.getMetadata().get("providers");
		assertNotNull(providers);
		assertEquals(1, providers.size());
		Map<String, Object> entry = providers.get(0);
		assertEquals("provider-uuid", entry.get("provider_uuid"));
		assertEquals("Dr. Ochieng", entry.get("provider_name"));
		assertEquals("role-uuid", entry.get("role_uuid"));
		assertEquals("Clinician", entry.get("role_name"));
	}

	@Test
	public void serialize_minimalEncounter_typeOnlyAnchorsText() {
		Encounter encounter = encounter("enc-uuid",
		        utcDate(2025, Calendar.MARCH, 15),
		        encounterType("encounter-type-uuid", "Adult Outpatient Visit"));

		QueryDocument doc = serializer.serialize(encounter);

		assertEquals("Encounter: Adult Outpatient Visit", doc.getText());
		assertNull(doc.getMetadata().get("location_uuid"));
		assertNull(doc.getMetadata().get("form_uuid"));
		assertNull(doc.getMetadata().get("visit_uuid"));
		assertNull(doc.getMetadata().get("providers"));
	}

	@Test
	public void serialize_missingEncounterType_returnsNull() {
		Encounter encounter = new Encounter();
		encounter.setUuid("enc-uuid");
		encounter.setEncounterDatetime(utcDate(2025, Calendar.MARCH, 15));
		encounter.setLocation(location("loc-uuid", "Kenyatta National Hospital"));

		assertNull(serializer.serialize(encounter));
	}

	@Test
	public void serialize_blankEncounterTypeName_returnsNull() {
		Encounter encounter = encounter("enc-uuid",
		        utcDate(2025, Calendar.MARCH, 15),
		        encounterType("type-uuid", "   "));

		assertNull(serializer.serialize(encounter));
	}

	@Test
	public void serialize_multipleProvidersInDifferentRoles_orderedByEncounterProviderId() {
		Encounter encounter = encounter("enc-uuid",
		        utcDate(2025, Calendar.MARCH, 15),
		        encounterType("type-uuid", "Adult Outpatient Visit"));
		// Insert in reverse id order so a stable sort is observable rather than masked by Set hash
		// iteration. The "primary provider" baked into text is the lowest-id entry, not whichever
		// hashes first.
		EncounterProvider second = encounterProvider(providerNamed("p2-uuid", "Nurse", "Akinyi"),
		        encounterRole("nurse-uuid", "Nurse"));
		second.setEncounterProviderId(2);
		EncounterProvider first = encounterProvider(providerNamed("p1-uuid", "Dr.", "Ochieng"),
		        encounterRole("clinician-uuid", "Clinician"));
		first.setEncounterProviderId(1);
		Set<EncounterProvider> set = new HashSet<>();
		set.add(second);
		set.add(first);
		encounter.setEncounterProviders(set);

		QueryDocument doc = serializer.serialize(encounter);

		assertEquals("Encounter: Adult Outpatient Visit. Provider: Dr. Ochieng (Clinician)",
		        doc.getText());
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> providers = (List<Map<String, Object>>) doc.getMetadata().get("providers");
		assertEquals(2, providers.size());
		assertEquals("p1-uuid", providers.get(0).get("provider_uuid"));
		assertEquals("clinician-uuid", providers.get(0).get("role_uuid"));
		assertEquals("p2-uuid", providers.get(1).get("provider_uuid"));
		assertEquals("nurse-uuid", providers.get(1).get("role_uuid"));
	}

	@Test
	public void serialize_providerWithoutResolvableName_entryOmitsProviderName() {
		Encounter encounter = encounter("enc-uuid",
		        utcDate(2025, Calendar.MARCH, 15),
		        encounterType("type-uuid", "Adult Outpatient Visit"));
		// Provider with no linked Person — Provider.getName() returns null (with a warn log).
		Provider unnamed = new Provider();
		unnamed.setUuid("p-uuid");
		encounter.setEncounterProviders(providerSet(unnamed, encounterRole("role-uuid", "Clinician")));

		QueryDocument doc = serializer.serialize(encounter);

		// Provider clause is omitted from text when the provider has no resolvable name.
		assertEquals("Encounter: Adult Outpatient Visit", doc.getText());
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> providers = (List<Map<String, Object>>) doc.getMetadata().get("providers");
		Map<String, Object> entry = providers.get(0);
		assertEquals("p-uuid", entry.get("provider_uuid"));
		assertTrue("provider_name omitted when no resolvable name", !entry.containsKey("provider_name"));
		assertEquals("role-uuid", entry.get("role_uuid"));
	}

	@Test
	public void serialize_nullEncounterDatetime_dateIsNull() {
		Encounter encounter = new Encounter();
		encounter.setUuid("enc-uuid");
		encounter.setEncounterType(encounterType("type-uuid", "Adult Outpatient Visit"));

		QueryDocument doc = serializer.serialize(encounter);

		assertNull(doc.getDate());
		assertEquals("Encounter: Adult Outpatient Visit", doc.getText());
	}

	@Test
	public void serialize_providerWithoutRole_entryOmitsRoleFields() {
		Encounter encounter = encounter("enc-uuid",
		        utcDate(2025, Calendar.MARCH, 15),
		        encounterType("type-uuid", "Adult Outpatient Visit"));
		encounter.setEncounterProviders(providerSet(providerNamed("p-uuid", "Dr.", "Ochieng"), null));

		QueryDocument doc = serializer.serialize(encounter);

		assertEquals("Encounter: Adult Outpatient Visit. Provider: Dr. Ochieng", doc.getText());
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> providers = (List<Map<String, Object>>) doc.getMetadata().get("providers");
		Map<String, Object> entry = providers.get(0);
		assertEquals("p-uuid", entry.get("provider_uuid"));
		assertEquals("Dr. Ochieng", entry.get("provider_name"));
		assertTrue("role_uuid omitted when no encounter role", !entry.containsKey("role_uuid"));
		assertTrue("role_name omitted when no encounter role", !entry.containsKey("role_name"));
	}

	@Test
	public void serialize_encounterProviderWithNullProvider_skipped() {
		Encounter encounter = encounter("enc-uuid",
		        utcDate(2025, Calendar.MARCH, 15),
		        encounterType("type-uuid", "Adult Outpatient Visit"));
		EncounterProvider orphan = new EncounterProvider();
		orphan.setEncounterRole(encounterRole("role-uuid", "Clinician"));
		Set<EncounterProvider> set = new HashSet<>();
		set.add(orphan);
		encounter.setEncounterProviders(set);

		QueryDocument doc = serializer.serialize(encounter);

		assertEquals("Encounter: Adult Outpatient Visit", doc.getText());
		assertNull(doc.getMetadata().get("providers"));
	}

	@Test
	public void serialize_carriesPatientAndResourceUuids() {
		Patient patient = new Patient();
		patient.setUuid("patient-uuid");
		Encounter encounter = encounter("encounter-uuid",
		        utcDate(2025, Calendar.MARCH, 15),
		        encounterType("type-uuid", "Adult Outpatient Visit"));
		encounter.setPatient(patient);

		QueryDocument doc = serializer.serialize(encounter);

		assertEquals("patient-uuid", doc.getPatientUuid());
		assertEquals("encounter-uuid", doc.getResourceUuid());
	}

	@Test
	public void serialize_usesEncounterDatetimeForDate() {
		Encounter encounter = encounter("enc-uuid",
		        utcDate(2025, Calendar.MARCH, 15),
		        encounterType("type-uuid", "Adult Outpatient Visit"));

		QueryDocument doc = serializer.serialize(encounter);

		assertEquals("2025-03-15", doc.getDate().toString());
	}

	private static Encounter encounter(String uuid, java.util.Date datetime, EncounterType type) {
		Encounter e = new Encounter();
		e.setUuid(uuid);
		e.setEncounterDatetime(datetime);
		e.setEncounterType(type);
		return e;
	}

	private static EncounterType encounterType(String uuid, String name) {
		EncounterType t = new EncounterType();
		t.setUuid(uuid);
		t.setName(name);
		return t;
	}

	private static Location location(String uuid, String name) {
		Location l = new Location();
		l.setUuid(uuid);
		l.setName(name);
		return l;
	}

	private static Form form(String uuid, String name) {
		Form f = new Form();
		f.setUuid(uuid);
		f.setName(name);
		return f;
	}

	private static Visit visit(String uuid) {
		Visit v = new Visit();
		v.setUuid(uuid);
		return v;
	}

	private static EncounterRole encounterRole(String uuid, String name) {
		EncounterRole r = new EncounterRole();
		r.setUuid(uuid);
		r.setName(name);
		return r;
	}

	private static EncounterProvider encounterProvider(Provider provider, EncounterRole role) {
		EncounterProvider ep = new EncounterProvider();
		ep.setProvider(provider);
		if (role != null) {
			ep.setEncounterRole(role);
		}
		return ep;
	}

	private static Set<EncounterProvider> providerSet(Provider provider, EncounterRole role) {
		Set<EncounterProvider> set = new HashSet<>();
		set.add(encounterProvider(provider, role));
		return set;
	}
}
