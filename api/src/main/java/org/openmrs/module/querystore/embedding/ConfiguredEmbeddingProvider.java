/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.embedding;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.querystore.QueryStoreConstants;
import org.openmrs.module.querystore.SkipLogFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GP-driven dispatcher that resolves the active {@link EmbeddingProvider} from the OpenMRS
 * application context per ADR Decision 8. Reads {@link QueryStoreConstants#GP_EMBEDDING_PROVIDER_BEAN}
 * (cached until the GP value changes) and delegates to the named bean. This is the v1
 * simplification the ADR explicitly calls out — a future revision may replace bean-name lookup
 * with a qualifier annotation if the indirection becomes fragile.
 *
 * <p>Modules may register their own {@link EmbeddingProvider} beans, but selecting which one is
 * active is a deployment-level decision (only the deployment owns global properties), preserving
 * Decision 13's invariant that modules cannot pick the embedding model their text is embedded
 * with.
 */
public class ConfiguredEmbeddingProvider implements EmbeddingProvider {

	private static final Logger log = LoggerFactory.getLogger(ConfiguredEmbeddingProvider.class);

	private volatile String cachedBeanName;

	private volatile EmbeddingProvider cachedDelegate;

	@Override
	public int getDimensions() {
		return resolve().getDimensions();
	}

	@Override
	public float[] embed(String text) {
		return resolve().embed(text);
	}

	@Override
	public float[] embedQuery(String text) {
		return resolve().embedQuery(text);
	}

	@Override
	public List<float[]> embed(List<String> texts) {
		return resolve().embed(texts);
	}

	@Override
	public String getModelName() {
		return resolve().getModelName();
	}

	private EmbeddingProvider resolve() {
		String beanName = readConfiguredBeanName();
		EmbeddingProvider delegate = cachedDelegate;
		if (delegate != null && beanName.equals(cachedBeanName)) {
			return delegate;
		}
		EmbeddingProvider resolved = Context.getRegisteredComponent(beanName, EmbeddingProvider.class);
		log.info("Resolved embedding provider bean '{}' -> {}", beanName,
				resolved.getClass().getName());
		cachedBeanName = beanName;
		cachedDelegate = resolved;
		return resolved;
	}

	private static String readConfiguredBeanName() {
		try {
			String trimmed = StringUtils.trimToNull(Context.getAdministrationService()
					.getGlobalProperty(QueryStoreConstants.GP_EMBEDDING_PROVIDER_BEAN));
			if (trimmed != null) {
				return trimmed;
			}
		}
		catch (RuntimeException e) {
			log.warn("Failed to read {}, falling back to default '{}': {}",
					QueryStoreConstants.GP_EMBEDDING_PROVIDER_BEAN,
					QueryStoreConstants.DEFAULT_EMBEDDING_PROVIDER_BEAN, SkipLogFormat.scrubUuids(e.getMessage()));
		}
		return QueryStoreConstants.DEFAULT_EMBEDDING_PROVIDER_BEAN;
	}
}
