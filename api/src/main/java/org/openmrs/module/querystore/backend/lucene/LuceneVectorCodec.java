/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.backend.lucene;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Raw little-endian float32 encoding for embedding bytes. Same shape as the MySQL backend's vector
 * codec — chartsearchai uses the identical encoding on its {@code chartsearchai_embedding}
 * column. The Lucene tier stores the bytes in a {@code StoredField} both for retrieval after a hit
 * and as the source the brute-force kNN scan iterates over (Lucene 8 ships no native HNSW kNN —
 * see ADR Decision 3 Lucene-tier consequences).
 */
final class LuceneVectorCodec {

	private LuceneVectorCodec() {
	}

	static byte[] encode(float[] vector) {
		if (vector == null) {
			return null;
		}
		ByteBuffer buf = ByteBuffer.allocate(vector.length * 4).order(ByteOrder.LITTLE_ENDIAN);
		buf.asFloatBuffer().put(vector);
		return buf.array();
	}

	/**
	 * Decode a {@code float[]} from a {@code byte[]} window — Lucene's {@code BytesRef} returns a
	 * backing array that can be longer than the actual stored content, so callers always pass the
	 * offset and length explicitly.
	 */
	static float[] decode(byte[] bytes, int offset, int length) {
		if (bytes == null) {
			return null;
		}
		float[] out = new float[length / 4];
		ByteBuffer.wrap(bytes, offset, length).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(out);
		return out;
	}

	/** L2 norm of a query vector. Cache once per kNN scan rather than per row. */
	static double norm(float[] vector) {
		if (vector == null) {
			return 0.0;
		}
		double sumSq = 0.0;
		for (float v : vector) {
			sumSq += v * v;
		}
		return Math.sqrt(sumSq);
	}

	/**
	 * Cosine similarity between {@code query} and a stored vector held in raw little-endian float32
	 * bytes. {@code queryNorm} must equal {@link #norm(float[])} of {@code query}; pre-computing it
	 * once per kNN scan rather than per row halves the multiply-adds across the candidate set and
	 * avoids materialising the stored vector as a {@code float[]} on the hot path. Mirrors the
	 * MySQL backend's same-named helper — same encoding, same per-row contract.
	 */
	static double cosineFromBytes(float[] query, double queryNorm, byte[] storedBytes, int offset, int length) {
		if (query == null || queryNorm == 0.0 || storedBytes == null || length / 4 != query.length) {
			return 0.0;
		}
		FloatBuffer stored = ByteBuffer.wrap(storedBytes, offset, length).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
		double dot = 0.0;
		double nb = 0.0;
		for (int i = 0; i < query.length; i++) {
			float b = stored.get(i);
			dot += query[i] * b;
			nb += b * b;
		}
		if (nb == 0.0) {
			return 0.0;
		}
		return dot / (queryNorm * Math.sqrt(nb));
	}
}
