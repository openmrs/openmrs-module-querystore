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

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.backend.DocFailure;
import org.openmrs.module.querystore.backend.WriteResult;
import org.openmrs.module.querystore.embedding.EmbeddingProvider;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.serialization.AbstractRecordSerializer;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;

/**
 * Per-type backfill driver shared across resource types (ADR open question on initial bootstrap).
 * Walks core's transactional data ordered by effective dateChanged ascending, projects each
 * record through the type's {@link ClinicalRecordSerializer}, embeds the text, and writes via
 * {@link QueryStoreService#index(QueryDocument)} — which honors the conditional-upsert-by-version
 * SPI invariant so a slow scan can't overwrite a fresher concurrent AOP / event write.
 *
 * <p>Subclasses implement {@link #fetchPage} and {@link #getSerializer} for their entity type;
 * {@link #getResourceType}, {@link #getDateChanged}, and {@link #getUuid} default off the serializer
 * and the {@link BaseOpenmrsData}/{@link OpenmrsObject} contracts and only need overriding for
 * exotic types. The {@link #run} loop persists the cursor after each page so an interrupted scan
 * resumes without re-projecting.
 */
public abstract class TypeBootstrapper<T> {

	private static final Log log = LogFactory.getLog(TypeBootstrapper.class);

	/** Page size for the paginated scan. Chosen to fit comfortably in memory across all types. */
	protected static final int PAGE_SIZE = 200;

	/** Defaults to the serializer's {@code resource_type} so a bootstrapper and its serializer can't
	 *  disagree on the resource_type they're projecting. Override only when the bootstrapper needs
	 *  a different name (no current use case). */
	public String getResourceType() {
		return getSerializer().getResourceType();
	}

	protected abstract ClinicalRecordSerializer<T> getSerializer();

	/**
	 * Returns up to {@code pageSize} records whose {@code dateChanged ?? dateCreated} is strictly
	 * after the cursor, ordered ascending with {@code uuid} as tie-breaker. Implementations
	 * typically query core via HQL. The cursor is {@code null} on the first call; treat null as
	 * "from the beginning."
	 */
	protected abstract List<T> fetchPage(Instant afterDateChanged, String afterUuid, int pageSize);

	/**
	 * Patient-scoped variant of {@link #fetchPage}: returns up to {@code pageSize} records belonging
	 * to {@code patientUuid} whose effective date is strictly after the cursor, ordered ascending
	 * with {@code uuid} as tie-breaker. Used by the lazy-per-patient projection path on cold-search
	 * (ADR Open Question: Initial backfill / bootstrap, "Lazy per-patient projection"). The default
	 * throws so a bootstrapper that has no per-patient story (e.g., reference-data types contributed
	 * via the SPI that don't carry a patient association) surfaces the no-op clearly; types that
	 * carry patient data override.
	 */
	protected List<T> fetchPageForPatient(String patientUuid, Instant afterDateChanged, String afterUuid,
	                                      int pageSize) {
		throw new UnsupportedOperationException(
		        "Per-patient fetch not implemented for " + getResourceType());
	}

	/**
	 * Returns the cursor timestamp for the entity. Default reads {@code dateChanged ?? dateCreated}
	 * via {@link AbstractRecordSerializer#lastModifiedOf} for any {@link BaseOpenmrsData} record and
	 * falls back to {@link Instant#EPOCH} (not null) so the cursor stays monotonic and a subsequent
	 * page fetch doesn't drop back to "from the beginning" semantics.
	 */
	protected Instant getDateChanged(T entity) {
		if (entity instanceof BaseOpenmrsData) {
			Instant i = AbstractRecordSerializer.lastModifiedOf((BaseOpenmrsData) entity);
			return i != null ? i : Instant.EPOCH;
		}
		return Instant.EPOCH;
	}

	/**
	 * Returns the entity's UUID for cursor tie-breaking. Default reads it via
	 * {@link OpenmrsObject#getUuid()}; subclasses override only when the entity is not an
	 * {@code OpenmrsObject} (test fixtures, exotic SPI types).
	 */
	protected String getUuid(T entity) {
		return entity instanceof OpenmrsObject ? ((OpenmrsObject) entity).getUuid() : null;
	}

