/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.backend.elasticsearch;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.openmrs.api.context.Context;
import org.openmrs.module.querystore.QueryStoreConstants;
import org.openmrs.module.querystore.SafeLog;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

/**
 * Builds and owns the singleton {@link ElasticsearchClient} for the Elasticsearch backend. The
 * client is lazy: nothing connects at construction so non-ES deployments (the default {@code mysql}
 * tier) can still load this bean at Spring startup without an ES cluster reachable.
 *
 * <p>Endpoint resolution: the {@code querystore.elasticsearch.uri} key in
 * {@code openmrs-runtime.properties} (see {@link QueryStoreConstants#RP_ELASTICSEARCH_URI}). Lives
 * in runtime properties rather than a global property because the URI may carry credentials, and
 * GPs are stored plaintext in the database and visible in the admin UI. Mirrors chartsearchai's
 * use of {@code hibernate.search.backend.uris} from the same file.
 *
 * <p>Tests bypass the runtime-property lookup by passing the URI directly via the override
 * constructor.
 */
public class ElasticsearchClientFactory implements Closeable {

	private static final Log log = LogFactory.getLog(ElasticsearchClientFactory.class);

	private final String overrideUri;

	private final Object lock = new Object();

	private volatile RestClient restClient;

	private volatile ElasticsearchClient client;

	/** Spring-friendly constructor: URI is resolved lazily from runtime properties on first call. */
	public ElasticsearchClientFactory() {
		this(null);
	}

	/** Test-friendly constructor: bypasses runtime-property lookup. */
	public ElasticsearchClientFactory(String overrideUri) {
		this.overrideUri = overrideUri;
	}

	/**
	 * Returns the {@link ElasticsearchClient}, connecting on the first call. Throws if the URI
	 * cannot be resolved — callers should only reach this method when the ES backend has been
	 * selected, so an unconfigured URI at that point is a deployment error worth surfacing.
	 */
	public ElasticsearchClient getClient() {
		ElasticsearchClient local = client;
		if (local != null) {
			return local;
		}
		synchronized (lock) {
			if (client == null) {
				String uri = overrideUri != null ? overrideUri : resolveUri();
				if (uri == null || uri.isEmpty()) {
					throw new IllegalStateException("Elasticsearch backend selected but "
					        + QueryStoreConstants.RP_ELASTICSEARCH_URI
					        + " is not set in openmrs-runtime.properties");
				}
				HttpHost host = HttpHost.create(uri);
				restClient = RestClient.builder(host).build();
				ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
				client = new ElasticsearchClient(transport);
			}
			return client;
		}
	}

	private static String resolveUri() {
		try {
			Properties props = Context.getRuntimeProperties();
			if (props != null) {
				String value = props.getProperty(QueryStoreConstants.RP_ELASTICSEARCH_URI);
				if (value != null) {
					String trimmed = value.trim();
					return trimmed.isEmpty() ? null : trimmed;
				}
			}
		}
		catch (RuntimeException e) {
			SafeLog.warn(ElasticsearchClientFactory.class, "Could not read " + QueryStoreConstants.RP_ELASTICSEARCH_URI + " runtime property", e);
		}
		return null;
	}

	@Override
	public void close() {
		synchronized (lock) {
			if (restClient != null) {
				try {
					restClient.close();
				}
				catch (IOException e) {
					SafeLog.warn(ElasticsearchClientFactory.class, "Could not close Elasticsearch REST client", e);
				}
				restClient = null;
				client = null;
			}
		}
	}
}
