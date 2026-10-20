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

import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_COMMENT;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DESCRIPTION;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_INTERPRETATION;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_MAPPING_CODES;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_MAPPING_NAMES;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_OBS_GROUP_CONCEPT_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_OBS_GROUP_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_STATUS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_SYNONYMS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_UNITS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_VALUE_BOOLEAN;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_VALUE_CODED_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_VALUE_CODED_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_VALUE_COMPLEX_HANDLER;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_VALUE_COMPLEX_URI;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_VALUE_DATETIME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_VALUE_DRUG_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_VALUE_DRUG_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_VALUE_NUMERIC;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_VALUE_TEXT;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptNumeric;
import org.openmrs.Drug;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.util.ConceptNameUtil;
import org.openmrs.module.querystore.util.DateFormatUtil;

/**
 * Serializes an {@link Obs} into a {@link QueryDocument} for the {@code querystore_obs} index.
 * Group obs members are emitted as atomic documents with {@code obs_group_uuid} and
 * {@code obs_group_concept_name} populated; group parents whose own value is empty are skipped
 * (their members are serialized individually) per the ADR decision 6 Synonyms and group obs
 * convention.
 *
 * <p>{@link #serialize} processes a single {@link Obs} at a time. The <em>sync</em> path
 * ({@code RecordProjector}) fans out across group members via {@link #collectTree(Obs)} — a
 * {@code saveObs} hands it only the in-memory parent, so it must walk the tree to reach members. The
 * backfill does not use {@code collectTree}: it scans the {@code obs} table, where members are their
 * own rows, and serializes each directly.
 */
public class ObsRecordSerializer extends AbstractRecordSerializer<Obs> {

	@Override
	public String getResourceType() {
		return "obs";
	}

	@Override
	public Class<Obs> getSupportedType() {
		return Obs.class;
	}

	/**
	 * Flattens an obs group into the parent plus every member, walked recursively — group members
	 * are themselves obs. Each node is then projected (or deleted, if voided) in its own right by
	 * {@code RecordProjector}, so a {@code saveObs} on a parent whose member was newly voided routes
	 * the voided member to delete while indexing the live siblings.
	 */
	@Override
	public List<Obs> collectTree(Obs root) {
		List<Obs> out = new ArrayList<>();
		collect(root, out);
		return out;
	}

	private static void collect(Obs node, List<Obs> out) {
		out.add(node);
		Set<Obs> members = node.getGroupMembers(true);
		if (members == null) {
			// Obs.getGroupMembers(true) returns the raw field, which is null on a freshly
			// constructed obs that never had members added.
			return;
		}
		for (Obs child : members) {
			collect(child, out);
		}
	}

	@Override
	protected String getPatientUuid(Obs obs) {
		// Patient and Person share the same UUID, so reading from getPerson is correct for any
		// patient-scoped obs. Non-patient Person obs would index under a non-patient UUID; tracked
		// under the Person vs Patient open question (docs/adr.md#person-vs-patient-model).
		return obs.getPerson() != null ? obs.getPerson().getUuid() : null;
	}

	@Override
	protected String getResourceUuid(Obs obs) {
		return obs.getUuid();
	}

	@Override
	protected LocalDate getDate(Obs obs) {
		return DateFormatUtil.toLocalDate(obs.getObsDatetime());
	}

	@Override
	protected void populate(Obs obs, QueryDocument doc) {
		Concept concept = obs.getConcept();
		ConceptDatatype datatype = concept != null ? concept.getDatatype() : null;

		String valueDisplay = extractValueAndPopulate(obs, concept, datatype, doc);
		if (valueDisplay.isEmpty()) {
			return;
		}
		String preferredName = ConceptNameUtil.getPreferredName(concept);
		doc.setText(preferredName.isEmpty() ? valueDisplay : preferredName + ": " + valueDisplay);

		putConceptFields(doc, concept, preferredName);
		putValueCodedConceptFields(doc, obs.getValueCoded(), valueDisplay);
		putObsMetaFields(doc, obs);
		putGroupFields(doc, obs);
		putEncounterContext(doc, obs.getEncounter());
	}

