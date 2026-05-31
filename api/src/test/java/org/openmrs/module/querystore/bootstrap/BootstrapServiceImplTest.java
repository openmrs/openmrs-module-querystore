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
import static org.junit.Assert.fail;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.embedding.EmbeddingProvider;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;
import org.openmrs.module.querystore.spi.ResourceTypeProvider;

public class BootstrapServiceImplTest {

	private BootstrapServiceImpl service;

	private InMemoryDao dao;

	@Before
	public void setUp() {
		service = new BootstrapServiceImpl();
		dao = new InMemoryDao();
		service.setProgressDao(dao);
		service.setQueryStoreService(new NullQueryStoreService());
		service.setEmbeddingProvider(new ZeroEmbedder());
		// Tests run without a live OpenMRS Spring context; pinning an empty provider list keeps
		// discovery off the Context.getRegisteredComponents path. Tests exercising providers
		// override this with their own list.
		service.setProvidersOverride(Collections.<ResourceTypeProvider>emptyList());
		// Match the providersOverride discipline for the backend-identity lookup: production wires a
		// BackendStoreSelector; tests must install a supplier (or a real selector) so the dispatcher
		// can compare stored vs current. A constant default is fine when the test doesn't care about
		// backend semantics — tests exercising the mismatch path override this with their own value.
		service.setBackendNameSupplierOverride(() -> "test-backend");
	}

	@Test
	public void bootstrap_iteratesEveryRegisteredType() {
		EmptyPageBootstrapper obs = new EmptyPageBootstrapper("obs");
		EmptyPageBootstrapper enc = new EmptyPageBootstrapper("encounter");
		service.setBootstrappers(Arrays.asList(obs, enc));

		service.bootstrap();

		assertEquals(1, obs.fetchCount);
		assertEquals(1, enc.fetchCount);
		assertEquals(BootstrapStatus.COMPLETED, dao.find("obs").getStatus());
		assertEquals(BootstrapStatus.COMPLETED, dao.find("encounter").getStatus());
	}

	@Test
	public void bootstrap_typeSpecific_runsOnlyMatchingBootstrapper() {
		EmptyPageBootstrapper obs = new EmptyPageBootstrapper("obs");
		EmptyPageBootstrapper enc = new EmptyPageBootstrapper("encounter");
		service.setBootstrappers(Arrays.asList(obs, enc));

		service.bootstrap("encounter");

		assertEquals(0, obs.fetchCount);
		assertEquals(1, enc.fetchCount);
	}

