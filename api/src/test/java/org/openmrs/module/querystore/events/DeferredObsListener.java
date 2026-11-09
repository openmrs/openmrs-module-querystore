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

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.querystore.serialization.ObsRecordSerializer;
import org.openmrs.module.querystore.sync.AfterCommitDispatcher;
import org.openmrs.module.querystore.sync.RecordIndexer;
import org.openmrs.module.querystore.sync.SyncExecutor;
import org.openmrs.module.querystore.sync.SyncTestSupport.RecordingService;
import org.openmrs.module.querystore.sync.SyncTestSupport.ZeroEmbedder;

/**
 * The events consumer with a dispatcher that holds tasks until {@link #runAfterCommit()}, so a
 * context test can run them after core's service call has finished: the moment after commit, when
 * an edited obs is already voided and its replacement saved. Writes are recorded, not indexed; the
 * obs lookups go to the real {@code ObsVersions} bean against the test database.
 */
public class DeferredObsListener extends CoreServiceEventListener {

	final RecordingService service = new RecordingService();

	private final RecordIndexer recordIndexer = new RecordIndexer(service, new ZeroEmbedder());

	private final List<Runnable> pending = new ArrayList<>();

	private final AfterCommitDispatcher deferred = new AfterCommitDispatcher(new SyncExecutor()) {

		@Override
		public void dispatch(Runnable task) {
			pending.add(task);
		}
	};

	private final SerializerRegistry obsOnly = EventsTestSupport.registryOf(new ObsRecordSerializer());

	/** Runs every held task, as the dispatcher would after commit. */
	void runAfterCommit() {
		List<Runnable> tasks = new ArrayList<>(pending);
		pending.clear();
		tasks.forEach(Runnable::run);
	}

	void reset() {
		pending.clear();
		service.indexed.clear();
		service.deleted.clear();
	}

	@Override
	SerializerRegistry registry() {
		return obsOnly;
	}

	@Override
	RecordIndexer indexer() {
		return recordIndexer;
	}

	@Override
	AfterCommitDispatcher dispatcher() {
		return deferred;
	}
}
