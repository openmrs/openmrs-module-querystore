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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Condition;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.api.ConditionService;
import org.openmrs.api.context.Context;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.serialization.ConditionRecordSerializer;
import org.openmrs.module.querystore.sync.SyncTestSupport.RecordingService;
import org.openmrs.module.querystore.sync.SyncTestSupport.ZeroEmbedder;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 * Core's {@code saveCondition} edits by refreshing the instance the event carried, voiding it and
 * DAO-saving the edit as a new condition, which no event announces. The projection the save event
 * starts must therefore delete the voided original (Decision 10) and index its replacement.
 */
public class ConditionEditProjectionTest extends BaseModuleContextSensitiveTest {

	@Test
	public void anEditDeletesTheVoidedOriginalAndIndexesTheReplacementCoreSaved() {
		ConditionService conditions = Context.getConditionService();
		Condition condition = new Condition();
		CodedOrFreeText name = new CodedOrFreeText();
		name.setNonCoded("Hypertension");
		condition.setCondition(name);
		condition.setClinicalStatus(ConditionClinicalStatus.ACTIVE);
		condition.setPatient(Context.getPatientService().getPatient(2));
		conditions.saveCondition(condition);
		Context.flushSession();
		String original = condition.getUuid();

		RecordingService store = new RecordingService();
		List<Runnable> afterCommit = new ArrayList<>();
		AfterCommitDispatcher deferred = new AfterCommitDispatcher(new SyncExecutor()) {
			@Override
			public void dispatch(Runnable task) {
				afterCommit.add(task);
			}
		};
		condition.setClinicalStatus(ConditionClinicalStatus.INACTIVE);
		// The save event publishes before the save runs, with the edited instance.
		RecordProjector.project(new ConditionRecordSerializer(), condition, false,
		    new RecordIndexer(store, new ZeroEmbedder()), deferred);
		conditions.saveCondition(condition);
		Context.flushSession();
		afterCommit.forEach(Runnable::run);

		assertEquals(1, store.deleted.size());
		assertEquals(original, store.deleted.get(0)[1]);
		assertEquals(1, store.indexed.size());
		QueryDocument replacement = store.indexed.get(0);
		assertNotEquals(original, replacement.getResourceUuid());
		assertTrue(replacement.getText(), replacement.getText().contains("Status: INACTIVE"));
	}
}
