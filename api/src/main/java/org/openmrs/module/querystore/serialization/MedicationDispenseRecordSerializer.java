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

import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DATE_HANDED_OVER;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DISPENSER_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DISPENSER_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DOSE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DOSE_UNITS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DOSE_UNITS_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DOSING_INSTRUCTIONS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DRUG_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DRUG_ORDER_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DRUG_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_FREQUENCY;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_FREQUENCY_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_LOCATION_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_LOCATION_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_QUANTITY;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_QUANTITY_UNITS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_QUANTITY_UNITS_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ROUTE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_ROUTE_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_STATUS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_SUBSTITUTION_REASON;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_SUBSTITUTION_REASON_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_SUBSTITUTION_TYPE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_SUBSTITUTION_TYPE_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_WAS_SUBSTITUTED;

import java.time.LocalDate;
import java.util.Date;

import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.MedicationDispense;
import org.openmrs.OrderFrequency;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.util.ConceptNameUtil;
import org.openmrs.module.querystore.util.DateFormatUtil;

/**
 * Serializes a {@link MedicationDispense} into a {@link QueryDocument} for the
 * {@code openmrs_medication_dispense} index. Mirrors {@link DrugOrderRecordSerializer} for the
 * shared dosing surface (dose, route, frequency, units) but is not an order — it has its own
 * lifecycle (status), its own clinical-event time ({@link MedicationDispense#getDateHandedOver()
 * dateHandedOver}), and a distinct provider role (dispenser, populated as separate
 * {@code dispenser_*} metadata fields alongside the encounter-derived {@code provider_*}). When
 * the dispense carries its own {@link MedicationDispense#getLocation() location}, it overrides
 * the encounter-derived location; the dispense event happened at the pharmacy, not necessarily
 * at the encounter's clinical location.
 */
public class MedicationDispenseRecordSerializer extends AbstractRecordSerializer<MedicationDispense> {

	@Override
	public String getResourceType() {
		return "medication_dispense";
	}

	@Override
	public Class<MedicationDispense> getSupportedType() {
		return MedicationDispense.class;
	}

	@Override
	protected String getPatientUuid(MedicationDispense dispense) {
		return dispense.getPatient() != null ? dispense.getPatient().getUuid() : null;
	}

	@Override
	protected String getResourceUuid(MedicationDispense dispense) {
		return dispense.getUuid();
	}

	@Override
	protected LocalDate getDate(MedicationDispense dispense) {
		Date handed = dispense.getDateHandedOver();
		return DateFormatUtil.toLocalDate(handed != null ? handed : dispense.getDateCreated());
	}

