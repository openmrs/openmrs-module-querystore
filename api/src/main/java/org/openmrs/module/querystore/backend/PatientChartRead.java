/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 */
package org.openmrs.module.querystore.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openmrs.module.querystore.model.QueryDocument;

/**
 * One complete-chart backend read and whether that backend actually omitted records because of a
 * documented result cap. A result count equal to a cap is not sufficient evidence: an uncapped
 * backend can legitimately return exactly that many records.
 */
public final class PatientChartRead {

	private final List<QueryDocument> documents;

	private final boolean truncated;

	public PatientChartRead(List<QueryDocument> documents, boolean truncated) {
		this.documents = documents == null ? Collections.<QueryDocument> emptyList()
		        : Collections.unmodifiableList(new ArrayList<QueryDocument>(documents));
		this.truncated = truncated;
	}

	public static PatientChartRead complete(List<QueryDocument> documents) {
		return new PatientChartRead(documents, false);
	}

	public List<QueryDocument> getDocuments() {
		return documents;
	}

	public boolean isTruncated() {
		return truncated;
	}
}