	public final void run(BootstrapProgress progress, QueryStoreService service, EmbeddingProvider embedder,
	                      BootstrapProgressDao progressDao) {
		progress.setStatus(BootstrapStatus.RUNNING);
		if (progress.getStartedAt() == null) {
			progress.setStartedAt(Instant.now());
		}
		progress.setFailureMessage(null);
		progressDao.save(progress);

		try {
			while (true) {
				List<T> page = fetchPage(progress.getCursorDateChanged(), progress.getCursorUuid(), PAGE_SIZE);
				if (page.isEmpty()) {
					break;
				}
				for (T entity : page) {
					indexOne(entity, progress, service, embedder);
				}
				progressDao.save(progress);
			}
			progress.setStatus(BootstrapStatus.COMPLETED);
			progress.setCompletedAt(Instant.now());
			progressDao.save(progress);
		}
		catch (RuntimeException e) {
			log.warn("Bootstrap failed for " + getResourceType() + " at cursor "
			        + progress.getCursorDateChanged() + "/" + progress.getCursorUuid(), e);
			progress.setStatus(BootstrapStatus.FAILED);
			// Include the exception class so a null-message failure (NPE, ISE) is still identifiable
			// from the persisted row alone; getMessage() can be null and would store as "null".
			progress.setFailureMessage(e.getClass().getSimpleName() + ": " + e.getMessage());
			progressDao.save(progress);
			throw e;
		}
	}

	/**
	 * Patient-scoped variant of {@link #run}: walks {@link #fetchPageForPatient} until exhausted and
	 * projects each record through the serializer + embedder + write path. No progress row is
	 * persisted — the per-patient path is invoked from the auto-index path on cold search and its
	 * cursor lives only for the duration of the call. Per-record failures are isolated by
	 * {@link #projectOne}; a fetch-side failure propagates.
	 */
	public final void runForPatient(String patientUuid, QueryStoreService service, EmbeddingProvider embedder) {
		Instant cursor = null;
		String cursorUuid = null;
		while (true) {
			List<T> page = fetchPageForPatient(patientUuid, cursor, cursorUuid, PAGE_SIZE);
			if (page.isEmpty()) {
				break;
			}
			for (T entity : page) {
				try {
					projectOne(entity, service, embedder);
				}
				finally {
					cursor = getDateChanged(entity);
					cursorUuid = getUuid(entity);
				}
			}
		}
	}

	private void indexOne(T entity, BootstrapProgress progress, QueryStoreService service,
	                      EmbeddingProvider embedder) {
		try {
			if (projectOne(entity, service, embedder)) {
				progress.setDocumentsIndexed(progress.getDocumentsIndexed() + 1);
			}
		}
		finally {
			// Advance the cursor whether or not a document was produced (null-serializing records
			// like group-obs parents) and whether or not the index call succeeded (per-entity
			// failure is treated as a skip); resume must not revisit them.
			progress.setCursorDateChanged(getDateChanged(entity));
			progress.setCursorUuid(getUuid(entity));
		}
	}

	/** Serialize → embed → index a single entity, returning true only when the backend confirmed
	 *  the write. Per-entity skip on failure: a poison record must not stall a scan (the
	 *  {@link #run} cursor advances past it; {@link #runForPatient} likewise advances its local
	 *  cursor). Reading {@link WriteResult#isSucceeded()} — rather than treating "no throw" as
	 *  success — is what keeps {@code documents_indexed} honest. The old contract counted any
	 *  call that didn't throw, which silently inflated the counter when the service swallowed a
	 *  null-backend wiring gap or a backend's per-doc IO failure. */
	private boolean projectOne(T entity, QueryStoreService service, EmbeddingProvider embedder) {
		try {
			QueryDocument doc = getSerializer().serialize(entity);
			if (doc == null) {
				return false;
			}
			doc.setEmbedding(embedder.embed(doc.getEmbeddingInput()));
			// Non-null is part of the SPI contract — see QueryStoreService.index javadoc. Enforce
			// here so an out-of-tree impl that returns null gets a clear error instead of an NPE
			// that the outer RuntimeException catch absorbs as if it were a poison-record skip.
			WriteResult result = Objects.requireNonNull(service.index(doc),
			        "QueryStoreService.index returned null for " + getResourceType() + "/"
			                + getUuid(entity) + " — non-null is part of the SPI contract");
			if (result.isSucceeded()) {
				return true;
			}
			DocFailure f = result.getFailure();
			log.warn("[bootstrap] write failed for " + getResourceType() + "/" + getUuid(entity)
			        + ": " + (f != null ? f.getErrorMessage() : "no failure detail"));
		}
		catch (RuntimeException e) {
			log.warn("[bootstrap] skipping " + getResourceType() + "/" + getUuid(entity)
			        + " due to failure", e);
		}
		return false;
	}
}