	@Test
	public void bootstrap_unknownType_throws() {
		service.setBootstrappers(Collections.singletonList(new EmptyPageBootstrapper("obs")));
		try {
			service.bootstrap("nope");
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage().contains("nope"));
		}
	}

	@Test
	public void bootstrap_perTypeFailureDoesNotAbortRemaining() {
		EmptyPageBootstrapper failing = new EmptyPageBootstrapper("obs");
		failing.failure = new RuntimeException("boom");
		EmptyPageBootstrapper ok = new EmptyPageBootstrapper("encounter");
		service.setBootstrappers(Arrays.asList(failing, ok));

		service.bootstrap();

		assertEquals("failing bootstrapper was attempted", 1, failing.fetchCount);
		assertEquals("non-failing bootstrapper still ran after the failure", 1, ok.fetchCount);
		assertEquals(BootstrapStatus.FAILED, dao.find("obs").getStatus());
		assertEquals(BootstrapStatus.COMPLETED, dao.find("encounter").getStatus());
	}

	@Test
	public void bootstrap_resumesFromPersistedProgress() {
		BootstrapProgress prior = new BootstrapProgress("obs");
		prior.setCursorUuid("z");
		prior.setCursorDateChanged(Instant.parse("2025-03-15T09:00:00Z"));
		prior.setDocumentsIndexed(42);
		prior.setBackend("lucene");
		dao.store.put("obs", prior);
		service.setBackendNameSupplierOverride(() -> "lucene");
		EmptyPageBootstrapper obs = new EmptyPageBootstrapper("obs");
		service.setBootstrappers(Collections.singletonList(obs));

		service.bootstrap("obs");

		assertEquals("the persisted cursor uuid was passed into the first fetchPage call",
		        "z", obs.lastAfterUuid);
		assertEquals("the persisted cursor timestamp was passed into the first fetchPage call",
		        Instant.parse("2025-03-15T09:00:00Z"), obs.lastAfterDateChanged);
		assertEquals("documents_indexed preserved across resume",
		        42, dao.find("obs").getDocumentsIndexed());
	}

	@Test
	public void bootstrap_resetsCursorWhenBackendChanges() {
		// A querystore.backend GP flip leaves the new backend's storage empty but inherits the old
		// backend's cursor. Without a reset, the loop reports "completed" against an empty index —
		// the silent-data-loss class of bug. The dispatcher must reset the row on mismatch.
		BootstrapProgress prior = new BootstrapProgress("obs");
		prior.setStatus(BootstrapStatus.COMPLETED);
		prior.setCursorUuid("z");
		prior.setCursorDateChanged(Instant.parse("2025-03-15T09:00:00Z"));
		prior.setDocumentsIndexed(42);
		prior.setBackend("elasticsearch");
		dao.store.put("obs", prior);
		service.setBackendNameSupplierOverride(() -> "lucene");
		EmptyPageBootstrapper obs = new EmptyPageBootstrapper("obs");
		service.setBootstrappers(Collections.singletonList(obs));

		service.bootstrap("obs");

		assertNull("cursor uuid cleared so the new backend re-scans from the start", obs.lastAfterUuid);
		assertNull("cursor timestamp cleared so the new backend re-scans from the start",
		        obs.lastAfterDateChanged);
		BootstrapProgress reloaded = dao.find("obs");
		assertEquals("documents_indexed reset on backend change", 0, reloaded.getDocumentsIndexed());
		assertEquals("backend identity updated to the current one", "lucene", reloaded.getBackend());
	}

	@Test
	public void bootstrap_resetsCursorWhenStoredBackendIsNull() {
		// Rows written before the backend column existed carry null. Treat them as mismatched so
		// one re-bootstrap follows the schema migration; otherwise we'd inherit a cursor that the
		// silent-no-op bug may have advanced past records that were never persisted.
		BootstrapProgress prior = new BootstrapProgress("obs");
		prior.setStatus(BootstrapStatus.COMPLETED);
		prior.setCursorUuid("z");
		prior.setCursorDateChanged(Instant.parse("2025-03-15T09:00:00Z"));
		prior.setDocumentsIndexed(42);
		// backend is null — simulates a row migrated from changeset -1.
		dao.store.put("obs", prior);
		service.setBackendNameSupplierOverride(() -> "lucene");
		EmptyPageBootstrapper obs = new EmptyPageBootstrapper("obs");
		service.setBootstrappers(Collections.singletonList(obs));

		service.bootstrap("obs");

		assertNull(obs.lastAfterUuid);
		assertNull(obs.lastAfterDateChanged);
		assertEquals("lucene", dao.find("obs").getBackend());
	}

	@Test
	public void bootstrap_failsFastWhenBackendIdentityNotWired() {
		// Production must always wire BackendStoreSelector; a silently-null answer would let one
		// dispatch stamp the row with null and the next properly-wired dispatch would reset
		// spuriously. Tests that don't care about backend identity install a constant supplier
		// (see setUp). Detecting the missing wiring loudly here surfaces the gap in the activator's
		// daemon thread instead of corrupting progress rows.
		BootstrapServiceImpl bare = new BootstrapServiceImpl();
		bare.setProgressDao(new InMemoryDao());
		bare.setQueryStoreService(new NullQueryStoreService());
		bare.setEmbeddingProvider(new ZeroEmbedder());
		bare.setProvidersOverride(Collections.<ResourceTypeProvider>emptyList());
		bare.setBootstrappers(Collections.singletonList(new EmptyPageBootstrapper("obs")));

		try {
			bare.bootstrap("obs");
			fail("expected IllegalStateException because no BackendStoreSelector or supplier override was installed");
		}
		catch (IllegalStateException expected) {
			assertTrue("error message should point to the wiring options",
			        expected.getMessage().contains("BackendStoreSelector")
			                && expected.getMessage().contains("setBackendNameSupplierOverride"));
		}
	}

	@Test
	public void bootstrap_stampsBackendOnFreshRow() {
		// A first-ever run for this resource type creates the row from scratch; the backend
		// identity must land on it so a subsequent GP flip is detectable.
		service.setBackendNameSupplierOverride(() -> "lucene");
		EmptyPageBootstrapper obs = new EmptyPageBootstrapper("obs");
		service.setBootstrappers(Collections.singletonList(obs));

		service.bootstrap("obs");

		assertEquals("lucene", dao.find("obs").getBackend());
	}

	@Test
	public void getStatus_byType_delegatesToDao() {
		BootstrapProgress p = new BootstrapProgress("obs");
		p.setDocumentsIndexed(7);
		dao.store.put("obs", p);

		BootstrapProgress out = service.getStatus("obs");
		assertEquals(7, out.getDocumentsIndexed());
	}

	@Test
	public void getStatus_all_delegatesToDao() {
		dao.store.put("obs", new BootstrapProgress("obs"));
		dao.store.put("encounter", new BootstrapProgress("encounter"));

		List<BootstrapProgress> all = service.getStatus();
		assertEquals(2, all.size());
	}

	@Test
	public void bootstrap_concurrentCallsForSameType_serialize() throws InterruptedException {
		// Two callers racing on bootstrap("test") must serialize on a per-type lock. Otherwise both
		// share the same progress row and trample each other's cursor/count writes. Documents are
		// version-protected by Slice A; bookkeeping needs this lock.
		CountDownLatch t1EnteredFetch = new CountDownLatch(1);
		CountDownLatch t1MayProceed = new CountDownLatch(1);
		AtomicInteger fetchCount = new AtomicInteger();

		BlockingBootstrapper b = new BlockingBootstrapper(t1EnteredFetch, t1MayProceed, fetchCount, false);
		service.setBootstrappers(Collections.singletonList(b));

		Thread t1 = new Thread(() -> service.bootstrap("test"));
		t1.start();
		assertTrue("first thread reached fetchPage", t1EnteredFetch.await(2, TimeUnit.SECONDS));

		Thread t2 = new Thread(() -> service.bootstrap("test"));
		t2.start();
		// Briefly: t2 has tried to acquire the lock; verify it's blocked, not running concurrently.
		Thread.sleep(150);
		assertEquals("second thread is blocked on the type lock; only one fetch in flight",
		        1, fetchCount.get());

		t1MayProceed.countDown();
		t1.join(2000);
		t2.join(2000);
		assertEquals("both callers completed; second ran sequentially after first released the lock",
		        2, fetchCount.get());
	}

	@Test
	public void registeredBootstrappersExposedReadOnly() {
		EmptyPageBootstrapper obs = new EmptyPageBootstrapper("obs");
		service.setBootstrappers(Collections.singletonList(obs));
		assertNotNull(service.getBootstrappers().get("obs"));
	}

	@Test
	public void bootstrap_runsProviderBootstrappersAfterCoreTypes() {
		EmptyPageBootstrapper core = new EmptyPageBootstrapper("obs");
		service.setBootstrappers(Collections.singletonList(core));
		EmptyPageBootstrapper providerBs = new EmptyPageBootstrapper("appointments_appointment");
		service.setProvidersOverride(Collections.<ResourceTypeProvider>singletonList(
		        new TestProvider("appointments_appointment", providerBs)));

		service.bootstrap();

		assertEquals(1, core.fetchCount);
		assertEquals("provider's bootstrapper ran", 1, providerBs.fetchCount);
		assertEquals(BootstrapStatus.COMPLETED, dao.find("appointments_appointment").getStatus());
	}

	@Test
	public void bootstrap_typeSpecific_resolvesProviderByName() {
		EmptyPageBootstrapper providerBs = new EmptyPageBootstrapper("billing_bill");
		service.setBootstrappers(Collections.<TypeBootstrapper<?>>emptyList());
		service.setProvidersOverride(Collections.<ResourceTypeProvider>singletonList(
		        new TestProvider("billing_bill", providerBs)));

		service.bootstrap("billing_bill");

		assertEquals(1, providerBs.fetchCount);
		assertEquals(BootstrapStatus.COMPLETED, dao.find("billing_bill").getStatus());
	}

	@Test
	public void bootstrap_skipsProviderWithNullBootstrapper() {
		// A provider with no historical backfill (e.g., type that didn't exist before module install)
		// is metadata-only — the all-types iteration must skip it instead of throwing.
		EmptyPageBootstrapper core = new EmptyPageBootstrapper("obs");
		service.setBootstrappers(Collections.singletonList(core));
		service.setProvidersOverride(Collections.<ResourceTypeProvider>singletonList(
		        new TestProvider("appointments_appointment", null)));

		service.bootstrap();

		assertEquals(1, core.fetchCount);
		// no progress row created for the no-backfill provider — bootstrap never ran for it
		assertEquals(null, dao.find("appointments_appointment"));
	}

	@Test
	public void bootstrap_typeSpecific_providerWithNullBootstrapper_throws() {
		// Asking explicitly to bootstrap a provider that declared no bootstrapper should surface
		// the no-op with a message distinct from "no such type" so the admin sees that the provider
		// exists but has nothing to backfill.
		service.setBootstrappers(Collections.<TypeBootstrapper<?>>emptyList());
		service.setProvidersOverride(Collections.<ResourceTypeProvider>singletonList(
		        new TestProvider("appointments_appointment", null)));

		try {
			service.bootstrap("appointments_appointment");
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException expected) {
			assertTrue("message must name the type", expected.getMessage().contains("appointments_appointment"));
			assertTrue("message must distinguish from 'unknown type'",
			        expected.getMessage().contains("no historical backfill")
			                || expected.getMessage().contains("declared no bootstrapper"));
		}
	}

	@Test
	public void bootstrap_skipsProviderWithInvalidName() {
		EmptyPageBootstrapper bad = new EmptyPageBootstrapper("UnprefixedAndCamel");
		EmptyPageBootstrapper ok = new EmptyPageBootstrapper("appointments_appointment");
		service.setProvidersOverride(Arrays.<ResourceTypeProvider>asList(
		        new TestProvider("UnprefixedAndCamel", bad),
		        new TestProvider("appointments_appointment", ok)));

		service.bootstrap();

		assertEquals("bad-name provider was skipped at discovery", 0, bad.fetchCount);
		assertEquals("no progress row created for dropped provider", null, dao.find("UnprefixedAndCamel"));
		assertEquals("well-formed sibling still ran", 1, ok.fetchCount);
	}

	@Test
	public void bootstrap_runsCoreTypesBeforeProviders() {
		// ADR Decision 13 promises providers run after core types (so a small provider isn't held
		// up by core obs only when the provider corpus is also small — the common case).
		final List<String> order = new java.util.ArrayList<>();
		EmptyPageBootstrapper obs = new EmptyPageBootstrapper("obs");
		obs.onFetch = order::add;
		EmptyPageBootstrapper enc = new EmptyPageBootstrapper("encounter");
		enc.onFetch = order::add;
		EmptyPageBootstrapper provider = new EmptyPageBootstrapper("appointments_appointment");
		provider.onFetch = order::add;
		service.setBootstrappers(Arrays.<TypeBootstrapper<?>>asList(obs, enc));
		service.setProvidersOverride(Collections.<ResourceTypeProvider>singletonList(
		        new TestProvider("appointments_appointment", provider)));

		service.bootstrap();

		assertEquals(Arrays.asList("obs", "encounter", "appointments_appointment"), order);
	}

	@Test
	public void bootstrap_isolatesProviderWhoseAccessorThrows() {
		// A malformed provider bean (e.g., getResourceType returning null or throwing) must not
		// abort discovery for well-formed peers — same per-type isolation as the bootstrap loop.
		ResourceTypeProvider broken = new ResourceTypeProvider() {
			@Override public String getResourceType() { throw new IllegalStateException("misconfigured"); }
			@Override public ClinicalRecordSerializer<?> getSerializer() { return null; }
			@Override public TypeBootstrapper<?> getBootstrapper() { return null; }
		};
		EmptyPageBootstrapper ok = new EmptyPageBootstrapper("appointments_appointment");
		service.setProvidersOverride(Arrays.asList(
		        broken, new TestProvider("appointments_appointment", ok)));

		service.bootstrap();

		assertEquals("well-formed sibling still ran", 1, ok.fetchCount);
	}

	@Test
	public void bootstrap_skipsProviderWithCoreReservedName() {
		// A provider claiming a core type name (e.g., 'obs') would corrupt routing if accepted.
		// Discovery drops it; the unrelated sibling still runs.
		EmptyPageBootstrapper colliding = new EmptyPageBootstrapper("obs");
		EmptyPageBootstrapper ok = new EmptyPageBootstrapper("appointments_appointment");
		service.setProvidersOverride(Arrays.<ResourceTypeProvider>asList(
		        new TestProvider("obs", colliding),
		        new TestProvider("appointments_appointment", ok)));

		service.bootstrap();

		assertEquals("core-reserved-name provider was skipped at discovery", 0, colliding.fetchCount);
		assertEquals("no progress row created for dropped provider", null, dao.find("obs"));
		assertEquals("well-formed sibling still ran", 1, ok.fetchCount);
	}

	@Test
	public void bootstrap_dropsDuplicateProviderForSameType() {
		// Two providers claiming the same resource type would silently overwrite each other in the
		// dispatch map. Discovery drops the duplicate (keeping the first); the first still runs.
		EmptyPageBootstrapper first = new EmptyPageBootstrapper("appointments_appointment");
		EmptyPageBootstrapper duplicate = new EmptyPageBootstrapper("appointments_appointment");
		service.setProvidersOverride(Arrays.<ResourceTypeProvider>asList(
		        new TestProvider("appointments_appointment", first),
		        new TestProvider("appointments_appointment", duplicate)));

		service.bootstrap();

		assertEquals("first provider ran", 1, first.fetchCount);
		assertEquals("duplicate provider was dropped", 0, duplicate.fetchCount);
	}

	@Test
	public void bootstrap_skipsProviderWithSerializerNameDrift() {
		// Provider claims one resource type but its serializer reports another — bootstrap path and
		// bridge path would route to different stores. Discovery rejects it.
		ClinicalRecordSerializer<Object> driftedSerializer = new ClinicalRecordSerializer<Object>() {
			@Override public String getResourceType() { return "appointments_appointment"; }
			@Override public Class<Object> getSupportedType() { return Object.class; }
			@Override public org.openmrs.module.querystore.model.QueryDocument serialize(Object r) { return null; }
		};
		EmptyPageBootstrapper bad = new EmptyPageBootstrapper("billing_bill");
		service.setProvidersOverride(Collections.<ResourceTypeProvider>singletonList(
		        new ResourceTypeProvider() {
			        @Override public String getResourceType() { return "billing_bill"; }
			        @Override public ClinicalRecordSerializer<?> getSerializer() { return driftedSerializer; }
			        @Override public TypeBootstrapper<?> getBootstrapper() { return bad; }
		        }));

		service.bootstrap();

		assertEquals("drifted-name provider was skipped at discovery", 0, bad.fetchCount);
		assertEquals("no progress row created for dropped provider", null, dao.find("billing_bill"));
	}

	@Test
	public void ensureIndexed_iteratesEveryRegisteredType() {
		EmptyPageBootstrapper obs = new EmptyPageBootstrapper("obs");
		EmptyPageBootstrapper enc = new EmptyPageBootstrapper("encounter");
		service.setBootstrappers(Arrays.asList(obs, enc));

		service.ensureIndexed("patient-uuid");

		assertEquals(1, obs.fetchForPatientCount);
		assertEquals("patient-uuid", obs.lastPatientUuid);
		assertEquals(1, enc.fetchForPatientCount);
		assertEquals("patient-uuid", enc.lastPatientUuid);
		assertEquals("global cursor untouched — per-patient path persists no progress",
		        null, dao.find("obs"));
	}

	@Test
	public void ensureIndexed_perTypeFailureIsolated() {
		EmptyPageBootstrapper failing = new EmptyPageBootstrapper("obs");
		failing.perPatientFailure = new RuntimeException("boom");
		EmptyPageBootstrapper ok = new EmptyPageBootstrapper("encounter");
		service.setBootstrappers(Arrays.asList(failing, ok));

		service.ensureIndexed("patient-uuid");

		assertEquals(1, failing.fetchForPatientCount);
		assertEquals("non-failing type still ran after the failure",
		        1, ok.fetchForPatientCount);
	}

	@Test
	public void ensureIndexed_skipsBootstrapperWithoutPerPatientImpl() {
		// A bootstrapper that doesn't override fetchPageForPatient throws UnsupportedOperationException
		// — the per-patient loop must treat it as a per-type skip (logged), not abort the whole call.
		TypeBootstrapper<Object> noPerPatient = new TypeBootstrapper<Object>() {
			@Override public String getResourceType() { return "no_per_patient"; }
			@Override protected ClinicalRecordSerializer<Object> getSerializer() { return null; }
			@Override protected List<Object> fetchPage(Instant a, String u, int p) {
				return Collections.emptyList();
			}
		};
		EmptyPageBootstrapper ok = new EmptyPageBootstrapper("encounter");
		service.setBootstrappers(Arrays.<TypeBootstrapper<?>>asList(noPerPatient, ok));

		service.ensureIndexed("patient-uuid");

		assertEquals("well-formed sibling still ran", 1, ok.fetchForPatientCount);
	}

	@Test
	public void ensureIndexed_nullPatientUuid_isNoOp() {
		EmptyPageBootstrapper obs = new EmptyPageBootstrapper("obs");
		service.setBootstrappers(Collections.singletonList(obs));

		service.ensureIndexed(null);
		service.ensureIndexed("");

		assertEquals("null/empty patient uuid is a no-op", 0, obs.fetchForPatientCount);
	}

	@Test
	public void ensureIndexed_includesProviderBootstrappers() {
		EmptyPageBootstrapper core = new EmptyPageBootstrapper("obs");
		EmptyPageBootstrapper providerBs = new EmptyPageBootstrapper("appointments_appointment");
		service.setBootstrappers(Collections.singletonList(core));
		service.setProvidersOverride(Collections.<ResourceTypeProvider>singletonList(
		        new TestProvider("appointments_appointment", providerBs)));

		service.ensureIndexed("patient-uuid");

		assertEquals(1, core.fetchForPatientCount);
		assertEquals("provider's bootstrapper got the per-patient call too",
		        1, providerBs.fetchForPatientCount);
	}

	@Test
	public void ensureIndexed_concurrentCallsForSamePatient_serialize() throws InterruptedException {
		// Mirrors bootstrap_concurrentCallsForSameType_serialize. The cross-JVM race is absorbed by
		// the Decision 3 version invariant; this lock just prevents redundant projection work in one JVM.
		CountDownLatch t1EnteredFetch = new CountDownLatch(1);
		CountDownLatch t1MayProceed = new CountDownLatch(1);
		AtomicInteger fetchCount = new AtomicInteger();

		BlockingBootstrapper b = new BlockingBootstrapper(t1EnteredFetch, t1MayProceed, fetchCount, true);
		service.setBootstrappers(Collections.singletonList(b));

		Thread t1 = new Thread(() -> service.ensureIndexed("patient-uuid"));
		t1.start();
		assertTrue("first thread reached fetchPageForPatient", t1EnteredFetch.await(2, TimeUnit.SECONDS));

		Thread t2 = new Thread(() -> service.ensureIndexed("patient-uuid"));
		t2.start();
		Thread.sleep(150);
		assertEquals("second thread is blocked on the per-patient lock; only one fetch in flight",
		        1, fetchCount.get());

		t1MayProceed.countDown();
		t1.join(2000);
		t2.join(2000);
		assertEquals("both callers completed; second ran sequentially after first released the lock",
		        2, fetchCount.get());
	}

	@Test
	public void ensureIndexed_concurrentCallsForDifferentPatients_runInParallel() throws InterruptedException {
		// Different patient UUIDs hit different lock entries, so the calls do not serialize.
		CountDownLatch bothEnteredFetch = new CountDownLatch(2);
		CountDownLatch mayProceed = new CountDownLatch(1);

		EmptyPageBootstrapper b = new EmptyPageBootstrapper("obs") {
			@Override
			protected List<Object> fetchPageForPatient(String patientUuid, Instant a, String u, int p) {
				bothEnteredFetch.countDown();
				try {
					if (!mayProceed.await(2, TimeUnit.SECONDS)) {
						fail("test setup deadlock: mayProceed never released");
					}
				}
				catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
				return Collections.emptyList();
			}
		};
		service.setBootstrappers(Collections.singletonList(b));

		Thread t1 = new Thread(() -> service.ensureIndexed("patient-A"));
		Thread t2 = new Thread(() -> service.ensureIndexed("patient-B"));
		t1.start();
		t2.start();

		assertTrue("both threads entered fetchPageForPatient concurrently (different patient locks)",
		        bothEnteredFetch.await(2, TimeUnit.SECONDS));

		mayProceed.countDown();
		t1.join(2000);
		t2.join(2000);
	}

	@Test
	public void reindexPatient_deletesExistingDocsThenReprojectsEveryType() {
		List<String> events = new java.util.ArrayList<String>();
		RecordingQueryStoreService qs = new RecordingQueryStoreService(events);
		service.setQueryStoreService(qs);
		EmptyPageBootstrapper obs = new EmptyPageBootstrapper("obs");
		obs.onFetch = t -> events.add("project:" + t);
		EmptyPageBootstrapper enc = new EmptyPageBootstrapper("encounter");
		enc.onFetch = t -> events.add("project:" + t);
		service.setBootstrappers(Arrays.asList(obs, enc));

		service.reindexPatient("patient-uuid");

		assertEquals("patient's existing docs must be deleted exactly once", 1, qs.bulkDeleteCount);
		assertEquals("patient-uuid", qs.lastDeletedPatient);
		assertEquals("every registered type must be re-projected", 1, obs.fetchForPatientCount);
		assertEquals(1, enc.fetchForPatientCount);
		assertEquals("patient-uuid", obs.lastPatientUuid);
		// Order invariant — load-bearing: delete MUST precede projection. Projecting first and then
		// deleting would re-add the patient's records and then wipe them, leaving the patient EMPTY
		// (silent data loss). Pin delete as the first recorded event.
		assertEquals("delete must happen before any projection", "delete", events.get(0));
		assertTrue("both types projected after the delete",
		        events.contains("project:obs") && events.contains("project:encounter"));
	}

	@Test
	public void reindexPatient_nullOrBlankUuid_isNoOp() {
		List<String> events = new java.util.ArrayList<String>();
		RecordingQueryStoreService qs = new RecordingQueryStoreService(events);
		service.setQueryStoreService(qs);
		EmptyPageBootstrapper obs = new EmptyPageBootstrapper("obs");
		obs.onFetch = t -> events.add("project:" + t);
		service.setBootstrappers(Arrays.asList(obs));

		service.reindexPatient(null);
		service.reindexPatient("");

		assertEquals("null/blank uuid must not delete anything", 0, qs.bulkDeleteCount);
		assertEquals("null/blank uuid must not project anything", 0, obs.fetchForPatientCount);
	}

	@Test
	public void reindexPatient_perTypeFailureIsolated() {
		RecordingQueryStoreService qs = new RecordingQueryStoreService(new java.util.ArrayList<String>());
		service.setQueryStoreService(qs);
		EmptyPageBootstrapper failing = new EmptyPageBootstrapper("obs");
		failing.perPatientFailure = new RuntimeException("boom");
		EmptyPageBootstrapper ok = new EmptyPageBootstrapper("encounter");
		service.setBootstrappers(Arrays.asList(failing, ok));

		service.reindexPatient("patient-uuid");

		assertEquals(1, qs.bulkDeleteCount);
		assertEquals("a per-type projection failure must not abort the rest", 1, ok.fetchForPatientCount);
	}

	// ---------- fakes ----------

	/** QueryStoreService that records bulkDeleteByPatient calls (and order, via the shared events
	 *  list) so reindex tests can pin the delete-before-projection invariant. */
	private static final class RecordingQueryStoreService implements QueryStoreService {

		private final List<String> events;

		int bulkDeleteCount;

		String lastDeletedPatient;

		RecordingQueryStoreService(List<String> events) {
			this.events = events;
		}

		@Override
		public void bulkDeleteByPatient(String patientUuid) {
			bulkDeleteCount++;
			lastDeletedPatient = patientUuid;
			events.add("delete");
		}

		@Override
		public org.openmrs.module.querystore.backend.WriteResult index(QueryDocument document) {
			return org.openmrs.module.querystore.backend.WriteResult.success();
		}

		@Override public void delete(String resourceType, String resourceUuid) { }

		@Override public List<QueryDocument> searchByPatient(String p, String q, int l) { return Collections.emptyList(); }

		@Override public List<QueryDocument> search(String q, int l) { return Collections.emptyList(); }

		@Override public List<QueryDocument> getPatientChart(String patientUuid) { return Collections.emptyList(); }

		@Override public void onStartup() { }

		@Override public void onShutdown() { }
	}

	/**
	 * Bootstrapper whose corpus is empty — fetchPage is called once, returns no records, the loop
	 * exits and the bootstrapper completes. Tracks the (cursor, uuid) it was invoked with so tests
	 * can pin the resume-from-progress semantics, and a failure can be injected to exercise the
	 * service's continue-on-error behavior.
	 */
	private static class EmptyPageBootstrapper extends TypeBootstrapper<Object> {
		private final String type;
		int fetchCount;
		int fetchForPatientCount;
		String lastPatientUuid;
		Instant lastAfterDateChanged;
		String lastAfterUuid;
		RuntimeException failure;
		RuntimeException perPatientFailure;
		java.util.function.Consumer<String> onFetch;

		EmptyPageBootstrapper(String type) { this.type = type; }

		@Override public String getResourceType() { return type; }
		@Override protected ClinicalRecordSerializer<Object> getSerializer() { return null; }
		@Override protected Instant getDateChanged(Object e) { return null; }
		@Override protected String getUuid(Object e) { return null; }

		@Override
		protected List<Object> fetchPage(Instant afterDateChanged, String afterUuid, int pageSize) {
			fetchCount++;
			lastAfterDateChanged = afterDateChanged;
			lastAfterUuid = afterUuid;
			if (onFetch != null) {
				onFetch.accept(type);
			}
			if (failure != null) {
				throw failure;
			}
			return Collections.emptyList();
		}

		@Override
		protected List<Object> fetchPageForPatient(String patientUuid, Instant afterDateChanged, String afterUuid,
		                                           int pageSize) {
			fetchForPatientCount++;
			lastPatientUuid = patientUuid;
			lastAfterDateChanged = afterDateChanged;
			lastAfterUuid = afterUuid;
			if (onFetch != null) {
				onFetch.accept(type);
			}
			if (perPatientFailure != null) {
				throw perPatientFailure;
			}
			return Collections.emptyList();
		}
	}

	/**
	 * Bootstrapper whose first invocation parks at a latch — lets a test verify that a second
	 * concurrent call waits for a lock rather than running in parallel. {@code blockOnPerPatient}
	 * selects which entry point blocks ({@code fetchPage} vs {@code fetchPageForPatient}); the
	 * other returns an empty page immediately so a single test can exercise just one path.
	 */
	private static final class BlockingBootstrapper extends TypeBootstrapper<Object> {
		private final CountDownLatch entered;
		private final CountDownLatch mayProceed;
		private final AtomicInteger fetchCount;
		private final boolean blockOnPerPatient;
		private boolean firstCall = true;

		BlockingBootstrapper(CountDownLatch entered, CountDownLatch mayProceed, AtomicInteger fetchCount,
		                     boolean blockOnPerPatient) {
			this.entered = entered;
			this.mayProceed = mayProceed;
			this.fetchCount = fetchCount;
			this.blockOnPerPatient = blockOnPerPatient;
		}

		@Override public String getResourceType() { return "test"; }
		@Override protected ClinicalRecordSerializer<Object> getSerializer() { return null; }
		@Override protected Instant getDateChanged(Object e) { return null; }
		@Override protected String getUuid(Object e) { return null; }

		@Override
		protected List<Object> fetchPage(Instant afterDateChanged, String afterUuid, int pageSize) {
			return blockOnPerPatient ? Collections.emptyList() : park();
		}

		@Override
		protected List<Object> fetchPageForPatient(String patientUuid, Instant a, String u, int p) {
			return blockOnPerPatient ? park() : Collections.emptyList();
		}

		private List<Object> park() {
			fetchCount.incrementAndGet();
			if (firstCall) {
				firstCall = false;
				entered.countDown();
				try {
					mayProceed.await();
				}
				catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
			}
			return Collections.emptyList();
		}
	}

	/** Minimal {@link ResourceTypeProvider} pairing a resource type name with a bootstrapper.
	 *  Serializer slot is unused in these tests because the bootstrapper is exercised directly. */
	private static final class TestProvider implements ResourceTypeProvider {
		private final String name;
		private final TypeBootstrapper<?> bootstrapper;

		TestProvider(String name, TypeBootstrapper<?> bootstrapper) {
			this.name = name;
			this.bootstrapper = bootstrapper;
		}

		@Override public String getResourceType() { return name; }
		@Override public ClinicalRecordSerializer<?> getSerializer() { return null; }
		@Override public TypeBootstrapper<?> getBootstrapper() { return bootstrapper; }
	}

	private static final class NullQueryStoreService implements QueryStoreService {
		@Override
		public org.openmrs.module.querystore.backend.WriteResult index(QueryDocument document) {
			return org.openmrs.module.querystore.backend.WriteResult.success();
		}
		@Override public void delete(String resourceType, String resourceUuid) { }
		@Override public void bulkDeleteByPatient(String patientUuid) { }
		@Override public List<QueryDocument> searchByPatient(String p, String q, int l) { return Collections.emptyList(); }
		@Override public List<QueryDocument> search(String q, int l) { return Collections.emptyList(); }
		@Override public List<QueryDocument> getPatientChart(String patientUuid) { return Collections.emptyList(); }
		@Override public void onStartup() { }
		@Override public void onShutdown() { }
	}

	private static final class ZeroEmbedder implements EmbeddingProvider {
		@Override public int getDimensions() { return 8; }
		@Override public float[] embed(String text) { return new float[8]; }
	}

	private static final class InMemoryDao extends BootstrapProgressDao {
		final Map<String, BootstrapProgress> store = new HashMap<>();

		InMemoryDao() { super(null); }

		@Override public BootstrapProgress find(String resourceType) { return store.get(resourceType); }

		@Override
		public List<BootstrapProgress> findAll() {
			return new java.util.ArrayList<>(store.values());
		}

		@Override public void save(BootstrapProgress p) { store.put(p.getResourceType(), p); }
	}
}
