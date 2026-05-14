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

import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ADDRESS1;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ADDRESSES;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_AGE_YEARS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ATTRIBUTES;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_BIRTHDATE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_BIRTHDATE_ESTIMATED;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_CAUSE_OF_DEATH_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_CAUSE_OF_DEATH_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_CITY_VILLAGE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_COUNTRY;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DEAD;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DEATH_DATE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_FAMILY_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_GENDER;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_GIVEN_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_IDENTIFIERS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_LOCATION_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_MIDDLE_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_POSTAL_CODE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_PREFERRED;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_STATE_PROVINCE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_TYPE_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_TYPE_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_VALUE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.openmrs.module.querystore.util.ConceptNameUtil;
import org.openmrs.module.querystore.util.DateFormatUtil;

/**
 * Serializes a {@link Patient} into a {@link QueryDocument} for the {@code querystore_patient} index.
 * The patient IS the document, so {@code resource_uuid} mirrors {@code patient_uuid}. Person and
 * Patient state is flattened into one document per the Decision 6 example — see the
 * {@code Person vs Patient model} open question in docs/adr.md for the explicit rationale.
 * Three nested-object arrays follow the Decision 6 nested-metadata convention
 * ({@code identifiers}, {@code addresses}, {@code attributes}); identifiers and addresses sort
 * preferred-first so the citation-baked text clauses lead with the patient's primary entries.
 * Cross-cutting {@code date} falls back to {@code dateCreated} since patients have no
 * clinical-event timestamp. The display name resolved from the preferred PersonName is the
 * required text anchor; a patient with no name produces no document per the skip-semantics
 * convention.
 */
public class PatientRecordSerializer extends AbstractRecordSerializer<Patient> {

	@Override
	public String getResourceType() {
		return "patient";
	}

	@Override
	public Class<Patient> getSupportedType() {
		return Patient.class;
	}

	@Override
	protected String getPatientUuid(Patient patient) {
		return patient.getUuid();
	}

	@Override
	protected String getResourceUuid(Patient patient) {
		return patient.getUuid();
	}

	@Override
	protected LocalDate getDate(Patient patient) {
		return DateFormatUtil.toLocalDate(patient.getDateCreated());
	}

	@Override
	protected void populate(Patient patient, QueryDocument doc) {
		PersonName personName = patient.getPersonName();
		String given = personName != null ? trimToNull(personName.getGivenName()) : null;
		String middle = personName != null ? trimToNull(personName.getMiddleName()) : null;
		String family = personName != null ? trimToNull(personName.getFamilyName()) : null;
		String displayName = buildDisplayName(given, middle, family);
		if (displayName == null) {
			return;
		}

		String genderCode = trimToNull(patient.getGender());
		String genderLabel = genderLabel(genderCode);
		Date birthdate = patient.getBirthdate();
		String birthdateText = DateFormatUtil.formatDate(birthdate);
		Integer ageYears = birthdate != null ? patient.getAge() : null;
		boolean dead = Boolean.TRUE.equals(patient.getDead());
		String deathDateText = DateFormatUtil.formatDate(patient.getDeathDate());
		Concept causeOfDeath = patient.getCauseOfDeath();
		String causeOfDeathName = ConceptNameUtil.getPreferredNameOrNull(causeOfDeath);

		List<Map<String, Object>> identifiers = collectIdentifiers(patient);
		List<Map<String, Object>> addresses = collectAddresses(patient.getAddresses());
		List<Map<String, Object>> attributes = collectAttributes(patient.getActiveAttributes());

		doc.setText(buildText(displayName, genderLabel, birthdateText, addresses, identifiers));

		if (given != null) {
			doc.putMetadata(FIELD_GIVEN_NAME, given);
		}
		if (middle != null) {
			doc.putMetadata(FIELD_MIDDLE_NAME, middle);
		}
		if (family != null) {
			doc.putMetadata(FIELD_FAMILY_NAME, family);
		}
		if (genderCode != null) {
			doc.putMetadata(FIELD_GENDER, genderCode);
		}
		if (birthdateText != null) {
			doc.putMetadata(FIELD_BIRTHDATE, birthdateText);
			// birthdate_estimated only carries meaning alongside a birthdate; core defaults it to
			// Boolean.FALSE rather than null when unset, so couple emission to birthdate presence.
			doc.putMetadata(FIELD_BIRTHDATE_ESTIMATED,
			        Boolean.TRUE.equals(patient.getBirthdateEstimated()));
		}
		if (ageYears != null) {
			doc.putMetadata(FIELD_AGE_YEARS, ageYears);
		}
		// Always emit so consumers can filter `dead=false` without "dead absent OR false" —
		// every patient has a yes/no answer here.
		doc.putMetadata(FIELD_DEAD, dead);
		if (deathDateText != null) {
			doc.putMetadata(FIELD_DEATH_DATE, deathDateText);
		}
		putConceptUuidAndName(doc, FIELD_CAUSE_OF_DEATH_UUID, FIELD_CAUSE_OF_DEATH_NAME,
		        causeOfDeath, causeOfDeathName);

		if (!identifiers.isEmpty()) {
			doc.putMetadata(FIELD_IDENTIFIERS, identifiers);
		}
		if (!addresses.isEmpty()) {
			doc.putMetadata(FIELD_ADDRESSES, addresses);
		}
		if (!attributes.isEmpty()) {
			doc.putMetadata(FIELD_ATTRIBUTES, attributes);
		}
	}

