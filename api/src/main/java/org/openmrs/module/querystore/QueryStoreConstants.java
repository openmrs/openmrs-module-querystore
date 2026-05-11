/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore;

public final class QueryStoreConstants {

	public static final String MODULE_ID = "querystore";

	public static final String INDEX_PREFIX = "openmrs_";

	public static final String FIELD_PATIENT_UUID = "patient_uuid";

	public static final String FIELD_RESOURCE_UUID = "resource_uuid";

	public static final String FIELD_RECORD_DATE = "record_date";

	// Document metadata field names per ADR Decision 6. Keep aligned with the example documents
	// and field-descriptions table in docs/adr.md.

	public static final String FIELD_CONCEPT_UUID = "concept_uuid";
	public static final String FIELD_CONCEPT_NAME = "concept_name";
	public static final String FIELD_CONCEPT_CLASS = "concept_class";
	public static final String FIELD_SYNONYMS = "synonyms";

	public static final String FIELD_VALUE_NUMERIC = "value_numeric";
	public static final String FIELD_VALUE_CODED_UUID = "value_coded_uuid";
	public static final String FIELD_VALUE_CODED_NAME = "value_coded_name";
	public static final String FIELD_VALUE_TEXT = "value_text";
	public static final String FIELD_VALUE_DATETIME = "value_datetime";
	public static final String FIELD_VALUE_BOOLEAN = "value_boolean";
	public static final String FIELD_VALUE_DRUG_UUID = "value_drug_uuid";
	public static final String FIELD_VALUE_DRUG_NAME = "value_drug_name";
	public static final String FIELD_VALUE_COMPLEX_URI = "value_complex_uri";
	public static final String FIELD_VALUE_COMPLEX_HANDLER = "value_complex_handler";

	public static final String FIELD_UNITS = "units";
	public static final String FIELD_INTERPRETATION = "interpretation";
	public static final String FIELD_STATUS = "status";
	public static final String FIELD_COMMENT = "comment";
	public static final String FIELD_OBS_GROUP_UUID = "obs_group_uuid";
	public static final String FIELD_OBS_GROUP_CONCEPT_NAME = "obs_group_concept_name";

	public static final String FIELD_NON_CODED = "non_coded";
	public static final String FIELD_CLINICAL_STATUS = "clinical_status";
	public static final String FIELD_VERIFICATION_STATUS = "verification_status";
	public static final String FIELD_ONSET_DATE = "onset_date";
	public static final String FIELD_END_DATE = "end_date";
	public static final String FIELD_ADDITIONAL_DETAIL = "additional_detail";
	public static final String FIELD_PREVIOUS_VERSION_UUID = "previous_version_uuid";

	public static final String FIELD_CERTAINTY = "certainty";
	public static final String FIELD_RANK = "rank";
	public static final String FIELD_CONDITION_UUID = "condition_uuid";

	public static final String FIELD_DRUG_UUID = "drug_uuid";
	public static final String FIELD_DRUG_NAME = "drug_name";
	public static final String FIELD_DOSE = "dose";
	public static final String FIELD_DOSE_UNITS_UUID = "dose_units_uuid";
	public static final String FIELD_DOSE_UNITS = "dose_units";
	public static final String FIELD_ROUTE_UUID = "route_uuid";
	public static final String FIELD_ROUTE = "route";
	public static final String FIELD_FREQUENCY_UUID = "frequency_uuid";
	public static final String FIELD_FREQUENCY = "frequency";
	public static final String FIELD_DURATION = "duration";
	public static final String FIELD_DURATION_UNITS_UUID = "duration_units_uuid";
	public static final String FIELD_DURATION_UNITS = "duration_units";
	public static final String FIELD_QUANTITY = "quantity";
	public static final String FIELD_QUANTITY_UNITS_UUID = "quantity_units_uuid";
	public static final String FIELD_QUANTITY_UNITS = "quantity_units";
	public static final String FIELD_ACTION = "action";
	public static final String FIELD_URGENCY = "urgency";
	public static final String FIELD_DOSING_INSTRUCTIONS = "dosing_instructions";
	public static final String FIELD_AS_NEEDED = "as_needed";
	public static final String FIELD_AS_NEEDED_CONDITION = "as_needed_condition";
	public static final String FIELD_NUM_REFILLS = "num_refills";
	public static final String FIELD_CARE_SETTING = "care_setting";
	public static final String FIELD_PREVIOUS_ORDER_UUID = "previous_order_uuid";
	public static final String FIELD_ORDER_NUMBER = "order_number";
	public static final String FIELD_DATE_STOPPED = "date_stopped";
	public static final String FIELD_AUTO_EXPIRE_DATE = "auto_expire_date";

