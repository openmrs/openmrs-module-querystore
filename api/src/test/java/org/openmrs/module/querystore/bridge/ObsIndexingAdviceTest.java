/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.bridge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.ObsService;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.embedding.EmbeddingProvider;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.serialization.ObsRecordSerializer;

public class ObsIndexingAdviceTest {

	private TestableAdvice advice;

	private ObsRecordSerializer serializer;

	private BridgeIndexer indexer;

	private ImmediateDispatcher dispatcher;

	private RecordingService service;

	@Before
	public void setUp() {
		serializer = new ObsRecordSerializer();
		service = new RecordingService();
		indexer = new BridgeIndexer(service, new ZeroEmbedder());
		dispatcher = new ImmediateDispatcher();
		advice = new TestableAdvice(serializer, indexer, dispatcher);
	}

	@Test
	public void afterReturning_saveObs_indexesProjectedDoc() throws Throwable {
		Obs obs = numericObs("u-1", 11.2);
		advice.afterReturning(obs, saveObs(), new Object[]{obs, "reason"}, null);

		assertEquals("one dispatch", 1, dispatcher.count);
		assertEquals(1, service.indexed.size());
		assertEquals("u-1", service.indexed.get(0).getResourceUuid());
		assertEquals("obs", service.indexed.get(0).getResourceType());
	}

	@Test
	public void afterReturning_saveVoidedObs_emitsDeleteInsteadOfIndex() throws Throwable {
		// Per ADR Decision 10, voided records are deleted from the read store. The UI "void this
		// obs" path sets voided=true and calls saveObs; the bridge must not project the voided
		// document. Per-node voided policy: voided node → delete regardless of which method fired.
		Obs obs = numericObs("u-v", 1.0);
		obs.setVoided(true);

		advice.afterReturning(obs, saveObs(), new Object[]{obs, "void"}, null);

		assertEquals("no index for voided obs", 0, service.indexed.size());
		assertEquals(1, service.deleted.size());
		assertEquals("u-v", service.deleted.get(0)[1]);
	}

	@Test
	public void afterReturning_voidObs_emitsDelete() throws Throwable {
		Obs obs = numericObs("u-2", 5.0);
		obs.setVoided(true);
		advice.afterReturning(obs, voidObs(), new Object[]{obs, "reason"}, null);

		assertEquals(1, dispatcher.count);
		assertEquals("no upsert on void", 0, service.indexed.size());
		assertEquals(1, service.deleted.size());
		assertEquals("obs", service.deleted.get(0)[0]);
		assertEquals("u-2", service.deleted.get(0)[1]);
	}

	@Test
	public void afterReturning_purgeObs_emitsDelete() throws Throwable {
		Obs obs = numericObs("u-3", 7.0);
		// Purge does not set voided — the obs is unconditionally removed. The bridge must still
		// emit a delete.
		advice.afterReturning(null, purgeObs(), new Object[]{obs}, null);

		assertEquals(1, service.deleted.size());
		assertEquals("u-3", service.deleted.get(0)[1]);
	}

	@Test
	public void afterReturning_unvoidObs_emitsIndex() throws Throwable {
		Obs obs = numericObs("u-4", 9.0);
		// unvoidObs has already cleared the voided flag by the time afterReturning fires.
		advice.afterReturning(obs, unvoidObs(), new Object[]{obs}, null);
		assertEquals(1, service.indexed.size());
		assertEquals("u-4", service.indexed.get(0).getResourceUuid());
	}

	@Test
	public void afterReturning_unrelatedMethod_isIgnored() throws Throwable {
		Obs obs = numericObs("u-5", 1.0);
		advice.afterReturning(obs, ObsService.class.getMethod("getObs", Integer.class),
		        new Object[]{1}, null);
		assertEquals("no dispatch", 0, dispatcher.count);
	}

	@Test
	public void afterReturning_returnValueMissingButArgsPresent_usesArgs() throws Throwable {
		// purgeObs returns void — the obs must come from args[0]. Without this fallback the
		// advice would silently drop every purge call.
		Obs obs = numericObs("u-6", 2.0);
		Method twoArg = ObsService.class.getMethod("purgeObs", Obs.class, boolean.class);
		advice.afterReturning(null, twoArg, new Object[]{obs, Boolean.TRUE}, null);
		assertEquals(1, service.deleted.size());
		assertEquals("u-6", service.deleted.get(0)[1]);
	}

	@Test
	public void afterReturning_nullObs_isNoop() throws Throwable {
		advice.afterReturning(null, saveObs(), new Object[]{null, "reason"}, null);
		assertEquals(0, dispatcher.count);
	}

