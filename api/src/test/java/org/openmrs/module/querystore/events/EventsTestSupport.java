/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;

/** Shared fixtures for the events-consumer unit tests. */
final class EventsTestSupport {

	private EventsTestSupport() {
	}

	/** A {@link SerializerRegistry} backed by exactly the given serializers, no context needed. */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static SerializerRegistry registryOf(ClinicalRecordSerializer... serializers) {
		final List<ClinicalRecordSerializer> list = new ArrayList<>(Arrays.asList(serializers));
		return new SerializerRegistry() {

			@Override
			List<ClinicalRecordSerializer> loadSerializers() {
				return list;
			}
		};
	}
}
