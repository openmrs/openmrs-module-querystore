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
import static org.openmrs.module.querystore.serialization.ConceptFixtures.concept;
import static org.openmrs.module.querystore.serialization.DateFixtures.utcDate;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
import org.openmrs.VisitType;
import org.openmrs.module.querystore.model.QueryDocument;

public class VisitRecordSerializerTest {

	private VisitRecordSerializer serializer;

	@Before
	public void setUp() {
		serializer = new VisitRecordSerializer();
	}

	@Test
	public void serialize_closedVisit_matchesAdrExample() {
		Visit visit = visit("visit-uuid",
		        visitType("visit-type-uuid", "Outpatient"),
		        utcDate(2025, Calendar.MARCH, 15));
		visit.setStopDatetime(utcDate(2025, Calendar.MARCH, 15));
		visit.setLocation(location("location-uuid", "Kenyatta National Hospital"));
		visit.setIndication(indication("indication-uuid", "Routine follow-up for diabetes"));
		visit.addEncounter(encounter("encounter-uuid"));
		visit.setAttributes(attributeSet(
		        attribute(1, "0bc9e46f-5211-a357-0b8c-b624afb27377",
		                "Insurance Provider", "NHIF")));

		QueryDocument doc = serializer.serialize(visit);

		assertEquals("visit", doc.getResourceType());
		assertEquals("visit-uuid", doc.getResourceUuid());
		assertEquals("2025-03-15", doc.getDate().toString());
		assertEquals("2025-03-15", doc.getMetadata().get("clinical_date"));
		assertEquals("clinical_event", doc.getMetadata().get("date_kind"));
		assertEquals("Visit: Outpatient at Kenyatta National Hospital."
		        + " Indication: Routine follow-up for diabetes", doc.getText());
		assertEquals("visit-type-uuid", doc.getMetadata().get("visit_type_uuid"));
		assertEquals("Outpatient", doc.getMetadata().get("visit_type_name"));
		assertEquals(Boolean.FALSE, doc.getMetadata().get("active"));
		assertEquals("indication-uuid", doc.getMetadata().get("indication_uuid"));
		assertEquals("Routine follow-up for diabetes", doc.getMetadata().get("indication_name"));
		assertEquals("location-uuid", doc.getMetadata().get("location_uuid"));
		assertEquals("Kenyatta National Hospital", doc.getMetadata().get("location_name"));
		assertEquals(Arrays.asList("encounter-uuid"), doc.getMetadata().get("encounter_uuids"));

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> attrs = (List<Map<String, Object>>) doc.getMetadata().get("attributes");
		assertNotNull(attrs);
		assertEquals(1, attrs.size());
		Map<String, Object> first = attrs.get(0);
		assertEquals("0bc9e46f-5211-a357-0b8c-b624afb27377", first.get("type_uuid"));
		assertEquals("Insurance Provider", first.get("type_name"));
		assertEquals("NHIF", first.get("value"));
	}

	@Test
	public void serialize_activeVisitWithoutStop_activeTrue() {
		Visit visit = visit("visit-uuid",
		        visitType("type-uuid", "Outpatient"),
		        utcDate(2025, Calendar.MARCH, 15));

		QueryDocument doc = serializer.serialize(visit);

		assertEquals(Boolean.TRUE, doc.getMetadata().get("active"));
		assertNull(doc.getMetadata().get("end_date_time"));
	}

	@Test
	public void serialize_missingVisitType_returnsNull() {
		Visit visit = new Visit();
		visit.setUuid("visit-uuid");
		visit.setStartDatetime(utcDate(2025, Calendar.MARCH, 15));

		assertNull(serializer.serialize(visit));
	}

	@Test
	public void serialize_blankVisitTypeName_returnsNull() {
		Visit visit = visit("visit-uuid", visitType("type-uuid", "   "),
		        utcDate(2025, Calendar.MARCH, 15));

		assertNull(serializer.serialize(visit));
	}

