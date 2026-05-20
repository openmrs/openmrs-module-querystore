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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.querystore.QueryStoreConstants;
import org.openmrs.module.querystore.api.impl.QueryStoreServiceImpl;
import org.openmrs.module.querystore.backend.SearchRequest;
import org.openmrs.module.querystore.backend.SearchResult;
import org.openmrs.module.querystore.backend.lucene.LuceneBackendStore;
import org.openmrs.module.querystore.embedding.EmbeddingProvider;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;
import org.openmrs.module.querystore.spi.ResourceTypeProvider;

/**
 * End-to-end verification of the {@link ResourceTypeProvider} SPI (ADR Decision 13). Walks the same
 * recipe {@code docs/spi-providers.md} hands a real module author: define a domain POJO, write a
 * serializer producing the cross-cutting fields, write a bootstrapper, wire a provider bean, and
 * verify that querystore picks it up.
 *
 * <p>Uses the embedded Lucene backend so the test runs in the default {@code mvn test} pass without
 * an external service. Assertions cover the four SPI-specific paths that core-type tests don't
 * exercise:
 * <ul>
 *   <li>{@link BootstrapServiceImpl} discovers the provider and runs its bootstrapper after core types.</li>
 *   <li>Documents land in the prefixed {@code querystore_<moduleid>_<type>} index, not the unprefixed core form.</li>
 *   <li>Cross-type search via the {@code querystore_*} wildcard returns the provider's documents alongside
 *       any core-type documents.</li>
 *   <li>Patient-scoped retrieval correctly filters provider documents by {@code patient_uuid}.</li>
 * </ul>
 *
 * <p>The AOP-trigger path is not re-tested here — it is identical to the 14 core advice classes in
 * the {@code bridge} package and is exercised by their existing tests.
 */
public class ProviderEndToEndTest {

	// Resource type the test contribution claims. Follows the Decision 13 convention
	// <moduleid>_<type>; the corresponding index is querystore_billing_bill.
	private static final String RESOURCE_TYPE = "billing_bill";

	private static final String INDEX_NAME = QueryStoreConstants.INDEX_PREFIX + RESOURCE_TYPE;

	private Path indexRoot;

	private LuceneBackendStore backend;

	private QueryStoreServiceImpl service;

	private BootstrapServiceImpl bootstrap;

	private InMemoryProgressDao progressDao;

	@Before
	public void setUp() throws IOException {
		indexRoot = Files.createTempDirectory("querystore-spi-endtoend-");
		backend = new LuceneBackendStore(indexRoot);

		service = new QueryStoreServiceImpl();
		service.setBackend(backend);
		// BM25-only at query time; SPI routing is the assertion, not kNN. ZeroEmbedder's all-zero
		// vectors break Lucene cosine kNN, so the service-layer embedder stays null.

		progressDao = new InMemoryProgressDao();
		bootstrap = new BootstrapServiceImpl();
		bootstrap.setProgressDao(progressDao);
		bootstrap.setQueryStoreService(service);
		bootstrap.setEmbeddingProvider(new ZeroEmbedder());
		bootstrap.setBootstrappers(Collections.<TypeBootstrapper<?>>emptyList());
	}

	@After
	public void tearDown() throws IOException {
		backend.close();
		deleteRecursive(indexRoot);
	}

	@Test
	public void bootstrapDiscoversProviderAndRunsItsBootstrapper() {
		installBills(
		        bill("bill-1", "patient-A", "Outpatient visit consultation fee", 50_00),
		        bill("bill-2", "patient-A", "Laboratory chemistry panel", 120_00),
		        bill("bill-3", "patient-B", "Inpatient ward bed daily charge", 250_00));

		bootstrap.bootstrap();

		BootstrapProgress progress = progressDao.find(RESOURCE_TYPE);
		assertEquals(BootstrapStatus.COMPLETED, progress.getStatus());
		assertEquals(3, progress.getDocumentsIndexed());
	}

	@Test
	public void providerDocumentsLandInPrefixedIndexNotCoreForm() throws IOException {
		installBills(bill("bill-1", "patient-A", "Outpatient consultation", 50_00));

		bootstrap.bootstrap();

		// Decision 4 mandates the querystore_<moduleid>_<type> form for module contributions; the
		// Lucene backend creates one directory per resource type under the index root.
		Path expectedDir = indexRoot.resolve(INDEX_NAME);
		assertTrue("expected Lucene directory '" + INDEX_NAME + "' to exist", Files.isDirectory(expectedDir));
		// And the unprefixed form must NOT exist — that would mean the provider's name was stripped
		// or routed to a core-type bucket.
		Path forbiddenDir = indexRoot.resolve(RESOURCE_TYPE);
		assertEquals("unprefixed directory must not be created", false, Files.isDirectory(forbiddenDir));
	}

