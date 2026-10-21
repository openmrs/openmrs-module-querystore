/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.sync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Condition;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.module.querystore.sync.SyncTestSupport.ImmediateDispatcher;
import org.openmrs.module.querystore.sync.SyncTestSupport.RecordingService;
import org.openmrs.module.querystore.sync.SyncTestSupport.ZeroEmbedder;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.serialization.ConditionRecordSerializer;
import org.openmrs.module.querystore.serialization.EncounterRecordSerializer;
import org.openmrs.module.querystore.serialization.PatientRecordSerializer;
import org.openmrs.module.querystore.util.DateFormatUtil;

/**
 * Direct tests of the projection the events consumer drives (ADR Decision 12). Locks its behaviour
 * — per-node voided routing, purge override, the serializer-carried patient sweep — independently of
 * the event-trigger plumbing (which {@code CoreServiceEventListenerTest} covers).
 */
public class RecordProjectorTest {

	private RecordingService service;

	private RecordIndexer indexer;

	private ImmediateDispatcher dispatcher;

	@Before
	public void setUp() {
		service = new RecordingService();
		indexer = new RecordIndexer(service, new ZeroEmbedder());
		dispatcher = new ImmediateDispatcher();
	}

	@Test
	public void project_nonVoided_indexes() {
		RecordProjector.project(new EncounterRecordSerializer(), encounter("enc-1", false), false,
		    indexer, dispatcher);

		assertEquals(1, service.indexed.size());
		assertTrue(service.deleted.isEmpty());
	}

	@Test
	public void project_voided_deletesInsteadOfIndexing() {
		RecordProjector.project(new EncounterRecordSerializer(), encounter("enc-v", true), false,
		    indexer, dispatcher);

		assertTrue("voided entity must not be indexed (ADR Decision 10)", service.indexed.isEmpty());
		assertEquals(1, service.deleted.size());
		assertEquals("enc-v", service.deleted.get(0)[1]);
	}

	@Test
	public void project_purge_deletesEvenWhenNotVoided() {
		RecordProjector.project(new EncounterRecordSerializer(), encounter("enc-p", false), true,
		    indexer, dispatcher);

		assertTrue(service.indexed.isEmpty());
		assertEquals(1, service.deleted.size());
		assertEquals("enc-p", service.deleted.get(0)[1]);
		// Load-bearing: a non-patient purge must NOT sweep the patient's whole chart. Only
		// PatientRecordSerializer overrides the sweep hook; the default returning null is what keeps
		// purging one encounter from silently erasing every document for that patient_uuid.
		assertTrue("non-patient purge must not trigger a patient sweep",
		    service.bulkDeletedPatients.isEmpty());
	}

	@Test
	public void project_purgePatient_sweepsTheWholeChart() {
		Patient patient = new Patient();
		patient.setUuid("pat-1");

		RecordProjector.project(new PatientRecordSerializer(), patient, true, indexer, dispatcher);

		assertEquals("patient row deleted", 1, service.deleted.size());
		assertEquals("cross-type sweep fired for the patient uuid (serializer-carried hook)",
		    1, service.bulkDeletedPatients.size());
		assertEquals("pat-1", service.bulkDeletedPatients.get(0));
	}

	@Test
	public void project_readsTheVersionTheSaveStampsBeforeTheWriteRuns() {
		// The save event publishes before the save stamps dateCreated, so the version is read when
		// the after-commit write runs; without it the document carries no version at all, which
		// disables the backend's version guard and the bootstrap top-up's skip.
		Encounter enc = encounter("enc-new", false);
		java.util.List<Runnable> pending = new java.util.ArrayList<>();
		AfterCommitDispatcher afterCommit = new AfterCommitDispatcher(new SyncExecutor()) {
			@Override
			public void dispatchPrepared(java.util.function.Supplier<Runnable> prepare) {
				pending.add(() -> prepare.get().run());
			}
		};

		RecordProjector.project(new EncounterRecordSerializer(), enc, false, indexer, afterCommit);
		Date stamped = new Date(1792479801789L);
		enc.setDateCreated(stamped);
		pending.forEach(Runnable::run);

		assertEquals(stamped.toInstant(), service.indexed.get(0).getLastModified());
	}

