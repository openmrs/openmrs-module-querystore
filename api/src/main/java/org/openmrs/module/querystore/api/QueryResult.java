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

import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_CONCEPT_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_CONCEPT_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_UNITS;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_VALUE_NUMERIC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.querystore.model.QueryDocument;

/**
 * The answer to a {@link QuerySpec}. {@link #toMap()} is the REST response body, kept here (like
 * {@code BootstrapStatusReport.toMap()}) so the JSON contract is unit-tested rather than hand-typed in
 * the controller.
 */
public final class QueryResult {

	/** How many recent values a concept group carries, enough for a sparkline. */
	public static final int RECENT_VALUES = 5;

	private final IndexStatus status;

	private final List<Hit> hits;

	private final List<ConceptGroup> groups;

	private final Map<String, Integer> resourceTypeCounts;

	/**
	 * @param groups {@code null} unless the spec collapsed by concept
	 * @param resourceTypeCounts {@code null} unless the spec asked for the resource-type facet
	 */
	public QueryResult(IndexStatus status, List<Hit> hits, List<ConceptGroup> groups,
	        Map<String, Integer> resourceTypeCounts) {
		this.status = status;
		this.hits = Collections.unmodifiableList(new ArrayList<>(hits));
		this.groups = groups == null ? null : Collections.unmodifiableList(new ArrayList<>(groups));
		this.resourceTypeCounts = resourceTypeCounts == null ? null
		        : Collections.unmodifiableMap(new LinkedHashMap<>(resourceTypeCounts));
	}

	/** A result with nothing in it yet, for a cold patient answered without waiting. */
	public static QueryResult empty(IndexStatus status, QuerySpec spec) {
		return new QueryResult(status, Collections.<Hit> emptyList(),
		        spec.getCollapse() == QuerySpec.Collapse.CONCEPT ? Collections.<ConceptGroup> emptyList() : null,
		        spec.getFacets().contains(QuerySpec.Facet.RESOURCE_TYPE) ? Collections.<String, Integer> emptyMap()
		                : null);
	}

	public IndexStatus getStatus() {
		return status;
	}

	/** The page of hits; empty when the result is collapsed (see {@link #getGroups()}). */
	public List<Hit> getHits() {
		return hits;
	}

	/** The page of concept groups, or {@code null} when the spec did not collapse. */
	public List<ConceptGroup> getGroups() {
		return groups;
	}

	/**
	 * Matches per resource type across the whole candidate pool (not just this page), or {@code null}
	 * when not requested. Approximate: the pool is capped, and in hybrid mode it holds the nearest
	 * vectors whether or not they match the words.
	 */
	public Map<String, Integer> getResourceTypeCounts() {
		return resourceTypeCounts;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> out = new LinkedHashMap<>();
		out.put("status", status.wireName());
		List<Object> hitMaps = new ArrayList<>(hits.size());
		for (Hit h : hits) {
			hitMaps.add(h.toMap());
		}
		out.put("hits", hitMaps);
		if (groups != null) {
			List<Object> groupMaps = new ArrayList<>(groups.size());
			for (ConceptGroup g : groups) {
				groupMaps.add(g.toMap());
			}
			out.put("groups", groupMaps);
		}
		if (resourceTypeCounts != null) {
			Map<String, Object> facet = new LinkedHashMap<>();
			facet.put("approximate", true);
			facet.put("counts", resourceTypeCounts);
			Map<String, Object> facets = new LinkedHashMap<>();
			facets.put("resourceType", facet);
			out.put("facets", facets);
		}
		return out;
	}

	/** One document with its relevance score ({@code null} when it was not a scored match). */
	public static final class Hit {

		private final QueryDocument document;

		private final Double score;

		public Hit(QueryDocument document, Double score) {
			this.document = document;
			this.score = score;
		}

		public QueryDocument getDocument() {
			return document;
		}

		/** BM25 score in keyword mode, fused RRF score in hybrid mode; not comparable across queries. */
		public Double getScore() {
			return score;
		}

		public Map<String, Object> toMap() {
			Map<String, Object> out = new LinkedHashMap<>();
			out.put("resourceType", document.getResourceType());
			out.put("resourceUuid", document.getResourceUuid());
			if (document.getDate() != null) {
				out.put("date", document.getDate().toString());
			}
			out.put("snippet", document.getText());
			if (score != null) {
				out.put("score", score);
			}
			putIfPresent(out, "conceptUuid", document, FIELD_CONCEPT_UUID);
			putIfPresent(out, "conceptName", document, FIELD_CONCEPT_NAME);
			putIfPresent(out, "valueNumeric", document, FIELD_VALUE_NUMERIC);
			putIfPresent(out, "units", document, FIELD_UNITS);
			return out;
		}

		private static void putIfPresent(Map<String, Object> out, String key, QueryDocument doc, String field) {
			Object value = doc.getMetadata().get(field);
			if (value != null) {
				out.put(key, value);
			}
		}
	}

	/**
	 * Every document of one concept in the chart, represented by its newest. {@code conceptUuid} is
	 * {@code null} for a document that has no concept (an encounter, a visit), which forms a group of
	 * one.
	 */
	public static final class ConceptGroup {

		private final String conceptUuid;

		private final String conceptName;

		private final int count;

		private final Double score;

		private final Hit latest;

		private final List<QueryDocument> recent;

		/**
		 * @param score the best score among the group's matching members
		 * @param recent the group's newest documents, newest first; the first {@link #RECENT_VALUES}
		 *            are kept
		 */
		public ConceptGroup(String conceptUuid, String conceptName, int count, Double score, Hit latest,
		        List<QueryDocument> recent) {
			this.conceptUuid = conceptUuid;
			this.conceptName = conceptName;
			this.count = count;
			this.score = score;
			this.latest = latest;
			this.recent = Collections.unmodifiableList(
			    new ArrayList<>(recent.subList(0, Math.min(recent.size(), RECENT_VALUES))));
		}

		public String getConceptUuid() {
			return conceptUuid;
		}

		public String getConceptName() {
			return conceptName;
		}

		/** Documents of this concept in the chart (within the spec's types), not just the matches. */
		public int getCount() {
			return count;
		}

		public Double getScore() {
			return score;
		}

		public Hit getLatest() {
			return latest;
		}

		/** Up to {@link #RECENT_VALUES} newest documents, newest first. */
		public List<QueryDocument> getRecent() {
			return recent;
		}

		public Map<String, Object> toMap() {
			Map<String, Object> out = new LinkedHashMap<>();
			out.put("conceptUuid", conceptUuid);
			out.put("conceptName", conceptName);
			out.put("count", count);
			if (score != null) {
				out.put("score", score);
			}
			out.put("latest", latest.toMap());
			List<Object> values = new ArrayList<>(recent.size());
			for (QueryDocument d : recent) {
				Map<String, Object> point = new LinkedHashMap<>();
				point.put("date", d.getDate() == null ? null : d.getDate().toString());
				point.put("valueNumeric", d.getMetadata().get(FIELD_VALUE_NUMERIC));
				point.put("resourceUuid", d.getResourceUuid());
				values.add(point);
			}
			out.put("recentValues", values);
			return out;
		}
	}
}
