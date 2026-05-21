/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.bootstrap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Statement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.api.impl.QueryStoreServiceImpl;
import org.openmrs.module.querystore.backend.Filter;
import org.openmrs.module.querystore.backend.JdbcSupport;
import org.openmrs.module.querystore.backend.SchemaSpec;
import org.openmrs.module.querystore.backend.SearchRequest;
import org.openmrs.module.querystore.backend.SearchResult;
import org.openmrs.module.querystore.backend.mysql.MysqlBackendStore;
import org.openmrs.module.querystore.backend.mysql.TestSessionFactories;
import org.openmrs.module.querystore.embedding.EmbeddingProvider;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;
import org.testcontainers.containers.MySQLContainer;

/**
 * End-to-end bootstrap exercised against a real MySQL via Testcontainers. Validates the
 * progress-DAO round-trip, multi-page scans, resume from a persisted cursor, and the key
 * race the version-protection invariant was added for: a concurrent AOP-style fresher write
 * during bootstrap must survive a slower bootstrap-projected write of the same record.
 */
public class BootstrapIntegrationTest {

	private static final String PROGRESS_DDL = "CREATE TABLE IF NOT EXISTS querystore_bootstrap_progress ("
	        + "  resource_type VARCHAR(64) NOT NULL,"
	        + "  status VARCHAR(16) NOT NULL,"
	        + "  cursor_date_changed DATETIME(3) NULL,"
	        + "  cursor_uuid CHAR(38) NULL,"
	        + "  documents_indexed BIGINT NOT NULL DEFAULT 0,"
	        + "  started_at DATETIME(3) NULL,"
	        + "  completed_at DATETIME(3) NULL,"
	        + "  failure_message TEXT NULL,"
	        + "  backend VARCHAR(32) NULL,"
	        + "  PRIMARY KEY (resource_type)"
	        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

	private static MySQLContainer<?> mysql;

	private static DbSessionFactory sessionFactory;

	private static MysqlBackendStore backend;

	private static QueryStoreService queryStoreService;

	private static BootstrapProgressDao progressDao;

	@BeforeClass
	public static void startContainer() {
		mysql = new MySQLContainer<>("mysql:8.0").withDatabaseName("openmrs_test")
		        .withUsername("test").withPassword("test");
		mysql.start();

		sessionFactory = TestSessionFactories.forContainer(mysql);
		backend = new MysqlBackendStore(sessionFactory);
		QueryStoreServiceImpl svc = new QueryStoreServiceImpl();
		svc.setBackend(backend);
		// Service-layer embedding is unused on the write path here — bootstrap embeds before
		// service.index(); leaving the provider null keeps search BM25-only, which is fine.
		queryStoreService = svc;
		progressDao = new BootstrapProgressDao(sessionFactory);

		JdbcSupport.inTransaction(sessionFactory, conn -> {
			try (Statement stmt = conn.createStatement()) {
				stmt.executeUpdate(PROGRESS_DDL);
			}
		});
	}

	@AfterClass
	public static void stopContainer() {
		if (sessionFactory != null) {
			sessionFactory.getHibernateSessionFactory().close();
		}
		if (mysql != null) {
			mysql.stop();
		}
	}

	@Before
	public void resetState() {
		backend.deleteSchema("test");
		backend.ensureSchema("test", SchemaSpec.builder(8).build());
		JdbcSupport.inTransaction(sessionFactory, conn -> {
			try (Statement stmt = conn.createStatement()) {
				stmt.executeUpdate("DELETE FROM querystore_bootstrap_progress");
			}
		});
	}

	@Test
	public void progressDao_roundTripsAllFields() {
		BootstrapProgress p = new BootstrapProgress("test");
		p.setStatus(BootstrapStatus.RUNNING);
		Instant t = Instant.parse("2025-03-15T09:00:00Z");
		p.setCursorDateChanged(t);
		p.setCursorUuid("uuid-cursor");
		p.setDocumentsIndexed(123);
		p.setStartedAt(t);
		p.setBackend("lucene");

		progressDao.save(p);
		BootstrapProgress loaded = progressDao.find("test");

		assertNotNull(loaded);
		assertEquals(BootstrapStatus.RUNNING, loaded.getStatus());
		assertEquals(t, loaded.getCursorDateChanged());
		assertEquals("uuid-cursor", loaded.getCursorUuid());
		assertEquals(123, loaded.getDocumentsIndexed());
		assertEquals(t, loaded.getStartedAt());
		assertNull(loaded.getCompletedAt());
		assertEquals("lucene", loaded.getBackend());
	}

