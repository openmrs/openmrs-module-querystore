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
	 * shape by hand. {@code totalCount} is the true count for a full chart; it is {@code null} for ranked
	 * (q-present) results, which are a top-K window with no browseable total. A {@code next} link is emitted
	 * when the page is full (possibly more); a {@code prev} link when {@code startIndex > 0}.
	 *
	 * @param ranked whether these are q-ranked results (drives the per-row {@code rank} and the null totalCount)
	 * @param baseParams the non-paging query params, already URL-encoded, ending in {@code &} (e.g. {@code "patient=x&q=y&"})
	 */
	static Map<String, Object> page(List<QueryDocument> docs, boolean ranked, int startIndex, int limit,
	        Integer totalCount, String baseParams) {
		return page(docs, ranked, startIndex, limit, totalCount, baseParams, null);
	}

	static Map<String, Object> page(List<QueryDocument> docs, boolean ranked, int startIndex, int limit,
	        Integer totalCount, String baseParams, String snapshotId) {
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

	/**
	 * The context-slice envelope (ADR Decision 17 §4): {@code {results (each with tier),
	 * totalCount, chartSize, chartTruncated}}, paged in memory. Slices claim no stable chart
	 * snapshot, so there is no {@code snapshotId} and no ETag participation.
	 */
	static Map<String, Object> contextPage(org.openmrs.module.querystore.model.ContextSlice slice,
	        int startIndex, int limit) {
		List<org.openmrs.module.querystore.model.ContextSliceRecord> all = slice.getRecords();
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		for (int i = startIndex; i < all.size() && i < startIndex + limit; i++) {
			Map<String, Object> m = toMap(all.get(i).getDocument(), null);
			m.put("tier", all.get(i).getTier());
			results.add(m);
		}
		Map<String, Object> env = new LinkedHashMap<String, Object>();
		env.put("results", results);
		env.put("totalCount", Integer.valueOf(all.size()));
		env.put("chartSize", Integer.valueOf(slice.getChartSize()));
		env.put("chartTruncated", Boolean.valueOf(slice.isChartTruncated()));
		return env;
	}

	/** Stable identity for an entire ordered chart, including date semantics and canonical metadata. */
	static String snapshotId(List<QueryDocument> docs) {
		StringBuilder canonical = new StringBuilder();
		for (QueryDocument doc : docs) {
			appendValue(canonical, doc.getResourceType());
			appendValue(canonical, doc.getResourceUuid());
			appendValue(canonical, doc.getDate() == null ? null : doc.getDate().toString());
			appendValue(canonical, metadataString(doc, FIELD_CLINICAL_DATE));
			appendValue(canonical, dateKind(doc));
			appendValue(canonical, doc.getText());
			appendValue(canonical, doc.getLastModified() == null ? null : doc.getLastModified().toString());
			appendValue(canonical, doc.getMetadata());
		}
		return sha256(canonical.toString());
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

	@SuppressWarnings("unchecked")
	private static void appendValue(StringBuilder out, Object value) {
		if (value == null) {
			out.append("-1:");
			return;
		}
		if (value instanceof Map) {
			out.append("{");
			Map<String, Object> sorted = new TreeMap<String, Object>();
			for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
				sorted.put(String.valueOf(entry.getKey()), entry.getValue());
			}
			for (Map.Entry<String, Object> entry : sorted.entrySet()) {
				appendValue(out, entry.getKey());
				appendValue(out, entry.getValue());
			}
			out.append("}");
			return;
		}
		if (value instanceof Collection) {
			out.append("[");
			List<Object> items = new ArrayList<Object>((Collection<Object>) value);
			if (!(value instanceof List)) {
				Collections.sort(items, (left, right) -> canonicalValue(left).compareTo(canonicalValue(right)));
			}
			for (Object item : items) {
				appendValue(out, item);
			}
			out.append("]");
			return;
		}
		String text = String.valueOf(value);
		out.append(text.length()).append(':').append(text);
	}

	private static String canonicalValue(Object value) {
		StringBuilder out = new StringBuilder();
		appendValue(out, value);
		return out.toString();
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
