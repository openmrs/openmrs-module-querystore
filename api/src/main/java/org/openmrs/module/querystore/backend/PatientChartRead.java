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
import java.util.Set;
import java.util.TreeSet;

import org.openmrs.module.querystore.model.QueryDocument;

/**
 * One full-chart backend read and whether the backend knows records may be missing. This includes
 * documented result caps and handled backend failures. A result count alone is not sufficient
 * evidence: an uncapped backend can legitimately return exactly the configured cap.
 */
public final class PatientChartRead {

	private final List<QueryDocument> documents;

	private final boolean truncated;

	private final boolean projectionComplete;

	private final Set<String> withheldTypes;

	/** A backend read cannot certify deployment-wide projection completeness. */
	public PatientChartRead(List<QueryDocument> documents, boolean truncated) {
		this(documents, truncated, false);
	}

	public PatientChartRead(List<QueryDocument> documents, boolean truncated, boolean projectionComplete) {
		this(documents, truncated, projectionComplete, null);
	}

	public PatientChartRead(List<QueryDocument> documents, boolean truncated, boolean projectionComplete,
	    Set<String> withheldTypes) {
		this.documents = documents == null ? Collections.<QueryDocument> emptyList()
		        : Collections.unmodifiableList(new ArrayList<QueryDocument>(documents));
		this.truncated = truncated;
		this.projectionComplete = projectionComplete;
		this.withheldTypes = withheldTypes == null ? Collections.<String> emptySet()
		        : Collections.unmodifiableSet(new TreeSet<String>(withheldTypes));
	}

	/** An untruncated backend read; projection completeness still requires explicit evidence. */
	public static PatientChartRead complete(List<QueryDocument> documents) {
		return new PatientChartRead(documents, false);
	}

	public List<QueryDocument> getDocuments() {
		return documents;
	}

	public boolean isTruncated() {
		return truncated;
	}

	/**
	 * Whether every configured resource type has completed its bootstrap and is not being rewritten by
	 * a resync or a backend reset. A type a restart's top-up is walking counts, so records written to
	 * core while the module was not syncing may still be missing from this read.
	 */
	public boolean isProjectionComplete() {
		return projectionComplete;
	}

	/**
	 * The resource types whose records this read left out because the caller holds no core read
	 * privilege for them, whether or not the patient has any (Decision 14 point 4). Naming them lets a
	 * consumer tell a refused type from a patient without such records.
	 */
	public Set<String> getWithheldTypes() {
		return withheldTypes;
	}
}
