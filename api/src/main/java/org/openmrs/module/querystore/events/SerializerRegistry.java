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

import org.openmrs.BaseOpenmrsData;
import org.openmrs.api.context.Context;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;

/**
 * Resolves the {@link ClinicalRecordSerializer} for a given entity by type, for the events consumer
 * (the AOP bridge resolves its serializer by a hard-coded bean id per advice instead). Matches on
 * {@link ClinicalRecordSerializer#getSupportedType()}{@code .isInstance(entity)} — robust to
 * Hibernate proxies, and unambiguous for the indexed type set (each serializer covers a concrete
 * type; an entity matches at most one). Picks up SPI-contributed serializers (ADR Decision 13) for
 * free since they are registered components like the core ones.
 *
 * <p>The serializer list is read from the Spring context once and cached: serializers (core and
 * SPI) are all registered at module load, before any clinical save can fire an event, so the cache
 * is stable and keeps the per-event resolve off the bean-lookup path.
 */
public class SerializerRegistry {

	// Raw element type throughout: Context.getRegisteredComponents(ClinicalRecordSerializer.class)
	// is typed to the raw Class token and returns a raw-element list. getSupportedType() works on the
	// raw reference, and resolve() casts the matched serializer to the caller's BaseOpenmrsData view.
	@SuppressWarnings("rawtypes")
	private volatile List<ClinicalRecordSerializer> cached;

	/**
	 * The serializer whose supported type the entity is an instance of, or {@code null} if no
	 * registered serializer handles it (i.e. the entity's type is not indexed). Typed to
	 * {@link BaseOpenmrsData} to fit
	 * {@link org.openmrs.module.querystore.bridge.RecordProjector#project}; callers pass only
	 * {@code BaseOpenmrsData} entities (every indexed type is data, not metadata), so the cast holds.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ClinicalRecordSerializer<BaseOpenmrsData> resolve(BaseOpenmrsData entity) {
		for (ClinicalRecordSerializer serializer : serializers()) {
			if (serializer.getSupportedType().isInstance(entity)) {
				return serializer;
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private List<ClinicalRecordSerializer> serializers() {
		List<ClinicalRecordSerializer> local = cached;
		if (local == null) {
			local = loadSerializers();
			cached = local;
		}
		return local;
	}

	/** Visible-for-testing seam over the {@link Context#getRegisteredComponents} lookup. */
	@SuppressWarnings("rawtypes")
	List<ClinicalRecordSerializer> loadSerializers() {
		return Context.getRegisteredComponents(ClinicalRecordSerializer.class);
	}
}
