/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.spi;

import org.openmrs.module.querystore.bootstrap.TypeBootstrapper;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;

/**
 * SPI for modules that provide custom resource types to the query store (ADR Decision 13).
 *
 * <p>A providing module — appointments, billing, radiology — packages a serializer producing the
 * cross-cutting document contract (ADR Decision 6) and an optional bootstrapper for initial backfill,
 * then declares the resource type name. Querystore discovers provider beans via
 * {@code Context.getRegisteredComponents(ResourceTypeProvider.class)} so a providing module only
 * needs to register its bean in its own {@code moduleApplicationContext.xml}; querystore does not
 * need to know about specific modules.
 *
 * <p><b>Indexing trigger is events-first.</b> querystore's sole sync path is
 * {@code CoreServiceEventListener}, which consumes core's #6084 {@code *ServiceEvent}s and projects
 * any entity for which a {@link org.openmrs.module.querystore.serialization.ClinicalRecordSerializer}
 * is registered. So a provider whose write service emits those events gets steady-state indexing for
 * free by registering a serializer — no querystore AOP advice (the migration bridge was removed; see
 * ADR Decision 12). Whether a module's service emits #6084 events is conditional and must be
 * verified at runtime (ADR Decision 13 §3: it must be an {@code OpenmrsService} with {@code save*}/
 * {@code void*}/… methods taking an {@code OpenmrsObject}, called externally); a service that doesn't
 * qualify contributes its own event listener or AOP shim. The {@code querystore.sync.indexer} and
 * {@code querystore.sync.dispatcher} beans remain reachable via {@code Context.getRegisteredComponent(...)}
 * for a provider that needs to drive the embed-then-upsert after-commit pipeline directly.
 *
 * <p><b>Name rule.</b> {@link #getResourceType()} must be {@code <moduleid>_<type>} per ADR
 * Decision 13 — e.g., {@code appointments_appointment}, {@code billing_bill}. Unprefixed names are
 * reserved for the types querystore itself indexes from core; querystore throws at startup if a
 * provider violates the rule.
 *
 * <p><b>Backends self-heal on first write.</b> All three reference backends (MySQL, Lucene,
 * Elasticsearch) call {@code ensureSchema} lazily on the first upsert per resource type. A
 * provider does not declare a {@link org.openmrs.module.querystore.backend.SchemaSpec} —
 * Decision 3 fixes the structured-field column as {@code metadata_json}, so providing modules
 * have no DDL knob to turn at v1.
 */
public interface ResourceTypeProvider {

	/** The provider's resource type name. Must match {@code <moduleid>_<type>}. */
	String getResourceType();

	/** Serializer producing the {@link org.openmrs.module.querystore.model.QueryDocument} for this
	 *  provider's records. Must populate the cross-cutting fields contract from ADR Decision 6
	 *  ({@code patient_uuid}, {@code resource_type}, {@code resource_uuid}, {@code last_modified},
	 *  {@code text}, encounter/visit/location/provider where applicable). */
	ClinicalRecordSerializer<?> getSerializer();

	/**
	 * Bootstrapper for initial backfill. May be {@code null} when the provider has no historical
	 * records to project (e.g., a type whose first record post-dates module install). When present,
	 * querystore appends it to its sequential bootstrap order — after all core types so a long-
	 * running core obs scan does not delay smaller provided types from being backfilled.
	 */
	TypeBootstrapper<?> getBootstrapper();
}
