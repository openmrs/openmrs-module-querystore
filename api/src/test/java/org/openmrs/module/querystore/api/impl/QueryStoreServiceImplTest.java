package org.openmrs.module.querystore.api.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.model.QueryDocument;

public class QueryStoreServiceImplTest {

	private QueryStoreService service;

	@Before
	public void setUp() {
		service = new QueryStoreServiceImpl();
	}

	@Test
	public void searchByPatient_returnsEmptyByDefault() {
		assertTrue(service.searchByPatient("patient-uuid", "glucose", 10).isEmpty());
	}

	@Test
	public void index_toleratesMinimalDocument() {
		QueryDocument doc = new QueryDocument();
		doc.setResourceType("obs");
		doc.setResourceUuid("obs-uuid");
		service.index(doc);
		assertNotNull(doc.getMetadata());
	}
}
