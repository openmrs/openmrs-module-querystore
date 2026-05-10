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

import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_CONCEPT_CLASS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_CONCEPT_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_CONCEPT_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ENCOUNTER_TYPE_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ENCOUNTER_TYPE_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ENCOUNTER_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_FORM_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_FORM_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_LOCATION_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_LOCATION_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_PROVIDER_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_PROVIDER_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_SYNONYMS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_VISIT_UUID;

import java.time.LocalDate;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.util.ConceptNameUtil;

/**
 * Skeleton for {@link ClinicalRecordSerializer} implementations. The {@link #serialize(Object)}
 * template method fills in the cross-cutting fields ({@code patient_uuid}, {@code resource_type},
 * {@code resource_uuid}, {@code date}); subclasses populate type-specific text and metadata in a
 * single {@link #populate} pass so each record is walked once. Helpers are provided for concept
 * and encounter denormalization.
 */
public abstract class AbstractRecordSerializer<T> implements ClinicalRecordSerializer<T> {

	@Override
	public final QueryDocument serialize(T record) {
		QueryDocument doc = new QueryDocument();
		doc.setResourceType(getResourceType());
		doc.setPatientUuid(getPatientUuid(record));
		doc.setResourceUuid(getResourceUuid(record));
		doc.setDate(getDate(record));
		populate(record, doc);
		String text = doc.getText();
		if (text == null || text.isEmpty()) {
			return null;
		}
		return doc;
	}

	protected abstract String getPatientUuid(T record);

	protected abstract String getResourceUuid(T record);

	protected abstract LocalDate getDate(T record);

	/**
	 * Walks the record exactly once to populate {@link QueryDocument#setText(String) text} and
	 * structured metadata. Leaving text unset (or empty) signals "no document for this record" —
	 * see the null-return contract on {@link ClinicalRecordSerializer#serialize}.
	 */
	protected abstract void populate(T record, QueryDocument doc);

	/**
	 * Populates concept_uuid / concept_name / concept_class / synonyms. The {@code preferredName}
	 * is taken as a parameter so the same string used in {@code text} composition is reused here
	 * — keeps the two surfaces consistent and avoids re-walking the concept's names collection.
	 */
	protected final void putConceptFields(QueryDocument doc, Concept concept, String preferredName) {
		if (concept == null) {
			return;
		}
		String name = preferredName != null ? preferredName : "";
		doc.putMetadata(FIELD_CONCEPT_UUID, concept.getUuid());
		if (!name.isEmpty()) {
			doc.putMetadata(FIELD_CONCEPT_NAME, name);
		}
		ConceptClass conceptClass = concept.getConceptClass();
		if (conceptClass != null && conceptClass.getName() != null) {
			doc.putMetadata(FIELD_CONCEPT_CLASS, conceptClass.getName());
		}
		List<String> synonyms = ConceptNameUtil.getSynonyms(concept, name);
		if (!synonyms.isEmpty()) {
			doc.putMetadata(FIELD_SYNONYMS, synonyms);
		}
	}

	protected final void putEncounterContext(QueryDocument doc, Encounter encounter) {
		if (encounter == null) {
			return;
		}
		doc.putMetadata(FIELD_ENCOUNTER_UUID, encounter.getUuid());
		putUuidAndName(doc, FIELD_ENCOUNTER_TYPE_UUID, FIELD_ENCOUNTER_TYPE_NAME, encounter.getEncounterType());

		Visit visit = encounter.getVisit();
		if (visit != null) {
			doc.putMetadata(FIELD_VISIT_UUID, visit.getUuid());
		}

		putUuidAndName(doc, FIELD_FORM_UUID, FIELD_FORM_NAME, encounter.getForm());
		putUuidAndName(doc, FIELD_LOCATION_UUID, FIELD_LOCATION_NAME, encounter.getLocation());

		Provider provider = pickActiveProvider(encounter);
		if (provider != null) {
			doc.putMetadata(FIELD_PROVIDER_UUID, provider.getUuid());
			if (provider.getName() != null) {
				doc.putMetadata(FIELD_PROVIDER_NAME, provider.getName());
			}
		}
	}

	private static void putUuidAndName(QueryDocument doc, String uuidKey, String nameKey, OpenmrsMetadata ref) {
		if (ref == null) {
			return;
		}
		doc.putMetadata(uuidKey, ref.getUuid());
		if (ref.getName() != null) {
			doc.putMetadata(nameKey, ref.getName());
		}
	}

	private static Provider pickActiveProvider(Encounter encounter) {
		for (EncounterProvider ep : encounter.getActiveEncounterProviders()) {
			if (ep.getProvider() != null) {
				return ep.getProvider();
			}
		}
		return null;
	}
}
