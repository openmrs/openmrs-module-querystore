/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.backend.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.junit.Test;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.util.ObjectBuilder;

/**
 * Elasticsearch refuses a request past {@code index.max_result_window} (10,000) instead of
 * answering what it has; the hybrid default triples the limit for each leg, so a raised
 * similarity limit reached the window and the slice silently fell back to its policy tiers.
 */
public class ElasticsearchSearchWindowTest {

	@Test
	public void bm25_cutsTheRequestedSizeAtTheResultWindow() throws Exception {
		SearchRequest request = capture(store -> store.bm25(org.openmrs.module.querystore.backend.SearchRequest
		        .builder().queryText("creatinine").limit(10_008).build()));

		assertEquals(Integer.valueOf(ElasticsearchBackendStore.FULL_CHART_MAX_HITS), request.size());
	}

	@Test
	public void knn_cutsKAndTheRequestedSizeAtTheResultWindow() throws Exception {
		SearchRequest request = capture(store -> store.knn(org.openmrs.module.querystore.backend.SearchRequest
		        .builder().queryVector(new float[] { 0.1f, 0.2f }).limit(10_008).build()));

		assertEquals(Integer.valueOf(ElasticsearchBackendStore.FULL_CHART_MAX_HITS), request.size());
		assertEquals(Long.valueOf(ElasticsearchBackendStore.FULL_CHART_MAX_HITS), request.knn().get(0).k());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static SearchRequest capture(java.util.function.Consumer<ElasticsearchBackendStore> search)
	        throws Exception {
		ElasticsearchClientFactory factory = mock(ElasticsearchClientFactory.class);
		ElasticsearchClient client = mock(ElasticsearchClient.class);
		when(factory.getClient()).thenReturn(client);
		AtomicReference<Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>>> requestFactory =
		        new AtomicReference<>();
		SearchResponse<Map> empty = SearchResponse.of(response -> response
		        .took(1)
		        .timedOut(false)
		        .shards(shards -> shards.total(1).successful(1).failed(0))
		        .hits(hits -> hits
		                .total(value -> value.value(0).relation(TotalHitsRelation.Eq))
		                .hits(Collections.emptyList())));
		when(client.search(any(Function.class), eq(Map.class))).thenAnswer(invocation -> {
			requestFactory.set((Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>>) invocation
			        .getArgument(0));
			return empty;
		});
		search.accept(new ElasticsearchBackendStore(factory));
		return requestFactory.get().apply(new SearchRequest.Builder()).build();
	}
}
