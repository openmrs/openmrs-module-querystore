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
import static org.junit.Assert.assertNull;
import static org.openmrs.module.querystore.serialization.ConceptFixtures.concept;
import static org.openmrs.module.querystore.serialization.ConceptFixtures.conceptName;
import static org.openmrs.module.querystore.serialization.ConceptFixtures.preferredName;
import static org.openmrs.module.querystore.serialization.DateFixtures.utcDate;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.module.querystore.model.QueryDocument;

public class PatientProgramRecordSerializerTest {

	private PatientProgramRecordSerializer serializer;

	@Before
	public void setUp() {
		serializer = new PatientProgramRecordSerializer();
	}

	@Test
	public void serialize_activeProgramWithCurrentState_matchesAdrExample() {
		// Realistic case: program entity name mirrors the program concept's preferred name.
		Program hiv = program("HIV Treatment", "program-uuid", concept("HIV Treatment"));
		PatientProgram enrollment = enrollment(hiv, utcDate(2024, Calendar.JANUARY, 15));
		enrollment.getStates().add(state("state-concept-uuid", "On ART",
		        utcDate(2024, Calendar.JANUARY, 15), null));

		QueryDocument doc = serializer.serialize(enrollment);

		assertEquals("program", doc.getResourceType());
		assertEquals("Program: HIV Treatment. Enrolled: 2024-01-15. Status: Active. Current state: On ART",
		        doc.getText());
		assertEquals("program-uuid", doc.getMetadata().get("program_uuid"));
		assertEquals("HIV Treatment", doc.getMetadata().get("program_name"));
		assertEquals("2024-01-15", doc.getMetadata().get("enrollment_date"));
		assertEquals("2024-01-15", doc.getMetadata().get("clinical_date"));
		assertEquals("clinical_event", doc.getMetadata().get("date_kind"));
		assertNull(doc.getMetadata().get("completion_date"));
		assertEquals(Boolean.TRUE, doc.getMetadata().get("active"));
		assertEquals("state-concept-uuid", doc.getMetadata().get("current_state_uuid"));
		assertEquals("On ART", doc.getMetadata().get("current_state"));
		assertNull(doc.getMetadata().get("outcome_uuid"));
	}

	@Test
	public void serialize_completedProgramWithOutcome_textShowsCompletedAndOutcome() {
		Program hiv = program("HIV Treatment", "program-uuid", null);
		PatientProgram enrollment = enrollment(hiv, utcDate(2023, Calendar.MARCH, 1));
		enrollment.setDateCompleted(utcDate(2024, Calendar.DECEMBER, 15));
		Concept outcome = concept("Cured");
		outcome.setUuid("outcome-uuid");
		enrollment.setOutcome(outcome);

		QueryDocument doc = serializer.serialize(enrollment);

		assertEquals("Program: HIV Treatment. Enrolled: 2023-03-01. Status: Completed."
		        + " Completed: 2024-12-15. Outcome: Cured", doc.getText());
		assertEquals("2024-12-15", doc.getMetadata().get("completion_date"));
		assertEquals(Boolean.FALSE, doc.getMetadata().get("active"));
		assertEquals("outcome-uuid", doc.getMetadata().get("outcome_uuid"));
		assertEquals("Cured", doc.getMetadata().get("outcome"));
	}

	@Test
	public void serialize_multipleCurrentStates_picksLatestByStartDate() {
		Program program = program("HIV Treatment", "program-uuid", null);
		PatientProgram enrollment = enrollment(program, utcDate(2024, Calendar.JANUARY, 1));
		// Earlier state, then later state in a second workflow.
		PatientState older = state("older-uuid", "Pre-ART",
		        utcDate(2024, Calendar.JANUARY, 1), null);
		PatientState newer = state("newer-uuid", "On ART",
		        utcDate(2024, Calendar.JUNE, 1), null);
		Set<PatientState> states = new HashSet<>();
		states.add(older);
		states.add(newer);
		enrollment.setStates(states);

		QueryDocument doc = serializer.serialize(enrollment);

		assertEquals("newer-uuid", doc.getMetadata().get("current_state_uuid"));
		assertEquals("On ART", doc.getMetadata().get("current_state"));
	}

