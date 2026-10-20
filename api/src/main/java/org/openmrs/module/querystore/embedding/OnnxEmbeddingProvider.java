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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtProvider;
import ai.onnxruntime.OrtSession;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.querystore.QueryStoreConstants;
import org.openmrs.module.querystore.embedding.WordPieceTokenizer.TokenizedInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link EmbeddingProvider} using ONNX Runtime. Supports any BERT-class sentence
 * embedding model. Embedding dimensions are detected automatically from the model's first
 * inference. Model and vocabulary file paths are configured via the {@code
 * querystore.embedding.modelFilePath} and {@code querystore.embedding.vocabFilePath} global
 * properties (relative to the OpenMRS application data directory). Dual-encoder models (e.g.
 * MedCPT) are supported by additionally configuring {@code
 * querystore.embedding.queryModelFilePath}.
 *
 * <p>Registered as Spring bean {@code querystore.embedding.onnx} (the default value of
 * {@link QueryStoreConstants#DEFAULT_EMBEDDING_PROVIDER_BEAN}); selection is GP-driven via
 * {@link ConfiguredEmbeddingProvider} per ADR Decision 8.
 *
 * <p>{@link #embed(String)} and {@link #embedQuery(String)} serialize on the instance lock —
 * ONNX {@code OrtSession.run} is not thread-safe, so concurrent kNN queries against this
 * provider queue.
 *
 * <p>{@link #close()} is terminal: once it has run, both throw
 * {@link EmbeddingProviderClosedException} rather than reopening a session.
 */
public class OnnxEmbeddingProvider implements EmbeddingProvider {

	private static final Logger log = LoggerFactory.getLogger(OnnxEmbeddingProvider.class);

	private OrtEnvironment env;

	private OrtSession session;

	private OrtSession.SessionOptions sessionOptions;

	private String loadedModelPath;

	private boolean expectsTokenTypeIds;

	private WordPieceTokenizer tokenizer;

	private volatile int detectedDimensions = -1;

	private String explicitModelPath;

	private String explicitVocabPath;

	private int explicitMaxSeqLen = -1;

	private OrtSession querySession;

	private boolean queryExpectsTokenTypeIds;

	private OrtSession.SessionOptions querySessionOptions;

	private String loadedQueryModelPath;

	private String explicitQueryModelPath;

	private boolean closed;

	/**
	 * Default constructor for Spring context — resolves model paths from OpenMRS global
	 * properties.
	 */
	public OnnxEmbeddingProvider() {
	}

	/**
	 * Test-friendly constructor that accepts explicit file paths, bypassing the OpenMRS Context
	 * dependency. Pass {@code null} for {@code queryModelPath} to use single-encoder mode.
	 */
	public OnnxEmbeddingProvider(String modelPath, String queryModelPath, String vocabPath) {
		this.explicitModelPath = modelPath;
		this.explicitQueryModelPath = queryModelPath;
		this.explicitVocabPath = vocabPath;
		this.explicitMaxSeqLen = QueryStoreConstants.DEFAULT_EMBEDDING_MAX_SEQUENCE_LENGTH;
	}

	/**
	 * Test-friendly constructor for single-encoder models. Equivalent to
	 * {@code new OnnxEmbeddingProvider(modelPath, null, vocabPath)}.
	 */
	public OnnxEmbeddingProvider(String modelPath, String vocabPath) {
		this(modelPath, null, vocabPath);
	}

	@Override
	public synchronized float[] embed(String text) {
		ensureOpen();
		try {
			return runInference(getSession(), expectsTokenTypeIds, getTokenizer(), text);
		}
		catch (OrtException e) {
			throw new RuntimeException("Failed to compute embedding", e);
		}
	}

	@Override
	public synchronized float[] embedQuery(String text) {
		ensureOpen();
		try {
			OrtSession qs = getQuerySession();
			if (qs != null) {
				return runInference(qs, queryExpectsTokenTypeIds, getTokenizer(), text);
			}
			return runInference(getSession(), expectsTokenTypeIds, getTokenizer(), text);
		}
		catch (OrtException e) {
			throw new RuntimeException("Failed to compute query embedding", e);
		}
	}

	@Override
	public int getDimensions() {
		return detectedDimensions;
	}

	@Override
	public String getModelName() {
		if (loadedModelPath != null) {
			return loadedModelPath;
		}
		return explicitModelPath;
	}

	/**
	 * Releases the sessions for good; a later {@link #embed} or {@link #embedQuery} throws
	 * {@link EmbeddingProviderClosedException}. Calling it again does nothing.
	 */
	public synchronized void close() {
		closed = true;
		closeArticleSession();
		closeQuerySession();
		env = null;
	}

	private void ensureOpen() {
		if (closed) {
			throw new EmbeddingProviderClosedException();
		}
	}

	private void closeArticleSession() {
		if (session == null) {
			return;
		}
		log.info("Closing ONNX embedding session");
		try {
			session.close();
		}
		catch (OrtException e) {
			log.warn("Error closing ONNX session", e);
		}
		session = null;
		if (sessionOptions != null) {
			sessionOptions.close();
			sessionOptions = null;
		}
		loadedModelPath = null;
		expectsTokenTypeIds = false;
		tokenizer = null;
		detectedDimensions = -1;
	}

	private void closeQuerySession() {
		if (querySession == null) {
			return;
		}
		try {
			querySession.close();
		}
		catch (OrtException e) {
			log.warn("Error closing query ONNX session", e);
		}
		querySession = null;
		if (querySessionOptions != null) {
			querySessionOptions.close();
			querySessionOptions = null;
		}
		loadedQueryModelPath = null;
		queryExpectsTokenTypeIds = false;
	}

	private float[] runInference(OrtSession ortSession, boolean tokenTypeIdsExpected,
			WordPieceTokenizer wpTokenizer, String text) throws OrtException {
		Map<String, OnnxTensor> inputs = new HashMap<String, OnnxTensor>();
		OrtSession.Result result = null;
		try {
			TokenizedInput tokenized = wpTokenizer.tokenize(text);
			int seqLen = tokenized.getLength();

			long[][] inputIdsArr = { tokenized.getInputIds() };
			long[][] attentionMaskArr = { tokenized.getAttentionMask() };

			OrtEnvironment ortEnv = getOrCreateEnv();
			inputs.put("input_ids", OnnxTensor.createTensor(ortEnv, inputIdsArr));
			inputs.put("attention_mask", OnnxTensor.createTensor(ortEnv, attentionMaskArr));

			// all-MiniLM-L6-v2 requires token_type_ids, e5-base-v2 does not — flag is cached
			// per-session at load time.
			if (tokenTypeIdsExpected) {
				long[][] tokenTypeIdsArr = { tokenized.getTokenTypeIds() };
				inputs.put("token_type_ids", OnnxTensor.createTensor(ortEnv, tokenTypeIdsArr));
			}

			result = ortSession.run(inputs);

			float[][][] output = (float[][][]) result.get(0).getValue();
			int modelDimensions = output[0][0].length;
			if (detectedDimensions == -1) {
				detectedDimensions = modelDimensions;
				log.info("Detected embedding dimensions: {}", detectedDimensions);
			} else if (modelDimensions != detectedDimensions) {
				throw new OrtException("ONNX model output dimensions changed (" + modelDimensions
						+ " vs previously detected " + detectedDimensions
						+ "). Was the model swapped without reloading?");
			}

			// Mean pooling over attention-masked tokens
			float[] embedding = new float[modelDimensions];
			long[] attentionMask = tokenized.getAttentionMask();
			int tokenCount = 0;
			for (int i = 0; i < seqLen; i++) {
				if (attentionMask[i] == 1) {
					for (int j = 0; j < embedding.length; j++) {
						embedding[j] += output[0][i][j];
					}
					tokenCount++;
				}
			}
			if (tokenCount > 0) {
				for (int j = 0; j < embedding.length; j++) {
					embedding[j] /= tokenCount;
				}
			}

			// L2 normalize
			double norm = 0;
			for (float v : embedding) {
				norm += v * v;
			}
			norm = Math.sqrt(norm);
			if (norm > 0) {
				for (int i = 0; i < embedding.length; i++) {
					embedding[i] /= (float) norm;
				}
			}

			return embedding;
		}
		finally {
			if (result != null) {
				try {
					result.close();
				}
				catch (Exception e) {
					log.warn("Error closing ONNX result", e);
				}
			}
			for (OnnxTensor tensor : inputs.values()) {
				try {
					tensor.close();
				}
				catch (Exception e) {
					log.warn("Error closing ONNX tensor", e);
				}
			}
		}
	}

	/**
	 * Creates an ONNX session, handling models with external data files (model.onnx.data). ONNX
	 * runtime resolves external data relative to the model file path, which fails when the path
	 * contains {@code ..} segments. This method canonicalizes the path first; if that still fails
	 * (runtime bug), it falls back to loading from a byte array.
	 */
	private OrtSession createSessionWithExternalData(OrtEnvironment ortEnv, String modelPath,
			OrtSession.SessionOptions options) throws OrtException {
		String canonical;
		try {
			canonical = new File(modelPath).getCanonicalPath();
		}
		catch (IOException e) {
			canonical = modelPath;
		}
		try {
			return ortEnv.createSession(canonical, options);
		}
		catch (OrtException e) {
			// External data resolution failed — fall back to byte-array load, which doesn't need
			// to resolve a .data file path. Only works for self-contained models (no external
			// initializers).
			log.warn("ONNX session creation failed with path '{}', trying byte array fallback: {}",
					canonical, e.getMessage());
			try {
				byte[] modelBytes = Files.readAllBytes(Paths.get(canonical));
				return ortEnv.createSession(modelBytes, options);
			}
			catch (IOException | OrtException fallbackE) {
				e.addSuppressed(fallbackE);
				throw e;
			}
		}
	}

	private OrtEnvironment getOrCreateEnv() {
		if (env == null) {
			env = OrtEnvironment.getEnvironment();
		}
		return env;
	}

	/**
	 * Resolves a model file path: returns the explicit path if set on the constructor, otherwise
	 * reads the global property and runs it through {@link ModelFileResolver}. When
	 * {@code optional} is true, returns {@code null} for an unset GP and swallows Context-resolution
	 * failures (single-encoder fallback). When {@code optional} is false, an unset GP throws
	 * {@link IllegalStateException} and any underlying Context exception propagates unchanged.
	 */
	private static String resolvePath(String explicitPath, String gpName, boolean optional) {
		if (explicitPath != null) {
			return explicitPath;
		}
		String configured;
		try {
			configured = StringUtils.trimToNull(
					Context.getAdministrationService().getGlobalProperty(gpName));
		}
		catch (RuntimeException e) {
			if (optional) {
				return null;
			}
			throw e;
		}
		if (configured == null) {
			if (optional) {
				return null;
			}
			throw new IllegalStateException(
					"Required global property is not configured: " + gpName);
		}
		return ModelFileResolver.resolveModelPath(configured, gpName);
	}

	private OrtSession getSession() throws OrtException {
		String modelPath = resolvePath(explicitModelPath,
				QueryStoreConstants.GP_EMBEDDING_MODEL_FILE_PATH, false);

		if (session != null && !modelPath.equals(loadedModelPath)) {
			log.info("Embedding model path changed from {} to {}, reloading", loadedModelPath,
					modelPath);
			closeArticleSession();
		}

		if (session == null) {
			log.info("Loading ONNX embedding model from {}", modelPath);
			OrtEnvironment ortEnv = getOrCreateEnv();
			sessionOptions = new OrtSession.SessionOptions();
			try {
				EnumSet<OrtProvider> available = OrtEnvironment.getAvailableProviders();
				log.info("Available ONNX execution providers: {}", available);
				if (available.contains(OrtProvider.CORE_ML)) {
					sessionOptions.addCoreML();
					log.info("CoreML execution provider enabled (GPU acceleration)");
				} else if (available.contains(OrtProvider.CUDA)) {
					sessionOptions.addCUDA();
					log.info("CUDA execution provider enabled (GPU acceleration)");
				}
				session = createSessionWithExternalData(ortEnv, modelPath, sessionOptions);
				expectsTokenTypeIds = session.getInputNames().contains("token_type_ids");
				loadedModelPath = modelPath;
				log.info("ONNX embedding model loaded successfully");
			}
			catch (OrtException | RuntimeException e) {
				closeArticleSession();
				if (sessionOptions != null) {
					sessionOptions.close();
					sessionOptions = null;
				}
				throw e;
			}
		}
		return session;
	}

	/**
	 * Returns the query-encoder session for dual-encoder models, or {@code null} for
	 * single-encoder models. Lazily loads the query model on first call.
	 */
	private OrtSession getQuerySession() throws OrtException {
		String queryModelPath = resolvePath(explicitQueryModelPath,
				QueryStoreConstants.GP_EMBEDDING_QUERY_MODEL_FILE_PATH, true);
		if (queryModelPath == null) {
			return null;
		}

		if (querySession != null && !queryModelPath.equals(loadedQueryModelPath)) {
			log.info("Query model path changed, reloading");
			closeQuerySession();
		}

		if (querySession == null) {
			log.info("Loading ONNX query encoder from {}", queryModelPath);
			OrtEnvironment ortEnv = getOrCreateEnv();
			querySessionOptions = new OrtSession.SessionOptions();
			try {
				// CPU for the query encoder avoids CoreML/CUDA conflicts with the article-encoder
				// session. Query inference is fast enough (~1ms) that GPU acceleration isn't worth
				// the contention.
				querySession = createSessionWithExternalData(ortEnv, queryModelPath,
						querySessionOptions);
				queryExpectsTokenTypeIds = querySession.getInputNames().contains("token_type_ids");
				loadedQueryModelPath = queryModelPath;
				log.info("ONNX query encoder loaded successfully");
			}
			catch (OrtException | RuntimeException e) {
				closeQuerySession();
				if (querySessionOptions != null) {
					querySessionOptions.close();
					querySessionOptions = null;
				}
				throw e;
			}
		}
		return querySession;
	}

	private WordPieceTokenizer getTokenizer() {
		if (tokenizer == null) {
			String vocabPath = resolvePath(explicitVocabPath,
					QueryStoreConstants.GP_EMBEDDING_VOCAB_FILE_PATH, false);
			int maxSeqLen = explicitMaxSeqLen > 0 ? explicitMaxSeqLen : getMaxSequenceLength();
			log.info("Loading WordPiece vocabulary from {} (maxSequenceLength={})", vocabPath,
					maxSeqLen);
			try {
				tokenizer = new WordPieceTokenizer(vocabPath, maxSeqLen);
			}
			catch (IOException e) {
				throw new IllegalStateException(
						"Failed to load WordPiece vocabulary from " + vocabPath, e);
			}
			log.info("WordPiece vocabulary loaded successfully");
		}
		return tokenizer;
	}

	private static int getMaxSequenceLength() {
		Integer parsed = Context.getAdministrationService().getGlobalPropertyValue(
				QueryStoreConstants.GP_EMBEDDING_MAX_SEQUENCE_LENGTH,
				QueryStoreConstants.DEFAULT_EMBEDDING_MAX_SEQUENCE_LENGTH);
		if (parsed >= 32 && parsed <= 8192) {
			return parsed;
		}
		log.warn("maxSequenceLength {} out of supported range [32, 8192], using default {}",
				parsed, QueryStoreConstants.DEFAULT_EMBEDDING_MAX_SEQUENCE_LENGTH);
		return QueryStoreConstants.DEFAULT_EMBEDDING_MAX_SEQUENCE_LENGTH;
	}
}
