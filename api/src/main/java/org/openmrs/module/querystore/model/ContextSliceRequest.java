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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openmrs.module.querystore.QueryStoreConstants;

/**
 * The caller's question interpretation for a context slice (ADR Decision 17): which resource
 * types must be typed-complete and whether the question is temporal (recency anchor applies).
 * Querystore performs mechanical selection only — interpreting the question into these flags
 * is the consumer's job.
 */
public final class ContextSliceRequest {

	private final Set<String> types;

	private final boolean temporal;

	private int recencyAnchorSize = QueryStoreConstants.CONTEXT_RECENCY_ANCHOR_DEFAULT;

	private int similarityLimit = QueryStoreConstants.CONTEXT_SIMILARITY_LIMIT_DEFAULT;

	private boolean interpretQuestion;

	public ContextSliceRequest(Set<String> types, boolean temporal) {
		this.types = types == null ? Collections.<String> emptySet()
		        : Collections.unmodifiableSet(new HashSet<String>(types));
		this.temporal = temporal;
	}

	public Set<String> getTypes() {
		return types;
	}

	public boolean isTemporal() {
		return temporal;
	}

	public int getRecencyAnchorSize() {
		return recencyAnchorSize;
	}

	public void setRecencyAnchorSize(int recencyAnchorSize) {
		this.recencyAnchorSize = recencyAnchorSize;
	}

	public int getSimilarityLimit() {
		return similarityLimit;
	}

	public void setSimilarityLimit(int similarityLimit) {
		this.similarityLimit = similarityLimit;
	}

	/**
	 * When set, querystore derives the typed scope and the temporal flag from the question
	 * itself (ADR Decision 18) and UNIONs the caller's {@link #getTypes types} / ORs the
	 * caller's temporal flag — so consumers stop duplicating cue routing while retaining
	 * module-contributed additions.
	 */
	public boolean isInterpretQuestion() {
		return interpretQuestion;
	}

	public void setInterpretQuestion(boolean interpretQuestion) {
		this.interpretQuestion = interpretQuestion;
	}
}