	@Test
	public void serialize_noCurrentState_omitsCurrentStateFields() {
		Program program = program("HIV Treatment", "program-uuid", null);
		PatientProgram enrollment = enrollment(program, utcDate(2024, Calendar.JANUARY, 1));

		QueryDocument doc = serializer.serialize(enrollment);

		assertEquals("Program: HIV Treatment. Enrolled: 2024-01-01. Status: Active", doc.getText());
		assertNull(doc.getMetadata().get("current_state_uuid"));
		assertNull(doc.getMetadata().get("current_state"));
	}

	@Test
	public void serialize_synonymsPopulatedFromProgramConcept() {
		Concept c = new Concept();
		c.addName(preferredName("HIV Program"));
		c.addName(conceptName("AIDS Treatment Program"));
		c.addName(conceptName("ART Programme"));
		Program program = program("HIV Treatment", "program-uuid", c);

		PatientProgram enrollment = enrollment(program, utcDate(2024, Calendar.JANUARY, 1));

		QueryDocument doc = serializer.serialize(enrollment);

		// Display anchors on the concept's preferred name, not the entity name.
		assertEquals("HIV Program", doc.getMetadata().get("program_name"));
		assertEquals(Arrays.asList("AIDS Treatment Program", "ART Programme"),
		        doc.getMetadata().get("synonyms"));
	}

	@Test
	public void serialize_description_populatedFromProgramConcept() {
		// PatientProgram serializer was retrofitted to call putDescription after the synonyms
		// write so program records pick up the BM25 vocabulary bridge. Without this assertion,
		// a future refactor that drops the putDescription call would silently regress
		// description-driven retrieval for program records (e.g. "HIV care" surfacing an "ART"
		// program via its description that explicitly mentions HIV).
		Concept c = new Concept();
		c.addName(preferredName("HIV Program"));
		org.openmrs.ConceptDescription d = new org.openmrs.ConceptDescription();
		d.setDescription("Antiretroviral therapy programme for patients living with HIV/AIDS.");
		d.setLocale(java.util.Locale.ENGLISH);
		c.addDescription(d);
		Program program = program("HIV Treatment", "program-uuid", c);

		PatientProgram enrollment = enrollment(program, utcDate(2024, Calendar.JANUARY, 1));
		QueryDocument doc = serializer.serialize(enrollment);

		assertEquals("Antiretroviral therapy programme for patients living with HIV/AIDS.",
				doc.getMetadata().get("description"));
	}

	@Test
	public void serialize_description_absentWhenNoProgramConcept() {
		// Program with no Concept (only an entity name) has no source for a description.
		// Metadata key must stay absent so the backends skip the description-field branch.
		Program program = program("HIV Treatment", "program-uuid", null);
		PatientProgram enrollment = enrollment(program, utcDate(2024, Calendar.JANUARY, 1));
		QueryDocument doc = serializer.serialize(enrollment);

		assertNull("description key must be absent when program has no concept",
				doc.getMetadata().get("description"));
	}

	@Test
	public void serialize_noProgramConcept_fallsBackToEntityNameAndOmitsSynonyms() {
		Program program = program("HIV Treatment", "program-uuid", null);

		PatientProgram enrollment = enrollment(program, utcDate(2024, Calendar.JANUARY, 1));

		QueryDocument doc = serializer.serialize(enrollment);

		assertEquals("Program: HIV Treatment. Enrolled: 2024-01-01. Status: Active", doc.getText());
		assertEquals("HIV Treatment", doc.getMetadata().get("program_name"));
		assertNull(doc.getMetadata().get("synonyms"));
	}