	@Override
	protected void populate(MedicationDispense dispense, QueryDocument doc) {
		Concept concept = dispense.getConcept();
		String preferredName = ConceptNameUtil.getPreferredName(concept);
		Drug drug = dispense.getDrug();
		String drugName = drug != null && drug.getName() != null ? drug.getName().trim() : null;
		String displayName = drugName != null && !drugName.isEmpty() ? drugName : preferredName;
		if (displayName.isEmpty()) {
			return;
		}

		Concept statusConcept = dispense.getStatus();
		Concept doseUnits = dispense.getDoseUnits();
		Concept route = dispense.getRoute();
		Concept quantityUnits = dispense.getQuantityUnits();
		OrderFrequency frequency = dispense.getFrequency();
		String statusName = ConceptNameUtil.getPreferredNameOrNull(statusConcept);
		String doseUnitsName = ConceptNameUtil.getPreferredNameOrNull(doseUnits);
		String routeName = ConceptNameUtil.getPreferredNameOrNull(route);
		String frequencyName = frequency != null ? ConceptNameUtil.getPreferredNameOrNull(frequency.getConcept()) : null;
		String quantityUnitsName = ConceptNameUtil.getPreferredNameOrNull(quantityUnits);
		String dateHandedOverText = DateFormatUtil.formatDate(dispense.getDateHandedOver());
		Double dose = dispense.getDose();
		Double quantity = dispense.getQuantity();
		Boolean asNeeded = dispense.getAsNeeded();
		String dosingInstructions = trimToNull(dispense.getDosingInstructions());

		doc.setText(buildText(displayName, dose, quantity, asNeeded, dosingInstructions,
		        statusName, doseUnitsName, routeName, frequencyName, quantityUnitsName,
		        dateHandedOverText));

		putConceptFields(doc, concept, preferredName);
		if (drug != null) {
			doc.putMetadata(FIELD_DRUG_UUID, drug.getUuid());
			if (drugName != null && !drugName.isEmpty()) {
				doc.putMetadata(FIELD_DRUG_NAME, drugName);
			}
		}
		DrugOrder drugOrder = dispense.getDrugOrder();
		if (drugOrder != null) {
			doc.putMetadata(FIELD_DRUG_ORDER_UUID, drugOrder.getUuid());
		}

		if (statusName != null) {
			doc.putMetadata(FIELD_STATUS, statusName);
		}
		if (quantity != null) {
			doc.putMetadata(FIELD_QUANTITY, quantity);
		}
		putConceptUuidAndName(doc, FIELD_QUANTITY_UNITS_UUID, FIELD_QUANTITY_UNITS,
		        quantityUnits, quantityUnitsName);
		if (dose != null) {
			doc.putMetadata(FIELD_DOSE, dose);
		}
		putConceptUuidAndName(doc, FIELD_DOSE_UNITS_UUID, FIELD_DOSE_UNITS, doseUnits, doseUnitsName);
		putConceptUuidAndName(doc, FIELD_ROUTE_UUID, FIELD_ROUTE, route, routeName);
		if (frequency != null) {
			doc.putMetadata(FIELD_FREQUENCY_UUID, frequency.getUuid());
			if (frequencyName != null) {
				doc.putMetadata(FIELD_FREQUENCY, frequencyName);
			}
		}
		if (dateHandedOverText != null) {
			doc.putMetadata(FIELD_DATE_HANDED_OVER, dateHandedOverText);
		}
		if (dosingInstructions != null) {
			doc.putMetadata(FIELD_DOSING_INSTRUCTIONS, dosingInstructions);
		}

		putSubstitutionFields(doc, dispense);

		putEncounterContext(doc, dispense.getEncounter());
		putUuidAndName(doc, FIELD_LOCATION_UUID, FIELD_LOCATION_NAME, dispense.getLocation());
		putUuidAndName(doc, FIELD_DISPENSER_UUID, FIELD_DISPENSER_NAME, dispense.getDispenser());
	}

	private static String buildText(String displayName, Double dose, Double quantity,
	                                Boolean asNeeded, String dosingInstructions,
	                                String statusName, String doseUnitsName, String routeName,
	                                String frequencyName, String quantityUnitsName,
	                                String dateHandedOverText) {
		StringBuilder sb = new StringBuilder("Dispensed: ").append(displayName);

		if (statusName != null) {
			sb.append(". Status: ").append(statusName);
		}
		if (quantity != null) {
			sb.append(". Quantity: ").append(quantity);
			if (quantityUnitsName != null) {
				sb.append(' ').append(quantityUnitsName);
			}
		}
		if (dose != null) {
			sb.append(". Dose: ").append(dose);
			if (doseUnitsName != null) {
				sb.append(' ').append(doseUnitsName);
			}
			if (routeName != null) {
				sb.append(' ').append(routeName);
			}
			if (frequencyName != null) {
				sb.append(' ').append(frequencyName);
			}
		}
		if (dateHandedOverText != null) {
			sb.append(". Handed over: ").append(dateHandedOverText);
		}
		if (Boolean.TRUE.equals(asNeeded)) {
			sb.append(". PRN");
		}
		if (dosingInstructions != null) {
			sb.append(". ").append(dosingInstructions);
		}
		return sb.toString();
	}

	private static void putSubstitutionFields(QueryDocument doc, MedicationDispense dispense) {
		Boolean wasSubstituted = dispense.getWasSubstituted();
		if (wasSubstituted != null) {
			doc.putMetadata(FIELD_WAS_SUBSTITUTED, wasSubstituted);
		}
		Concept substitutionType = dispense.getSubstitutionType();
		putConceptUuidAndName(doc, FIELD_SUBSTITUTION_TYPE_UUID, FIELD_SUBSTITUTION_TYPE,
		        substitutionType, ConceptNameUtil.getPreferredNameOrNull(substitutionType));
		Concept substitutionReason = dispense.getSubstitutionReason();
		putConceptUuidAndName(doc, FIELD_SUBSTITUTION_REASON_UUID, FIELD_SUBSTITUTION_REASON,
		        substitutionReason, ConceptNameUtil.getPreferredNameOrNull(substitutionReason));
	}

}
