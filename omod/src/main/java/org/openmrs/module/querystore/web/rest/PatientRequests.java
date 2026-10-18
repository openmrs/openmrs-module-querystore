/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.web.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.util.PrivilegeConstants;

/**
 * The checks every endpoint that reads one patient's documents makes before it touches the index:
 * the patient is one core shows the caller, the query is bounded, and the types searched are ones
 * the caller could read through core. One home, so a new endpoint cannot skip half of them.
 */
final class PatientRequests {

	/** Longest query text accepted: type-ahead and questions are short, and hybrid mode encodes it. */
	static final int MAX_QUERY_LENGTH = 500;

	/** Longest slice of a caller's value echoed back in a 400. */
	private static final int MAX_ECHO = 40;

	// The core privilege that reads each core type's records. A type not listed here (one a module
	// contributes) is denied: its records may be clinical, and nothing says which privilege reads them.
	private static final Map<String, String> READ_PRIVILEGE;

	static {
		Map<String, String> m = new HashMap<String, String>();
		m.put("patient", PrivilegeConstants.GET_PATIENTS);
		m.put("obs", PrivilegeConstants.GET_OBS);
		m.put("encounter", PrivilegeConstants.GET_ENCOUNTERS);
		m.put("visit", PrivilegeConstants.GET_VISITS);
		m.put("condition", PrivilegeConstants.GET_CONDITIONS);
		m.put("diagnosis", PrivilegeConstants.GET_DIAGNOSES);
		m.put("allergy", PrivilegeConstants.GET_ALLERGIES);
		m.put("drug_order", PrivilegeConstants.GET_ORDERS);
		m.put("test_order", PrivilegeConstants.GET_ORDERS);
		m.put("referral_order", PrivilegeConstants.GET_ORDERS);
		m.put("medication_dispense", PrivilegeConstants.GET_MEDICATION_DISPENSE);
		m.put("program", PrivilegeConstants.GET_PATIENT_PROGRAMS);
		READ_PRIVILEGE = Collections.unmodifiableMap(m);
	}

	// What a patient uuid can look like (core's uuid column is 38 characters). Anything else is not a
	// patient's, without asking core.
	private static final Pattern PATIENT_UUID_SHAPE = Pattern.compile("[A-Za-z0-9-]{1,38}");

	private PatientRequests() {
	}

	/**
	 * 404 ({@link PatientNotFoundException}) unless core shows this caller the patient and it is not
	 * voided. The lookup goes through core's own patient read, so whatever patient-level restriction a
	 * deployment applies there applies here too (ADR Decision 14 leaves querystore's own checks at the
	 * privilege), and a uuid that is no patient's never reaches projection. A voided patient is
	 * refused although its clinical documents can remain indexed: voiding a patient removes only its
	 * patient document (ADR Decision 10 deletes voided records, and the obs are not themselves
	 * voided). One 404 for a bad shape, an unknown uuid and a voided patient, before any index call.
	 */
	static void requirePatient(PatientService patients, String patientUuid) {
		if (patientUuid == null || !PATIENT_UUID_SHAPE.matcher(patientUuid).matches()) {
			throw new PatientNotFoundException();
		}
		Patient patient = patients.getPatientByUuid(patientUuid);
		if (patient == null || Boolean.TRUE.equals(patient.getVoided())) {
			throw new PatientNotFoundException();
		}
	}

	/** The query text, trimmed: required, and at most {@link #MAX_QUERY_LENGTH} characters (400). */
	static String requireQuery(String q) {
		String query = StringUtils.trimToNull(q);
		if (query == null) {
			throw new BadParameterException("q is required");
		}
		if (query.length() > MAX_QUERY_LENGTH) {
			throw new BadParameterException("q must be at most " + MAX_QUERY_LENGTH + " characters");
		}
		return query;
	}

	/**
	 * The types to search: those the caller named, or every registered type, limited to types whose
	 * records the caller could read through core ({@link #READ_PRIVILEGE}). Duplicates are dropped. A
	 * named type that is not registered (the Lucene and MySQL backends create an index or table for
	 * any well-formed type a search names) and a named type the caller may not read get the same 403
	 * ({@link TypeNotAvailableException}), so the answer does not tell a caller which types are
	 * registered; an unnamed unreadable one is left out. An empty result means every type; a caller
	 * who can read none of them gets a 403, never an empty list.
	 */
	static List<String> readableTypes(String csv, List<String> known) {
		List<String> requested = splitList(csv);
		if (!requested.isEmpty()) {
			for (String type : requested) {
				if (!known.contains(type) || !canRead(type)) {
					throw new TypeNotAvailableException();
				}
			}
			return requested;
		}
		List<String> readable = new ArrayList<String>();
		for (String type : known) {
			if (canRead(type)) {
				readable.add(type);
			}
		}
		if (readable.isEmpty()) {
			throw new ContextAuthenticationException("Insufficient privileges to read any type");
		}
		return readable.size() == known.size() ? new ArrayList<String>() : readable;
	}

	private static boolean canRead(String type) {
		String privilege = READ_PRIVILEGE.get(type);
		return privilege != null && Context.hasPrivilege(privilege);
	}

	/** A comma-separated parameter's non-blank values, trimmed, each once, in order. */
	static List<String> splitList(String csv) {
		Set<String> out = new LinkedHashSet<String>();
		if (csv != null) {
			for (String part : csv.split(",")) {
				if (StringUtils.isNotBlank(part)) {
					out.add(part.trim());
				}
			}
		}
		return new ArrayList<String>(out);
	}

	/** A caller's value as a 400 may echo it: abbreviated. */
	static String echo(String value) {
		return StringUtils.abbreviate(value, MAX_ECHO);
	}
}
