/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.bootstrap;

import java.util.List;

import org.openmrs.api.OpenmrsService;

/**
 * Entry point for the initial-backfill path (ADR open question on bootstrap). Walks core's
 * transactional data per resource type and writes documents to the read store; concurrent steady-
 * state writes (AOP bridge or event handlers) are version-protected by the Decision 3 invariant
 * so the freshest projection always wins. Invocation is admin-triggered; this service does not
 * auto-run from {@code QueryStoreActivator}.
 */
public interface BootstrapService extends OpenmrsService {

	/** Runs backfill for every registered resource type sequentially. A per-type failure does not
	 *  abort the rest; failed types stay in {@link BootstrapStatus#FAILED} and can be retried. */
	void bootstrap();

	/** Runs backfill for a single resource type. Resumes from the persisted cursor if a prior
	 *  run was interrupted; restarts from the beginning only if no progress row exists. */
	void bootstrap(String resourceType);

	/**
	 * Synchronously projects every clinical record belonging to {@code patientUuid} into the read
	 * store across every registered resource type, using the same serializers, embedder, and write
	 * path the per-type bootstrap uses. Scoped to a single patient — distinct from {@link #bootstrap}'s
	 * global scan. Used by the lazy-projection path on cold {@code searchByPatient} (ADR Open
	 * Question: Initial backfill / bootstrap, "Lazy per-patient projection").
	 *
	 * <p>Idempotent: repeated calls write the same documents and rely on the Decision 3
	 * version-protection invariant for races with concurrent steady-state writes. Concurrent calls
	 * for the same patient serialize on a per-patient lock; concurrent calls for different patients
	 * run in parallel. Per-record and per-type failures are isolated and logged; the method returns
	 * after attempting every type.
	 *
	 * <p>Persists no progress row — the per-patient cursor lives only for the call. A bootstrapper
	 * that declares no per-patient story (the {@link TypeBootstrapper#fetchPageForPatient} default
	 * throws) is skipped at debug-log level instead of failing the whole call.
	 */
	void ensureIndexed(String patientUuid);

	/**
	 * Forces a full re-projection of one patient: deletes the patient's existing read-store
	 * documents, then re-projects every registered resource type from core — the same per-type
	 * projection {@link #ensureIndexed(String)} uses, but <em>unconditional</em>. Unlike
	 * {@code ensureIndexed} (which short-circuits when the patient already has any document), this
	 * repairs a <em>partially</em>-indexed patient — e.g. one whose recent records were added by a
	 * SQL dump that bypassed the live indexing bridge and which the lazy cold-touch path can
	 * therefore never refresh.
	 *
	 * <p>Delete and re-projection run under the same per-patient lock as {@code ensureIndexed}, so
	 * a concurrent cold-touch projection cannot interleave with the delete and leave the patient
	 * under-indexed. No-op for a null/blank uuid. Per-record and per-type failures are isolated and
	 * logged, matching {@code ensureIndexed}.
	 */
	void reindexPatient(String patientUuid);

	List<BootstrapProgress> getStatus();

	BootstrapProgress getStatus(String resourceType);
}
