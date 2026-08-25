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

import static org.openmrs.module.querystore.QueryStoreConstants.DATE_KIND_UNKNOWN;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_CLINICAL_DATE;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DATE_KIND;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Canonical fingerprint for one complete, ordered patient-chart materialization. */
public final class PatientChartFingerprint {

	private PatientChartFingerprint() {
	}

	public static String snapshotId(List<QueryDocument> documents, boolean chartTruncated) {
		return snapshotId(documents, chartTruncated, true);
	}

	public static String snapshotId(List<QueryDocument> documents, boolean chartTruncated,
	        boolean projectionComplete) {
		StringBuilder canonical = new StringBuilder();
		appendValue(canonical, Boolean.valueOf(chartTruncated));
		appendValue(canonical, Boolean.valueOf(projectionComplete));
		for (QueryDocument document : documents) {
			appendValue(canonical, document.getResourceType());
			appendValue(canonical, document.getResourceUuid());
			appendValue(canonical, document.getDate() == null ? null : document.getDate().toString());
			appendValue(canonical, metadataString(document, FIELD_CLINICAL_DATE));
			String dateKind = metadataString(document, FIELD_DATE_KIND);
			appendValue(canonical, dateKind == null ? DATE_KIND_UNKNOWN : dateKind);
			appendValue(canonical, document.getText());
			appendValue(canonical,
			        document.getLastModified() == null ? null : document.getLastModified().toString());
			appendValue(canonical, document.getMetadata());
		}
		return sha256(canonical.toString());
	}

	private static String metadataString(QueryDocument document, String key) {
		Object value = document.getMetadata().get(key);
		return value instanceof String ? (String) value : null;
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
		if (value instanceof List) {
			out.append("[");
			for (Object child : (Collection<?>) value) {
				appendValue(out, child);
			}
			out.append("]");
			return;
		}
		if (value instanceof Collection) {
			List<String> children = new ArrayList<String>();
			for (Object child : (Collection<?>) value) {
				StringBuilder canonicalChild = new StringBuilder();
				appendValue(canonicalChild, child);
				children.add(canonicalChild.toString());
			}
			Collections.sort(children);
			out.append("[");
			for (String child : children) {
				out.append(child);
			}
			out.append("]");
			return;
		}
		String stringValue = String.valueOf(value);
		out.append(stringValue.length()).append(':').append(stringValue);
	}
}
