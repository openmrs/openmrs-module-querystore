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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;

import org.junit.Test;
import org.openmrs.module.querystore.backend.BulkWriteResult;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.MgetResponse;
import co.elastic.clients.elasticsearch.core.mget.MultiGetResponseItem;

/** The per-id accounting of {@code bulkDelete} when the request itself fails, which needs no cluster. */
public class ElasticsearchBulkDeleteAccountingTest {

	@Test
	@SuppressWarnings("unchecked")
	public void aMissingIndexReportedPerItemIsTheNoOpSuccess() throws Exception {
		ElasticsearchClientFactory factory = mock(ElasticsearchClientFactory.class);
		ElasticsearchClient client = mock(ElasticsearchClient.class);
		when(factory.getClient()).thenReturn(client);
		// mget on an index that was never created does not throw: it reports the missing index per item.
		MgetResponse<Void> read = MgetResponse.of(r -> r.docs(Arrays.asList(
		        MultiGetResponseItem.<Void> of(i -> i.failure(f -> f.index("querystore_obs").id("a")
		                .error(e -> e.type("index_not_found_exception").reason("no such index [querystore_obs]")))),
		        MultiGetResponseItem.<Void> of(i -> i.failure(f -> f.index("querystore_obs").id("b")
		                .error(e -> e.type("index_not_found_exception").reason("no such index [querystore_obs]")))))));
		when(client.mget(any(Function.class), eq(Void.class))).thenReturn(read);

		BulkWriteResult result = new ElasticsearchBackendStore(factory).bulkDelete("obs", Arrays.asList("a", "b"));

		assertEquals("a missing index is the second-call no-op outcome, as for delete()", 2, result.getSucceeded());
		assertTrue(result.getFailures().isEmpty());
		verify(client, never()).bulk(any(BulkRequest.class));
	}
}
