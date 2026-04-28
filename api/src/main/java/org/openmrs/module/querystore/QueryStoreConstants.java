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
	public static final String GP_EMBEDDING_MODEL = "querystore.embedding.model";
	public static final String GP_EMBEDDING_DIMENSIONS = "querystore.embedding.dimensions";

	public static final String DEFAULT_ELASTICSEARCH_HOST = "localhost";
	public static final String DEFAULT_ELASTICSEARCH_PORT = "9200";
	public static final String DEFAULT_ELASTICSEARCH_SCHEME = "http";
	public static final String DEFAULT_EMBEDDING_MODEL = "multilingual-e5";
	public static final int DEFAULT_EMBEDDING_DIMENSIONS = 768;

	private QueryStoreConstants() {
	}
}
