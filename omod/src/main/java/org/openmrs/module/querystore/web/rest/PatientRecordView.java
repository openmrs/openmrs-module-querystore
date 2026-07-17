/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.web.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.webservices.rest.web.RestConstants;

/**
 * Maps {@link QueryDocument}s to the {@code /querystore/patientrecord} REST response (ADR Decision 16).
 * The response shape lives here, unit-tested, so the controller stays a thin adapter — mirroring the
 * {@link org.openmrs.module.querystore.bootstrap.BootstrapStatusReport#toMap()} convention the
 * operational endpoints use.
 *
 * <p>The {@code embedding} vector is intentionally never serialized (backend infrastructure — ADR
 * Decision 3), and no {@code score} is emitted (the service discards the backend's per-hit score, so
 * relevance is conveyed only by list order plus a 1-based {@code rank} on ranked results).
 */
final class PatientRecordView {

	private PatientRecordView() {
	}

	/** One record. {@code rank} is the 1-based position on ranked (q-present) results, else {@code null}. */
	static Map<String, Object> toMap(QueryDocument doc, Integer rank) {
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		m.put("resourceType", doc.getResourceType());
		m.put("resourceUuid", doc.getResourceUuid());
		m.put("date", doc.getDate() == null ? null : doc.getDate().toString());
		m.put("text", doc.getText());
		m.put("metadata", doc.getMetadata());
		if (rank != null) {
			m.put("rank", rank);
		}
		return m;
	}

	/**
	 * The paged envelope {@code {results, totalCount, links}}, mirroring the OpenMRS {@code PageableResult}
	 * shape by hand. {@code totalCount} is the true count for a full chart; it is {@code null} for ranked
	 * (q-present) results, which are a top-K window with no browseable total. A {@code next} link is emitted
	 * when the page is full (possibly more); a {@code prev} link when {@code startIndex > 0}.
	 *
	 * @param ranked whether these are q-ranked results (drives the per-row {@code rank} and the null totalCount)
	 * @param baseParams the non-paging query params, already URL-encoded, ending in {@code &} (e.g. {@code "patient=x&q=y&"})
	 */
	static Map<String, Object> page(List<QueryDocument> docs, boolean ranked, int startIndex, int limit,
	        Integer totalCount, String baseParams) {
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>(docs.size());
		for (int i = 0; i < docs.size(); i++) {
			results.add(toMap(docs.get(i), ranked ? Integer.valueOf(startIndex + i + 1) : null));
		}
		Map<String, Object> env = new LinkedHashMap<String, Object>();
		env.put("results", results);
		env.put("totalCount", totalCount);

		List<Map<String, Object>> links = new ArrayList<Map<String, Object>>(2);
		if (startIndex > 0) {
			links.add(link("prev", baseParams, Math.max(0, startIndex - limit), limit));
		}
		if (docs.size() == limit) {
			links.add(link("next", baseParams, startIndex + limit, limit));
		}
		if (!links.isEmpty()) {
			env.put("links", links);
		}
		return env;
	}

	private static Map<String, Object> link(String rel, String baseParams, int startIndex, int limit) {
		Map<String, Object> l = new LinkedHashMap<String, Object>();
		l.put("rel", rel);
		l.put("uri", "/ws/rest/" + RestConstants.VERSION_1 + "/querystore/patientrecord?" + baseParams
		        + "startIndex=" + startIndex + "&limit=" + limit);
		return l;
	}

	/** URL-encodes a single query-param value (UTF-8) for the prev/next link uris. */
	static String encode(String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 unavailable", e); // unreachable on any JVM
		}
	}
}
