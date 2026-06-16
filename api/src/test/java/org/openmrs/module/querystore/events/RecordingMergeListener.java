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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openmrs.module.querystore.bootstrap.BootstrapProgress;
import org.openmrs.module.querystore.bootstrap.BootstrapService;
import org.openmrs.module.querystore.bootstrap.DriftReport;
import org.openmrs.module.querystore.sync.AfterCommitDispatcher;
import org.openmrs.module.querystore.sync.RecordIndexer;
import org.openmrs.module.querystore.sync.SyncTestSupport.ImmediateDispatcher;
import org.openmrs.module.querystore.sync.SyncTestSupport.RecordingService;
import org.openmrs.module.querystore.sync.SyncTestSupport.ZeroEmbedder;

/**
 * Test-only {@link CoreServiceEventListener} wired into context tests via
 * {@code TestingApplicationContext.xml} (alongside {@link ServiceEventProbe}). Spring delivers the
 * <em>real</em> {@code SaveServiceEvent<PersonMergeLog>} a real {@code mergePatients} publishes to
 * this bean's inherited {@code onSave}, so the production {@link #reconcileMerge} routing runs
 * against capturing doubles — verifying the real merge event's runtime type and payload drive a
 * delete-loser + reindex-winner with the right uuids, with no commit, no wired backend, and no ONNX
 * model.
 *
 * <p>The terminal collaborators are doubles on purpose: their backend behaviour is covered by the
 * MySQL/ES backend and bootstrap integration tests; here we pin only the event → routing → targets
 * seam. The {@link ImmediateDispatcher} runs the reconcile inline (synchronously, on the event
 * thread) so the capture is observable without the after-commit hook that the rolled-back test
 * transaction never fires. Every non-merge save routes through the inherited {@code project} with an
 * empty registry — a no-op — so this listener is inert in every other context test.
 */
public class RecordingMergeListener extends CoreServiceEventListener {

	private final RecordingService service = new RecordingService();

	private final RecordIndexer recordIndexer = new RecordIndexer(service, new ZeroEmbedder());

	private final ImmediateDispatcher immediateDispatcher = new ImmediateDispatcher();

	private final SerializerRegistry emptyRegistry = EventsTestSupport.registryOf();

	private final RecordingBootstrapService bootstrap = new RecordingBootstrapService();

	/** Loser uuids handed to {@code bulkDeleteByPatient} by the reconcile, in order. */
	List<String> sweptLoserUuids() {
		return service.bulkDeletedPatients;
	}

	/** Winner uuids handed to {@code reindexPatient} by the reconcile, in order. */
	List<String> reindexedWinnerUuids() {
		return bootstrap.reindexed;
	}

	/** Clear captures so an assertion sees only this test's merge, regardless of context reuse. */
	void reset() {
		service.bulkDeletedPatients.clear();
		bootstrap.reindexed.clear();
	}

	@Override
	SerializerRegistry registry() {
		return emptyRegistry;
	}

	@Override
	RecordIndexer indexer() {
		return recordIndexer;
	}

	@Override
	AfterCommitDispatcher dispatcher() {
		return immediateDispatcher;
	}

	@Override
	BootstrapService bootstrapService() {
		return bootstrap;
	}

	/** Records {@code reindexPatient} calls; inert for every other {@link BootstrapService} method. */
	static final class RecordingBootstrapService implements BootstrapService {

		final List<String> reindexed = new CopyOnWriteArrayList<>();

		@Override
		public void reindexPatient(String patientUuid) {
			reindexed.add(patientUuid);
		}

		@Override
		public void bootstrap() {
		}

		@Override
		public void bootstrap(String resourceType) {
		}

		@Override
		public void ensureIndexed(String patientUuid) {
		}

		@Override
		public List<BootstrapProgress> getStatus() {
			return Collections.emptyList();
		}

		@Override
		public BootstrapProgress getStatus(String resourceType) {
			return null;
		}

		@Override
		public DriftReport getDrift() {
			return null;
		}

		@Override
		public void onStartup() {
		}

		@Override
		public void onShutdown() {
		}
	}
}
