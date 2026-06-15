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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.openmrs.module.querystore.events.EventsTestSupport.registryOf;

import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.module.querystore.serialization.EncounterRecordSerializer;
import org.openmrs.module.querystore.serialization.ObsRecordSerializer;

public class SerializerRegistryTest {

	@Test
	public void resolve_matchesEntityToItsSerializerByType() {
		ObsRecordSerializer obs = new ObsRecordSerializer();
		EncounterRecordSerializer encounter = new EncounterRecordSerializer();
		SerializerRegistry registry = registryOf(obs, encounter);

		assertSame(obs, registry.resolve(new Obs()));
		assertSame(encounter, registry.resolve(new Encounter()));
	}

	@Test
	public void resolve_unindexedType_returnsNull() {
		SerializerRegistry registry = registryOf(new ObsRecordSerializer());

		assertNull("no serializer covers Encounter in this registry", registry.resolve(new Encounter()));
	}
}
