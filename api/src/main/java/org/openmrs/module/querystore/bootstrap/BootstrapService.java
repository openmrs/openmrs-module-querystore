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
 * state writes (the events sync pipeline) are version-protected by the Decision 3 invariant
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
	 * Reconciliation remediation (ADR: Sync reliability and reconciliation): force a full re-walk of
	 * one resource type, used to correct drift surfaced by {@link #getDrift()}. Unlike
	 * {@link #bootstrap(String)} — which resumes from the persisted cursor and so is a near no-op for
	 * an already-{@code COMPLETED} type — this resets the type's progress (cursor cleared,
	 * {@code NOT_STARTED}) and re-scans the whole type from the beginning, picking up records the
	 * original scan skipped (poison rows) or never saw (lost events, post-bootstrap arrivals before
	 * the cursor). Idempotent via the Decision 3 conditional-upsert-by-version invariant, so
	 * re-walking already-indexed records writes no duplicates.
	 *
	 * <p><b>Re-walk only:</b> this corrects <em>positive</em> drift (under-indexing — the common
	 * case). It does not delete <em>stale extras</em> (negative drift: docs whose core record was
	 * voided/deleted but never evented out), since the scan only visits live core records. Runs under
	 * the same per-type lock as {@link #bootstrap(String)}; expensive (a full type scan), so callers
	 * should run it off the request thread. Throws {@code IllegalArgumentException} for an unknown
	 * resource type.
	 */
	void resyncType(String resourceType);

	/**
	 * The registered indexed resource-type names (core bootstrappers plus SPI-contributed providers
	 * that declare a bootstrapper). A cheap registry read — no counts or scans — suitable for
	 * validating a requested {@code resourceType} before launching a {@link #resyncType(String)}.
	 */
	java.util.List<String> getResourceTypeNames();

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
	 * run in parallel. Per-record and per-type failures are isolated and logged, so every type is
	 * attempted; if any record was left out the method then throws {@link IncompleteProjectionException}
	 * naming how many types and records failed. Whatever did project stays indexed. Such a patient
	 * stays cold (see {@link #isPatientProjectionIncomplete}): a later call projects it again even
	 * though it has documents.
	 *
	 * <p>Persists no progress row — the per-patient cursor lives only for the call. A bootstrapper
	 * that declares no per-patient story (the {@link TypeBootstrapper#fetchPageForPatient} default
	 * throws) is skipped at debug-log level instead of failing the whole call.
	 */
	void ensureIndexed(String patientUuid);

	/**
	 * Whether the last per-patient projection of {@code patientUuid} ({@link #ensureIndexed} or
	 * {@link #reindexPatient}) in this JVM left records out. The patient may already have documents,
	 * so a reader that gates the cold touch on the backend's existence probe asks this as well,
	 * projects the patient again, and reports the chart incomplete while the gap remains.
	 */
	boolean isPatientProjectionIncomplete(String patientUuid);

	/**
	 * Forces a full re-projection of one patient: deletes the patient's existing read-store
	 * documents, then re-projects every registered resource type from core — the same per-type
	 * projection {@link #ensureIndexed(String)} uses, but <em>unconditional</em>. Unlike
	 * {@code ensureIndexed} (which short-circuits when the patient already has any document), this
	 * repairs a <em>partially</em>-indexed patient — e.g. one whose recent records were added by a
	 * SQL dump that bypassed the live indexing path and which the lazy cold-touch path can
	 * therefore never refresh.
	 *
	 * <p>Delete and re-projection run under the same per-patient lock as {@code ensureIndexed}, so
	 * a concurrent cold-touch projection cannot interleave with the delete and leave the patient
	 * under-indexed. No-op for a null/blank uuid. Per-record and per-type failures are isolated,
	 * logged and then reported as an {@link IncompleteProjectionException}, matching {@code ensureIndexed}.
	 */
	void reindexPatient(String patientUuid);

	List<BootstrapProgress> getStatus();

	BootstrapProgress getStatus(String resourceType);

	/**
	 * Read-only drift snapshot for every resource type (ADR: Sync reliability and reconciliation):
	 * the core "expected" count ({@link TypeBootstrapper#countIndexable()}) versus the live index count
	 * ({@link org.openmrs.module.querystore.backend.BackendStore#countByType(String)}). Detection only
	 * — surfacing a gap is the operator's cue to re-run an existing reindex path; this method changes
	 * nothing. Either count may be {@code -1} ("unknown"), in which case the entry's drift is {@code null}.
	 * Runs a COUNT per type, so it is heavier than {@link #getStatus()} (which reads persisted rows) —
	 * intended for on-demand admin/ops use, not a hot path.
	 */
	DriftReport getDrift();
}
