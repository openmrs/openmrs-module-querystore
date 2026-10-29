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

import static org.openmrs.module.querystore.QueryStoreConstants.DATE_KIND_UNKNOWN;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_CLINICAL_DATE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DATE_KIND;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.model.PatientChartFingerprint;

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
		m.put("clinicalDate", metadataString(doc, FIELD_CLINICAL_DATE));
		m.put("dateKind", dateKind(doc));
		m.put("lastModified", doc.getLastModified() == null ? null : doc.getLastModified().toString());
		m.put("text", doc.getText());
		m.put("metadata", doc.getMetadata());
		if (rank != null) {
			m.put("rank", rank);
		}
		return m;
	}

	/**
	 * The paged envelope {@code {results, totalCount, links}}, mirroring the OpenMRS {@code PageableResult}
	 * shape by hand. {@code totalCount} is the materialized count for a full-chart read; a backend-documented
	 * cap may omit older records. It is {@code null} for ranked (q-present) results, which are a top-K window
	 * with no browseable total. A {@code next} link is emitted when a known total has more records, or when an
	 * unknown-total page is full and the ranked window has remaining capacity. The final ranked page's
	 * limit is reduced to that remaining capacity; a {@code prev} link is emitted when {@code startIndex > 0}.
	 *
 * @param ranked whether these are q-ranked results (drives the per-row {@code rank} and the null totalCount)
 * @param endpointUrl the request URL, including the deployment's web-application context path
	 * @param baseParams the non-paging query params, already URL-encoded, ending in {@code &} (e.g. {@code "patient=x&q=y&"})
	 */
	static Map<String, Object> page(String endpointUrl, List<QueryDocument> docs, boolean ranked, int startIndex, int limit,
	        Integer totalCount, String baseParams, String snapshotId, Boolean chartTruncated,
	        Boolean projectionComplete, int maximumResultWindow) {
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>(docs.size());
		for (int i = 0; i < docs.size(); i++) {
			results.add(toMap(docs.get(i), ranked ? Integer.valueOf(startIndex + i + 1) : null));
		}
		Map<String, Object> env = new LinkedHashMap<String, Object>();
		env.put("results", results);
		env.put("totalCount", totalCount);
		if (snapshotId != null) {
			env.put("snapshotId", snapshotId);
		}
		if (chartTruncated != null) {
			env.put("chartTruncated", chartTruncated);
		}
		if (projectionComplete != null) {
			env.put("projectionComplete", projectionComplete);
		}

		List<Map<String, Object>> links = new ArrayList<Map<String, Object>>(2);
		if (startIndex > 0) {
			links.add(link("prev", endpointUrl, baseParams, Math.max(0, startIndex - limit), limit));
		}
		boolean canAdvance = startIndex <= Integer.MAX_VALUE - limit;
		boolean hasNext = canAdvance && (totalCount != null
		        ? startIndex + docs.size() < totalCount.intValue()
		        : docs.size() == limit);
		if (hasNext) {
			int nextStart = startIndex + limit;
			int nextLimit = ranked ? Math.min(limit, maximumResultWindow - nextStart) : limit;
			if (nextLimit > 0) {
				links.add(link("next", endpointUrl, baseParams, nextStart, nextLimit));
			}
		}
		if (!links.isEmpty()) {
			env.put("links", links);
		}
		return env;
	}

	/** Stable identity for an entire ordered chart, including completeness and canonical metadata. */
	static String snapshotId(List<QueryDocument> docs, boolean chartTruncated, boolean projectionComplete) {
		return PatientChartFingerprint.snapshotId(docs, chartTruncated, projectionComplete);
	}

	/** A page-specific strong ETag, derived from the complete snapshot and paging parameters. */
	static String pageEtag(String snapshotId, int startIndex, int limit) {
		return "\"" + sha256(snapshotId + "|" + startIndex + "|" + limit) + "\"";
	}

	private static String metadataString(QueryDocument doc, String key) {
		Object value = doc.getMetadata().get(key);
		return value instanceof String ? (String) value : null;
	}

	private static String dateKind(QueryDocument doc) {
		String value = metadataString(doc, FIELD_DATE_KIND);
		return value == null ? DATE_KIND_UNKNOWN : value;
	}

	private static String sha256(String value) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
			        .digest(value.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder(digest.length * 2);
			for (byte b : digest) {
				int unsignedByte = b & 0xff;
				hex.append(Character.forDigit(unsignedByte >>> 4, 16));
				hex.append(Character.forDigit(unsignedByte & 0x0f, 16));
			}
			return hex.toString();
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 is unavailable", e);
		}
	}

	private static Map<String, Object> link(String rel, String endpointUrl, String baseParams, int startIndex, int limit) {
		Map<String, Object> l = new LinkedHashMap<String, Object>();
		l.put("rel", rel);
		l.put("uri", endpointUrl + "?" + baseParams
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
