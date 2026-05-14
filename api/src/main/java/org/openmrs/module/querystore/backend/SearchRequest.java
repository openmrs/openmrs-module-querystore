/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Search request crossing the backend SPI boundary.
 *
 * <p>{@code resourceTypes} narrows the search to a subset of per-type stores. An empty list means
 * "all querystore-prefixed types" (the {@code querystore_*} wildcard).
 *
 * <p>{@code queryText} is required for {@link BackendStore#bm25(SearchRequest)} and
 * {@link BackendStore#hybrid(SearchRequest)}; {@code queryVector} is required for
 * {@link BackendStore#knn(SearchRequest)} and {@link BackendStore#hybrid(SearchRequest)}.
 */
public final class SearchRequest {

	public static final int DEFAULT_LIMIT = 10;

	private final List<String> resourceTypes;

	private final String queryText;

	private final float[] queryVector;

	private final List<Filter> filters;

	private final int limit;

	private final int offset;

	private SearchRequest(List<String> resourceTypes, String queryText, float[] queryVector, List<Filter> filters,
	        int limit, int offset) {
		this.resourceTypes = Collections.unmodifiableList(new ArrayList<>(resourceTypes));
		this.queryText = queryText;
		this.queryVector = queryVector;
		this.filters = Collections.unmodifiableList(new ArrayList<>(filters));
		this.limit = limit;
		this.offset = offset;
	}

	public List<String> getResourceTypes() {
		return resourceTypes;
	}

	public String getQueryText() {
		return queryText;
	}

	public float[] getQueryVector() {
		return queryVector;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public int getLimit() {
		return limit;
	}

	public int getOffset() {
		return offset;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private final List<String> resourceTypes = new ArrayList<>();

		private String queryText;

		private float[] queryVector;

		private final List<Filter> filters = new ArrayList<>();

		private int limit = DEFAULT_LIMIT;

		private int offset = 0;

		public Builder resourceType(String resourceType) {
			this.resourceTypes.add(resourceType);
			return this;
		}

		public Builder resourceTypes(List<String> resourceTypes) {
			this.resourceTypes.clear();
			this.resourceTypes.addAll(resourceTypes);
			return this;
		}

		public Builder queryText(String queryText) {
			this.queryText = queryText;
			return this;
		}

		public Builder queryVector(float[] queryVector) {
			this.queryVector = queryVector;
			return this;
		}

		public Builder filter(Filter filter) {
			this.filters.add(filter);
			return this;
		}

		public Builder limit(int limit) {
			this.limit = limit;
			return this;
		}

		public Builder offset(int offset) {
			this.offset = offset;
			return this;
		}

		public SearchRequest build() {
			return new SearchRequest(resourceTypes, queryText, queryVector, filters, limit, offset);
		}
	}
}
