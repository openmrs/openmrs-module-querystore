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

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import org.openmrs.BaseOpenmrsData;
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
		doc.setLastModified(getLastModified(record));
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
	 * Returns the source-entity timestamp the backend uses as a write-version: any concurrent write
	 * carrying an older value is dropped so a slow bootstrap projection can't overwrite a fresher
	 * AOP / event projection (see the QueryDocument javadoc). The default reads
	 * {@code dateChanged ?? dateCreated} from any {@link BaseOpenmrsData} record and falls back to
	 * {@code null} (last-write-wins) for sources without audit timestamps; subclasses override only
	 * when their version source lives elsewhere.
	 */
	protected Instant getLastModified(T record) {
		return record instanceof BaseOpenmrsData ? lastModifiedOf((BaseOpenmrsData) record) : null;
	}

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
		putUuidAndName(doc, FIELD_PROVIDER_UUID, FIELD_PROVIDER_NAME, pickActiveProvider(encounter));
	}

	/**
	 * Writes a UUID + name pair for any {@link OpenmrsMetadata}-typed reference (Form, Location,
	 * Provider, CareSetting, EncounterType, etc.). No-ops on null. The name is read via the
	 * entity's own {@code getName()}; for Concept references the name needs locale-aware
	 * resolution and should be written via {@link #putConceptUuidAndName} instead.
	 */
	protected static void putUuidAndName(QueryDocument doc, String uuidKey, String nameKey, OpenmrsMetadata ref) {
		if (ref == null) {
			return;
		}
		doc.putMetadata(uuidKey, ref.getUuid());
		if (ref.getName() != null) {
			doc.putMetadata(nameKey, ref.getName());
		}
	}

	/**
	 * Writes a UUID + name pair for a {@link Concept} reference, e.g. dose units, route,
	 * specimen source, substitution type. The caller resolves the locale-aware preferred name
	 * once (typically via {@link ConceptNameUtil#getPreferredNameOrNull}) and passes it through,
	 * preserving the single-walk-per-concept invariant. No-ops when the concept is null.
	 */
	protected static void putConceptUuidAndName(QueryDocument doc, String uuidKey, String nameKey,
	                                            Concept concept, String resolvedName) {
		if (concept == null) {
			return;
		}
		doc.putMetadata(uuidKey, concept.getUuid());
		if (resolvedName != null) {
			doc.putMetadata(nameKey, resolvedName);
		}
	}

	/**
	 * Standard {@code dateChanged ?? dateCreated} formula for the version timestamp on a record
	 * sourced from a {@link BaseOpenmrsData} entity. Returns {@code null} when both audit dates are
	 * unset (a transient or test-constructed entity), which leaves the document without version
	 * protection on the backend write path.
	 */
	protected static Instant lastModifiedOf(BaseOpenmrsData record) {
		if (record == null) {
			return null;
		}
		Date d = record.getDateChanged() != null ? record.getDateChanged() : record.getDateCreated();
		return d != null ? d.toInstant() : null;
	}

	/**
	 * Trims a free-text string and returns {@code null} when empty or null, so callers can
	 * null-check once instead of separately guarding null and emptiness, and can reuse the
	 * trimmed result across text composition and metadata writes without re-trimming.
	 */
	protected static String trimToNull(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}

	/**
	 * Stable-order Comparator for nested-object metadata arrays per the Decision 6
	 * "Nested-object metadata arrays" convention. Sorts by the entity's primary-key id with a UUID
	 * tiebreaker; both keys use {@code nullsLast} so transient or test-constructed records (no id
	 * yet) and partially-built entities (no UUID) still produce a deterministic order. Callers that
	 * need a leading preferred-first key chain this as the secondary sort via
	 * {@code .thenComparing(byIdThenUuid(...))}.
	 */
	protected static <T> Comparator<T> byIdThenUuid(Function<T, Integer> idGetter,
	                                                Function<T, String> uuidGetter) {
		return Comparator.comparing(idGetter, Comparator.nullsLast(Comparator.naturalOrder()))
		        .thenComparing(uuidGetter, Comparator.nullsLast(Comparator.naturalOrder()));
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
