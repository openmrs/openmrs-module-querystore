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
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.module.querystore.sync.SyncTestSupport.ImmediateDispatcher;
import org.openmrs.module.querystore.sync.SyncTestSupport.RecordingService;
import org.openmrs.module.querystore.sync.SyncTestSupport.ZeroEmbedder;
import org.openmrs.module.querystore.serialization.EncounterRecordSerializer;
import org.openmrs.module.querystore.serialization.PatientRecordSerializer;

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