	@Test
	public void serialize_minimalVisit_typeOnlyAnchorsText() {
		Visit visit = visit("visit-uuid", visitType("type-uuid", "Outpatient"),
		        utcDate(2025, Calendar.MARCH, 15));

		QueryDocument doc = serializer.serialize(visit);

		assertEquals("Visit: Outpatient", doc.getText());
		assertNull(doc.getMetadata().get("location_uuid"));
		assertNull(doc.getMetadata().get("indication_uuid"));
		assertNull(doc.getMetadata().get("encounter_uuids"));
		assertNull(doc.getMetadata().get("attributes"));
	}

	@Test
	public void serialize_emitsIsoTimestamps() {
		Visit visit = visit("visit-uuid", visitType("type-uuid", "Outpatient"),
		        utcDate(2025, Calendar.MARCH, 15));
		visit.setStopDatetime(utcDate(2025, Calendar.MARCH, 16));

		QueryDocument doc = serializer.serialize(visit);

		assertEquals("2025-03-15T12:00:00", doc.getMetadata().get("start_date_time"));
		assertEquals("2025-03-16T12:00:00", doc.getMetadata().get("end_date_time"));
	}

	@Test
	public void serialize_encounterUuids_sortedLexicographicallyAndVoidedFiltered() {
		Visit visit = visit("visit-uuid", visitType("type-uuid", "Outpatient"),
		        utcDate(2025, Calendar.MARCH, 15));
		visit.addEncounter(encounter("c-uuid"));
		visit.addEncounter(encounter("a-uuid"));
		visit.addEncounter(encounter("b-uuid"));
		Encounter voided = encounter("voided-uuid");
		voided.setVoided(true);
		visit.addEncounter(voided);

		QueryDocument doc = serializer.serialize(visit);

		assertEquals(Arrays.asList("a-uuid", "b-uuid", "c-uuid"),
		        doc.getMetadata().get("encounter_uuids"));
	}

	@Test
	public void serialize_multipleAttributes_sortedByVisitAttributeId() {
		Visit visit = visit("visit-uuid", visitType("type-uuid", "Outpatient"),
		        utcDate(2025, Calendar.MARCH, 15));
		// Insert in reverse id order so sort visibility isn't masked by Set hash order.
		visit.setAttributes(attributeSet(
		        attribute(2, "type-2-uuid", "Insurance Provider", "NHIF"),
		        attribute(1, "type-1-uuid", "Referral Source", "Community Health Worker")));

		QueryDocument doc = serializer.serialize(visit);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> attrs = (List<Map<String, Object>>) doc.getMetadata().get("attributes");
		assertEquals(2, attrs.size());
		assertEquals("type-1-uuid", attrs.get(0).get("type_uuid"));
		assertEquals("type-2-uuid", attrs.get(1).get("type_uuid"));
	}

	@Test
	public void serialize_voidedAttributes_filtered() {
		Visit visit = visit("visit-uuid", visitType("type-uuid", "Outpatient"),
		        utcDate(2025, Calendar.MARCH, 15));
		VisitAttribute voided = attribute(1, "type-uuid", "Insurance Provider", "NHIF");
		voided.setVoided(true);
		visit.setAttributes(attributeSet(voided,
		        attribute(2, "type-2-uuid", "Referral Source", "CHW")));

		QueryDocument doc = serializer.serialize(visit);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> attrs = (List<Map<String, Object>>) doc.getMetadata().get("attributes");
		assertEquals(1, attrs.size());
		assertEquals("type-2-uuid", attrs.get(0).get("type_uuid"));
	}

	@Test
	public void serialize_attributeWithBlankValue_skipped() {
		Visit visit = visit("visit-uuid", visitType("type-uuid", "Outpatient"),
		        utcDate(2025, Calendar.MARCH, 15));
		visit.setAttributes(attributeSet(attribute(1, "type-uuid", "Insurance Provider", "   ")));

		QueryDocument doc = serializer.serialize(visit);

		assertNull(doc.getMetadata().get("attributes"));
	}

