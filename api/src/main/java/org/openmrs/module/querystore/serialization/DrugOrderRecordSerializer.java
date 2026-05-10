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

import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ACTION;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_AS_NEEDED;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_AS_NEEDED_CONDITION;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_AUTO_EXPIRE_DATE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_CARE_SETTING;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DATE_STOPPED;
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
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ORDER_NUMBER;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_PREVIOUS_ORDER_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_PROVIDER_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_PROVIDER_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_QUANTITY;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_QUANTITY_UNITS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_QUANTITY_UNITS_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ROUTE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ROUTE_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_URGENCY;

import java.time.LocalDate;

import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.OrderFrequency;
import org.openmrs.Provider;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.util.ConceptNameUtil;
import org.openmrs.module.querystore.util.DateFormatUtil;

/**
 * Serializes a {@link DrugOrder} into a {@link QueryDocument} for the {@code openmrs_drug_order}
 * index. The display name is taken from the {@link Drug} formulation when present (e.g.,
 * "Metformin 500mg"), otherwise from the order's coded concept's preferred name. Coded units
 * (dose units, route, frequency, duration units, quantity units) are stored as both UUID and
 * human-readable name per ADR Decision 9. Provider context comes from the order's
 * {@link Order#getOrderer() orderer} rather than the encounter's first active provider —
 * the orderer is the authoritative prescriber for an order. If the order has no orderer, the
 * encounter's first active provider is retained as a fallback.
 */
public class DrugOrderRecordSerializer extends AbstractRecordSerializer<DrugOrder> {

	@Override
	public String getResourceType() {
		return "drug_order";
	}

	@Override
	public Class<DrugOrder> getSupportedType() {
		return DrugOrder.class;
	}

	@Override
	protected String getPatientUuid(DrugOrder order) {
		return order.getPatient() != null ? order.getPatient().getUuid() : null;
	}

	@Override
	protected String getResourceUuid(DrugOrder order) {
		return order.getUuid();
	}

	@Override
	protected LocalDate getDate(DrugOrder order) {
		return DateFormatUtil.toLocalDate(order.getDateActivated());
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

		String doseUnitsName = unitName(order.getDoseUnits());
		String routeName = unitName(order.getRoute());
		OrderFrequency frequency = order.getFrequency();
		// Read the frequency name via its wrapped concept rather than OrderFrequency.getName(),
		// which routes through Concept.getName() (no-arg) and requires the OpenMRS Context.
		// ConceptNameUtil's locale resolution is Context-safe per its own contract.
		String frequencyName = frequency != null ? unitName(frequency.getConcept()) : null;
		String durationUnitsName = unitName(order.getDurationUnits());
		String quantityUnitsName = unitName(order.getQuantityUnits());

		String dateStoppedText = order.getDateStopped() != null
		        ? DateFormatUtil.formatDate(order.getDateStopped()) : null;
		String autoExpireText = order.getAutoExpireDate() != null
		        ? DateFormatUtil.formatDate(order.getAutoExpireDate()) : null;

		doc.setText(buildText(displayName, order, doseUnitsName, routeName, frequencyName,
		        durationUnitsName, quantityUnitsName, dateStoppedText));

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
		putUnitFields(doc, FIELD_DOSE_UNITS_UUID, FIELD_DOSE_UNITS, order.getDoseUnits(), doseUnitsName);
		putUnitFields(doc, FIELD_ROUTE_UUID, FIELD_ROUTE, order.getRoute(), routeName);
		if (frequency != null) {
			doc.putMetadata(FIELD_FREQUENCY_UUID, frequency.getUuid());
			if (frequencyName != null) {
				doc.putMetadata(FIELD_FREQUENCY, frequencyName);
			}
		}
		if (order.getDuration() != null) {
			doc.putMetadata(FIELD_DURATION, order.getDuration());
		}
		putUnitFields(doc, FIELD_DURATION_UNITS_UUID, FIELD_DURATION_UNITS,
		        order.getDurationUnits(), durationUnitsName);
		if (order.getQuantity() != null) {
			doc.putMetadata(FIELD_QUANTITY, order.getQuantity());
		}
		putUnitFields(doc, FIELD_QUANTITY_UNITS_UUID, FIELD_QUANTITY_UNITS,
		        order.getQuantityUnits(), quantityUnitsName);

		putOrderMetaFields(doc, order, dateStoppedText, autoExpireText);

		putEncounterContext(doc, order.getEncounter());
		putOrderer(doc, order.getOrderer());
	}

