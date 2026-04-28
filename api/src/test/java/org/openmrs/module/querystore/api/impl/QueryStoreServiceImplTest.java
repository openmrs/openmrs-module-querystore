/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
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

	@Test
	public void delete_toleratesNullArguments() {
		service.delete(null, null);
		service.delete("obs", null);
		service.delete(null, "obs-uuid");
	}
}