	public static final String FIELD_DRUG_ORDER_UUID = "drug_order_uuid";
	public static final String FIELD_DATE_HANDED_OVER = "date_handed_over";
	public static final String FIELD_WAS_SUBSTITUTED = "was_substituted";
	public static final String FIELD_SUBSTITUTION_TYPE_UUID = "substitution_type_uuid";
	public static final String FIELD_SUBSTITUTION_TYPE = "substitution_type";
	public static final String FIELD_SUBSTITUTION_REASON_UUID = "substitution_reason_uuid";
	public static final String FIELD_SUBSTITUTION_REASON = "substitution_reason";
	public static final String FIELD_DISPENSER_UUID = "dispenser_uuid";
	public static final String FIELD_DISPENSER_NAME = "dispenser_name";

	public static final String FIELD_LATERALITY = "laterality";
	public static final String FIELD_CLINICAL_HISTORY = "clinical_history";
	public static final String FIELD_INSTRUCTIONS = "instructions";
	public static final String FIELD_SPECIMEN_SOURCE_UUID = "specimen_source_uuid";
	public static final String FIELD_SPECIMEN_SOURCE_NAME = "specimen_source_name";

	public static final String FIELD_ALLERGEN_UUID = "allergen_uuid";
	public static final String FIELD_ALLERGEN_NAME = "allergen_name";
	public static final String FIELD_ALLERGEN_NON_CODED = "allergen_non_coded";
	public static final String FIELD_ALLERGEN_TYPE = "allergen_type";
	public static final String FIELD_SEVERITY = "severity";
	public static final String FIELD_REACTIONS = "reactions";

	public static final String FIELD_PROGRAM_UUID = "program_uuid";
	public static final String FIELD_PROGRAM_NAME = "program_name";
	public static final String FIELD_ENROLLMENT_DATE = "enrollment_date";
	public static final String FIELD_COMPLETION_DATE = "completion_date";
	public static final String FIELD_ACTIVE = "active";
	public static final String FIELD_OUTCOME_UUID = "outcome_uuid";
	public static final String FIELD_OUTCOME = "outcome";
	public static final String FIELD_CURRENT_STATE_UUID = "current_state_uuid";
	public static final String FIELD_CURRENT_STATE = "current_state";

	public static final String FIELD_ENCOUNTER_UUID = "encounter_uuid";
	public static final String FIELD_ENCOUNTER_TYPE_UUID = "encounter_type_uuid";
	public static final String FIELD_ENCOUNTER_TYPE_NAME = "encounter_type_name";
	public static final String FIELD_VISIT_UUID = "visit_uuid";
	public static final String FIELD_FORM_UUID = "form_uuid";
	public static final String FIELD_FORM_NAME = "form_name";
	public static final String FIELD_LOCATION_UUID = "location_uuid";
	public static final String FIELD_LOCATION_NAME = "location_name";
	public static final String FIELD_PROVIDER_UUID = "provider_uuid";
	public static final String FIELD_PROVIDER_NAME = "provider_name";
	public static final String FIELD_PROVIDERS = "providers";
	public static final String FIELD_ROLE_UUID = "role_uuid";
	public static final String FIELD_ROLE_NAME = "role_name";

	public static final String FIELD_VISIT_TYPE_UUID = "visit_type_uuid";
	public static final String FIELD_VISIT_TYPE_NAME = "visit_type_name";
	public static final String FIELD_START_DATE_TIME = "start_date_time";
	public static final String FIELD_END_DATE_TIME = "end_date_time";
	public static final String FIELD_INDICATION_UUID = "indication_uuid";
	public static final String FIELD_INDICATION_NAME = "indication_name";
	public static final String FIELD_ENCOUNTER_UUIDS = "encounter_uuids";
	public static final String FIELD_ATTRIBUTES = "attributes";
	public static final String FIELD_TYPE_UUID = "type_uuid";
	public static final String FIELD_TYPE_NAME = "type_name";
	public static final String FIELD_VALUE = "value";

