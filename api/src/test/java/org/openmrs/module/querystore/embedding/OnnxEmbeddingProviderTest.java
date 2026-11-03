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

import org.junit.Test;

public class OnnxEmbeddingProviderTest {

	// The model paths need not exist: a closed provider must refuse before it resolves or loads
	// anything, since loading is what would reopen the session (and its CoreML files) after a stop.

	@Test(expected = EmbeddingProviderClosedException.class)
	public void embed_afterClose_throwsInsteadOfReopeningTheSession() {
		OnnxEmbeddingProvider provider = new OnnxEmbeddingProvider("missing-model.onnx", "missing-vocab.txt");
		provider.close();
		provider.embed("hello");
	}

	@Test(expected = EmbeddingProviderClosedException.class)
	public void embedQuery_afterClose_throwsInsteadOfReopeningTheSession() {
		OnnxEmbeddingProvider provider = new OnnxEmbeddingProvider("missing-model.onnx", "missing-query.onnx",
				"missing-vocab.txt");
		provider.close();
		provider.embedQuery("hello");
	}

	@Test
	public void close_calledTwice_doesNothingTheSecondTime() {
		OnnxEmbeddingProvider provider = new OnnxEmbeddingProvider("missing-model.onnx", "missing-vocab.txt");
		provider.close();
		provider.close();
	}
}
