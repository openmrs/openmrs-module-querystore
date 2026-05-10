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

	public static final String INDEX_OBS = INDEX_PREFIX + "obs";
	public static final String INDEX_CONDITION = INDEX_PREFIX + "condition";
	public static final String INDEX_DIAGNOSIS = INDEX_PREFIX + "diagnosis";
	public static final String INDEX_DRUG_ORDER = INDEX_PREFIX + "drug_order";
	public static final String INDEX_TEST_ORDER = INDEX_PREFIX + "test_order";
	public static final String INDEX_ALLERGY = INDEX_PREFIX + "allergy";
	public static final String INDEX_PROGRAM = INDEX_PREFIX + "program";
	public static final String INDEX_MEDICATION_DISPENSE = INDEX_PREFIX + "medication_dispense";
	public static final String INDEX_PATIENT = INDEX_PREFIX + "patient";
	public static final String INDEX_ENCOUNTER = INDEX_PREFIX + "encounter";
	public static final String INDEX_VISIT = INDEX_PREFIX + "visit";

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
