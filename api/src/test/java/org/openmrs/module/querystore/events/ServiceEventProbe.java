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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.aop.event.SaveServiceEvent;
import org.openmrs.aop.event.VoidServiceEvent;
import org.springframework.context.event.EventListener;

/**
 * Test-only probe wired into context tests via {@code TestingApplicationContext.xml}. Settles two
 * facts the events consumer depends on, by observing core's #6084 events directly:
 *
 * <ul>
 *   <li>that a bean in querystore's context receives {@code SaveServiceEvent} from a core service
 *       save (see {@code CoreServiceEventTest}); and</li>
 *   <li>that the entity's {@code voided} flag is already set when {@code VoidServiceEvent}
 *       publishes — the consumer routes a void to a delete by reading that flag (purge=false), so if
 *       it were still false here the consumer would wrongly index a voided record.</li>
 * </ul>
 *
 * <p>Handlers are synchronous (the core advice publishes inside the transaction), so a test saves or
 * voids then asserts without waiting.
 */
public class ServiceEventProbe {

	private final List<Object> savedEntities = new CopyOnWriteArrayList<>();

	private final List<Boolean> voidedFlagAtVoidEvent = new CopyOnWriteArrayList<>();

	@EventListener
	public void onSave(SaveServiceEvent<?> event) {
		savedEntities.add(event.getEntity());
	}

	@EventListener
	public void onVoid(VoidServiceEvent<?> event) {
		Object entity = event.getEntity();
		voidedFlagAtVoidEvent.add(entity instanceof BaseOpenmrsData && ((BaseOpenmrsData) entity).getVoided());
	}

	public List<Object> savedEntities() {
		return savedEntities;
	}

	public List<Boolean> voidedFlagAtVoidEvent() {
		return voidedFlagAtVoidEvent;
	}
}