	private void putValueCodedConceptFields(QueryDocument doc, Concept valueCoded, String codedPreferredName) {
		if (valueCoded == null) {
			return;
		}
		List<String> codedSynonyms = ConceptNameUtil.getSynonyms(valueCoded, codedPreferredName);
		if (!codedSynonyms.isEmpty()) {
			mergeStringListMetadata(doc, FIELD_SYNONYMS, codedSynonyms);
		}
		String codedDescription = ConceptNameUtil.getDescription(valueCoded);
		if (!codedDescription.isEmpty()) {
			Object existing = doc.getMetadata().get(FIELD_DESCRIPTION);
			if (existing instanceof String && !((String) existing).isEmpty()) {
				if (!existing.equals(codedDescription)) {
					doc.putMetadata(FIELD_DESCRIPTION, existing + " " + codedDescription);
				}
			} else {
				doc.putMetadata(FIELD_DESCRIPTION, codedDescription);
			}
		}
		List<String> codedMappings = ConceptNameUtil.getMappingNames(valueCoded);
		if (!codedMappings.isEmpty()) {
			mergeStringListMetadata(doc, FIELD_MAPPING_NAMES, codedMappings);
		}
		// The answer's names are merged above, so a question naming the answer's code (CIEL:703)
		// must match the record too.
		List<String> codedCodes = ConceptNameUtil.getMappingCodes(valueCoded);
		if (!codedCodes.isEmpty()) {
			mergeStringListMetadata(doc, FIELD_MAPPING_CODES, codedCodes);
		}
	}

	/** Appends the answer concept's list after the question concept's, dropping duplicates. Each part
	 *  is already sorted and capped, so the merged list is deterministic and holds up to twice the cap. */
	private static void mergeStringListMetadata(QueryDocument doc, String key, List<String> additions) {
		Object existing = doc.getMetadata().get(key);
		if (!(existing instanceof List)) {
			doc.putMetadata(key, additions);
			return;
		}
		LinkedHashSet<String> merged = new LinkedHashSet<>();
		for (Object item : (List<?>) existing) {
			if (item instanceof String && !((String) item).isEmpty()) {
				merged.add((String) item);
			}
		}
		merged.addAll(additions);
		doc.putMetadata(key, new ArrayList<>(merged));
	}

	/**
	 * Walks the obs's value fields exactly once, populating value-related metadata and returning
	 * the display string used to compose {@code text}. Returns an empty string when the obs has
	 * no recordable value (e.g., a group parent whose members carry the data).
	 */
	private String extractValueAndPopulate(Obs obs, Concept concept, ConceptDatatype datatype, QueryDocument doc) {
		Concept valueCoded = obs.getValueCoded();
		if (valueCoded != null) {
			String codedName = ConceptNameUtil.getPreferredName(valueCoded);
			if (!codedName.isEmpty()) {
				doc.putMetadata(FIELD_VALUE_CODED_UUID, valueCoded.getUuid());
				doc.putMetadata(FIELD_VALUE_CODED_NAME, codedName);
			}
			return codedName;
		}
		Drug drug = obs.getValueDrug();
		if (drug != null) {
			String drugName = drug.getName();
			if (drugName != null) {
				doc.putMetadata(FIELD_VALUE_DRUG_UUID, drug.getUuid());
				doc.putMetadata(FIELD_VALUE_DRUG_NAME, drugName);
				return drugName;
			}
			return "";
		}
		Double valueNumeric = obs.getValueNumeric();
		if (valueNumeric != null) {
			doc.putMetadata(FIELD_VALUE_NUMERIC, valueNumeric);
			String units = getUnits(concept, datatype);
			if (units != null && !units.isEmpty()) {
				doc.putMetadata(FIELD_UNITS, units);
			}
			String modifier = obs.getValueModifier();
			StringBuilder sb = new StringBuilder();
			if (modifier != null) {
				sb.append(modifier);
			}
			sb.append(valueNumeric);
			if (units != null && !units.isEmpty()) {
				sb.append(' ').append(units);
			}
			return sb.toString();
		}
		String valueText = obs.getValueText();
		if (valueText != null && !valueText.isEmpty()) {
			doc.putMetadata(FIELD_VALUE_TEXT, valueText);
			return valueText;
		}
		if (obs.getValueDatetime() != null) {
			String formatted = DateFormatUtil.formatDate(obs.getValueDatetime());
			doc.putMetadata(FIELD_VALUE_DATETIME, formatted);
			return formatted;
		}
		if (datatype != null && datatype.isBoolean() && obs.getValueBoolean() != null) {
			Boolean b = obs.getValueBoolean();
			doc.putMetadata(FIELD_VALUE_BOOLEAN, b);
			return b ? "Yes" : "No";
		}
		if (datatype != null && datatype.isComplex() && obs.getValueComplex() != null) {
			doc.putMetadata(FIELD_VALUE_COMPLEX_URI, obs.getValueComplex());
			String handler = resolveComplexHandler(obs.getConcept());
			if (handler != null && !handler.isEmpty()) {
				doc.putMetadata(FIELD_VALUE_COMPLEX_HANDLER, handler);
			}
			return "[complex value]";
		}
		return "";
	}

