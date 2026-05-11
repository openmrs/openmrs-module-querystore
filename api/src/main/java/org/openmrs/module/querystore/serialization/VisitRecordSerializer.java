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
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ATTRIBUTES;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ENCOUNTER_UUIDS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_END_DATE_TIME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_INDICATION_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_INDICATION_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_LOCATION_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_LOCATION_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_START_DATE_TIME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_TYPE_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_TYPE_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_VALUE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_VISIT_TYPE_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_VISIT_TYPE_UUID;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
import org.openmrs.VisitType;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.util.ConceptNameUtil;
import org.openmrs.module.querystore.util.DateFormatUtil;

/**
 * Serializes a {@link Visit} into a {@link QueryDocument} for the {@code openmrs_visit} index.
 * The visit IS the document, so {@code resource_uuid} is the visit UUID and the cross-cutting
 * {@code encounter_uuid} field is not used — visits aggregate encounters via {@code encounter_uuids}
 * instead, an array carrying the visit's non-voided encounter UUIDs per the Decision 6 example.
 * The cross-cutting {@code date} comes from {@code startDatetime}; the visit's full start/stop
 * timestamps are exposed separately as {@code start_date_time} / {@code end_date_time} since
 * visits are time-ranged rather than single-instant events. Visit type is the required text
 * anchor; a visit without a type produces no document per the skip-semantics convention.
 */
public class VisitRecordSerializer extends AbstractRecordSerializer<Visit> {

	@Override
	public String getResourceType() {
		return "visit";
	}

	@Override
	public Class<Visit> getSupportedType() {
		return Visit.class;
	}

	@Override
	protected String getPatientUuid(Visit visit) {
		return visit.getPatient() != null ? visit.getPatient().getUuid() : null;
	}

	@Override
	protected String getResourceUuid(Visit visit) {
		return visit.getUuid();
	}

	@Override
	protected LocalDate getDate(Visit visit) {
		return DateFormatUtil.toLocalDate(visit.getStartDatetime());
	}

	@Override
	protected void populate(Visit visit, QueryDocument doc) {
		VisitType type = visit.getVisitType();
		String typeName = type != null ? trimToNull(type.getName()) : null;
		if (typeName == null) {
			return;
		}

		Location location = visit.getLocation();
		String locationName = location != null ? trimToNull(location.getName()) : null;
		Concept indication = visit.getIndication();
		String indicationName = ConceptNameUtil.getPreferredNameOrNull(indication);

		doc.setText(buildText(typeName, locationName, indicationName));

		putUuidAndName(doc, FIELD_VISIT_TYPE_UUID, FIELD_VISIT_TYPE_NAME, type);
		String startText = DateFormatUtil.formatDateTime(visit.getStartDatetime());
		if (startText != null) {
			doc.putMetadata(FIELD_START_DATE_TIME, startText);
		}
		Date stopDatetime = visit.getStopDatetime();
		String stopText = DateFormatUtil.formatDateTime(stopDatetime);
		if (stopText != null) {
			doc.putMetadata(FIELD_END_DATE_TIME, stopText);
		}
		doc.putMetadata(FIELD_ACTIVE, stopDatetime == null);
		putConceptUuidAndName(doc, FIELD_INDICATION_UUID, FIELD_INDICATION_NAME, indication, indicationName);
		putUuidAndName(doc, FIELD_LOCATION_UUID, FIELD_LOCATION_NAME, location);

		List<String> encounterUuids = collectEncounterUuids(visit);
		if (!encounterUuids.isEmpty()) {
			doc.putMetadata(FIELD_ENCOUNTER_UUIDS, encounterUuids);
		}
		List<Map<String, Object>> attributes = collectAttributes(visit);
		if (!attributes.isEmpty()) {
			doc.putMetadata(FIELD_ATTRIBUTES, attributes);
		}
	}

	private static String buildText(String typeName, String locationName, String indicationName) {
		StringBuilder sb = new StringBuilder("Visit: ").append(typeName);
		if (locationName != null) {
			sb.append(" at ").append(locationName);
		}
		if (indicationName != null) {
			sb.append(". Indication: ").append(indicationName);
		}
		return sb.toString();
	}

	private static List<String> collectEncounterUuids(Visit visit) {
		List<Encounter> nonVoided = visit.getNonVoidedEncounters();
		if (nonVoided == null || nonVoided.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> uuids = new ArrayList<>(nonVoided.size());
		for (Encounter e : nonVoided) {
			if (e != null && e.getUuid() != null) {
				uuids.add(e.getUuid());
			}
		}
		// Sort lexicographically so the array is stable across re-projections —
		// getNonVoidedEncounters() is Set-backed and encounter UUIDs are an unordered traversal
		// handle per ADR field description, not a chronological list.
		uuids.sort(Comparator.naturalOrder());
		return uuids;
	}

	private static List<Map<String, Object>> collectAttributes(Visit visit) {
		Collection<VisitAttribute> active = visit.getActiveAttributes();
		if (active == null || active.isEmpty()) {
			return Collections.emptyList();
		}
		List<VisitAttribute> sorted = new ArrayList<>(active);
		// Sort by visitAttributeId (insertion order) so the array is stable across re-projections —
		// getActiveAttributes() returns a Collection backed by a Set with unspecified iteration order.
		sorted.sort(byIdThenUuid(VisitAttribute::getVisitAttributeId, VisitAttribute::getUuid));
		List<Map<String, Object>> out = new ArrayList<>(sorted.size());
		for (VisitAttribute attr : sorted) {
			VisitAttributeType attributeType = attr.getAttributeType();
			if (attributeType == null) {
				continue;
			}
			// valueReference is the persisted string form. getValue() invokes the custom-datatype
			// deserializer and can throw on malformed data — keep the projection resilient by reading
			// the raw reference, which is also what consumers cite per the ADR example shape.
			String value = trimToNull(attr.getValueReference());
			if (value == null) {
				continue;
			}
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put(FIELD_TYPE_UUID, attributeType.getUuid());
			if (attributeType.getName() != null) {
				entry.put(FIELD_TYPE_NAME, attributeType.getName());
			}
			entry.put(FIELD_VALUE, value);
			out.add(entry);
		}
		return out;
	}
}
