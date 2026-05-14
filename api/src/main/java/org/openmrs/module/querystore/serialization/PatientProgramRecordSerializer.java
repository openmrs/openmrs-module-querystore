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

import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ACTIVE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_COMPLETION_DATE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_CURRENT_STATE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_CURRENT_STATE_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ENROLLMENT_DATE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_LOCATION_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_LOCATION_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_OUTCOME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_OUTCOME_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_PROGRAM_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_PROGRAM_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_SYNONYMS;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.util.ConceptNameUtil;
import org.openmrs.module.querystore.util.DateFormatUtil;

/**
 * Serializes a {@link PatientProgram} into a {@link QueryDocument} for the {@code querystore_program}
 * index. The primary clinical concept is wrapped in a {@link Program} metadata entity, so
 * UUID/name surface as {@code program_uuid}/{@code program_name} from the entity (not the
 * concept) — analogous to the allergen rename in {@link AllergyRecordSerializer} — while the
 * program concept's synonyms still populate the cross-cutting {@code synonyms} field per the
 * Decision 6 synonyms convention. Enrollment and completion dates are clinical events and appear
 * in both {@code text} and metadata per ADR Decision 7. When the program has multiple active
 * states (rare — most deployments use one workflow per program), the most recently started state
 * is chosen as {@code current_state}; the multi-workflow projection is tracked as an Open
 * Question.
 */
public class PatientProgramRecordSerializer extends AbstractRecordSerializer<PatientProgram> {

	@Override
	public String getResourceType() {
		return "program";
	}

	@Override
	public Class<PatientProgram> getSupportedType() {
		return PatientProgram.class;
	}

	@Override
	protected String getPatientUuid(PatientProgram program) {
		return program.getPatient() != null ? program.getPatient().getUuid() : null;
	}

	@Override
	protected String getResourceUuid(PatientProgram program) {
		return program.getUuid();
	}

	@Override
	protected LocalDate getDate(PatientProgram program) {
		Date enrolled = program.getDateEnrolled();
		return DateFormatUtil.toLocalDate(enrolled != null ? enrolled : program.getDateCreated());
	}

	@Override
	protected void populate(PatientProgram patientProgram, QueryDocument doc) {
		Program program = patientProgram.getProgram();
		if (program == null) {
			return;
		}
		// Prefer the program concept's locale-aware preferred name over Program.getName() so that
		// the display string, the program_name field, and the synonyms-exclusion key all anchor on
		// the same identity (mirrors AllergyRecordSerializer's codedAllergen-preferred-name path).
		// Program.getName() is the entity's metadata-layer label, used as a fallback when no
		// concept is wired up.
		Concept programConcept = program.getConcept();
		String conceptPreferredName = ConceptNameUtil.getPreferredNameOrNull(programConcept);
		String displayName = conceptPreferredName != null
		        ? conceptPreferredName : trimToNull(program.getName());
		if (displayName == null) {
			return;
		}

		String enrolledText = DateFormatUtil.formatDate(patientProgram.getDateEnrolled());
		String completedText = DateFormatUtil.formatDate(patientProgram.getDateCompleted());
		boolean active = patientProgram.getActive();
		Concept outcomeConcept = patientProgram.getOutcome();
		String outcomeName = ConceptNameUtil.getPreferredNameOrNull(outcomeConcept);
		PatientState currentState = pickCurrentState(patientProgram.getCurrentStates());
		Concept currentStateConcept = currentState != null && currentState.getState() != null
		        ? currentState.getState().getConcept() : null;
		String currentStateName = ConceptNameUtil.getPreferredNameOrNull(currentStateConcept);

		doc.setText(buildText(displayName, enrolledText, completedText, active, outcomeName, currentStateName));

		doc.putMetadata(FIELD_PROGRAM_UUID, program.getUuid());
		doc.putMetadata(FIELD_PROGRAM_NAME, displayName);
		// Only resolve synonyms when the display anchor IS the concept's preferred name —
		// otherwise the dedupe-key contract on ConceptNameUtil.getSynonyms is violated and we'd
		// pay for a second walk of concept.getNames() with no signal to add.
		if (conceptPreferredName != null) {
			List<String> synonyms = ConceptNameUtil.getSynonyms(programConcept, conceptPreferredName);
			if (!synonyms.isEmpty()) {
				doc.putMetadata(FIELD_SYNONYMS, synonyms);
			}
		}

		if (enrolledText != null) {
			doc.putMetadata(FIELD_ENROLLMENT_DATE, enrolledText);
		}
		if (completedText != null) {
			doc.putMetadata(FIELD_COMPLETION_DATE, completedText);
		}
		doc.putMetadata(FIELD_ACTIVE, active);
		putConceptUuidAndName(doc, FIELD_OUTCOME_UUID, FIELD_OUTCOME, outcomeConcept, outcomeName);
		putConceptUuidAndName(doc, FIELD_CURRENT_STATE_UUID, FIELD_CURRENT_STATE,
		        currentStateConcept, currentStateName);

		putUuidAndName(doc, FIELD_LOCATION_UUID, FIELD_LOCATION_NAME, patientProgram.getLocation());
	}

	private static String buildText(String displayName, String enrolledText, String completedText,
	                                boolean active, String outcomeName, String currentStateName) {
		StringBuilder sb = new StringBuilder("Program: ").append(displayName);
		if (enrolledText != null) {
			sb.append(". Enrolled: ").append(enrolledText);
		}
		sb.append(". Status: ").append(active ? "Active" : "Completed");
		if (completedText != null) {
			sb.append(". Completed: ").append(completedText);
		}
		if (outcomeName != null) {
			sb.append(". Outcome: ").append(outcomeName);
		}
		if (currentStateName != null) {
			sb.append(". Current state: ").append(currentStateName);
		}
		return sb.toString();
	}

	/**
	 * Picks one state out of the potentially-multi-workflow {@link PatientProgram#getCurrentStates()
	 * current states}. Most deployments use a single workflow per program so this set has one
	 * element; for multi-workflow programs the latest-started state is returned (ties broken by
	 * {@link PatientState#getId() patientStateId} for determinism). Returns {@code null} when no
	 * active state exists or when all candidates have a null {@code ProgramWorkflowState} (a
	 * transient or partially-constructed record); the caller treats either case as "omit
	 * {@code current_state_*} fields." Trusts {@code getCurrentStates()} for the endDate/voided
	 * filter — this method only resolves which of the active states to project.
	 */
	private static PatientState pickCurrentState(Set<PatientState> currentStates) {
		if (currentStates == null || currentStates.isEmpty()) {
			return null;
		}
		return currentStates.stream()
		        .filter(s -> s != null && s.getState() != null)
		        .max(Comparator
		                .comparing(PatientState::getStartDate, Comparator.nullsFirst(Comparator.naturalOrder()))
		                .thenComparing(PatientState::getId, Comparator.nullsFirst(Comparator.naturalOrder())))
		        .orElse(null);
	}

}
