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
		log.debug("Indexing " + document.getResourceType() + " " + document.getResourceUuid());
	}

	@Override
	public void delete(String resourceType, String resourceUuid) {
		log.debug("Deleting " + resourceType + " " + resourceUuid);
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
