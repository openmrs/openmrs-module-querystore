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

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.model.QueryDocument;

/**
 * Default implementation of {@link QueryStoreService}. Writes and reads are delegated to the
 * configured backend (Elasticsearch by default, per ADR decision 3). This scaffold logs calls and
 * returns empty results; wire a real {@code QueryStoreBackend} in {@code moduleApplicationContext.xml}
 * to activate indexing and search.
 */
public class QueryStoreServiceImpl extends BaseOpenmrsService implements QueryStoreService {

	private static final Log log = LogFactory.getLog(QueryStoreServiceImpl.class);

	@Override
	public void index(QueryDocument document) {
		if (document == null || document.getResourceUuid() == null) {
			return;
		}
		if (log.isDebugEnabled()) {
			log.debug("Indexing " + document.getResourceType() + " " + document.getResourceUuid());
		}
	}

	@Override
	public void delete(String resourceType, String resourceUuid) {
		if (resourceType == null || resourceUuid == null) {
			return;
		}
		if (log.isDebugEnabled()) {
			log.debug("Deleting " + resourceType + " " + resourceUuid);
		}
	}

	@Override
	public List<QueryDocument> searchByPatient(String patientUuid, String query, int limit) {
		return Collections.emptyList();
	}

	@Override
	public List<QueryDocument> search(String query, int limit) {
		return Collections.emptyList();
	}
}
