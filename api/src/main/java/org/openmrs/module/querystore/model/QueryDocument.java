package org.openmrs.module.querystore.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A document written to the query store. Mirrors the three-component model from ADR decision 6:
 * plain-text chunk, dense-vector embedding, and structured metadata.
 */
public class QueryDocument {

	private String patientUuid;

	private String resourceType;

	private String resourceUuid;

	private LocalDate date;

	private String text;

	private float[] embedding;

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

	public Map<String, Object> getMetadata() {
		return Collections.unmodifiableMap(metadata);
	}

	public QueryDocument putMetadata(String key, Object value) {
		metadata.put(key, value);
		return this;
	}
}
