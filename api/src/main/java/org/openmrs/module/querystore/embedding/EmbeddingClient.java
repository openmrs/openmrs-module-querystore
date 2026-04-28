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

import java.util.ArrayList;
import java.util.List;

/**
 * Computes dense vector embeddings for serialized clinical text. Implementations must use a
 * multilingual model, per ADR decision 8.
 */
public interface EmbeddingClient {

	int getDimensions();

	float[] embed(String text);

	/**
	 * Embeds a batch of texts. Implementations should override to take advantage of bulk APIs
	 * where the underlying model supports them; the default delegates to {@link #embed(String)}
	 * sequentially. Backfill paths (see ADR open question on initial bootstrap) are the primary
	 * caller.
	 */
	default List<float[]> embed(List<String> texts) {
		List<float[]> out = new ArrayList<>(texts.size());
		for (String text : texts) {
			out.add(embed(text));
		}
		return out;
	}
}
