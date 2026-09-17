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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_OBS_GROUP_CONCEPT_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_SYNONYMS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.backend.DocFailure;
import org.openmrs.module.querystore.backend.WriteResult;
import org.openmrs.module.querystore.sync.SyncTestSupport.RecordingService;
import org.openmrs.module.querystore.embedding.EmbeddingProvider;
import org.openmrs.module.querystore.model.QueryDocument;

public class RecordIndexerTest {

	private RecordIndexer indexer;

	private RecordingService service;

	private CountingEmbedder embedder;

	@Before
	public void setUp() {
		service = new RecordingService();
		embedder = new CountingEmbedder();
		indexer = new RecordIndexer(service, embedder);
	}

	@Test
	public void index_embedsAndForwardsToService() {
		QueryDocument doc = new QueryDocument();
		doc.setResourceType("obs");
		doc.setResourceUuid("u-1");
		doc.setText("Fasting blood glucose: 11.2 mmol/L");

		indexer.index(doc);

		assertEquals(1, service.indexed.size());
		QueryDocument indexed = service.indexed.get(0);
		assertSame(doc, indexed);
		assertNotNull("embedding populated", indexed.getEmbedding());
		assertEquals(8, indexed.getEmbedding().length);
		assertEquals(1, embedder.inputs.size());
		assertEquals("Fasting blood glucose: 11.2 mmol/L", embedder.inputs.get(0));
	}

	@Test
	public void index_embeddingInputIncludesGroupNameAndSynonyms() {
		QueryDocument doc = new QueryDocument();
		doc.setResourceType("obs");
		doc.setResourceUuid("u-2");
		doc.setText("Systolic blood pressure: 120 mmHg");
		doc.putMetadata(FIELD_OBS_GROUP_CONCEPT_NAME, "Vital signs");
		doc.putMetadata(FIELD_SYNONYMS, Arrays.asList("SBP", "Systolic BP"));

		indexer.index(doc);

		assertEquals(1, embedder.inputs.size());
		assertEquals(
		    "Vital signs — Systolic blood pressure: 120 mmHg SBP Systolic BP",
		    embedder.inputs.get(0));
	}

	@Test
	public void delete_forwardsToService() {
		indexer.delete("obs", "u-3");
		assertEquals(1, service.deleted.size());
		assertEquals("obs", service.deleted.get(0)[0]);
		assertEquals("u-3", service.deleted.get(0)[1]);
	}

	@Test
	public void bulkDeleteByPatient_forwardsToService() {
		indexer.bulkDeleteByPatient("patient-9");
		assertEquals(1, service.bulkDeletedPatients.size());
		assertEquals("patient-9", service.bulkDeletedPatients.get(0));
	}

	@Test
	public void index_swallowsServiceFailureSoAfterCommitDispatchSurvives() {
		// The sync pipeline runs from AfterCommitDispatcher, which catches per-task RuntimeException but
		// can't distinguish "the indexer's own logic broke" from "the backend dropped this write."
		// Sync writes failing must NOT propagate — they're logged at this layer with the offending
		// resource_uuid context, and the dispatcher continues with subsequent tasks. A future
		// "simplification" that re-introduces propagation would surface every dropped write as a
		// noisy after-commit failure, masking the per-doc context this layer's log line carries.
		QueryStoreService failingService = new QueryStoreService() {
			@Override public WriteResult index(QueryDocument document) {
				return WriteResult.failed(new DocFailure(document.getResourceType(),
				        document.getResourceUuid(), "simulated backend drop", false));
			}
			@Override public void delete(String resourceType, String resourceUuid) { }
			@Override public void bulkDeleteByPatient(String patientUuid) { }
			@Override public List<QueryDocument> searchByPatient(String p, String q, int l) { return Collections.emptyList(); }
			@Override public List<QueryDocument> search(String q, int l) { return Collections.emptyList(); }
			@Override public List<QueryDocument> getPatientChart(String patientUuid) { return Collections.emptyList(); }
			@Override public org.openmrs.module.querystore.model.ContextSlice getContextSlice(String patientUuid, String question, org.openmrs.module.querystore.model.ContextSliceRequest request) { return new org.openmrs.module.querystore.model.ContextSlice(java.util.Collections.emptyList(), 0, false); }
			@Override public void onStartup() { }
			@Override public void onShutdown() { }
		};
		CountingEmbedder failingEmbedder = new CountingEmbedder();
		RecordIndexer failingIndexer = new RecordIndexer(failingService, failingEmbedder);
		QueryDocument doc = new QueryDocument();
		doc.setResourceType("obs");
		doc.setResourceUuid("u-fail");
		doc.setText("anything");

		// Must not throw — assertion is the absence of an exception. If propagation regresses, this
		// line throws and the test fails loudly.
		failingIndexer.index(doc);

		// Pin the embed-then-index ordering: a future "short-circuit on failing backend" refactor
		// that skips the embed call when the service is unhealthy would change the indexer's semantics
		// (downstream consumers of the document mutation depend on the embedding being populated
		// before the write attempt). Verify the embed step still ran even though the write was
		// reported as failed.
		assertEquals("embedder still ran before the failing index call",
		        1, failingEmbedder.inputs.size());
	}

	private static final class CountingEmbedder implements EmbeddingProvider {
		final List<String> inputs = new ArrayList<>();

		@Override public int getDimensions() { return 8; }

		@Override
		public float[] embed(String text) {
			inputs.add(text);
			return new float[8];
		}
	}
}
