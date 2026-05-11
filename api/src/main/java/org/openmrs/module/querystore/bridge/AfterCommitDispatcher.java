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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Hands a projection task to the {@link BridgeExecutor} once the originating transaction has
 * committed, so indexing never runs against uncommitted state and never blocks the clinical
 * request thread (ADR Decision 12 "Migration bridge"). When no transaction is active the task is
 * submitted immediately; at-least-once semantics still hold via the bootstrap reconciliation path.
 *
 * <p>Failures inside the dispatched task are caught and logged here. The aspect's contract per the
 * ADR is "log and swallow"; per-document failures must never bubble back to the clinical thread
 * (which has already returned anyway) and must not poison subsequent dispatches.
 */
public class AfterCommitDispatcher {

	private static final Log log = LogFactory.getLog(AfterCommitDispatcher.class);

	private final BridgeExecutor executor;

	public AfterCommitDispatcher(BridgeExecutor executor) {
		this.executor = executor;
	}

	public void dispatch(Runnable task) {
		Runnable guarded = wrap(task);
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					executor.submit(guarded);
				}
			});
		} else {
			executor.submit(guarded);
		}
	}

	private Runnable wrap(Runnable task) {
		return () -> {
			try {
				task.run();
			}
			catch (RuntimeException e) {
				// Log and swallow: the bridge is best-effort per ADR Decision 12. The
				// conditional-upsert-by-version invariant (ADR Decision 3) means a missed AOP
				// projection is corrected by the next save, an event handler when it ships, or
				// the bootstrap pass — none of those overwrite the freshest document.
				log.warn("Bridge projection task failed; swallowing per ADR Decision 12", e);
			}
		};
	}
}
