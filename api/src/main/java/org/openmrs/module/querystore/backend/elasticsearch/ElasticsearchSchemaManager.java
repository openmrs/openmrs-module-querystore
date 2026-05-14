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

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.querystore.QueryStoreConstants;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.mapping.DenseVectorIndexOptions;
import co.elastic.clients.elasticsearch._types.mapping.DynamicMapping;
import co.elastic.clients.elasticsearch._types.mapping.DynamicTemplate;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import co.elastic.clients.elasticsearch.indices.IndexState;

/**
 * Owns per-resource-type Elasticsearch indices. Idempotent {@link #ensureIndex(String, int)}
 * matches the SPI's "ensureSchema is a no-op if already created" contract; the embedding-dimension
 * check on cache hits and on the first ES round-trip surfaces model swaps loudly rather than
 * silently producing dim-mismatch errors on subsequent writes (Decision 8 treats embedding-model
 * swaps as re-index events).
 */
final class ElasticsearchSchemaManager {

	private static final Log log = LogFactory.getLog(ElasticsearchSchemaManager.class);

	private final ElasticsearchClientFactory clientFactory;

	/** Cache of {@code querystore_<type>} → embedding dim, primed lazily on first ensure/list. */
	private final ConcurrentMap<String, Integer> knownDims = new ConcurrentHashMap<>();

