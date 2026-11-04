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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.querystore.LogCapture;
import org.openmrs.module.querystore.QueryStoreConstants;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.sync.ObsVersions;
import org.openmrs.module.querystore.serialization.ObsRecordSerializer;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 * Obs edits against real core (ADR: Sync reliability and reconciliation, "Obs edits"). Core edits an
 * obs by cloning it into a new obs and voiding the original, both inside {@code saveObs}; the save
 * event carries only the original. These drive real {@code ObsService} and {@code EncounterService}
 * calls, then run the consumer's after-commit work and check what it writes: the replacement indexed
 * with the new value, the original deleted, and nothing indexed under the original's uuid.
 */
public class ObsEditSyncTest extends BaseModuleContextSensitiveTest {

	private static final int WEIGHT_KG = 5089;

	private DeferredObsListener listener;

	@Before
	public void setUp() {
		listener = Context.getRegisteredComponent("querystore.test.obsEditListener", DeferredObsListener.class);
		listener.reset();
	}

	@Test
	public void newObs_isIndexedFromCommittedState() {
		Obs obs = Context.getObsService().saveObs(weight(61.0, null), null);

		afterCommit();

		assertEquals(1, listener.service.indexed.size());
		assertEquals(obs.getUuid(), listener.service.indexed.get(0).getResourceUuid());
		assertTrue(listener.service.deleted.isEmpty());
	}

	@Test
	public void directEdit_indexesTheReplacementAndDeletesTheVoidedOriginal() {
		Obs original = Context.getObsService().saveObs(weight(61.0, null), null);
		String originalUuid = original.getUuid();
		afterCommit();
		listener.reset();

		original.setValueNumeric(62.5);
		Obs replacement = Context.getObsService().saveObs(original, "corrected");
		afterCommit();

		assertNotEquals("core edits by replacing the obs", originalUuid, replacement.getUuid());
		assertReplaced(originalUuid, replacement.getUuid(), 62.5);
	}

	@Test
	public void editThroughItsEncounter_indexesTheReplacementAndDeletesTheVoidedOriginal() {
		// The O3 form path: the encounter is re-saved with one of its obs changed.
		Encounter encounter = new Encounter();
		encounter.setPatient(Context.getPatientService().getPatient(7));
		encounter.setEncounterType(Context.getEncounterService().getEncounterType(1));
		encounter.setLocation(Context.getLocationService().getLocation(1));
		encounter.setEncounterDatetime(new Date());
		encounter.addObs(weight(70.0, encounter));
		Context.getEncounterService().saveEncounter(encounter);
		Obs original = encounter.getAllObs().iterator().next();
		String originalUuid = original.getUuid();
		afterCommit();
		listener.reset();

		original.setValueNumeric(71.5);
		Context.getEncounterService().saveEncounter(encounter);
		afterCommit();

		List<Obs> live = new ArrayList<>(Context.getEncounterService().getEncounterByUuid(encounter.getUuid())
		        .getAllObs(false));
		assertEquals(1, live.size());
		assertNotEquals(originalUuid, live.get(0).getUuid());
		assertReplaced(originalUuid, live.get(0).getUuid(), 71.5);
	}

	@Test
	public void void_deletesAndIndexesNothing() {
		Obs obs = Context.getObsService().saveObs(weight(61.0, null), null);
		afterCommit();
		listener.reset();

		Context.getObsService().voidObs(obs, "entered in error");
		afterCommit();

		assertTrue(listener.service.indexed.isEmpty());
		assertTrue(deletedUuids().contains(obs.getUuid()));
	}

	@Test
	public void aChainOfEdits_resolvesToTheLiveObs_andOneLongerThanTheCap_isTruncatedVisibly() {
		ObsVersions versions = Context.getRegisteredComponent("querystore.sync.obsVersions", ObsVersions.class);
		ObsRecordSerializer serializer = new ObsRecordSerializer();

		Obs short1 = Context.getObsService().saveObs(weight(60.0, null), null);
		String shortOriginal = short1.getUuid();
		Obs current = short1;
		for (int i = 1; i <= 3; i++) {
			current.setValueNumeric(60.0 + i);
			current = Context.getObsService().saveObs(current, "edit " + i);
		}
		Context.flushSession();
		ObsVersions.Resolution within = versions.resolve(shortOriginal, serializer);
		assertEquals(Collections.singletonList(shortOriginal), within.getToDelete());
		assertEquals(1, within.getToIndex().size());
		assertEquals(current.getUuid(), within.getToIndex().get(0).getResourceUuid());

		Obs long1 = Context.getObsService().saveObs(weight(70.0, null), null);
		String longOriginal = long1.getUuid();
		current = long1;
		for (int i = 1; i <= ObsVersions.MAX_CHAIN + 5; i++) {
			current.setValueNumeric(70.0 + i);
			current = Context.getObsService().saveObs(current, "edit " + i);
		}
		Context.flushSession();
		try (LogCapture log = LogCapture.of(ObsVersions.class)) {
			ObsVersions.Resolution past = versions.resolve(longOriginal, serializer);

			assertEquals("the original still goes", Collections.singletonList(longOriginal), past.getToDelete());
			assertTrue("the live obs past the cap is not reached", past.getToIndex().isEmpty());
			assertEquals("and the truncation is logged, without the uuid", 1, log.at("WARN").size());
			assertFalse(log.at("WARN").get(0).contains(longOriginal));
		}
	}

	private void assertReplaced(String originalUuid, String replacementUuid, double newValue) {
		assertTrue("the voided original's document is deleted", deletedUuids().contains(originalUuid));
		for (QueryDocument doc : listener.service.indexed) {
			assertFalse("nothing is indexed under the voided original's uuid", originalUuid.equals(doc.getResourceUuid()));
		}
		QueryDocument indexed = listener.service.indexed.stream()
		        .filter(d -> replacementUuid.equals(d.getResourceUuid())).findFirst()
		        .orElseThrow(() -> new AssertionError("the replacement obs was not indexed"));
		assertEquals(newValue, ((Number) indexed.getMetadata().get(QueryStoreConstants.FIELD_VALUE_NUMERIC)).doubleValue(),
		    0.0);
	}

	private List<String> deletedUuids() {
		List<String> out = new ArrayList<>();
		listener.service.deleted.forEach(d -> out.add(d[1]));
		return out;
	}

	/** Flushes core's writes, then runs the consumer's held tasks as the dispatcher would after commit. */
	private void afterCommit() {
		Context.flushSession();
		listener.runAfterCommit();
	}

	private static Obs weight(double kg, Encounter encounter) {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(7));
		obs.setConcept(Context.getConceptService().getConcept(WEIGHT_KG));
		obs.setObsDatetime(new Date());
		obs.setLocation(Context.getLocationService().getLocation(1));
		obs.setValueNumeric(kg);
		obs.setEncounter(encounter);
		return obs;
	}
}