	@Test
	public void serialize_attributeWithoutType_skipped() {
		Visit visit = visit("visit-uuid", visitType("type-uuid", "Outpatient"),
		        utcDate(2025, Calendar.MARCH, 15));
		// VisitAttribute with no attributeType — transient or partially-constructed record. Core's
		// foreign-key constraint shouldn't allow it in persisted data, but the serializer skips
		// rather than NPE if it ever sees one.
		VisitAttribute orphan = new VisitAttribute();
		orphan.setVisitAttributeId(1);
		orphan.setValueReferenceInternal("NHIF");
		visit.setAttributes(attributeSet(orphan));

		QueryDocument doc = serializer.serialize(visit);

		assertEquals("Visit: Outpatient", doc.getText());
		assertNull(doc.getMetadata().get("attributes"));
	}

	@Test
	public void serialize_nullStartDatetime_dateIsNullAndStartTextOmitted() {
		Visit visit = new Visit();
		visit.setUuid("visit-uuid");
		visit.setVisitType(visitType("type-uuid", "Outpatient"));

		QueryDocument doc = serializer.serialize(visit);

		assertNull(doc.getDate());
		assertNull(doc.getMetadata().get("start_date_time"));
		assertNull(doc.getMetadata().get("end_date_time"));
	}

	@Test
	public void serialize_indicationWithoutPreferredName_uuidSetButNameAndTextClauseOmitted() {
		Visit visit = visit("visit-uuid", visitType("type-uuid", "Outpatient"),
		        utcDate(2025, Calendar.MARCH, 15));
		Concept namelessIndication = new Concept();
		namelessIndication.setUuid("indication-uuid");
		visit.setIndication(namelessIndication);

		QueryDocument doc = serializer.serialize(visit);

		assertEquals("Visit: Outpatient", doc.getText());
		assertEquals("indication-uuid", doc.getMetadata().get("indication_uuid"));
		assertNull(doc.getMetadata().get("indication_name"));
	}

	@Test
	public void serialize_sameInstantStartAndStop_activeFalse() {
		Visit visit = visit("visit-uuid", visitType("type-uuid", "Outpatient"),
		        utcDate(2025, Calendar.MARCH, 15));
		visit.setStopDatetime(utcDate(2025, Calendar.MARCH, 15));

		QueryDocument doc = serializer.serialize(visit);

		assertEquals(Boolean.FALSE, doc.getMetadata().get("active"));
	}

	@Test
	public void serialize_carriesPatientAndResourceUuids() {
		Patient patient = new Patient();
		patient.setUuid("patient-uuid");
		Visit visit = visit("visit-uuid", visitType("type-uuid", "Outpatient"),
		        utcDate(2025, Calendar.MARCH, 15));
		visit.setPatient(patient);

		QueryDocument doc = serializer.serialize(visit);

		assertEquals("patient-uuid", doc.getPatientUuid());
		assertEquals("visit-uuid", doc.getResourceUuid());
	}

	private static Visit visit(String uuid, VisitType type, java.util.Date startDatetime) {
		Visit v = new Visit();
		v.setUuid(uuid);
		v.setVisitType(type);
		v.setStartDatetime(startDatetime);
		v.setEncounters(new HashSet<>());
		v.setAttributes(new HashSet<>());
		return v;
	}

	private static VisitType visitType(String uuid, String name) {
		VisitType t = new VisitType();
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

	private static Concept indication(String uuid, String name) {
		Concept c = concept(name);
		c.setUuid(uuid);
		return c;
	}

	private static Encounter encounter(String uuid) {
		Encounter e = new Encounter();
		e.setUuid(uuid);
		return e;
	}

	private static VisitAttribute attribute(Integer id, String typeUuid, String typeName, String value) {
		VisitAttributeType type = new VisitAttributeType();
		type.setUuid(typeUuid);
		type.setName(typeName);
		VisitAttribute attr = new VisitAttribute();
		attr.setVisitAttributeId(id);
		attr.setAttributeType(type);
		attr.setValueReferenceInternal(value);
		return attr;
	}

	private static Set<VisitAttribute> attributeSet(VisitAttribute... attrs) {
		Set<VisitAttribute> set = new HashSet<>();
		for (VisitAttribute a : attrs) {
			set.add(a);
		}
		return set;
	}
}