	private static String buildText(String displayName, DrugOrder order, String doseUnitsName,
	                                String routeName, String frequencyName,
	                                String durationUnitsName, String quantityUnitsName,
	                                String dateStoppedText) {
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
			String condition = trimToNull(order.getAsNeededCondition());
			if (condition != null) {
				sb.append(" for ").append(condition);
			}
		}
		String instructions = trimToNull(order.getDosingInstructions());
		if (instructions != null) {
			sb.append(". ").append(instructions);
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

	private static void putOrderMetaFields(QueryDocument doc, DrugOrder order,
	                                       String dateStoppedText, String autoExpireText) {
		if (order.getAction() != null) {
			doc.putMetadata(FIELD_ACTION, order.getAction().name());
		}
		if (order.getUrgency() != null) {
			doc.putMetadata(FIELD_URGENCY, order.getUrgency().name());
		}
		String instructions = trimToNull(order.getDosingInstructions());
		if (instructions != null) {
			doc.putMetadata(FIELD_DOSING_INSTRUCTIONS, instructions);
		}
		if (order.getAsNeeded() != null) {
			doc.putMetadata(FIELD_AS_NEEDED, order.getAsNeeded());
		}
		String asNeededCondition = trimToNull(order.getAsNeededCondition());
		if (asNeededCondition != null) {
			doc.putMetadata(FIELD_AS_NEEDED_CONDITION, asNeededCondition);
		}
		if (order.getNumRefills() != null) {
			doc.putMetadata(FIELD_NUM_REFILLS, order.getNumRefills());
		}
		CareSetting careSetting = order.getCareSetting();
		if (careSetting != null && careSetting.getName() != null) {
			doc.putMetadata(FIELD_CARE_SETTING, careSetting.getName());
		}
		Order previous = order.getPreviousOrder();
		if (previous != null) {
			doc.putMetadata(FIELD_PREVIOUS_ORDER_UUID, previous.getUuid());
		}
		if (order.getOrderNumber() != null) {
			doc.putMetadata(FIELD_ORDER_NUMBER, order.getOrderNumber());
		}
		if (dateStoppedText != null) {
			doc.putMetadata(FIELD_DATE_STOPPED, dateStoppedText);
		}
		if (autoExpireText != null) {
			doc.putMetadata(FIELD_AUTO_EXPIRE_DATE, autoExpireText);
		}
	}

	private static void putUnitFields(QueryDocument doc, String uuidKey, String nameKey,
	                                  Concept unit, String resolvedName) {
		if (unit == null) {
			return;
		}
		doc.putMetadata(uuidKey, unit.getUuid());
		if (resolvedName != null) {
			doc.putMetadata(nameKey, resolvedName);
		}
	}

	private static void putOrderer(QueryDocument doc, Provider orderer) {
		if (orderer == null) {
			return;
		}
		doc.putMetadata(FIELD_PROVIDER_UUID, orderer.getUuid());
		if (orderer.getName() != null) {
			doc.putMetadata(FIELD_PROVIDER_NAME, orderer.getName());
		}
	}

	private static String unitName(Concept unit) {
		if (unit == null) {
			return null;
		}
		String name = ConceptNameUtil.getPreferredName(unit);
		return name.isEmpty() ? null : name;
	}

	private static String trimToNull(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}
