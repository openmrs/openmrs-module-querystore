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

import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ENCOUNTER_TYPE_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ENCOUNTER_TYPE_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_FORM_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_FORM_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_LOCATION_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_LOCATION_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_PROVIDERS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_PROVIDER_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_PROVIDER_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ROLE_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ROLE_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_VISIT_UUID;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.util.DateFormatUtil;

/**
 * Serializes an {@link Encounter} into a {@link QueryDocument} for the {@code querystore_encounter}
 * index. The encounter IS the document, so {@code resource_uuid} is the encounter UUID and the
 * cross-cutting {@code encounter_uuid} field is not duplicated — that field belongs to
 * encounter-scoped data records (obs, condition, order, etc.) that point back to their parent
 * encounter. Unlike those single-provider events, the encounter carries the full {@code providers}
 * list as nested objects with role information ({@code provider_uuid}, {@code provider_name},
 * {@code role_uuid}, {@code role_name}) per the Decision 6 example, since encounters can have
 * multiple providers in different roles. Encounter type is the required text anchor; an encounter
 * without a type produces no document per the skip-semantics convention.
 */
public class EncounterRecordSerializer extends AbstractRecordSerializer<Encounter> {

	@Override
	public String getResourceType() {
		return "encounter";
	}

	@Override
	public Class<Encounter> getSupportedType() {
		return Encounter.class;
	}

	@Override
	protected String getPatientUuid(Encounter encounter) {
		return encounter.getPatient() != null ? encounter.getPatient().getUuid() : null;
	}

	@Override
	protected String getResourceUuid(Encounter encounter) {
		return encounter.getUuid();
	}

	@Override
	protected LocalDate getDate(Encounter encounter) {
		return DateFormatUtil.toLocalDate(encounter.getEncounterDatetime());
	}

	@Override
	protected void populate(Encounter encounter, QueryDocument doc) {
		EncounterType type = encounter.getEncounterType();
		String typeName = type != null ? trimToNull(type.getName()) : null;
		if (typeName == null) {
			return;
		}

		Location location = encounter.getLocation();
		String locationName = location != null ? trimToNull(location.getName()) : null;
		Form form = encounter.getForm();
		String formName = form != null ? trimToNull(form.getName()) : null;
		List<Map<String, Object>> providers = collectProviders(encounter);
		String primaryProviderName = null;
		String primaryRoleName = null;
		if (!providers.isEmpty()) {
			Map<String, Object> primary = providers.get(0);
			primaryProviderName = (String) primary.get(FIELD_PROVIDER_NAME);
			primaryRoleName = (String) primary.get(FIELD_ROLE_NAME);
		}

		doc.setText(buildText(typeName, locationName, primaryProviderName, primaryRoleName, formName));

		putUuidAndName(doc, FIELD_ENCOUNTER_TYPE_UUID, FIELD_ENCOUNTER_TYPE_NAME, type);
		Visit visit = encounter.getVisit();
		if (visit != null) {
			doc.putMetadata(FIELD_VISIT_UUID, visit.getUuid());
		}
		putUuidAndName(doc, FIELD_FORM_UUID, FIELD_FORM_NAME, form);
		putUuidAndName(doc, FIELD_LOCATION_UUID, FIELD_LOCATION_NAME, location);
		if (!providers.isEmpty()) {
			doc.putMetadata(FIELD_PROVIDERS, providers);
		}
	}

	private static String buildText(String typeName, String locationName,
	                                String primaryProviderName, String primaryRoleName, String formName) {
		StringBuilder sb = new StringBuilder("Encounter: ").append(typeName);
		if (locationName != null) {
			sb.append(" at ").append(locationName);
		}
		if (primaryProviderName != null) {
			sb.append(". Provider: ").append(primaryProviderName);
			if (primaryRoleName != null) {
				sb.append(" (").append(primaryRoleName).append(")");
			}
		}
		if (formName != null) {
			sb.append(". Form: ").append(formName);
		}
		return sb.toString();
	}

	private static List<Map<String, Object>> collectProviders(Encounter encounter) {
		List<EncounterProvider> sorted = new ArrayList<>(encounter.getActiveEncounterProviders());
		// Sort by encounterProviderId (insertion order) so the text-baked primary provider and the
		// providers array are stable across re-projections — getActiveEncounterProviders() returns a
		// Set whose iteration order is unspecified.
		sorted.sort(byIdThenUuid(EncounterProvider::getEncounterProviderId,
		        ep -> ep.getProvider() != null ? ep.getProvider().getUuid() : null));
		List<Map<String, Object>> out = new ArrayList<>(sorted.size());
		for (EncounterProvider ep : sorted) {
			Provider provider = ep.getProvider();
			if (provider == null) {
				continue;
			}
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put(FIELD_PROVIDER_UUID, provider.getUuid());
			String providerName = trimToNull(provider.getName());
			if (providerName != null) {
				entry.put(FIELD_PROVIDER_NAME, providerName);
			}
			EncounterRole role = ep.getEncounterRole();
			if (role != null) {
				entry.put(FIELD_ROLE_UUID, role.getUuid());
				if (role.getName() != null) {
					entry.put(FIELD_ROLE_NAME, role.getName());
				}
			}
			out.add(entry);
		}
		return out;
	}
}