	ElasticsearchSchemaManager(ElasticsearchClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	void ensureIndex(String resourceType, int embeddingDims) {
		String name = indexName(resourceType);
		Integer cached = knownDims.get(name);
		if (cached != null) {
			assertDimsMatch(name, cached, embeddingDims);
			return;
		}
		ElasticsearchClient client = clientFactory.getClient();
		try {
			boolean exists = client.indices().exists(e -> e.index(name)).value();
			if (exists) {
				Integer remoteDims = readEmbeddingDims(client, name);
				if (remoteDims != null) {
					assertDimsMatch(name, remoteDims, embeddingDims);
					knownDims.put(name, remoteDims);
				} else {
					// Index exists without an embedding mapping (e.g. created out-of-band). Treat as
					// "we don't manage this" — surface clearly rather than write into it.
					throw new IllegalStateException("Index " + name
					        + " exists but has no dense_vector 'embedding' field; refusing to use");
				}
				return;
			}
			createIndex(client, name, embeddingDims);
			knownDims.put(name, embeddingDims);
		}
		catch (ElasticsearchException | IOException e) {
			throw new IllegalStateException("Could not ensure Elasticsearch index " + name, e);
		}
	}

	void deleteIndex(String resourceType) {
		String name = indexName(resourceType);
		ElasticsearchClient client = clientFactory.getClient();
		try {
			client.indices().delete(d -> d.index(name).ignoreUnavailable(true));
		}
		catch (ElasticsearchException e) {
			// Best-effort: index_not_found is the only case worth swallowing; everything else is a
			// real failure the caller deserves to see.
			if (!"index_not_found_exception".equals(e.error().type())) {
				throw new IllegalStateException("Could not delete Elasticsearch index " + name, e);
			}
		}
		catch (IOException e) {
			throw new IllegalStateException("Could not delete Elasticsearch index " + name, e);
		}
		knownDims.remove(name);
	}

	Set<String> knownIndexNames() {
		return new HashSet<>(knownDims.keySet());
	}

	/**
	 * Enumerates {@code querystore_*} indices on the cluster. Side-effect: any discovered index is
	 * primed into the dim cache for subsequent {@link #ensureIndex(String, int)} hits.
	 */
	Set<String> listAllIndexes() {
		ElasticsearchClient client = clientFactory.getClient();
		try {
			GetIndexResponse resp = client.indices().get(g -> g.index(QueryStoreConstants.INDEX_PREFIX + "*")
			        .allowNoIndices(true).ignoreUnavailable(true));
			Set<String> names = new HashSet<>(resp.result().keySet());
			for (Map.Entry<String, IndexState> entry : resp.result().entrySet()) {
				Integer dims = readEmbeddingDimsFromState(entry.getValue());
				if (dims != null) {
					knownDims.put(entry.getKey(), dims);
				}
			}
			return names;
		}
		catch (ElasticsearchException | IOException e) {
			throw new IllegalStateException("Could not enumerate Elasticsearch indexes under "
			        + QueryStoreConstants.INDEX_PREFIX + "*", e);
		}
	}

	static String indexName(String resourceType) {
		if (resourceType == null || !resourceType.matches("[a-z][a-z0-9_]*")) {
			throw new IllegalArgumentException(
			        "Invalid resource type (must match [a-z][a-z0-9_]*): " + resourceType);
		}
		return QueryStoreConstants.INDEX_PREFIX + resourceType;
	}

	private void createIndex(ElasticsearchClient client, String name, int embeddingDims) throws IOException {
		TypeMapping mapping = buildMapping(embeddingDims);
		client.indices().create(c -> c.index(name).mappings(mapping));
		log.info("Created Elasticsearch index " + name + " (embedding dims=" + embeddingDims + ")");
	}

	private static TypeMapping buildMapping(int embeddingDims) {
		Map<String, Property> properties = new LinkedHashMap<>();
		properties.put(ElasticsearchFieldNames.RESOURCE_UUID, Property.of(p -> p.keyword(k -> k)));
		properties.put(ElasticsearchFieldNames.PATIENT_UUID, Property.of(p -> p.keyword(k -> k)));
		properties.put(ElasticsearchFieldNames.RECORD_DATE, Property.of(p -> p.date(d -> d.format("yyyy-MM-dd"))));
		properties.put(ElasticsearchFieldNames.TEXT, Property.of(p -> p.text(t -> t.analyzer("standard"))));
		properties.put(ElasticsearchFieldNames.EMBEDDING, Property.of(p -> p.denseVector(v -> v
		        .dims(embeddingDims)
		        .index(true)
		        .similarity("cosine")
		        .indexOptions(DenseVectorIndexOptions.of(o -> o.type("hnsw").m(16).efConstruction(100))))));
		// Stored-only blob: not searched, not filtered, only retrieved on hit to round-trip metadata.
		properties.put(ElasticsearchFieldNames.METADATA_JSON,
		    Property.of(p -> p.keyword(k -> k.index(false))));
		properties.put(ElasticsearchFieldNames.LAST_MODIFIED, Property.of(p -> p.date(d -> d)));
		// META_PARENT holds per-key companion fields populated alongside the JSON blob. Top level is
		// dynamic=false so spurious keys are rejected; this subtree is dynamic=true so the
		// dynamic_template below maps each {@code meta.<key>} as keyword without per-key DDL work.
		properties.put(ElasticsearchFieldNames.META_PARENT,
		    Property.of(p -> p.object(o -> o.dynamic(DynamicMapping.True))));

		Map<String, DynamicTemplate> metaKeywords = Collections.singletonMap("meta_keywords",
		    DynamicTemplate.of(t -> t.pathMatch(ElasticsearchFieldNames.META_PREFIX + "*")
		            .mapping(m -> m.keyword(k -> k))));

		return TypeMapping.of(b -> b
		        .dynamic(DynamicMapping.False)
		        .dynamicTemplates(Collections.singletonList(metaKeywords))
		        .properties(properties));
	}

	private static Integer readEmbeddingDims(ElasticsearchClient client, String name) throws IOException {
		GetIndexResponse resp = client.indices().get(g -> g.index(name));
		IndexState state = resp.result().get(name);
		return readEmbeddingDimsFromState(state);
	}

	private static Integer readEmbeddingDimsFromState(IndexState state) {
		if (state == null || state.mappings() == null) {
			return null;
		}
		Property embedding = state.mappings().properties().get(ElasticsearchFieldNames.EMBEDDING);
		if (embedding == null || !embedding.isDenseVector()) {
			return null;
		}
		return embedding.denseVector().dims();
	}

	private static void assertDimsMatch(String name, int existing, int requested) {
		if (existing != requested) {
			// Loud failure on dim mismatch: Decision 8 treats embedding-model swaps as re-index
			// events. Silently writing into a stale-dim index would produce subtler downstream
			// failures (vector-size mismatch on write, opaque recall regressions). Fail here so the
			// operator runs the re-index path.
			throw new IllegalStateException(
			        "Index " + name + " has embedding dims=" + existing + " but caller requested " + requested
			                + "; embedding model changed? Re-index required");
		}
	}
}
