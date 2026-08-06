/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 */
package org.openmrs.module.querystore.backend.elasticsearch;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

import org.junit.Test;
import org.openmrs.module.querystore.backend.PatientChartRead;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

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
}
