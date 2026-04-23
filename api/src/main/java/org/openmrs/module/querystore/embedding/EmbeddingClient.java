package org.openmrs.module.querystore.embedding;

/**
 * Computes dense vector embeddings for serialized clinical text. Implementations must use a
 * multilingual model, per ADR decision 8.
 */
public interface EmbeddingClient {

	int getDimensions();

	float[] embed(String text);
}
