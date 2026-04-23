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
