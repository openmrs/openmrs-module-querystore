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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.aop.event.SaveServiceEvent;
import org.openmrs.aop.event.VoidServiceEvent;
import org.openmrs.api.context.Context;
import org.openmrs.person.PersonMergeLog;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 * Context-sensitive checks that ground the events consumer against real core event publishing
 * (in-memory context test DB, not {@code *IntegrationTest} / testcontainers):
 *
 * <ul>
 *   <li>a bean in querystore's context receives {@link SaveServiceEvent} from a core service save
 *       via a plain {@code @EventListener} — and for a legacy {@code TransactionProxyFactoryBean}
 *       service ({@link org.openmrs.api.LocationService}), the shape the #6084 guard de-duplicates
 *       rather than suppresses; and</li>
 *   <li>the {@code voided} flag is already set when {@link VoidServiceEvent} publishes, which is
 *       what lets the consumer route a void to a delete by reading the flag (purge=false); and</li>
 *   <li>a real {@code mergePatients} publishes a {@code SaveServiceEvent<PersonMergeLog>} carrying
 *       winner + loser — the sole signal the consumer's patient-merge reconciliation rides, since
 *       core fires no dedicated merge event; and</li>
 *   <li>a real {@link CoreServiceEventListener} receiving that real event routes it to delete-loser
 *       + reindex-winner with the correct uuids (via {@link RecordingMergeListener}'s capturing
 *       doubles) — the event → routing → targets seam the unit test cannot reach.</li>
 * </ul>
 */
public class CoreServiceEventTest extends BaseModuleContextSensitiveTest {

	@Test
	public void coreServiceSave_isReceivedByAContextBeanEventListener() {
		ServiceEventProbe probe = probe();
		int before = probe.savedEntities().size();

		Location location = new Location();
		location.setName("QueryStore event-probe location");
		Context.getLocationService().saveLocation(location);

		assertTrue("a context bean's @EventListener should receive SaveServiceEvent from a core "
		        + "service save (legacy TransactionProxyFactoryBean-wired LocationService)",
		    probe.savedEntities().size() > before);
	}

	@Test
	public void voidServiceEvent_entityIsAlreadyVoidedWhenPublished() {
		ServiceEventProbe probe = probe();
		Patient patient = Context.getPatientService().getPatient(2);
		assertNotNull("standard test data should have patient 2", patient);

		Context.getPatientService().voidPatient(patient, "querystore event-timing test");

		assertFalse("voiding a patient should publish a VoidServiceEvent",
		    probe.voidedFlagAtVoidEvent().isEmpty());
		assertTrue("the entity must already be voided when VoidServiceEvent publishes, so the consumer "
		        + "can route it to a delete by reading the flag",
		    probe.voidedFlagAtVoidEvent().stream().allMatch(Boolean::booleanValue));
	}

	@Test
	public void mergePatients_publishesPersonMergeLogSaveEvent() throws Exception {
		// The load-bearing fact behind patient-merge handling: core fires NO dedicated merge event,
		// but mergePatients ends by saving a PersonMergeLog through the service proxy. Verify that
		// surfaces as a SaveServiceEvent<PersonMergeLog> a context @EventListener receives, carrying
		// the winner + loser the consumer's reconcile reads.
		ServiceEventProbe probe = probe();
		Patient preferred = Context.getPatientService().getPatient(7);
		Patient notPreferred = Context.getPatientService().getPatient(8);
		assertNotNull("standard test data should have patient 7", preferred);
		assertNotNull("standard test data should have patient 8", notPreferred);

		Context.getPatientService().mergePatients(preferred, notPreferred);

		PersonMergeLog merge = probe.savedEntities().stream()
		        .filter(e -> e instanceof PersonMergeLog)
		        .map(e -> (PersonMergeLog) e)
		        .reduce((first, second) -> second)
		        .orElse(null);
		assertNotNull("mergePatients must publish a SaveServiceEvent<PersonMergeLog> — the consumer's "
		        + "sole merge signal", merge);
		assertNotNull("the merge event must carry the surviving person for reindex", merge.getWinner());
		assertNotNull("the merge event must carry the merged-away person for the sweep", merge.getLoser());
	}

	@Test
	public void mergePatients_realConsumerRoutesToReconcileWithCorrectTargets() throws Exception {
		// The seam the unit test cannot reach: a real mergePatients publishes a real
		// SaveServiceEvent<PersonMergeLog> that Spring delivers to a real CoreServiceEventListener,
		// whose reconcileMerge must read winner/loser off the live payload and drive delete-loser +
		// reindex-winner. RecordingMergeListener is that listener with capturing doubles (the backend
		// effect is integration-tested separately); the production bean also receives the event, but
		// its after-commit work rolls back with the test transaction.
		RecordingMergeListener mergeListener = Context.getRegisteredComponent(
		    "querystore.test.mergeListener", RecordingMergeListener.class);
		mergeListener.reset();

		Patient preferred = Context.getPatientService().getPatient(7);
		Patient notPreferred = Context.getPatientService().getPatient(8);
		assertNotNull("standard test data should have patient 7", preferred);
		assertNotNull("standard test data should have patient 8", notPreferred);
		String winnerUuid = preferred.getUuid();
		String loserUuid = notPreferred.getUuid();

		Context.getPatientService().mergePatients(preferred, notPreferred);

		assertTrue("the merged-away patient's documents must be swept (bulkDeleteByPatient(loser))",
		    mergeListener.sweptLoserUuids().contains(loserUuid));
		assertTrue("the surviving patient's chart must be re-projected (reindexPatient(winner))",
		    mergeListener.reindexedWinnerUuids().contains(winnerUuid));
	}

	private static ServiceEventProbe probe() {
		return Context.getRegisteredComponent("querystore.test.serviceEventProbe", ServiceEventProbe.class);
	}
}
