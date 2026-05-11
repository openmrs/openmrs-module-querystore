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

import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_AS_NEEDED;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_AS_NEEDED_CONDITION;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DOSE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DOSE_UNITS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DOSE_UNITS_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DOSING_INSTRUCTIONS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DRUG_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DRUG_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DURATION;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DURATION_UNITS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DURATION_UNITS_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_FREQUENCY;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_FREQUENCY_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_NUM_REFILLS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_QUANTITY;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_QUANTITY_UNITS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_QUANTITY_UNITS_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ROUTE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ROUTE_UUID;

import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.OrderFrequency;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.util.ConceptNameUtil;
import org.openmrs.module.querystore.util.DateFormatUtil;

/**
 * Serializes a {@link DrugOrder} into a {@link QueryDocument} for the {@code openmrs_drug_order}
 * index. Order-base fields (action, urgency, care setting, previous order, order number, date
 * stopped, auto-expire date) and the Order-identity overrides plus the orderer-overrides-encounter-
 * provider convention live in {@link AbstractOrderRecordSerializer}; this class handles the
 * drug-specific surface — the {@link Drug} formulation (preferred over the order's concept
 * preferred name for display when present), dose + dose units, route, frequency, duration +
 * duration units, quantity + quantity units (all coded units stored as UUID+name per ADR Decision
 * 9), PRN flag and condition, dosing instructions, and number of refills.
 */
public class DrugOrderRecordSerializer extends AbstractOrderRecordSerializer<DrugOrder> {

	@Override
	public String getResourceType() {
		return "drug_order";
	}

	@Override
	public Class<DrugOrder> getSupportedType() {
		return DrugOrder.class;
	}

	@Override
	protected void populate(DrugOrder order, QueryDocument doc) {
		Concept concept = order.getConcept();
		String preferredName = ConceptNameUtil.getPreferredName(concept);
		Drug drug = order.getDrug();
		String drugName = drug != null && drug.getName() != null ? drug.getName().trim() : null;
		String displayName = drugName != null && !drugName.isEmpty() ? drugName : preferredName;
		if (displayName.isEmpty()) {
			return;
		}

		String doseUnitsName = ConceptNameUtil.getPreferredNameOrNull(order.getDoseUnits());
		String routeName = ConceptNameUtil.getPreferredNameOrNull(order.getRoute());
		OrderFrequency frequency = order.getFrequency();
		// Read the frequency name via its wrapped concept rather than OrderFrequency.getName(),
		// which routes through Concept.getName() (no-arg) and requires the OpenMRS Context.
		// ConceptNameUtil's locale resolution is Context-safe per its own contract.
		String frequencyName = frequency != null
		        ? ConceptNameUtil.getPreferredNameOrNull(frequency.getConcept()) : null;
		String durationUnitsName = ConceptNameUtil.getPreferredNameOrNull(order.getDurationUnits());
		String quantityUnitsName = ConceptNameUtil.getPreferredNameOrNull(order.getQuantityUnits());

		String dateStoppedText = DateFormatUtil.formatDate(order.getDateStopped());
		String autoExpireText = DateFormatUtil.formatDate(order.getAutoExpireDate());
		String dosingInstructions = trimToNull(order.getDosingInstructions());
		String asNeededCondition = trimToNull(order.getAsNeededCondition());

		doc.setText(buildText(displayName, order, doseUnitsName, routeName, frequencyName,
		        durationUnitsName, quantityUnitsName, dateStoppedText, dosingInstructions,
		        asNeededCondition));

		putConceptFields(doc, concept, preferredName);
		if (drug != null) {
			doc.putMetadata(FIELD_DRUG_UUID, drug.getUuid());
			if (drugName != null && !drugName.isEmpty()) {
				doc.putMetadata(FIELD_DRUG_NAME, drugName);
			}
		}

		if (order.getDose() != null) {
			doc.putMetadata(FIELD_DOSE, order.getDose());
		}
		putConceptUuidAndName(doc, FIELD_DOSE_UNITS_UUID, FIELD_DOSE_UNITS, order.getDoseUnits(), doseUnitsName);
		putConceptUuidAndName(doc, FIELD_ROUTE_UUID, FIELD_ROUTE, order.getRoute(), routeName);
		if (frequency != null) {
			doc.putMetadata(FIELD_FREQUENCY_UUID, frequency.getUuid());
			if (frequencyName != null) {
				doc.putMetadata(FIELD_FREQUENCY, frequencyName);
			}
		}
		if (order.getDuration() != null) {
			doc.putMetadata(FIELD_DURATION, order.getDuration());
		}
		putConceptUuidAndName(doc, FIELD_DURATION_UNITS_UUID, FIELD_DURATION_UNITS,
		        order.getDurationUnits(), durationUnitsName);
		if (order.getQuantity() != null) {
			doc.putMetadata(FIELD_QUANTITY, order.getQuantity());
		}
		putConceptUuidAndName(doc, FIELD_QUANTITY_UNITS_UUID, FIELD_QUANTITY_UNITS,
		        order.getQuantityUnits(), quantityUnitsName);

		if (dosingInstructions != null) {
			doc.putMetadata(FIELD_DOSING_INSTRUCTIONS, dosingInstructions);
		}
		if (order.getAsNeeded() != null) {
			doc.putMetadata(FIELD_AS_NEEDED, order.getAsNeeded());
		}
		if (asNeededCondition != null) {
			doc.putMetadata(FIELD_AS_NEEDED_CONDITION, asNeededCondition);
		}
		if (order.getNumRefills() != null) {
			doc.putMetadata(FIELD_NUM_REFILLS, order.getNumRefills());
		}

		putOrderBaseFields(doc, order, dateStoppedText, autoExpireText);
		putOrderEncounterAndProvider(doc, order);
	}

