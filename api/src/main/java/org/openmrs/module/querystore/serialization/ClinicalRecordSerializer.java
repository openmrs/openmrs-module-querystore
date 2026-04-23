package org.openmrs.module.querystore.serialization;

/**
 * Renders a clinical record of type {@code T} as the labeled plain text described in ADR decision 5.
 * Record timestamps are excluded (ADR decision 7) and concept names are resolved in the deployment's
 * configured locale (ADR decision 8).
 */
public interface ClinicalRecordSerializer<T> {

	String getResourceType();

	Class<T> getSupportedType();

	String serialize(T record);
}