	/**
	 * Resolves the registered {@link org.openmrs.obs.ComplexObsHandler} name for a complex-typed obs.
	 * When the obs's concept is already a {@link ConceptComplex} instance (the common case under
	 * Hibernate's joined-table inheritance), we read the handler directly. When Hibernate returned a
	 * plain {@code Concept} proxy, we fall back to a typed service lookup — one extra query per
	 * complex obs, which is acceptable because complex obs are sparse (images, PDFs, DICOM) relative
	 * to numeric/coded obs.
	 */
	private String resolveComplexHandler(Concept concept) {
		if (concept instanceof ConceptComplex) {
			return ((ConceptComplex) concept).getHandler();
		}
		if (concept == null || concept.getConceptId() == null) {
			return null;
		}
		ConceptComplex typed = Context.getConceptService().getConceptComplex(concept.getConceptId());
		return typed == null ? null : typed.getHandler();
	}

	private void putObsMetaFields(QueryDocument doc, Obs obs) {
		if (obs.getInterpretation() != null) {
			doc.putMetadata(FIELD_INTERPRETATION, obs.getInterpretation().name());
		}
		if (obs.getStatus() != null) {
			doc.putMetadata(FIELD_STATUS, obs.getStatus().name());
		}
		String comment = obs.getComment();
		if (comment != null && !comment.trim().isEmpty()) {
			doc.putMetadata(FIELD_COMMENT, comment.trim());
		}
	}

	private void putGroupFields(QueryDocument doc, Obs obs) {
		Obs parent = obs.getObsGroup();
		if (parent == null) {
			return;
		}
		doc.putMetadata(FIELD_OBS_GROUP_UUID, parent.getUuid());
		String parentName = ConceptNameUtil.getPreferredName(parent.getConcept());
		if (!parentName.isEmpty()) {
			doc.putMetadata(FIELD_OBS_GROUP_CONCEPT_NAME, parentName);
		}
	}

	private String getUnits(Concept concept, ConceptDatatype datatype) {
		if (concept == null) {
			return null;
		}
		if (concept instanceof ConceptNumeric) {
			return ((ConceptNumeric) concept).getUnits();
		}
		// Hibernate proxies don't pass instanceof; resolve via the concept service when the
		// datatype is numeric.
		if (datatype != null && datatype.isNumeric()) {
			ConceptNumeric cn = Context.getConceptService().getConceptNumeric(concept.getConceptId());
			if (cn != null) {
				return cn.getUnits();
			}
		}
		return null;
	}
}
