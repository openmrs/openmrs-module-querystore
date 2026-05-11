/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.eval;

import java.util.ArrayList;
import java.util.List;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.openmrs.module.querystore.api.impl.QueryStoreServiceImpl;
import org.openmrs.module.querystore.backend.mysql.MysqlBackendStore;
import org.openmrs.module.querystore.embedding.OnnxEmbeddingProvider;
import org.openmrs.module.querystore.model.QueryDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MySQLContainer;

/**
 * MySQL-tier retrieval-quality eval against a Testcontainers MySQL 8 cluster. Shared shape lives
 * in {@link AbstractRetrievalQualityEvalTest}; this class boots the container and wires the
 * backend. Third tier next to the Lucene and ES siblings — the migration doc anticipated MySQL
 * FULLTEXT-flavoured BM25 might drift from Lucene/ES scoring on the fused RRF result, so this
 * test gives that delta a real measurement instead of a hypothetical.
 *
 * <p>Run locally with:
 * <pre>
 * mvn -pl api -Pintegration -Dtest=MysqlRetrievalQualityEvalIntegrationTest \
 *   -Dquerystore.eval.modelDir=/abs/path/to/model-and-vocab test
 * </pre>
 */
public class MysqlRetrievalQualityEvalIntegrationTest extends AbstractRetrievalQualityEvalTest {

	private static final Logger log = LoggerFactory.getLogger(MysqlRetrievalQualityEvalIntegrationTest.class);

	private MySQLContainer<?> mysql;

	private MysqlBackendStore backend;

	@Override
	protected long perCaseLatencyBudgetMs() {
		// Brute-force cosine scan over the patient-filtered subset, ~100ms observed. 2000ms catches
		// pathological regressions without flaking on cold-cache first calls.
		return 2000L;
	}

	@BeforeAll
	public void setUpClass() throws Exception {
		if (!modelFilesExist()) {
			log.info("Skipping retrieval eval: set -Dquerystore.eval.modelDir to a directory "
			        + "containing a self-contained model.onnx and vocab.txt to run "
			        + "(current value: {})", MODEL_DIR);
			return;
		}
		mysql = new MySQLContainer<>("mysql:8.0").withDatabaseName("openmrs_test").withUsername("test")
		        .withPassword("test");
		mysql.start();

		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setURL(mysql.getJdbcUrl());
		dataSource.setUser(mysql.getUsername());
		dataSource.setPassword(mysql.getPassword());
		backend = new MysqlBackendStore(dataSource);

		provider = new OnnxEmbeddingProvider(MODEL_PATH, VOCAB_PATH);

		QueryStoreServiceImpl impl = new QueryStoreServiceImpl();
		impl.setBackend(backend);
		impl.setEmbeddingProvider(provider);
		service = impl;

		evalDataset = EvalDataset.load("eval/retrieval-eval-dataset.json");

		List<String> records = loadDataset();
		long start = System.currentTimeMillis();
		List<QueryDocument> docs = new ArrayList<>(records.size());
		for (int i = 0; i < records.size(); i++) {
			docs.add(toDocument(i + 1, records.get(i)));
		}
		backend.bulkUpsert(docs);
		log.info("Indexed {} records in {} ms", records.size(), System.currentTimeMillis() - start);
	}

	@AfterAll
	public void tearDownClass() {
		if (provider != null) {
			provider.close();
		}
		if (mysql != null) {
			mysql.stop();
		}
	}
}