	@Test
	public void project_indexesWhatTheSaveCommittedNotTheStateTheEventSaw() {
		// The save event publishes before the save stamps dateCreated, and a condition's record_date
		// is its dateCreated. Indexing the event-time serialization would store no record_date, and
		// with the committed version stamped on it the restart top-up would never repair it.
		Condition condition = new Condition();
		condition.setUuid("cond-new");
		CodedOrFreeText name = new CodedOrFreeText();
		name.setNonCoded("Hypertension");
		condition.setCondition(name);
		Patient patient = new Patient();
		patient.setUuid("patient-uuid");
		condition.setPatient(patient);
		java.util.List<Runnable> pending = new java.util.ArrayList<>();

		RecordProjector.project(new ConditionRecordSerializer(), condition, false, indexer, deferred(pending));
		Date stamped = new Date(1792479801789L);
		condition.setDateCreated(stamped);
		pending.forEach(Runnable::run);

		QueryDocument indexed = service.indexed.get(0);
		assertEquals(DateFormatUtil.toLocalDate(stamped), indexed.getDate());
		assertEquals(stamped.toInstant(), indexed.getLastModified());
	}

	@Test
	public void project_leavesTheVersionUnsetWhenTheCommittedRecordCannotBeSerialized() {
		// The event-time content predates the save, so it must not carry a version, neither the
		// committed one nor the previous save's that an edited record carries at event time: without
		// one, the restart top-up rewrites the document from core instead of skipping it.
		Encounter enc = encounter("enc-new", false);
		enc.setDateChanged(new Date(1792479801000L));
		java.util.List<Runnable> pending = new java.util.ArrayList<>();
		EncounterRecordSerializer failsAfterCommit = new EncounterRecordSerializer() {
			private int calls;

			@Override
			protected void populate(Encounter encounter, QueryDocument doc) {
				if (++calls > 1) {
					throw new IllegalStateException("lazy association outside its session");
				}
				super.populate(encounter, doc);
			}
		};

		RecordProjector.project(failsAfterCommit, enc, false, indexer, deferred(pending));
		enc.setDateCreated(new Date(1792479801789L));
		pending.forEach(Runnable::run);

		assertEquals(1, service.indexed.size());
		assertNull(service.indexed.get(0).getLastModified());
	}

	@Test
	public void project_indexesTheCommittedStateEvenWhenTheEntityChangesBeforeTheWorkerRuns() {
		// The committing thread prepares the document right after commit; the pool worker that embeds
		// and writes may run after a later transaction changed the same instance and rolled back.
		Encounter enc = encounter("enc-committed", false);
		java.util.List<java.util.function.Supplier<Runnable>> afterCommit = new java.util.ArrayList<>();
		AfterCommitDispatcher twoPhase = new AfterCommitDispatcher(new SyncExecutor()) {
			@Override
			public void dispatchPrepared(java.util.function.Supplier<Runnable> prepare) {
				afterCommit.add(prepare);
			}
		};

		RecordProjector.project(new EncounterRecordSerializer(), enc, false, indexer, twoPhase);
		Date committed = new Date(1792479801000L);
		enc.setDateCreated(committed);
		Runnable worker = afterCommit.get(0).get();
		enc.setDateCreated(new Date(1792566201000L));
		enc.setVoided(true);
		worker.run();

		assertEquals(1, service.indexed.size());
		assertEquals(committed.toInstant(), service.indexed.get(0).getLastModified());
	}

	private static AfterCommitDispatcher deferred(java.util.List<Runnable> pending) {
		return new AfterCommitDispatcher(new SyncExecutor()) {
			@Override
			public void dispatchPrepared(java.util.function.Supplier<Runnable> prepare) {
				pending.add(() -> prepare.get().run());
			}
		};
	}

	private static Encounter encounter(String uuid, boolean voided) {
		Encounter enc = new Encounter();
		enc.setUuid(uuid);
		enc.setVoided(voided);
		enc.setEncounterDatetime(new Date());
		Patient patient = new Patient();
		patient.setUuid("patient-uuid");
		enc.setPatient(patient);
		EncounterType type = new EncounterType();
		type.setUuid("type-uuid");
		type.setName("Adult Outpatient Visit");
		enc.setEncounterType(type);
		return enc;
	}
}
