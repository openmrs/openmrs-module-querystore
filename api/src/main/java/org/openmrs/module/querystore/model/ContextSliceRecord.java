/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.model;

/**
 * One selected record in a context slice (ADR Decision 17): the document plus the
 * highest-priority selection tier that admitted it — see the
 * {@code QueryStoreConstants.TIER_*} constants. Mandatory, exact, typed-complete, and panel
 * records are protected by budget-constrained clinical consumers. Similarity records also retain
 * their original retrieval rank even though the complete slice remains in chart order.
 */
public final class ContextSliceRecord {

	private final QueryDocument document;

	private final String tier;

	private final Integer rank;

	public ContextSliceRecord(QueryDocument document, String tier) {
		this(document, tier, null);
	}

	public ContextSliceRecord(QueryDocument document, String tier, Integer rank) {
		this.document = document;
		this.tier = tier;
		this.rank = rank;
	}

	public QueryDocument getDocument() {
		return document;
	}

	public String getTier() {
		return tier;
	}

	/**
	 * The original 1-based retrieval position for a {@code similarity} record, or
	 * {@code null} for policy-selected records.
	 */
	public Integer getRank() {
		return rank;
	}
}
