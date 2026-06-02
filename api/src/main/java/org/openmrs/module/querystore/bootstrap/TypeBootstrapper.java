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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.querystore.SkipLogFormat;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.backend.BulkWriteResult;
import org.openmrs.module.querystore.backend.DocFailure;
import org.openmrs.module.querystore.embedding.EmbeddingProvider;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.serialization.AbstractRecordSerializer;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;

/**
 * Per-type backfill driver shared across resource types (ADR open question on initial bootstrap).
 * Walks core's transactional data ordered by effective dateChanged ascending, projects each
 * record through the type's {@link ClinicalRecordSerializer}, embeds the text, and writes each page
 * in one {@link QueryStoreService#bulkIndex} call — which honors the conditional-upsert-by-version
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
				PageResult<T> page = nextPage(progress.getCursorDateChanged(), progress.getCursorUuid(), PAGE_SIZE);
				if (page == null) {
					break;
				}
				// Batch the writes: one bulkIndex per page instead of one index() per record avoids the
				// per-doc commit (MySQL per-row txn+fsync, Lucene segment-per-doc) that dominates backfill
				// wall-clock — ADR Decision 3. The cursor is checkpointed per page, not per record; a crash
				// mid-page re-does that page on resume, which the version-guarded upsert makes idempotent.
				progress.setDocumentsIndexed(progress.getDocumentsIndexed()
				        + projectBatch(page.entities, service, embedder));
				progress.setCursorDateChanged(page.nextCursorDate);
				progress.setCursorUuid(page.nextCursorUuid);
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
	 * batch-projects each page through the serializer + embedder + bulk write path. No progress row is
	 * persisted — the per-patient path is invoked from the auto-index path on cold search and its
	 * cursor lives only for the duration of the call. Per-record serialize/embed failures are isolated
	 * by {@link #serializeAndEmbed}; a page-fetch failure propagates (the poison-page fallback that
	 * {@link #run} uses is scoped to the global scan for now — a per-patient orphan still surfaces).
	 */
	public final void runForPatient(String patientUuid, QueryStoreService service, EmbeddingProvider embedder) {
		Instant cursor = null;
		String cursorUuid = null;
		while (true) {
			List<T> page = fetchPageForPatient(patientUuid, cursor, cursorUuid, PAGE_SIZE);
			if (page.isEmpty()) {
				break;
			}
			projectBatch(page, service, embedder);
			T last = page.get(page.size() - 1);
			cursor = getDateChanged(last);
			cursorUuid = getUuid(last);
		}
	}

	/**
	 * Fetches the next page as a {@link PageResult} carrying the page plus the cursor to resume after it
	 * (the last record's effective date + uuid). Returns {@code null} when the scan is exhausted.
	 */
	private PageResult<T> nextPage(Instant cursor, String cursorUuid, int pageSize) {
		List<T> fast = fetchPage(cursor, cursorUuid, pageSize);
		if (fast.isEmpty()) {
			return null;
		}
		T last = fast.get(fast.size() - 1);
		return new PageResult<T>(fast, getDateChanged(last), getUuid(last));
	}

	/** Serialize + embed every entity in the page and write the resulting documents in one
	 *  {@link QueryStoreService#bulkIndex} call, returning the count of confirmed writes. A null
	 *  serialization (group-obs parent) or a per-record serialize/embed failure is skipped, not fatal;
	 *  a backend write failure is logged via the per-doc {@link BulkWriteResult#getFailures()} and not
	 *  counted, keeping {@code documents_indexed} honest (writes confirmed, not "calls that didn't throw"). */
	private int projectBatch(List<T> page, QueryStoreService service, EmbeddingProvider embedder) {
		List<QueryDocument> batch = new ArrayList<QueryDocument>(page.size());
		for (T entity : page) {
			QueryDocument doc = serializeAndEmbed(entity, embedder);
			if (doc != null) {
				batch.add(doc);
			}
		}
		if (batch.isEmpty()) {
			return 0;
		}
		BulkWriteResult result = service.bulkIndex(batch);
		for (DocFailure failure : result.getFailures()) {
			// Grep-able by the [querystore-skip] tag; operators filter retryable=true to find candidates
			// to re-project after a transient backend incident.
			log.warn(SkipLogFormat.format("bootstrap", failure));
		}
		return result.getSucceeded();
	}

	/** Serializes and embeds one entity into a write-ready document, or returns {@code null} to skip it:
	 *  a null serialization (e.g. a group-obs parent) or a per-record serialize/embed failure (logged,
	 *  never fatal to the scan). */
	private QueryDocument serializeAndEmbed(T entity, EmbeddingProvider embedder) {
		try {
			QueryDocument doc = getSerializer().serialize(entity);
			if (doc == null) {
				return null;
			}
			doc.setEmbedding(embedder.embed(doc.getEmbeddingInput()));
			return doc;
		}
		catch (RuntimeException e) {
			// retryable=null → "unknown": a thrown exception, not a DocFailure-bearing WriteResult.
			log.warn(SkipLogFormat.format("bootstrap", getResourceType(), getUuid(entity),
			        null, e.getMessage()), e);
			return null;
		}
	}

	/** A fetched page plus the cursor to resume after it. {@code entities} may be empty (an all-poison
	 *  window the fallback skipped) while {@code nextCursorDate}/{@code nextCursorUuid} still advance past
	 *  it, so the scan progresses rather than re-fetching the same poison window forever. */
	protected static final class PageResult<E> {

		final List<E> entities;

		final Instant nextCursorDate;

		final String nextCursorUuid;

		protected PageResult(List<E> entities, Instant nextCursorDate, String nextCursorUuid) {
			this.entities = entities;
			this.nextCursorDate = nextCursorDate;
			this.nextCursorUuid = nextCursorUuid;
		}
	}
}
