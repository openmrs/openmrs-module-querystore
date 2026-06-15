/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.aop.event.PurgeServiceEvent;
import org.openmrs.aop.event.SaveServiceEvent;
import org.openmrs.aop.event.UnvoidServiceEvent;
import org.openmrs.aop.event.VoidServiceEvent;
import org.openmrs.module.querystore.bridge.AfterCommitDispatcher;
import org.openmrs.module.querystore.bridge.BridgeIndexer;
import org.openmrs.module.querystore.bridge.BridgeAdviceTestSupport.ImmediateDispatcher;
import org.openmrs.module.querystore.bridge.BridgeAdviceTestSupport.RecordingService;
import org.openmrs.module.querystore.bridge.BridgeAdviceTestSupport.ZeroEmbedder;
import org.openmrs.module.querystore.serialization.EncounterRecordSerializer;

/**
 * Unit tests for the events consumer's routing — gate, event-type → purge flag, serializer
 * resolution — exercised through the real {@code on*} handlers and {@code RecordProjector}, with a
 * recording {@code QueryStoreService}. No OpenMRS context: the consumer's context seams
 * ({@code eventsEnabled}, {@code registry}, {@code indexer}, {@code dispatcher}) are overridden.
 */
public class CoreServiceEventListenerTest {

	private RecordingService service;

	private TestableListener listener;

	@Before
	public void setUp() {
		service = new RecordingService();
		BridgeIndexer indexer = new BridgeIndexer(service, new ZeroEmbedder());
		listener = new TestableListener(indexer, new ImmediateDispatcher(),
		    EventsTestSupport.registryOf(new EncounterRecordSerializer()));
		listener.eventsEnabled = true;
	}

	@Test
	public void onSave_nonVoided_indexes() {
		listener.onSave(new SaveServiceEvent<>(encounter("e1", false)));
		assertEquals(1, service.indexed.size());
	}

	@Test
	public void gatedOff_doesNothing() {
		listener.eventsEnabled = false;
		listener.onSave(new SaveServiceEvent<>(encounter("e1", false)));
		assertTrue(service.indexed.isEmpty());
		assertTrue(service.deleted.isEmpty());
	}

	@Test
	public void onVoid_deletes() {
		// The voided flag is set before the event publishes; purge=false routing lets the flag drive
		// the per-node delete, parity with the bridge.
		listener.onVoid(new VoidServiceEvent<>(encounter("e2", true)));
		assertTrue(service.indexed.isEmpty());
		assertEquals(1, service.deleted.size());
		assertEquals("e2", service.deleted.get(0)[1]);
	}

	@Test
	public void onUnvoid_reindexes() {
		// A now-unvoided (non-voided) entity routes through purge=false and indexes — the opposite of
		// onVoid. Locks that this handler is not a delete.
		listener.onUnvoid(new UnvoidServiceEvent<>(encounter("e4", false)));
		assertEquals(1, service.indexed.size());
		assertTrue(service.deleted.isEmpty());
	}

	@Test
	public void onPurge_deletes() {
		listener.onPurge(new PurgeServiceEvent<>(encounter("e3", false)));
		assertTrue(service.indexed.isEmpty());
		assertEquals(1, service.deleted.size());
	}

	@Test
	public void unindexedDataType_isSkipped() {
		// Registry has only the Encounter serializer; a Patient resolves to no serializer.
		listener.onSave(new SaveServiceEvent<>(patient("p1")));
		assertTrue(service.indexed.isEmpty());
		assertTrue(service.deleted.isEmpty());
	}

	@Test
	public void nonDataEntity_isSkipped() {
		// Location is OpenmrsMetadata, not OpenmrsData — the projector has no path for it.
		Location location = new Location();
		location.setUuid("loc");
		listener.onSave(new SaveServiceEvent<>(location));
		assertTrue(service.indexed.isEmpty());
		assertTrue(service.deleted.isEmpty());
	}

	private static Encounter encounter(String uuid, boolean voided) {
		Encounter enc = new Encounter();
		enc.setUuid(uuid);
		enc.setVoided(voided);
		enc.setEncounterDatetime(new Date());
		enc.setPatient(patient("patient-uuid"));
		EncounterType type = new EncounterType();
		type.setUuid("type-uuid");
		type.setName("Adult Outpatient Visit");
		enc.setEncounterType(type);
		return enc;
	}

	private static Patient patient(String uuid) {
		Patient patient = new Patient();
		patient.setUuid(uuid);
		return patient;
	}

	private static final class TestableListener extends CoreServiceEventListener {

		private final BridgeIndexer indexer;

		private final AfterCommitDispatcher dispatcher;

		private final SerializerRegistry registry;

		private boolean eventsEnabled;

		TestableListener(BridgeIndexer indexer, AfterCommitDispatcher dispatcher, SerializerRegistry registry) {
			this.indexer = indexer;
			this.dispatcher = dispatcher;
			this.registry = registry;
		}

		@Override
		boolean eventsEnabled() {
			return eventsEnabled;
		}

		@Override
		SerializerRegistry registry() {
			return registry;
		}

		@Override
		BridgeIndexer indexer() {
			return indexer;
		}

		@Override
		AfterCommitDispatcher dispatcher() {
			return dispatcher;
		}
	}
}