	@Test
	public void serialize_stateWithoutProgramWorkflowState_skipped() {
		Program program = program("HIV Treatment", "program-uuid", null);
		PatientProgram enrollment = enrollment(program, utcDate(2024, Calendar.JANUARY, 1));
		// PatientState with no ProgramWorkflowState — transient or partially-constructed record;
		// the active-state picker must skip it rather than NPE on state.getState().getConcept().
		PatientState orphan = new PatientState();
		orphan.setStartDate(utcDate(2024, Calendar.JANUARY, 1));
		enrollment.getStates().add(orphan);

		QueryDocument doc = serializer.serialize(enrollment);

		assertEquals("Program: HIV Treatment. Enrolled: 2024-01-01. Status: Active", doc.getText());
		assertNull(doc.getMetadata().get("current_state_uuid"));
		assertNull(doc.getMetadata().get("current_state"));
	}

	@Test
	public void serialize_location_populated() {
		Program program = program("HIV Treatment", "program-uuid", null);
		PatientProgram enrollment = enrollment(program, utcDate(2024, Calendar.JANUARY, 1));
		Location loc = new Location();
		loc.setUuid("loc-uuid");
		loc.setName("Kenyatta National Hospital");
		enrollment.setLocation(loc);

		QueryDocument doc = serializer.serialize(enrollment);

		assertEquals("loc-uuid", doc.getMetadata().get("location_uuid"));
		assertEquals("Kenyatta National Hospital", doc.getMetadata().get("location_name"));
	}

	@Test
	public void serialize_nullProgram_returnsNull() {
		PatientProgram enrollment = new PatientProgram();
		enrollment.setDateEnrolled(utcDate(2024, Calendar.JANUARY, 1));
		assertNull(serializer.serialize(enrollment));
	}

	@Test
	public void serialize_blankProgramName_returnsNull() {
		Program blank = program("   ", "program-uuid", null);
		PatientProgram enrollment = enrollment(blank, utcDate(2024, Calendar.JANUARY, 1));
		assertNull(serializer.serialize(enrollment));
	}

	@Test
	public void serialize_missingEnrollmentDate_omitsEnrolledFromText() {
		Program program = program("HIV Treatment", "program-uuid", null);
		PatientProgram enrollment = new PatientProgram();
		enrollment.setProgram(program);
		enrollment.setDateCreated(new Date());

		QueryDocument doc = serializer.serialize(enrollment);

		assertEquals("Program: HIV Treatment. Status: Active", doc.getText());
		assertNull(doc.getMetadata().get("enrollment_date"));
	}

	@Test
	public void serialize_carriesPatientAndResourceUuids() {
		Patient patient = new Patient();
		patient.setUuid("patient-uuid");
		Program program = program("HIV Treatment", "program-uuid", null);
		PatientProgram enrollment = enrollment(program, utcDate(2024, Calendar.JANUARY, 1));
		enrollment.setUuid("enrollment-uuid");
		enrollment.setPatient(patient);

		QueryDocument doc = serializer.serialize(enrollment);

		assertEquals("patient-uuid", doc.getPatientUuid());
		assertEquals("enrollment-uuid", doc.getResourceUuid());
	}

	private static Program program(String name, String uuid, Concept concept) {
		Program p = new Program();
		p.setName(name);
		p.setUuid(uuid);
		p.setConcept(concept);
		return p;
	}

	private static PatientProgram enrollment(Program program, Date dateEnrolled) {
		PatientProgram pp = new PatientProgram();
		pp.setProgram(program);
		pp.setDateEnrolled(dateEnrolled);
		pp.setStates(new HashSet<>());
		return pp;
	}

	private static PatientState state(String conceptUuid, String name, Date startDate, Date endDate) {
		Concept c = concept(name);
		c.setUuid(conceptUuid);
		ProgramWorkflowState pws = new ProgramWorkflowState();
		pws.setConcept(c);
		PatientState s = new PatientState();
		s.setState(pws);
		s.setStartDate(startDate);
		s.setEndDate(endDate);
		return s;
	}
}