	@Test
	public void crossTypeSearchReturnsProviderDocumentsAlongsideCoreTypes() {
		// Seed a core-type obs document directly via the backend so the cross-type query has both
		// a core hit and a provider hit to choose from.
		QueryDocument core = doc(QueryStoreConstants.INDEX_OBS.substring(QueryStoreConstants.INDEX_PREFIX.length()),
		        "patient-A", "Fasting glucose elevated", new float[8]);
		backend.upsert(core);

		installBills(
		        bill("bill-1", "patient-A", "Laboratory glucose test fee", 30_00),
		        bill("bill-2", "patient-B", "Outpatient consultation fee", 50_00));
		bootstrap.bootstrap();

		// Cross-type query — no resourceType filter → BackendStore searches all querystore_* stores.
		List<QueryDocument> hits = service.search("glucose", 10);

		// Both the core obs and the provider bill mention "glucose"; cross-type retrieval must
		// surface both.
		Map<String, Boolean> seenByType = new HashMap<>();
		for (QueryDocument h : hits) {
			seenByType.put(h.getResourceType(), Boolean.TRUE);
		}
		assertTrue("core obs hit present", seenByType.containsKey("obs"));
		assertTrue("provider " + RESOURCE_TYPE + " hit present", seenByType.containsKey(RESOURCE_TYPE));
	}

	@Test
	public void patientScopedSearchFiltersProviderDocuments() {
		installBills(
		        bill("bill-1", "patient-A", "Outpatient consultation", 50_00),
		        bill("bill-2", "patient-B", "Outpatient consultation", 50_00));
		bootstrap.bootstrap();

		List<QueryDocument> hits = service.searchByPatient("patient-A", "consultation", 10);

		assertEquals("patient scope must filter to patient-A only", 1, hits.size());
		assertEquals("patient-A", hits.get(0).getPatientUuid());
		assertEquals(RESOURCE_TYPE, hits.get(0).getResourceType());
	}

	@Test
	public void patientCascadeDeleteRemovesProviderDocuments() {
		installBills(
		        bill("bill-1", "patient-A", "Outpatient consultation", 50_00),
		        bill("bill-2", "patient-B", "Outpatient consultation", 50_00));
		bootstrap.bootstrap();

		// Backend-level patient cascade — used by the void/merge paths. Must iterate querystore_*
		// directories including the provider's prefixed one, not just core indices.
		backend.bulkDeleteByPatient("patient-A");

		SearchResult after = backend.bm25(SearchRequest.builder()
		        .resourceType(RESOURCE_TYPE).queryText("consultation").limit(10).build());
		assertEquals("only patient-B's document survives after patient-A cascade", 1, after.getHits().size());
		assertEquals("patient-B", after.getHits().get(0).getDocument().getPatientUuid());
	}

	// ---------- The recipe a real provider module would ship (see docs/spi-providers.md) ----------

	/** Domain entity. A real module would point this at its Hibernate-mapped {@code BaseOpenmrsData}
	 *  subclass; the test uses a plain POJO so no schema is needed. The AOP advice step from the
	 *  walkthrough is intentionally absent here — it's identical to the 14 core advice classes and
	 *  is exercised by their existing tests. */
	private static final class Bill {
		final String uuid;
		final String patientUuid;
		final String description;
		final int amountCents;
		final Instant lastModified;

		Bill(String uuid, String patientUuid, String description, int amountCents) {
			this.uuid = uuid;
			this.patientUuid = patientUuid;
			this.description = description;
			this.amountCents = amountCents;
			this.lastModified = Instant.parse("2026-05-01T00:00:00Z");
		}
	}

	/** Walkthrough Step 1 — serializer. Must populate the cross-cutting fields (Decision 6):
	 *  {@code patient_uuid}, {@code resource_type}, {@code resource_uuid}, {@code last_modified},
	 *  {@code text}. Type-specific fields go in metadata. */
	private static final class BillSerializer implements ClinicalRecordSerializer<Bill> {
		@Override
		public String getResourceType() {
			return RESOURCE_TYPE;
		}

		@Override
		public Class<Bill> getSupportedType() {
			return Bill.class;
		}

