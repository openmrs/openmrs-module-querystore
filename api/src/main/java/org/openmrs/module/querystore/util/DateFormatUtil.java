/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Shared date formatting for clinical record serializers. Renders dates to ADR decision 7's
 * {@code yyyy-MM-dd} format, and timestamps to {@code yyyy-MM-dd'T'HH:mm:ss}, in the server's own zone
 * ({@link ZoneId#systemDefault()}), per ADR decision 19: OpenMRS stores a date and time without an
 * offset, in the zone the server runs in, so that zone's wall clock is what the record holds.
 * Rendered through UTC, a day entered as a date read one earlier on any server east of UTC. A
 * date-only value ({@code person.birthdate}, a {@code DATE} column JDBC materializes at the server's
 * midnight, possibly as a {@link java.sql.Date}) is the same case and takes the same method (#81).
 */
public final class DateFormatUtil {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	private DateFormatUtil() {
	}

	// Returns null on null input — caller decides whether to omit the field, set a placeholder, or
	// short-circuit text composition. Centralising the null-check here lets call sites drop the
	// redundant `date != null ? formatDate(date) : null` guard pattern.
	public static String formatDate(Date date) {
		if (date == null) {
			return null;
		}
		return inTheServersZone(date).toLocalDate().format(DATE_FORMAT);
	}

	public static String formatDateTime(Date date) {
		if (date == null) {
			return null;
		}
		return inTheServersZone(date).toLocalDateTime().format(DATE_TIME_FORMAT);
	}

	public static LocalDate toLocalDate(Date date) {
		if (date == null) {
			return null;
		}
		return inTheServersZone(date).toLocalDate();
	}

	/** {@code date} on the server's wall clock. Never {@code toInstant()}, which {@link java.sql.Date} throws on. */
	private static java.time.ZonedDateTime inTheServersZone(Date date) {
		return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault());
	}
}
