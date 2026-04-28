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
	public static final String INDEX_CONDITIONS = INDEX_PREFIX + "conditions";
	public static final String INDEX_DIAGNOSES = INDEX_PREFIX + "diagnoses";
	public static final String INDEX_DRUG_ORDERS = INDEX_PREFIX + "drug_orders";
	public static final String INDEX_TEST_ORDERS = INDEX_PREFIX + "test_orders";
	public static final String INDEX_ALLERGIES = INDEX_PREFIX + "allergies";
	public static final String INDEX_PROGRAMS = INDEX_PREFIX + "programs";
	public static final String INDEX_MEDICATION_DISPENSE = INDEX_PREFIX + "medication_dispense";
	public static final String INDEX_PATIENTS = INDEX_PREFIX + "patients";
	public static final String INDEX_ENCOUNTERS = INDEX_PREFIX + "encounters";
	public static final String INDEX_VISITS = INDEX_PREFIX + "visits";

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