		@Override
		public QueryDocument serialize(Bill bill) {
			QueryDocument doc = new QueryDocument();
			doc.setResourceType(RESOURCE_TYPE);
			doc.setResourceUuid(bill.uuid);
			doc.setPatientUuid(bill.patientUuid);
			doc.setLastModified(bill.lastModified);
			doc.setDate(bill.lastModified.atZone(java.time.ZoneOffset.UTC).toLocalDate());
			doc.setText("Bill: " + bill.description + ". Amount: "
			        + (bill.amountCents / 100.0) + " USD.");
			doc.putMetadata("amount_cents", bill.amountCents);
			return doc;
		}
	}

	/** Walkthrough Step 2 — bootstrapper. A real module would extend
	 *  {@code HibernateTypeBootstrapper<Bill>} and let its base class build the HQL; the test
	 *  uses a pre-built corpus to keep the assertion focused on the SPI dispatch, not Hibernate. */
	private static final class BillBootstrapper extends TypeBootstrapper<Bill> {
		private final List<Bill> corpus;
		private final BillSerializer serializer = new BillSerializer();

		BillBootstrapper(List<Bill> corpus) {
			this.corpus = new ArrayList<>(corpus);
		}

		@Override
		protected ClinicalRecordSerializer<Bill> getSerializer() {
			return serializer;
		}

		@Override
		protected List<Bill> fetchPage(Instant afterDateChanged, String afterUuid, int pageSize) {
			// Single-page fixture; pagination not under test.
			return afterDateChanged == null ? corpus : Collections.<Bill>emptyList();
		}

		@Override
		protected Instant getDateChanged(Bill bill) {
			return bill.lastModified;
		}

		@Override
		protected String getUuid(Bill bill) {
			return bill.uuid;
		}
	}

	/** Walkthrough Step 4 — provider bean. Returns the resource type name, the serializer, and the
	 *  optional bootstrapper. A real module declares this as a Spring bean in its own
	 *  {@code moduleApplicationContext.xml}. */
	private static final class BillProvider implements ResourceTypeProvider {
		private final BillSerializer serializer = new BillSerializer();
		private final BillBootstrapper bootstrapper;

		BillProvider(List<Bill> corpus) {
			this.bootstrapper = new BillBootstrapper(corpus);
		}

		@Override
		public String getResourceType() {
			return RESOURCE_TYPE;
		}

		@Override
		public ClinicalRecordSerializer<?> getSerializer() {
			return serializer;
		}

		@Override
		public TypeBootstrapper<?> getBootstrapper() {
			return bootstrapper;
		}
	}

	// ---------- test helpers ----------

	/** Registers a single {@link BillProvider} with the given corpus as the discovery override. */
	private void installBills(Bill... corpus) {
		bootstrap.setProvidersOverride(Collections.<ResourceTypeProvider>singletonList(
		        new BillProvider(Arrays.asList(corpus))));
	}

	private static Bill bill(String uuid, String patientUuid, String description, int amountCents) {
		return new Bill(uuid, patientUuid, description, amountCents);
	}

	private static QueryDocument doc(String resourceType, String patientUuid, String text, float[] embedding) {
		QueryDocument d = new QueryDocument();
		d.setResourceType(resourceType);
		d.setResourceUuid(UUID.randomUUID().toString());
		d.setPatientUuid(patientUuid);
		d.setDate(LocalDate.now());
		d.setLastModified(Instant.now());
		d.setText(text);
		d.setEmbedding(embedding);
		return d;
	}

	private static final class ZeroEmbedder implements EmbeddingProvider {
		@Override public int getDimensions() { return 8; }
		@Override public float[] embed(String text) { return new float[8]; }
	}

	private static final class InMemoryProgressDao extends BootstrapProgressDao {
		final Map<String, BootstrapProgress> store = new HashMap<>();

		InMemoryProgressDao() { super(null); }

		@Override public BootstrapProgress find(String resourceType) { return store.get(resourceType); }

		@Override
		public List<BootstrapProgress> findAll() { return new ArrayList<>(store.values()); }

		@Override public void save(BootstrapProgress p) { store.put(p.getResourceType(), p); }
	}

	private static void deleteRecursive(Path dir) throws IOException {
		if (!Files.exists(dir)) {
			return;
		}
		try (DirectoryStream<Path> children = Files.newDirectoryStream(dir)) {
			for (Path child : children) {
				if (Files.isDirectory(child)) {
					deleteRecursive(child);
				} else {
					Files.deleteIfExists(child);
				}
			}
		}
		Files.deleteIfExists(dir);
	}
}
