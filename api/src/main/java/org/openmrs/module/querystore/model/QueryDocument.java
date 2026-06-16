/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.model;

import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DESCRIPTION;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_MAPPING_NAMES;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_OBS_GROUP_CONCEPT_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_SYNONYMS;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A document written to the query store. Mirrors the three-component model from ADR decision 6:
 * plain-text chunk, dense-vector embedding, and structured metadata. {@code text} is the
 * citation-clean stored body — what BM25 indexes and what the LLM cites. The embedding input is
 * a separate, derived view from {@link #getEmbeddingInput()} that enriches {@code text} with
 * synonyms and group-concept context per ADR Decision 6. The two must not be conflated at write
 * time.
 *
 * <p>{@code lastModified} carries the source entity's "this projection is current as of" timestamp
 * (typically {@code dateChanged} falling back to {@code dateCreated}) so the backend can drop
 * stale writes when concurrent paths — bootstrap scan and the events sync pipeline — race on the
 * same record. Optional: when null, the backend falls back to last-write-wins.
 */
public class QueryDocument {

	private String patientUuid;

	private String resourceType;

	private String resourceUuid;

	private LocalDate date;

	private String text;

	private float[] embedding;

	private Instant lastModified;

	private final Map<String, Object> metadata = new LinkedHashMap<>();

	public String getPatientUuid() {
		return patientUuid;
	}

	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getResourceUuid() {
		return resourceUuid;
	}

	public void setResourceUuid(String resourceUuid) {
		this.resourceUuid = resourceUuid;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	/**
	 * Stored, citation-clean text body. This is NOT what the embedder receives — use
	 * {@link #getEmbeddingInput()} at write time for the enriched embedding input per ADR
	 * Decision 6's Synonyms-and-group-obs convention.
	 */
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public float[] getEmbedding() {
		return embedding;
	}

	public void setEmbedding(float[] embedding) {
		this.embedding = embedding;
	}

	public Instant getLastModified() {
		return lastModified;
	}

	public void setLastModified(Instant lastModified) {
		this.lastModified = lastModified;
	}

	public Map<String, Object> getMetadata() {
		return Collections.unmodifiableMap(metadata);
	}

	public QueryDocument putMetadata(String key, Object value) {
		metadata.put(key, value);
		return this;
	}

	/**
	 * The input the embedder should see for this document, derived from {@link #text} and the
	 * structured fields per ADR Decision 6's Synonyms-and-group-obs convention:
	 * {@code embed = [obs_group_concept_name + " — "] + text + [" " + synonyms.join(" ")]}
	 * (bracketed parts conditional on presence). Both write paths
	 * ({@code RecordIndexer}, {@code TypeBootstrapper}) call this so they produce identical vectors
	 * for the same source record, and so the embedding-input contract lives on the model rather
	 * than being re-derived at each write site.
	 *
	 * <p>Edge-case behavior: the group-concept prefix is only emitted when {@code text} is also
	 * present — a dangling separator with no body to prefix would add noise to the embedder. Empty
	 * synonym strings are filtered for the same reason.
	 */
	public String getEmbeddingInput() {
		String stored = text == null ? "" : text;
		StringBuilder sb = new StringBuilder();
		if (!stored.isEmpty()) {
			Object groupName = metadata.get(FIELD_OBS_GROUP_CONCEPT_NAME);
			if (groupName instanceof String && !((String) groupName).isEmpty()) {
				sb.append(groupName).append(" — ");
			}
			sb.append(stored);
		}
		String synonymsText = getSynonymsText();
		if (!synonymsText.isEmpty()) {
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(synonymsText);
		}
		return sb.toString();
	}

	/**
	 * Concept description string suitable for BM25 indexing as a top-level companion of
	 * {@link #text} (Lucene and Elasticsearch tiers). Returns the empty string when the
	 * {@code description} metadata is absent, not a String, or empty. Mirrors
	 * {@link #getSynonymsText()} so backends use one shape — not two inline {@code instanceof
	 * String} checks — for "BM25-shaped view of a concept-derived metadata field." Description
	 * is deliberately NOT included in {@link #getEmbeddingInput()} to avoid the asymmetric-bias
	 * concern documented on the embedding-input convention.
	 */
	public String getDescriptionText() {
		Object value = metadata.get(FIELD_DESCRIPTION);
		return value instanceof String ? (String) value : "";
	}

	/**
	 * Space-joined synonyms suitable for BM25 indexing as a top-level companion of {@link #text}
	 * (Lucene and Elasticsearch tiers per ADR Decision 3). Returns the empty string when the
	 * {@code synonyms} metadata is absent, not a List, or contains only null/empty entries.
	 * The filter rules match {@link #getEmbeddingInput()}'s synonym branch so both consumers
	 * see the same blob — empty strings dropped to avoid double-spaces in the indexed text.
	 */
	public String getSynonymsText() {
		return listToText(FIELD_SYNONYMS);
	}

	/**
	 * Space-joined reference-term mapping names suitable for BM25 indexing as a top-level
	 * companion of {@link #text} (Lucene and Elasticsearch tiers). Returns the empty string
	 * when the {@code mapping_names} metadata is absent, not a List, or contains only
	 * null/empty entries. Same shape as {@link #getSynonymsText()} so the Lucene serializer
	 * uses one filter pattern for both. Per Decision 6 this is NOT added to
	 * {@link #getEmbeddingInput()} — the per-record body is long and vocabulary-overlaps
	 * across related concepts (the same asymmetric-bias rule applied to description).
	 */
	public String getMappingNamesText() {
		return listToText(FIELD_MAPPING_NAMES);
	}

	/**
	 * Shared join behavior for the two BM25-companion list metadata fields ({@link #FIELD_SYNONYMS}
	 * and {@link #FIELD_MAPPING_NAMES}). Returns empty string when the field is absent or not a
	 * List; otherwise space-joins the contained Strings, skipping null and empty entries. Centralised
	 * so the synonyms-and-mapping-names BM25 channel can't drift in filter behavior — a fix or
	 * test contract change in one place applies to both consumers.
	 */
	private String listToText(String metadataKey) {
		Object value = metadata.get(metadataKey);
		if (!(value instanceof List)) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (Object item : (List<?>) value) {
			if (item instanceof String && !((String) item).isEmpty()) {
				if (sb.length() > 0) {
					sb.append(' ');
				}
				sb.append(item);
			}
		}
		return sb.toString();
	}
}
