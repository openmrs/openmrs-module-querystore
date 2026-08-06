/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 */
package org.openmrs.module.querystore.backend.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.openmrs.module.querystore.backend.PatientChartRead;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.util.ObjectBuilder;

public class ElasticsearchPatientChartReadTest {

	@Test
	@SuppressWarnings("unchecked")
	public void findPatientChart_marksHandledReadFailureIncomplete() throws Exception {
		ElasticsearchClientFactory factory = mock(ElasticsearchClientFactory.class);
		ElasticsearchClient client = mock(ElasticsearchClient.class);
		when(factory.getClient()).thenReturn(client);
		when(client.search(
		        any(Function.class),
		        eq(Map.class))).thenThrow(new IOException("simulated read failure"));

		PatientChartRead read = new ElasticsearchBackendStore(factory)
		        .findPatientChart("patient-1");

		assertTrue("a handled backend error must not masquerade as a complete empty chart",
		        read.isTruncated());
		assertTrue(read.getDocuments().isEmpty());
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void findPatientChart_requestsExactTotalsAndDisclosesCappedResults() throws Exception {
		ElasticsearchClientFactory factory = mock(ElasticsearchClientFactory.class);
		ElasticsearchClient client = mock(ElasticsearchClient.class);
		when(factory.getClient()).thenReturn(client);
		AtomicReference<Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>>> requestFactory =
		        new AtomicReference<>();
		SearchResponse<Map> response = response(1L, 0);
		when(client.search(any(Function.class), eq(Map.class))).thenAnswer(invocation -> {
			requestFactory.set((Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>>) invocation
			        .getArgument(0));
			return response;
		});

		PatientChartRead read = new ElasticsearchBackendStore(factory).findPatientChart("patient-1");
		SearchRequest request = requestFactory.get().apply(new SearchRequest.Builder()).build();

		assertNotNull(request.trackTotalHits());
		assertEquals(Boolean.TRUE, request.trackTotalHits().enabled());
		assertEquals(Integer.valueOf(ElasticsearchBackendStore.FULL_CHART_MAX_HITS), request.size());
		assertTrue("a total larger than returned hits must be disclosed", read.isTruncated());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void findPatientChart_disclosesSuccessfulResponsesWithFailedShards() throws Exception {
		ElasticsearchClientFactory factory = mock(ElasticsearchClientFactory.class);
		ElasticsearchClient client = mock(ElasticsearchClient.class);
		when(factory.getClient()).thenReturn(client);
		when(client.search(any(Function.class), eq(Map.class))).thenReturn(response(0L, 1));

		PatientChartRead read = new ElasticsearchBackendStore(factory).findPatientChart("patient-1");

		assertTrue("failed shards must not masquerade as a complete chart", read.isTruncated());
	}

	private static SearchResponse<Map> response(long total, int failedShards) {
		return SearchResponse.of(response -> response
		        .took(1)
		        .timedOut(false)
		        .shards(shards -> shards.total(1).successful(1 - failedShards).failed(failedShards))
		        .hits(hits -> hits
		                .total(value -> value.value(total).relation(TotalHitsRelation.Eq))
		                .hits(Collections.emptyList())));
	}
}
