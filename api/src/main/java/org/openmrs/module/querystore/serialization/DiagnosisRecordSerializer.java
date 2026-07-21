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

import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_CERTAINTY;
import static org.openmrs.module.querystore.QueryStoreConstants.DATE_KIND_ADMINISTRATIVE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_CONDITION_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_NON_CODED;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_RANK;

import java.time.LocalDate;

import org.openmrs.CodedOrFreeText;
import org.openmrs.Concept;
import org.openmrs.Condition;
import org.openmrs.Diagnosis;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.util.ConceptNameUtil;
import org.openmrs.module.querystore.util.DateFormatUtil;

/**
 * Serializes a {@link Diagnosis} into a {@link QueryDocument} for the {@code querystore_diagnosis}
 * index. Mirrors {@link ConditionRecordSerializer}: coded diagnoses populate
 * {@code concept_uuid}/{@code concept_name}/{@code synonyms}; non-coded diagnoses populate
 * {@code non_coded} with the free-text label and use it as the display name. Rank is mapped to
 * "Primary" for {@code 1} (matching core's {@code PRIMARY_RANK}) and "Secondary" otherwise.
 */
public class DiagnosisRecordSerializer extends AbstractRecordSerializer<Diagnosis> {

	private static final Integer PRIMARY_RANK = 1;

	private static final String RANK_PRIMARY = "Primary";

	private static final String RANK_SECONDARY = "Secondary";

	@Override
	public String getResourceType() {
		return "diagnosis";
	}

	@Override
	public Class<Diagnosis> getSupportedType() {
		return Diagnosis.class;
	}

	@Override
	protected String getPatientUuid(Diagnosis diagnosis) {
		return diagnosis.getPatient() != null ? diagnosis.getPatient().getUuid() : null;
	}

	@Override
	protected String getResourceUuid(Diagnosis diagnosis) {
		return diagnosis.getUuid();
	}

	@Override
	protected LocalDate getDate(Diagnosis diagnosis) {
		return DateFormatUtil.toLocalDate(diagnosis.getDateCreated());
	}

	@Override
	protected LocalDate getClinicalDate(Diagnosis diagnosis) {
		return diagnosis.getEncounter() == null ? null
		        : DateFormatUtil.toLocalDate(diagnosis.getEncounter().getEncounterDatetime());
	}

	@Override
	protected String getDateKind(Diagnosis diagnosis) {
		return DATE_KIND_ADMINISTRATIVE;
	}

	@Override
	protected void populate(Diagnosis diagnosis, QueryDocument doc) {
		CodedOrFreeText cft = diagnosis.getDiagnosis();
		if (cft == null) {
			return;
		}
		Concept coded = cft.getCoded();
		String preferredName = coded != null ? ConceptNameUtil.getPreferredName(coded) : "";
		String name = coded != null
		        ? preferredName
		        : (cft.getNonCoded() != null ? cft.getNonCoded().trim() : "");
		if (name.isEmpty()) {
			return;
		}

		String rankLabel = rankLabel(diagnosis.getRank());

		doc.setText(buildText(name, diagnosis, rankLabel));

		if (coded != null) {
			putConceptFields(doc, coded, preferredName);
		} else {
			doc.putMetadata(FIELD_NON_CODED, name);
		}

		if (diagnosis.getCertainty() != null) {
			doc.putMetadata(FIELD_CERTAINTY, diagnosis.getCertainty().name());
		}
		if (rankLabel != null) {
			doc.putMetadata(FIELD_RANK, rankLabel);
		}
		Condition condition = diagnosis.getCondition();
		if (condition != null) {
			doc.putMetadata(FIELD_CONDITION_UUID, condition.getUuid());
		}

		putEncounterContext(doc, diagnosis.getEncounter());
	}

	private static String buildText(String name, Diagnosis diagnosis, String rankLabel) {
		StringBuilder sb = new StringBuilder("Diagnosis: ").append(name);
		if (diagnosis.getCertainty() != null) {
			sb.append(". Certainty: ").append(diagnosis.getCertainty().name());
		}
		if (rankLabel != null) {
			sb.append(". Rank: ").append(rankLabel);
		}
		return sb.toString();
	}

	private static String rankLabel(Integer rank) {
		if (rank == null) {
			return null;
		}
		return PRIMARY_RANK.equals(rank) ? RANK_PRIMARY : RANK_SECONDARY;
	}
}
