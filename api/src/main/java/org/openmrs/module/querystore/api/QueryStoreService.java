package org.openmrs.module.querystore.api;

import java.util.List;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.querystore.model.QueryDocument;

/**
 * Entry point for indexing and searching the read-side projection of OpenMRS clinical data.
 */
public interface QueryStoreService extends OpenmrsService {

	/**
	 * Indexes a clinical record into the query store, routing it to the correct per-type index
	 * based on {@link QueryDocument#getResourceType()}.
	 */
	void index(QueryDocument document);

	/**
	 * Removes the document with the given resource UUID from the given per-type index.
	 */
	void delete(String resourceType, String resourceUuid);

	/**
	 * Full-text search within a patient's chart.
	 */
	List<QueryDocument> searchByPatient(String patientUuid, String query, int limit);

	/**
	 * Hybrid (BM25 + semantic) search across all clinical record types.
	 */
	List<QueryDocument> search(String query, int limit);
}