	public static final String FIELD_GIVEN_NAME = "given_name";
	public static final String FIELD_MIDDLE_NAME = "middle_name";
	public static final String FIELD_FAMILY_NAME = "family_name";
	public static final String FIELD_GENDER = "gender";
	public static final String FIELD_BIRTHDATE = "birthdate";
	public static final String FIELD_BIRTHDATE_ESTIMATED = "birthdate_estimated";
	public static final String FIELD_AGE_YEARS = "age_years";
	public static final String FIELD_DEAD = "dead";
	public static final String FIELD_DEATH_DATE = "death_date";
	public static final String FIELD_CAUSE_OF_DEATH_UUID = "cause_of_death_uuid";
	public static final String FIELD_CAUSE_OF_DEATH_NAME = "cause_of_death_name";
	public static final String FIELD_IDENTIFIERS = "identifiers";
	public static final String FIELD_ADDRESSES = "addresses";
	public static final String FIELD_PREFERRED = "preferred";
	public static final String FIELD_ADDRESS1 = "address1";
	public static final String FIELD_CITY_VILLAGE = "city_village";
	public static final String FIELD_STATE_PROVINCE = "state_province";
	public static final String FIELD_POSTAL_CODE = "postal_code";
	public static final String FIELD_COUNTRY = "country";

	public static final String INDEX_OBS = INDEX_PREFIX + "obs";
	public static final String INDEX_CONDITION = INDEX_PREFIX + "condition";
	public static final String INDEX_DIAGNOSIS = INDEX_PREFIX + "diagnosis";
	public static final String INDEX_DRUG_ORDER = INDEX_PREFIX + "drug_order";
	public static final String INDEX_TEST_ORDER = INDEX_PREFIX + "test_order";
	public static final String INDEX_REFERRAL_ORDER = INDEX_PREFIX + "referral_order";
	public static final String INDEX_ALLERGY = INDEX_PREFIX + "allergy";
	public static final String INDEX_PROGRAM = INDEX_PREFIX + "program";
	public static final String INDEX_MEDICATION_DISPENSE = INDEX_PREFIX + "medication_dispense";
	public static final String INDEX_PATIENT = INDEX_PREFIX + "patient";
	public static final String INDEX_ENCOUNTER = INDEX_PREFIX + "encounter";
	public static final String INDEX_VISIT = INDEX_PREFIX + "visit";

	public static final String GP_BOOTSTRAP_AUTOSTART = "querystore.bootstrap.autostart";

	public static final String GP_ELASTICSEARCH_HOST = "querystore.elasticsearch.host";
	public static final String GP_ELASTICSEARCH_PORT = "querystore.elasticsearch.port";
	public static final String GP_ELASTICSEARCH_SCHEME = "querystore.elasticsearch.scheme";
	public static final String GP_EMBEDDING_PROVIDER_BEAN = "querystore.embedding.providerBean";
	public static final String GP_EMBEDDING_MODEL_FILE_PATH = "querystore.embedding.modelFilePath";
	public static final String GP_EMBEDDING_QUERY_MODEL_FILE_PATH = "querystore.embedding.queryModelFilePath";
	public static final String GP_EMBEDDING_VOCAB_FILE_PATH = "querystore.embedding.vocabFilePath";
	public static final String GP_EMBEDDING_MAX_SEQUENCE_LENGTH = "querystore.embedding.maxSequenceLength";

	public static final String DEFAULT_ELASTICSEARCH_HOST = "localhost";
	public static final String DEFAULT_ELASTICSEARCH_PORT = "9200";
	public static final String DEFAULT_ELASTICSEARCH_SCHEME = "http";
	public static final String DEFAULT_EMBEDDING_PROVIDER_BEAN = "querystore.embedding.onnx";
	public static final int DEFAULT_EMBEDDING_MAX_SEQUENCE_LENGTH = 512;

	private QueryStoreConstants() {
	}
}
