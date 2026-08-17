/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 */
package org.openmrs.module.querystore.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.Answers;
import org.openmrs.module.querystore.backend.PatientChartRead;
import org.openmrs.module.querystore.model.ContextSliceRequest;
import org.openmrs.module.querystore.model.QueryDocument;

public class QueryStoreServiceCompatibilityTest {

	@Test
	public void defaultPatientChartReadWrapsLegacyImplementationsAsComplete() {
		QueryStoreService legacy = mock(QueryStoreService.class, Answers.CALLS_REAL_METHODS);
		List<QueryDocument> records = Collections.singletonList(new QueryDocument());
		when(legacy.getPatientChart("patient-1")).thenReturn(records);

		PatientChartRead read = legacy.getPatientChartRead("patient-1");

		assertSame(records.get(0), read.getDocuments().get(0));
		assertFalse(read.isTruncated());
		verify(legacy).getPatientChart("patient-1");
	}

	@Test
	public void defaultContextSliceFailsExplicitlyForImplementationsWithoutSliceSupport() {
		QueryStoreService legacy = mock(QueryStoreService.class, Answers.CALLS_REAL_METHODS);

		assertThrows(UnsupportedOperationException.class,
		        () -> legacy.getContextSlice("patient-1", "question",
		                new ContextSliceRequest(Collections.<String> emptySet(), false)));
	}
}
