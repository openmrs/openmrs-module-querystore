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

import org.apache.commons.lang3.StringUtils;
import org.openmrs.module.querystore.QueryStoreConstants;
import org.openmrs.module.querystore.model.QueryDocument;

/**
 * Cross-tier helpers shared by every {@link BackendStore} implementation. Centralises the rules
 * that every tier must apply identically — input validation, the scalar-metadata filter
 * eligibility predicate, and the {@code querystore_<type>} ↔ {@code type} prefix conversion — so a
 * future SPI change (e.g. tightening identity requirements, expanding filterable metadata types)
 * lands in one place rather than three.
 */
public final class BackendDocs {

	private BackendDocs() {
	}

	/**
	 * Fail-fast identity check applied by every backend before any write hits storage. Throws on
	 * the first invariant violation; matches the SPI's "bulk writes are all-or-nothing on input
	 * validity" contract.
	 */
	public static void validate(QueryDocument doc) {
		if (doc == null || doc.getResourceType() == null || doc.getResourceUuid() == null
		        || doc.getPatientUuid() == null) {
			throw new IllegalArgumentException(
			        "QueryDocument must have resourceType, resourceUuid, and patientUuid");
		}
	}

	/**
	 * Whether a metadata value is eligible for the per-key indexed filter companion. v1 supports
	 * scalars only ({@code String|Number|Boolean}); collections and maps stay in the JSON blob and
	 * are not directly filterable. The same rule applies on every tier — diverging here would mean
	 * {@code Filter.term("concept_uuid", "X")} silently works on one backend and not another.
	 */
	public static boolean isFilterableScalar(Object value) {
		return value instanceof String || value instanceof Number || value instanceof Boolean;
	}

	/** Strips the {@code querystore_} prefix off a per-type index/table name. */
	public static String stripPrefix(String prefixed) {
		return StringUtils.removeStart(prefixed, QueryStoreConstants.INDEX_PREFIX);
	}
}
