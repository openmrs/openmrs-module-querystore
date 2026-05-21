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

public final class WriteResult {

	/** The single success instance the {@link #success()} factory returns. Success carries no
	 *  state — a flyweight is safe and avoids ~one allocation per write across the bootstrap loop
	 *  and every per-row inner loop in the backend stores (Lucene/MySQL/ES each call
	 *  {@code success()} per record). The type is immutable (both fields final), so sharing a
	 *  single instance can't leak state across callers. */
	private static final WriteResult SUCCESS = new WriteResult(true, null);

	private final boolean succeeded;

	private final DocFailure failure;

	private WriteResult(boolean succeeded, DocFailure failure) {
		this.succeeded = succeeded;
		this.failure = failure;
	}

	public boolean isSucceeded() {
		return succeeded;
	}

	public DocFailure getFailure() {
		return failure;
	}

	public static WriteResult success() {
		return SUCCESS;
	}

	public static WriteResult failed(DocFailure failure) {
		return new WriteResult(false, failure);
	}
}