	@Test
	public void afterReturning_saveGroupObs_indexesEveryMemberAndSkipsEmptyParent() throws Throwable {
		// Group parents whose own value is empty produce a null doc (ADR Decision 6 group obs
		// convention). The bridge must still walk into members and index each one — the
		// alternative is a silent gap whenever a group is saved via ObsService.saveObs(parent).
		Obs parent = obsShell("parent-uuid");
		parent.addGroupMember(numericObs("m-1", 3.0));
		parent.addGroupMember(numericObs("m-2", 4.5));

		advice.afterReturning(parent, saveObs(), new Object[]{parent, "reason"}, null);

		assertEquals(1, dispatcher.count);
		assertEquals("two member documents indexed; group parent skipped", 2, service.indexed.size());
		Set<String> indexedUuids = new LinkedHashSet<>();
		for (QueryDocument doc : service.indexed) {
			indexedUuids.add(doc.getResourceUuid());
		}
		assertTrue(indexedUuids.contains("m-1"));
		assertTrue(indexedUuids.contains("m-2"));
	}

	@Test
	public void afterReturning_saveGroupObs_mixedVoidedMembers_partitionsDeleteAndIndex() throws Throwable {
		// A saveObs on a parent group can land with some members newly voided (UI cleared them)
		// and some still active. Per-node policy: the voided member is deleted from the read
		// store, the active member is indexed.
		Obs parent = obsShell("p");
		Obs live = numericObs("live", 3.0);
		Obs voided = numericObs("voided", 4.0);
		voided.setVoided(true);
		parent.addGroupMember(live);
		parent.addGroupMember(voided);

		advice.afterReturning(parent, saveObs(), new Object[]{parent, "reason"}, null);

		assertEquals(1, service.indexed.size());
		assertEquals("live", service.indexed.get(0).getResourceUuid());
		assertEquals(1, service.deleted.size());
		assertEquals("voided", service.deleted.get(0)[1]);
	}

	@Test
	public void afterReturning_voidGroupObs_emitsDeleteForParentAndMembers() throws Throwable {
		// On void the read store must shed every reachable obs in the group, including the parent
		// whose value was empty: chartsearchai data showed group parents do reach the store via
		// the index path on prior saves, so deleting only the leaves would leak parent documents.
		Obs parent = obsShell("p");
		parent.setVoided(true);
		Obs child = numericObs("c", 1.0);
		child.setVoided(true);
		parent.addGroupMember(child);

		advice.afterReturning(parent, voidObs(), new Object[]{parent, "reason"}, null);

		assertEquals(2, service.deleted.size());
		Set<String> deletedUuids = new LinkedHashSet<>();
		for (String[] d : service.deleted) {
			deletedUuids.add(d[1]);
		}
		assertTrue(deletedUuids.contains("p"));
		assertTrue(deletedUuids.contains("c"));
	}

	@Test
	public void afterReturning_purgeGroupObs_emitsDeleteRegardlessOfVoidedFlag() throws Throwable {
		// Purge can run on a non-voided tree (purgeObs is "remove from DB," not "void"). The
		// bridge must delete every reachable obs from the read store even though their voided
		// flag is false.
		Obs parent = obsShell("p");
		parent.addGroupMember(numericObs("c", 2.0));

		advice.afterReturning(null, purgeObs(), new Object[]{parent}, null);

		assertEquals("no index on purge", 0, service.indexed.size());
		assertEquals(2, service.deleted.size());
	}

	@Test
	public void afterReturning_indexerThrowsForOneMember_siblingsStillIndexed() throws Throwable {
		// Per-entity failure isolation: when index() throws on one document the bridge must still
		// process the rest of the batch. Without this, a single poison member skips its siblings
		// until the bootstrap reconciles — recoverable but worse than necessary.
		Obs parent = obsShell("p");
		parent.addGroupMember(numericObs("ok-1", 1.0));
		parent.addGroupMember(numericObs("poison", 2.0));
		parent.addGroupMember(numericObs("ok-2", 3.0));

		BridgeIndexer poisonIndexer = new BridgeIndexer(service, new ZeroEmbedder()) {
			@Override public void index(QueryDocument doc) {
				if ("poison".equals(doc.getResourceUuid())) {
					throw new RuntimeException("simulated poison");
				}
				super.index(doc);
			}
		};
		TestableAdvice resilient = new TestableAdvice(serializer, poisonIndexer, dispatcher);

		resilient.afterReturning(parent, saveObs(), new Object[]{parent, "reason"}, null);

		assertEquals("ok-1 and ok-2 still indexed despite poison sibling", 2, service.indexed.size());
	}

