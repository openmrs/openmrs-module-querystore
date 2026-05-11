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

import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.embedding.EmbeddingProvider;
import org.openmrs.module.querystore.model.QueryDocument;

/**
 * Shared {@code embed → index} terminal stage for bridge advice and (later) event handlers. The
 * upstream caller serializes the record while the Hibernate session is still open and passes the
 * resulting {@link QueryDocument} here; this class adds the embedding off the request thread and
 * writes via {@link QueryStoreService#index(QueryDocument)}, which honors the
 * conditional-upsert-by-version invariant (ADR Decision 3).
 *
 * <p>Mirrors {@code TypeBootstrapper.indexOne} so the two write paths produce identical documents
 * for the same source record.
 */
public class BridgeIndexer {

	private final QueryStoreService queryStoreService;

	private final EmbeddingProvider embeddingProvider;

	public BridgeIndexer(QueryStoreService queryStoreService, EmbeddingProvider embeddingProvider) {
		this.queryStoreService = queryStoreService;
		this.embeddingProvider = embeddingProvider;
	}

	public void index(QueryDocument doc) {
		doc.setEmbedding(embeddingProvider.embed(doc.getText()));
		queryStoreService.index(doc);
	}

	public void delete(String resourceType, String resourceUuid) {
		queryStoreService.delete(resourceType, resourceUuid);
	}
}