	private static String buildDisplayName(String given, String middle, String family) {
		List<String> parts = new ArrayList<>(3);
		if (given != null) {
			parts.add(given);
		}
		if (middle != null) {
			parts.add(middle);
		}
		if (family != null) {
			parts.add(family);
		}
		return parts.isEmpty() ? null : String.join(" ", parts);
	}

	private static String buildText(String displayName, String genderLabel, String birthdateText,
	                                List<Map<String, Object>> addresses,
	                                List<Map<String, Object>> identifiers) {
		StringBuilder sb = new StringBuilder("Patient: ").append(displayName);
		if (genderLabel != null) {
			sb.append(". ").append(genderLabel);
		}
		if (birthdateText != null) {
			sb.append(". Born ").append(birthdateText);
		}
		String addressClause = formatAddressClause(addresses);
		if (addressClause != null) {
			sb.append(". Address: ").append(addressClause);
		}
		String identifierClause = formatIdentifierClause(identifiers);
		if (identifierClause != null) {
			sb.append(". Identifiers: ").append(identifierClause);
		}
		return sb.toString();
	}

	// "M" → "Male", "F" → "Female"; anything else passes through verbatim so deployments using
	// non-OpenMRS-standard gender codes still get a meaningful citation. Returns null when the raw
	// code is null so the gender clause is omitted from text entirely.
	private static String genderLabel(String genderCode) {
		if (genderCode == null) {
			return null;
		}
		if ("M".equals(genderCode)) {
			return "Male";
		}
		if ("F".equals(genderCode)) {
			return "Female";
		}
		return genderCode;
	}

	private static String formatAddressClause(List<Map<String, Object>> addresses) {
		if (addresses.isEmpty()) {
			return null;
		}
		Map<String, Object> primary = addresses.get(0);
		List<String> parts = new ArrayList<>(3);
		Object city = primary.get(FIELD_CITY_VILLAGE);
		Object state = primary.get(FIELD_STATE_PROVINCE);
		Object country = primary.get(FIELD_COUNTRY);
		if (city != null) {
			parts.add(city.toString());
		}
		if (state != null) {
			parts.add(state.toString());
		}
		if (country != null) {
			parts.add(country.toString());
		}
		return parts.isEmpty() ? null : String.join(", ", parts);
	}

	private static String formatIdentifierClause(List<Map<String, Object>> identifiers) {
		if (identifiers.isEmpty()) {
			return null;
		}
		List<String> parts = new ArrayList<>(identifiers.size());
		for (Map<String, Object> id : identifiers) {
			Object typeName = id.get(FIELD_TYPE_NAME);
			Object value = id.get(FIELD_VALUE);
			if (value == null) {
				continue;
			}
			parts.add(typeName != null ? typeName + " " + value : value.toString());
		}
		return parts.isEmpty() ? null : String.join(", ", parts);
	}

