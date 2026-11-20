/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.bootstrap;

import java.util.Arrays;
import java.util.Collections;

import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.backend.BackendStore;
import org.openmrs.module.querystore.backend.BackendStoreSelector;
import org.openmrs.module.querystore.spi.ResourceTypeProvider;

/**
 * A real {@link BootstrapServiceImpl} for tests outside this package: its per-patient lock, delete
 * and projection are the production code's, over the given store and bootstrappers, with no module
 * providers and no Spring context.
 */
public final class RealBootstraps {

	private RealBootstraps() {
	}

	public static BootstrapServiceImpl create(QueryStoreService queryStore, BackendStore store,
	        TypeBootstrapper<?>... bootstrappers) {
		BootstrapServiceImpl service = new BootstrapServiceImpl();
		service.setQueryStoreService(queryStore);
		service.setBootstrappers(Arrays.<TypeBootstrapper<?>> asList(bootstrappers));
		service.setProvidersOverride(Collections.<ResourceTypeProvider> emptyList());
		service.setBackendSelector(new BackendStoreSelector(Collections.<String, BackendStore> emptyMap()) {

			@Override
			public BackendStore getStore() {
				return store;
			}
		});
		return service;
	}

	/** The lock the service's projections of this patient take. */
	public static Object patientLock(BootstrapServiceImpl service, String patientUuid) {
		return service.patientLock(patientUuid);
	}
}
