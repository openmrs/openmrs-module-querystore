/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore;

import org.openmrs.module.querystore.backend.DocFailure;

/**
 * Single source of truth for the per-record skip log line emitted whenever a write does not land
 * (bootstrap and sync alike). The line is grep-able by the {@code [querystore-skip]} token and
 * carries {@code key=value} fields so an operator (or log-shipper) can extract the per-record
 * context — layer, resource type, resource uuid, retryable hint, and the backend's failure
 * message — without parsing prose.
 *
 * <p>This is the deliberately minimal alternative to a durable failures table (see fix #3 scope
 * discussion in the commit history): no schema, no API surface, no automated retry. Operators
 * recovering from a transient backend incident can still locate the affected records by grepping
 * {@code openmrs.log} for {@code [querystore-skip]}, filter by {@code retryable=true}, and feed
 * the resulting uuids back through whatever re-projection path they have available (today: delete
 * the row from {@code querystore_bootstrap_progress} and restart so the next bootstrap re-walks).
 */
public final class SkipLogFormat {

	/**
	 * Operator-facing grep tag every skip line starts with. External log shippers / alerting rules
	 * match on this literal; treat it as a stable token across releases. The
	 * {@code skipTagConstantMatchesEmittedPrefix} test pins this to the emitted prefix so a
	 * mismatched edit between the constant and the format string surfaces in CI.
	 */
	public static final String LOG_SKIP_TAG = "[querystore-skip]";

	private SkipLogFormat() {
	}

	/**
	 * Convenience overload for the WriteResult-failed call sites that already hold a {@link
	 * DocFailure}. Reads resource type, resource uuid, retryable hint, and error message off the
	 * failure object so the call sites don't repeat the {@code f != null ? f.getX() : null}
	 * ternary pair. The catch-RuntimeException site has no DocFailure and uses the explicit-args
	 * overload below.
	 *
	 * @param layer "bootstrap" or "sync"
	 * @param f     the backend's per-doc failure; may be null when the WriteResult was failed but
	 *              carried no DocFailure (the {@code WriteResult.failed(null)} edge case is
	 *              theoretically possible per the API)
	 */
	public static String format(String layer, DocFailure f) {
		if (f == null) {
			return format(layer, null, null, null, null);
		}
		return format(layer, f.getResourceType(), f.getResourceUuid(),
		        f.isRetryable(), f.getErrorMessage());
	}

	/**
	 * Builds the skip-log message body. The returned string is intended to be passed verbatim to
	 * {@code log.warn(...)}; the layout adds the timestamp and class context.
	 *
	 * @param layer "bootstrap" or "sync" — names the write path that observed the failure
	 * @param resourceType the document's resource type (e.g. "condition")
	 * @param resourceUuid the document's resource uuid; may be null on degenerate inputs
	 * @param retryable    the backend's retryable hint, or null when the path doesn't have one
	 *                     (e.g. a catch-everything RuntimeException that didn't surface a
	 *                     {@link DocFailure}). <b>Do not pass {@code Boolean.FALSE} to mean
	 *                     "unknown"</b>: {@code false} renders as the backend's explicit
	 *                     non-retryable verdict, which downstream alerting rules treat
	 *                     differently from {@code unknown}.
	 * @param reason       the backend's error message or the thrown exception's message; null
	 *                     renders as {@code "no failure detail"} so the parser still sees a value
	 */
	public static String format(String layer, String resourceType, String resourceUuid,
	                            Boolean retryable, String reason) {
		StringBuilder sb = new StringBuilder(128);
		sb.append(LOG_SKIP_TAG)
		        .append(" layer=").append(layer)
		        .append(" type=").append(resourceType)
		        .append(" uuid=").append(resourceUuid)
		        .append(" retryable=").append(retryable == null ? "unknown" : retryable.toString())
		        .append(" reason=\"").append(sanitizeReason(reason))
		        .append('"');
		return sb.toString();
	}

	/**
	 * Keeps each emitted skip event on one log4j entry with balanced quoting. Stack traces ride
	 * the {@code Throwable} argument to {@code log.warn(msg, t)} unaffected.
	 */
	private static String sanitizeReason(String reason) {
		if (reason == null) {
			return "no failure detail";
		}
		return reason.replace('\n', ' ').replace('\r', ' ').replace('"', '\'');
	}
}