	private static List<Map<String, Object>> collectIdentifiers(Patient patient) {
		List<PatientIdentifier> active = patient.getActiveIdentifiers();
		if (active == null || active.isEmpty()) {
			return Collections.emptyList();
		}
		List<PatientIdentifier> sorted = new ArrayList<>(active);
		// Preferred identifiers sort first so the citation-baked text clause leads with the
		// preferred ID (typically MRN). Mirrors the address sort.
		sorted.sort(Comparator
		        .comparing((PatientIdentifier i) -> !Boolean.TRUE.equals(i.getPreferred()))
		        .thenComparing(byIdThenUuid(PatientIdentifier::getPatientIdentifierId,
		                PatientIdentifier::getUuid)));
		List<Map<String, Object>> out = new ArrayList<>(sorted.size());
		for (PatientIdentifier id : sorted) {
			String value = trimToNull(id.getIdentifier());
			if (value == null) {
				continue;
			}
			Map<String, Object> entry = new LinkedHashMap<>();
			PatientIdentifierType type = id.getIdentifierType();
			if (type != null) {
				entry.put(FIELD_TYPE_UUID, type.getUuid());
				if (type.getName() != null) {
					entry.put(FIELD_TYPE_NAME, type.getName());
				}
			}
			entry.put(FIELD_VALUE, value);
			entry.put(FIELD_PREFERRED, Boolean.TRUE.equals(id.getPreferred()));
			Location location = id.getLocation();
			if (location != null) {
				entry.put(FIELD_LOCATION_UUID, location.getUuid());
			}
			out.add(entry);
		}
		return out;
	}

	private static List<Map<String, Object>> collectAddresses(Set<PersonAddress> all) {
		if (all == null || all.isEmpty()) {
			return Collections.emptyList();
		}
		List<PersonAddress> nonVoided = new ArrayList<>(all.size());
		for (PersonAddress a : all) {
			if (a != null && !Boolean.TRUE.equals(a.getVoided())) {
				nonVoided.add(a);
			}
		}
		if (nonVoided.isEmpty()) {
			return Collections.emptyList();
		}
		// Preferred addresses sort first so the formatAddressClause primary pick lands on the
		// preferred one when present.
		nonVoided.sort(Comparator
		        .comparing((PersonAddress a) -> !Boolean.TRUE.equals(a.getPreferred()))
		        .thenComparing(byIdThenUuid(PersonAddress::getPersonAddressId, PersonAddress::getUuid)));
		List<Map<String, Object>> out = new ArrayList<>(nonVoided.size());
		for (PersonAddress a : nonVoided) {
			Map<String, Object> entry = new LinkedHashMap<>();
			putIfPresent(entry, FIELD_ADDRESS1, trimToNull(a.getAddress1()));
			putIfPresent(entry, FIELD_CITY_VILLAGE, trimToNull(a.getCityVillage()));
			putIfPresent(entry, FIELD_STATE_PROVINCE, trimToNull(a.getStateProvince()));
			putIfPresent(entry, FIELD_POSTAL_CODE, trimToNull(a.getPostalCode()));
			putIfPresent(entry, FIELD_COUNTRY, trimToNull(a.getCountry()));
			// Drop the entry entirely when no structured field carries data — an isolated
			// preferred-flag with no address content is noise, not signal, and would consume an
			// array slot for no retrieval value.
			if (entry.isEmpty()) {
				continue;
			}
			entry.put(FIELD_PREFERRED, Boolean.TRUE.equals(a.getPreferred()));
			out.add(entry);
		}
		return out;
	}

	private static List<Map<String, Object>> collectAttributes(List<PersonAttribute> active) {
		if (active == null || active.isEmpty()) {
			return Collections.emptyList();
		}
		List<PersonAttribute> sorted = new ArrayList<>(active);
		sorted.sort(byIdThenUuid(PersonAttribute::getPersonAttributeId, PersonAttribute::getUuid));
		List<Map<String, Object>> out = new ArrayList<>(sorted.size());
		for (PersonAttribute attr : sorted) {
			PersonAttributeType type = attr.getAttributeType();
			if (type == null) {
				continue;
			}
			String value = trimToNull(attr.getValue());
			if (value == null) {
				continue;
			}
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put(FIELD_TYPE_UUID, type.getUuid());
			if (type.getName() != null) {
				entry.put(FIELD_TYPE_NAME, type.getName());
			}
			entry.put(FIELD_VALUE, value);
			out.add(entry);
		}
		return out;
	}

	private static void putIfPresent(Map<String, Object> entry, String key, String value) {
		if (value != null) {
			entry.put(key, value);
		}
	}
}
