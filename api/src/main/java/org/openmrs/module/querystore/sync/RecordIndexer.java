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

import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.querystore.SkipLogFormat;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.backend.WriteResult;
import org.openmrs.module.querystore.embedding.EmbeddingProvider;
import org.openmrs.module.querystore.model.QueryDocument;

/**
 * The {@code embed → index} terminal stage of the events-first sync pipeline. The events consumer
 * serializes the record while the Hibernate session is still open and passes the resulting
 * {@link QueryDocument} here; this class adds the embedding off the request thread and
 * writes via {@link QueryStoreService#index(QueryDocument)}, which honors the
 * conditional-upsert-by-version invariant (ADR Decision 3).
 *
 * <p>Mirrors {@code TypeBootstrapper.serializeAndEmbed} so the two write paths produce identical
 * documents for the same source record — both delegate the embedding-input construction to
 * {@link QueryDocument#getEmbeddingInput()} so the rule lives on the model, not at the call sites.
 */
public class RecordIndexer {

	private static final Log log = LogFactory.getLog(RecordIndexer.class);

	private final QueryStoreService queryStoreService;

	private final EmbeddingProvider embeddingProvider;

	public RecordIndexer(QueryStoreService queryStoreService, EmbeddingProvider embeddingProvider) {
		this.queryStoreService = queryStoreService;
		this.embeddingProvider = embeddingProvider;
	}

	/**
	 * Embed and write the document. This runs fire-and-forget by design (invoked from
	 * {@code AfterCommitDispatcher} after the source-record transaction has committed), so per-doc
	 * failures are logged here rather than thrown — there's no caller upstack who could meaningfully
	 * react. Bootstrap goes through {@code TypeBootstrapper.projectBatch} instead and consumes the
	 * {@link org.openmrs.module.querystore.backend.BulkWriteResult} directly so its progress counter
	 * only credits confirmed writes.
	 */
	public void index(QueryDocument doc) {
		doc.setEmbedding(embeddingProvider.embed(doc.getEmbeddingInput()));
		// The SPI contract is non-null; an out-of-tree QueryStoreService that returns null would NPE
		// inside the dispatcher's swallow guard and the failure would look identical to a per-task
		// throw. Enforce the contract here so the message names the offending implementation path.
		WriteResult result = Objects.requireNonNull(queryStoreService.index(doc),
		        "QueryStoreService.index returned null for " + doc.getResourceType() + "/"
		                + doc.getResourceUuid() + " — non-null is part of the SPI contract");
		if (!result.isSucceeded()) {
			// Grep-able by the [querystore-skip] tag SkipLogFormat owns. Same shape as the bootstrap
			// path so an operator's grok pattern catches both layers with one rule.
			log.warn(SkipLogFormat.format("sync", result.getFailure()));
		}
	}

	public void delete(String resourceType, String resourceUuid) {
		queryStoreService.delete(resourceType, resourceUuid);
	}

	/**
	 * Cross-type sweep of every per-type-store document keyed by {@code patientUuid}. Driven by
	 * {@link org.openmrs.module.querystore.serialization.PatientRecordSerializer#bulkDeletePatientUuidFor}
	 * on a {@code PurgeServiceEvent} for a patient, so a core {@code purgePatient} removes the
	 * patient's full read-store footprint, not just the {@code querystore_patient} row.
	 */
	public void bulkDeleteByPatient(String patientUuid) {
		queryStoreService.bulkDeleteByPatient(patientUuid);
	}
}
