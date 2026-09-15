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

import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ADDITIONAL_DETAIL;
import static org.openmrs.module.querystore.QueryStoreConstants.DATE_KIND_ADMINISTRATIVE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_CLINICAL_STATUS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_END_DATE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_NON_CODED;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ONSET_DATE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_PREVIOUS_VERSION_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_VERIFICATION_STATUS;

import java.time.LocalDate;

import org.openmrs.CodedOrFreeText;
import org.openmrs.Concept;
import org.openmrs.Condition;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.util.ConceptNameUtil;
import org.openmrs.module.querystore.util.DateFormatUtil;

/**
 * Serializes a {@link Condition} into a {@link QueryDocument} for the {@code querystore_condition}
 * index. Coded conditions populate {@code concept_uuid}/{@code concept_name}/{@code synonyms};
 * non-coded conditions populate {@code non_coded} with the free-text label and use it as the
 * display name. Onset and end (resolution) dates are clinically significant and appear in both
 * {@code text} and metadata per ADR decisions 6 and 7.
 */
public class ConditionRecordSerializer extends AbstractRecordSerializer<Condition> {

	@Override
	public String getResourceType() {
		return "condition";
	}

	@Override
	public Class<Condition> getSupportedType() {
		return Condition.class;
	}

	@Override
	protected String getPatientUuid(Condition condition) {
		return condition.getPatient() != null ? condition.getPatient().getUuid() : null;
	}

	@Override
	protected String getResourceUuid(Condition condition) {
		return condition.getUuid();
	}

	@Override
	protected LocalDate getDate(Condition condition) {
		return DateFormatUtil.toLocalDate(condition.getDateCreated());
	}

	@Override
	protected LocalDate getClinicalDate(Condition condition) {
		return DateFormatUtil.toLocalDate(condition.getOnsetDate());
	}

	@Override
	protected String getDateKind(Condition condition) {
		return DATE_KIND_ADMINISTRATIVE;
	}

	@Override
	protected void populate(Condition condition, QueryDocument doc) {
		CodedOrFreeText cft = condition.getCondition();
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

		String onsetText = DateFormatUtil.formatDate(condition.getOnsetDate());
		String endText = DateFormatUtil.formatDate(condition.getEndDate());

		doc.setText(buildText(name, condition, onsetText, endText));

		if (coded != null) {
			putConceptFields(doc, coded, preferredName);
		} else {
			doc.putMetadata(FIELD_NON_CODED, name);
		}

		if (condition.getClinicalStatus() != null) {
			doc.putMetadata(FIELD_CLINICAL_STATUS, condition.getClinicalStatus().name());
		}
		if (condition.getVerificationStatus() != null) {
			doc.putMetadata(FIELD_VERIFICATION_STATUS, condition.getVerificationStatus().name());
		}
		if (onsetText != null) {
			doc.putMetadata(FIELD_ONSET_DATE, onsetText);
		}
		if (endText != null) {
			doc.putMetadata(FIELD_END_DATE, endText);
		}
		String detail = condition.getAdditionalDetail();
		if (detail != null && !detail.trim().isEmpty()) {
			doc.putMetadata(FIELD_ADDITIONAL_DETAIL, detail.trim());
		}
		Condition previousVersion = condition.getPreviousVersion();
		if (previousVersion != null) {
			doc.putMetadata(FIELD_PREVIOUS_VERSION_UUID, previousVersion.getUuid());
		}

		putEncounterContext(doc, condition.getEncounter());
	}

	private static String buildText(String name, Condition condition, String onsetText, String endText) {
		StringBuilder sb = new StringBuilder("Condition: ").append(name);
		if (condition.getClinicalStatus() != null) {
			sb.append(". Status: ").append(condition.getClinicalStatus().name());
		}
		if (condition.getVerificationStatus() != null) {
			sb.append(". Verification: ").append(condition.getVerificationStatus().name());
		}
		if (onsetText != null) {
			sb.append(". Onset: ").append(onsetText);
		}
		if (endText != null) {
			sb.append(". Resolved: ").append(endText);
		}
		return sb.toString();
	}
}
