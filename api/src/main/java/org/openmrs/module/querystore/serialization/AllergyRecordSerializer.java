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

import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ALLERGEN_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.DATE_KIND_ADMINISTRATIVE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ALLERGEN_NON_CODED;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ALLERGEN_TYPE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ALLERGEN_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_COMMENT;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_REACTIONS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_SEVERITY;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_SYNONYMS;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openmrs.Allergen;
import org.openmrs.AllergenType;
import org.openmrs.Allergy;
import org.openmrs.AllergyReaction;
import org.openmrs.Concept;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.util.ConceptNameUtil;
import org.openmrs.module.querystore.util.DateFormatUtil;

/**
 * Serializes an {@link Allergy} into a {@link QueryDocument} for the {@code querystore_allergy}
 * index. The allergen is wrapped in an {@link Allergen} carrying either a coded concept or a
 * free-text label plus an {@link AllergenType} discriminator (DRUG/FOOD/ENVIRONMENT/OTHER); the
 * coded-or-free-text resolution mirrors {@link ConditionRecordSerializer} but the resulting
 * fields are named {@code allergen_uuid}/{@code allergen_name}/{@code allergen_non_coded} rather
 * than the generic {@code concept_*} because the allergen role is domain-specific (ADR Decision 6
 * example for {@code querystore_allergy}). {@code severity} and {@code reactions} are name-only per
 * the Decision 9 small-stable-value-set exception. Reaction UUIDs are intentionally omitted —
 * reactions are a refinement filter alongside {@code allergen_uuid}, never a primary query axis,
 * so name-based matching is sufficient. The allergen concept's synonyms still populate the
 * cross-cutting {@code synonyms} field per the Decision 6 synonyms convention.
 */
public class AllergyRecordSerializer extends AbstractRecordSerializer<Allergy> {

	@Override
	public String getResourceType() {
		return "allergy";
	}

	@Override
	public Class<Allergy> getSupportedType() {
		return Allergy.class;
	}

	@Override
	protected String getPatientUuid(Allergy allergy) {
		return allergy.getPatient() != null ? allergy.getPatient().getUuid() : null;
	}

	@Override
	protected String getResourceUuid(Allergy allergy) {
		return allergy.getUuid();
	}

	@Override
	protected LocalDate getDate(Allergy allergy) {
		return DateFormatUtil.toLocalDate(allergy.getDateCreated());
	}

	@Override
	protected LocalDate getClinicalDate(Allergy allergy) {
		return allergy.getEncounter() == null ? null
		        : DateFormatUtil.toLocalDate(allergy.getEncounter().getEncounterDatetime());
	}

	@Override
	protected String getDateKind(Allergy allergy) {
		return DATE_KIND_ADMINISTRATIVE;
	}

	@Override
	protected void populate(Allergy allergy, QueryDocument doc) {
		Allergen allergen = allergy.getAllergen();
		if (allergen == null) {
			return;
		}
		// Allergen.isCoded() returns false when codedAllergen points at the global "Other
		// (non-coded)" sentinel concept — core uses that pattern to back free-text allergens
		// while preserving the FK. Branching on codedAllergen != null would misclassify those
		// records as coded and emit the sentinel UUID as allergen_uuid, breaking BM25 and
		// UUID-filtered queries. Trust isCoded().
		boolean coded = allergen.isCoded();
		Concept codedAllergen = coded ? allergen.getCodedAllergen() : null;
		String preferredName = codedAllergen != null ? ConceptNameUtil.getPreferredName(codedAllergen) : "";
		String nonCoded = trimToNull(allergen.getNonCodedAllergen());
		String displayName = !preferredName.isEmpty() ? preferredName : (nonCoded != null ? nonCoded : "");
		if (displayName.isEmpty()) {
			return;
		}

		AllergenType allergenType = allergen.getAllergenType();
		String severityName = ConceptNameUtil.getPreferredNameOrNull(allergy.getSeverity());
		List<String> reactionNames = resolveReactionNames(allergy.getReactions());

		doc.setText(buildText(displayName, allergenType, severityName, reactionNames));

		if (codedAllergen != null) {
			doc.putMetadata(FIELD_ALLERGEN_UUID, codedAllergen.getUuid());
			doc.putMetadata(FIELD_ALLERGEN_NAME, preferredName);
			List<String> synonyms = ConceptNameUtil.getSynonyms(codedAllergen, preferredName);
			if (!synonyms.isEmpty()) {
				doc.putMetadata(FIELD_SYNONYMS, synonyms);
			}
			putDescription(doc, codedAllergen);
			putMappingNames(doc, codedAllergen);
		} else {
			doc.putMetadata(FIELD_ALLERGEN_NON_CODED, displayName);
		}
		if (allergenType != null) {
			doc.putMetadata(FIELD_ALLERGEN_TYPE, allergenType.name());
		}
		if (severityName != null) {
			doc.putMetadata(FIELD_SEVERITY, severityName);
		}
		if (!reactionNames.isEmpty()) {
			doc.putMetadata(FIELD_REACTIONS, reactionNames);
		}
		String comment = trimToNull(allergy.getComment());
		if (comment != null) {
			doc.putMetadata(FIELD_COMMENT, comment);
		}

		putEncounterContext(doc, allergy.getEncounter());
	}

	private static String buildText(String displayName, AllergenType allergenType,
	                                String severityName, List<String> reactionNames) {
		StringBuilder sb = new StringBuilder("Allergy: ").append(displayName);
		if (allergenType != null) {
			sb.append(" (").append(allergenType.name().toLowerCase()).append(" allergen)");
		}
		if (severityName != null) {
			sb.append(". Severity: ").append(severityName);
		}
		if (!reactionNames.isEmpty()) {
			sb.append(". Reactions: ").append(String.join(", ", reactionNames));
		}
		return sb.toString();
	}

	private static List<String> resolveReactionNames(List<AllergyReaction> reactions) {
		if (reactions == null || reactions.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> names = new ArrayList<>(reactions.size());
		for (AllergyReaction r : reactions) {
			if (r == null) {
				continue;
			}
			String name = ConceptNameUtil.getPreferredNameOrNull(r.getReaction());
			if (name == null) {
				name = trimToNull(r.getReactionNonCoded());
			}
			if (name != null) {
				names.add(name);
			}
		}
		return names;
	}
}