	@Test
	public void progressDao_saveActsAsUpsert() {
		progressDao.save(progress("test", BootstrapStatus.RUNNING, 1));
		progressDao.save(progress("test", BootstrapStatus.COMPLETED, 99));

		List<BootstrapProgress> all = progressDao.findAll();
		assertEquals(1, all.size());
		assertEquals(BootstrapStatus.COMPLETED, all.get(0).getStatus());
		assertEquals(99, all.get(0).getDocumentsIndexed());
	}

	@Test
	public void bootstrap_endToEnd_indexesAllPagesIntoBackend() {
		FakeBootstrapper b = new FakeBootstrapper();
		Instant t = Instant.parse("2025-03-15T09:00:00Z");
		b.queuePage(entity("e1", "Glucose 5.1", t),
		        entity("e2", "Hemoglobin 12.4", t.plus(1, ChronoUnit.MINUTES)));
		b.queuePage(entity("e3", "Pulse 72", t.plus(2, ChronoUnit.MINUTES)));

		BootstrapProgress progress = new BootstrapProgress("test");
		b.run(progress, queryStoreService, new ZeroEmbedder(), progressDao);

		assertEquals(BootstrapStatus.COMPLETED, progress.getStatus());
		assertEquals(3, progress.getDocumentsIndexed());

		// Verify each text is searchable from the backend.
		SearchResult res = backend.bm25(SearchRequest.builder().resourceType("test").queryText("Glucose")
		        .filter(Filter.patientScope("patient-A")).limit(10).build());
		assertEquals(1, res.getHits().size());
		assertEquals("e1", res.getHits().get(0).getDocument().getResourceUuid());

		// Progress is persisted post-completion.
		BootstrapProgress loaded = progressDao.find("test");
		assertEquals(BootstrapStatus.COMPLETED, loaded.getStatus());
		assertEquals(3, loaded.getDocumentsIndexed());
	}

	@Test
	public void bootstrap_concurrentFresherWriteDuringBootstrap_isPreservedByVersionGuard() {
		// Simulate the race: an AOP / event handler writes a fresher version of record "X" while
		// bootstrap is still in flight. The bootstrap-projected doc carries the entity's older
		// dateChanged, so the Decision 3 conditional-upsert guard must drop the bootstrap write.
		Instant t1 = Instant.parse("2025-03-15T09:00:00Z");
		Instant t2 = t1.plus(1, ChronoUnit.HOURS);

		QueryDocument fresh = doc("test", "X", "Glucose 8.1 (10:00 update)", t2);
		queryStoreService.index(fresh);

		FakeBootstrapper b = new FakeBootstrapper();
		b.queuePage(entity("X", "Glucose 5.4 (09:00 baseline)", t1));
		b.run(new BootstrapProgress("test"), queryStoreService, new ZeroEmbedder(), progressDao);

		SearchResult res = backend.bm25(SearchRequest.builder().resourceType("test").queryText("Glucose")
		        .filter(Filter.patientScope("patient-A")).limit(10).build());
		assertEquals(1, res.getHits().size());
		assertEquals("AOP fresher write must survive — bootstrap's stale projection is dropped",
		        "Glucose 8.1 (10:00 update)", res.getHits().get(0).getDocument().getText());
	}

	@Test
	public void bootstrap_resumesFromPersistedCursor() {
		Instant t1 = Instant.parse("2025-03-15T09:00:00Z");
		BootstrapProgress prior = new BootstrapProgress("test");
		prior.setCursorDateChanged(t1);
		prior.setCursorUuid("e1");
		prior.setDocumentsIndexed(1);
		prior.setStartedAt(t1);
		progressDao.save(prior);

		FakeBootstrapper b = new FakeBootstrapper();
		b.queuePage(entity("e2", "Pulse 72", t1.plus(1, ChronoUnit.MINUTES)));

		BootstrapProgress loaded = progressDao.find("test");
		b.run(loaded, queryStoreService, new ZeroEmbedder(), progressDao);

		// fetchPage was called with the persisted cursor on the first call.
		assertEquals(t1, b.fetchCalls.get(0).afterDateChanged);
		assertEquals("e1", b.fetchCalls.get(0).afterUuid);
		assertEquals("documentsIndexed accumulates across resume", 2, loaded.getDocumentsIndexed());
	}