	private static String buildText(String displayName, DrugOrder order, String doseUnitsName,
	                                String routeName, String frequencyName,
	                                String durationUnitsName, String quantityUnitsName,
	                                String dateStoppedText, String dosingInstructions,
	                                String asNeededCondition) {
		StringBuilder sb = new StringBuilder("Drug order: ").append(displayName);

		// Only emit ". Dose: ..." when an actual dose number is present. Without a dose, the
		// "Dose:" label misreads if it precedes route/frequency alone; those still appear in
		// metadata for filtering. The ADR golden example always carries a dose.
		if (order.getDose() != null) {
			String dosing = formatDosing(order, doseUnitsName, routeName, frequencyName);
			if (!dosing.isEmpty()) {
				sb.append(". Dose: ").append(dosing);
			}
		}

		if (order.getDuration() != null) {
			sb.append(". Duration: ").append(order.getDuration());
			if (durationUnitsName != null) {
				sb.append(' ').append(durationUnitsName);
			}
		}

		if (order.getQuantity() != null) {
			sb.append(". Quantity: ").append(order.getQuantity());
			if (quantityUnitsName != null) {
				sb.append(' ').append(quantityUnitsName);
			}
		}

		if (order.getAction() != null) {
			sb.append(". Action: ").append(order.getAction().name());
		}
		if (order.getUrgency() != null) {
			sb.append(". Urgency: ").append(order.getUrgency().name());
		}
		if (dateStoppedText != null) {
			sb.append(". Stopped: ").append(dateStoppedText);
		}
		if (Boolean.TRUE.equals(order.getAsNeeded())) {
			sb.append(". PRN");
			if (asNeededCondition != null) {
				sb.append(" for ").append(asNeededCondition);
			}
		}
		if (dosingInstructions != null) {
			sb.append(". ").append(dosingInstructions);
		}
		return sb.toString();
	}

	private static String formatDosing(DrugOrder order, String doseUnitsName,
	                                   String routeName, String frequencyName) {
		StringBuilder sb = new StringBuilder();
		if (order.getDose() != null) {
			sb.append(order.getDose());
			if (doseUnitsName != null) {
				sb.append(' ').append(doseUnitsName);
			}
		}
		if (routeName != null) {
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(routeName);
		}
		if (frequencyName != null) {
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(frequencyName);
		}
		return sb.toString();
	}
}
