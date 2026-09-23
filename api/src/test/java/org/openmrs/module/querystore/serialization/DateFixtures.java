/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.serialization;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Shared date builders for serializer unit tests. {@link #utcDate} fixes the UTC zone and the
 * mid-day hour so the resulting {@link Date} is stable regardless of the host JVM's default zone
 * — without this, tests that assert {@code DateFormatUtil.toLocalDate} output drift across CI
 * runners west of UTC. Static-import from test classes.
 */
final class DateFixtures {

	private DateFixtures() {
	}

	static Date utcDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.set(year, month, day, 12, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/**
	 * A date-only value in the shape Hibernate materializes a {@code DATE} column: a
	 * {@link Timestamp} at midnight in the JVM default zone. Use it for fields rendered through
	 * {@code DateFormatUtil.formatCalendarDate}, which reads the date back in that zone — a
	 * {@link #utcDate} instant renders as the next day on a host at UTC+12 or beyond.
	 */
	static Date localMidnight(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(year, month, day);
		return new Timestamp(cal.getTimeInMillis());
	}
}