	@Test
	public void bootstrappers_registeredByService_findEachOther() {
		BootstrapServiceImpl service = new BootstrapServiceImpl();
		service.setProgressDao(progressDao);
		service.setQueryStoreService(queryStoreService);
		service.setEmbeddingProvider(new ZeroEmbedder());
		// Production wires a BackendStoreSelector; tests install a constant supplier when backend
		// identity is incidental to the assertion.
		service.setBackendNameSupplierOverride(() -> "lucene");

		FakeBootstrapper b1 = new FakeBootstrapper();
		FakeBootstrapper b2 = new FakeBootstrapper() {
			@Override public String getResourceType() { return "test_two"; }
		};
		service.setBootstrappers(Arrays.asList(b1, b2));

		service.bootstrap();

		assertTrue("both registered bootstrappers were dispatched",
		        progressDao.find("test") != null && progressDao.find("test_two") != null);
	}

	// ---------- helpers ----------

	private static BootstrapProgress progress(String type, BootstrapStatus status, long count) {
		BootstrapProgress p = new BootstrapProgress(type);
		p.setStatus(status);
		p.setDocumentsIndexed(count);
		return p;
	}

	private static TestEntity entity(String uuid, String text, Instant ts) {
		return new TestEntity(uuid, text, ts);
	}

	private static QueryDocument doc(String type, String uuid, String text, Instant lastModified) {
		QueryDocument d = new QueryDocument();
		d.setResourceType(type);
		d.setResourceUuid(uuid);
		d.setPatientUuid("patient-A");
		d.setText(text);
		d.setLastModified(lastModified);
		return d;
	}

	private static final class TestEntity {
		final String uuid;
		final String text;
		final Instant ts;
		TestEntity(String uuid, String text, Instant ts) { this.uuid = uuid; this.text = text; this.ts = ts; }
	}

	private static final class TestSerializer implements ClinicalRecordSerializer<TestEntity> {
		@Override public String getResourceType() { return "test"; }
		@Override public Class<TestEntity> getSupportedType() { return TestEntity.class; }
		@Override
		public QueryDocument serialize(TestEntity record) {
			QueryDocument d = new QueryDocument();
			d.setResourceType("test");
			d.setResourceUuid(record.uuid);
			d.setPatientUuid("patient-A");
			d.setText(record.text);
			d.setLastModified(record.ts);
			return d;
		}
	}

	private static class FakeBootstrapper extends TypeBootstrapper<TestEntity> {
		private final Deque<List<TestEntity>> pages = new ArrayDeque<>();
		final List<FetchCall> fetchCalls = new ArrayList<>();
		private final TestSerializer serializer = new TestSerializer();

		void queuePage(TestEntity... entities) {
			List<TestEntity> page = new ArrayList<>();
			Collections.addAll(page, entities);
			pages.add(page);
		}

		@Override public String getResourceType() { return "test"; }
		@Override protected ClinicalRecordSerializer<TestEntity> getSerializer() { return serializer; }
		@Override protected Instant getDateChanged(TestEntity e) { return e.ts; }
		@Override protected String getUuid(TestEntity e) { return e.uuid; }

		@Override
		protected List<TestEntity> fetchPage(Instant afterDateChanged, String afterUuid, int pageSize) {
			fetchCalls.add(new FetchCall(afterDateChanged, afterUuid));
			return pages.isEmpty() ? Collections.emptyList() : pages.poll();
		}
	}

	private static final class FetchCall {
		final Instant afterDateChanged;
		final String afterUuid;
		FetchCall(Instant afterDateChanged, String afterUuid) {
			this.afterDateChanged = afterDateChanged;
			this.afterUuid = afterUuid;
		}
	}

	private static final class ZeroEmbedder implements EmbeddingProvider {
		@Override public int getDimensions() { return 8; }
		@Override public float[] embed(String text) { return new float[8]; }
	}

}
