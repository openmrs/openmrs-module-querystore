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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Shared date formatting for clinical record serializers. Renders {@link Date} values to ADR
 * decision 7's {@code yyyy-MM-dd} format in UTC; the time-zone convention is tracked under the
 * Timestamp time-zone convention open question.
 */
public final class DateFormatUtil {

	private static final ZoneId UTC = ZoneOffset.UTC;

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
		return date.toInstant().atZone(UTC).toLocalDate().format(DATE_FORMAT);
	}

	public static String formatDateTime(Date date) {
		if (date == null) {
			return null;
		}
		return date.toInstant().atZone(UTC).toLocalDateTime().format(DATE_TIME_FORMAT);
	}

	public static LocalDate toLocalDate(Date date) {
		if (date == null) {
			return null;
		}
		return date.toInstant().atZone(UTC).toLocalDate();
	}
}