	@Test
	public void afterReturning_serializerThrows_isSwallowed() throws Throwable {
		// Per ADR Decision 12 the bridge is best-effort. A poison record must not propagate back
		// to the clinical thread (the obs save already succeeded).
		TestableAdvice failing = new TestableAdvice(new ObsRecordSerializer() {
			@Override protected void populate(Obs record, QueryDocument doc) {
				throw new RuntimeException("boom");
			}
		}, indexer, dispatcher);
		Obs obs = numericObs("u-7", 1.0);
		failing.afterReturning(obs, saveObs(), new Object[]{obs, "reason"}, null);
		assertEquals("no index produced after serializer failure", 0, service.indexed.size());
	}

	@Test
	public void collectTree_walksEverythingIncludingVoided() {
		Obs parent = obsShell("p");
		Obs voided = numericObs("v", 1.0);
		voided.setVoided(true);
		parent.addGroupMember(voided);
		parent.addGroupMember(numericObs("ok", 2.0));

		List<Obs> all = ObsIndexingAdvice.collectTree(parent);

		assertEquals("voided members are visited; the per-node dispatch decides delete vs index",
		        3, all.size());
	}

	// ---------- helpers ----------

	private static Method saveObs() throws NoSuchMethodException {
		return ObsService.class.getMethod("saveObs", Obs.class, String.class);
	}

	private static Method voidObs() throws NoSuchMethodException {
		return ObsService.class.getMethod("voidObs", Obs.class, String.class);
	}

	private static Method unvoidObs() throws NoSuchMethodException {
		return ObsService.class.getMethod("unvoidObs", Obs.class);
	}

	private static Method purgeObs() throws NoSuchMethodException {
		return ObsService.class.getMethod("purgeObs", Obs.class);
	}

	private static Obs numericObs(String uuid, double value) {
		Concept concept = concept("Fasting blood glucose");
		Obs obs = new Obs();
		obs.setUuid(uuid);
		obs.setConcept(concept);
		obs.setValueNumeric(value);
		obs.setObsDatetime(new Date());
		Person p = new Person();
		p.setUuid("patient-uuid");
		obs.setPerson(p);
		return obs;
	}

	private static Obs obsShell(String uuid) {
		Obs obs = new Obs();
		obs.setUuid(uuid);
		obs.setObsDatetime(new Date());
		Person p = new Person();
		p.setUuid("patient-uuid");
		obs.setPerson(p);
		return obs;
	}

	private static Concept concept(String name) {
		Concept c = new Concept();
		ConceptName cn = new ConceptName();
		cn.setName(name);
		cn.setLocale(Locale.ENGLISH);
		c.addName(cn);
		return c;
	}

	private static final class ImmediateDispatcher extends AfterCommitDispatcher {
		int count;

		ImmediateDispatcher() {
			super(new BridgeExecutor());
		}

		@Override
		public void dispatch(Runnable task) {
			count++;
			// Run immediately, no after-commit gating, to keep the assertions synchronous.
			task.run();
		}
	}

	private static final class TestableAdvice extends ObsIndexingAdvice {
		private final ObsRecordSerializer serializer;
		private final BridgeIndexer indexer;
		private final AfterCommitDispatcher dispatcher;

		TestableAdvice(ObsRecordSerializer s, BridgeIndexer i, AfterCommitDispatcher d) {
			this.serializer = s;
			this.indexer = i;
			this.dispatcher = d;
		}

		@Override ObsRecordSerializer serializer() { return serializer; }
		@Override BridgeIndexer indexer() { return indexer; }
		@Override AfterCommitDispatcher dispatcher() { return dispatcher; }
	}

	private static final class RecordingService implements QueryStoreService {
		final List<QueryDocument> indexed = new ArrayList<>();
		final List<String[]> deleted = new ArrayList<>();

		@Override public void index(QueryDocument document) { indexed.add(document); }
		@Override public void delete(String resourceType, String resourceUuid) {
			deleted.add(new String[]{resourceType, resourceUuid});
		}
		@Override public List<QueryDocument> searchByPatient(String p, String q, int l) {
			return java.util.Collections.emptyList();
		}
		@Override public List<QueryDocument> search(String q, int l) {
			return java.util.Collections.emptyList();
		}
		@Override public void onStartup() { }
		@Override public void onShutdown() { }
	}

	private static final class ZeroEmbedder implements EmbeddingProvider {
		@Override public int getDimensions() { return 8; }
		@Override public float[] embed(String text) { return new float[8]; }
	}
}
