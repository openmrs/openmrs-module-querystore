/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * A patient-scoped query for {@link QueryStoreService#query(QuerySpec)}. Prototype consumer API for
 * the omnibar: one request shape that covers type-ahead (keyword, collapsed by concept), a recency
 * browse (no query text, newest first) and a results page (facets, offset).
 *
 * <p>The patient is required: there is no cross-patient form of this request.
 */
public final class QuerySpec {

	/** Retrieval mode. {@code KEYWORD} is BM25 only and never runs the query encoder. */
	public enum Mode {
		KEYWORD,
		HYBRID
	}

	public enum Sort {
		RELEVANCE,
		/** Newest {@code record_date} first; relevance breaks ties. */
		RECENT
	}

	public enum Collapse {
		NONE,
		/** One group per {@code concept_uuid}; documents without a concept are groups of one. */
		CONCEPT
	}

	public enum Facet {
		RESOURCE_TYPE
	}

	public static final int DEFAULT_LIMIT = 10;

	private final String patientUuid;

	private final String query;

	private final Mode mode;

	private final List<String> types;

	private final Sort sort;

	private final Collapse collapse;

	private final Set<Facet> facets;

	private final int limit;

	private final int offset;

	private final boolean wait;

	private final boolean partialResults;

	private QuerySpec(Builder b) {
		this.patientUuid = b.patientUuid;
		this.query = StringUtils.trimToNull(b.query);
		this.mode = b.mode;
		this.types = Collections.unmodifiableList(new ArrayList<>(b.types));
		this.sort = b.sort;
		this.collapse = b.collapse;
		this.facets = b.facets.isEmpty() ? Collections.<Facet> emptySet()
		        : Collections.unmodifiableSet(EnumSet.copyOf(b.facets));
		this.limit = b.limit;
		this.offset = b.offset;
		this.wait = b.wait;
		this.partialResults = b.partialResults;
	}

	public String getPatientUuid() {
		return patientUuid;
	}

	/** The query text, trimmed; {@code null} for a recency browse. */
	public String getQuery() {
		return query;
	}

	public Mode getMode() {
		return mode;
	}

	/** Resource types to search; empty means every type. */
	public List<String> getTypes() {
		return types;
	}

	public Sort getSort() {
		return sort;
	}

	public Collapse getCollapse() {
		return collapse;
	}

	public Set<Facet> getFacets() {
		return facets;
	}

	/** Page size: hits, or groups when collapsed. */
	public int getLimit() {
		return limit;
	}

	public int getOffset() {
		return offset;
	}

	/**
	 * Whether a cold patient is projected before answering (seconds on a large chart). When false, a
	 * cold patient gets a background warm and an empty {@link IndexStatus#PROJECTING} result at once.
	 */
	public boolean isWait() {
		return wait;
	}

	/**
	 * Whether a {@link IndexStatus#PROJECTING} chart is searched as far as it has landed, with its
	 * progress. Off by default: then a projecting chart answers its bare status, no hits, groups,
	 * facets or progress. Turn it on only for a caller who can read every registered type: rows and
	 * progress arrive type by type, so their timing marks when each type, readable or not, finished.
	 */
	public boolean isPartialResults() {
		return partialResults;
	}

	public static Builder forPatient(String patientUuid) {
		return new Builder(patientUuid);
	}

	public static final class Builder {

		private final String patientUuid;

		private String query;

		private Mode mode = Mode.HYBRID;

		private final List<String> types = new ArrayList<>();

		private Sort sort = Sort.RELEVANCE;

		private Collapse collapse = Collapse.NONE;

		private final Set<Facet> facets = EnumSet.noneOf(Facet.class);

		private int limit = DEFAULT_LIMIT;

		private int offset = 0;

		private boolean wait = true;

		private boolean partialResults;

		private Builder(String patientUuid) {
			this.patientUuid = patientUuid;
		}

		public Builder query(String query) {
			this.query = query;
			return this;
		}

		public Builder mode(Mode mode) {
			this.mode = mode;
			return this;
		}

		public Builder types(List<String> types) {
			this.types.clear();
			this.types.addAll(types);
			return this;
		}

		public Builder sort(Sort sort) {
			this.sort = sort;
			return this;
		}

		public Builder collapse(Collapse collapse) {
			this.collapse = collapse;
			return this;
		}

		public Builder facet(Facet facet) {
			this.facets.add(facet);
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

		public Builder wait(boolean wait) {
			this.wait = wait;
			return this;
		}

		public Builder partialResults(boolean partialResults) {
			this.partialResults = partialResults;
			return this;
		}

		/**
		 * @throws IllegalArgumentException when the patient is blank, the limit is not positive, the
		 *         offset is negative, an enum is null, or there is no query text and the sort is
		 *         relevance (there is nothing to rank by)
		 */
		public QuerySpec build() {
			if (StringUtils.isBlank(patientUuid)) {
				throw new IllegalArgumentException("patientUuid is required");
			}
			if (limit <= 0) {
				throw new IllegalArgumentException("limit must be positive");
			}
			if (offset < 0) {
				throw new IllegalArgumentException("offset must not be negative");
			}
			if (mode == null || sort == null || collapse == null) {
				throw new IllegalArgumentException("mode, sort and collapse must not be null");
			}
			if (StringUtils.isBlank(query) && sort == Sort.RELEVANCE) {
				throw new IllegalArgumentException("a query without text must sort by RECENT");
			}
			return new QuerySpec(this);
		}
	}
}
