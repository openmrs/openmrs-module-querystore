/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.eval;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmrs.module.querystore.QueryStoreConstants;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.embedding.OnnxEmbeddingProvider;
import org.openmrs.module.querystore.model.QueryDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared retrieval-quality eval skeleton for the 153-record chartsearchai benchmark. Concrete
 * subclasses wire their backend in their own {@code @BeforeAll}, expose the assembled service and
 * dataset via the protected fields, and supply a tier-specific per-case latency budget; the two
 * recall {@code @Test}s plus the helpers (record→document mapping, dataset load, model-file
 * gating) live here so a regression on either tier — or on the resource-type routing the per-type
 * indices depend on — fails loudly on every backend the eval ships against.
 *
 * <p>Gated on {@code -Dquerystore.eval.modelDir} pointing at an all-MiniLM-L6-v2 export (or
 * compatible BERT-class sentence encoder). Subclasses' {@code @BeforeAll} must also check
 * {@link #modelFilesExist()} and skip backend bootstrap when files are missing.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractRetrievalQualityEvalTest {

	private static final Logger log = LoggerFactory.getLogger(AbstractRetrievalQualityEvalTest.class);

	protected static final String MODEL_DIR = System.getProperty("querystore.eval.modelDir");

	protected static final String MODEL_PATH = MODEL_DIR != null ? MODEL_DIR + "/model.onnx" : null;

	protected static final String VOCAB_PATH = MODEL_DIR != null ? MODEL_DIR + "/vocab.txt" : null;

	protected static final String PATIENT_UUID = UUID.nameUUIDFromBytes("retrieval-eval".getBytes()).toString();

	protected static final int TOP_K = 30;

	// chartsearchai's RetrievalQualityEvalTest asserts the same overall recall threshold; mirrored
	// here so a querystore regression below the chartsearchai bar fails loudly.
	protected static final double MIN_AVG_RECALL = 0.4;

	protected OnnxEmbeddingProvider provider;

	protected QueryStoreService service;

	protected EvalDataset evalDataset;

	/** Per-case retrieval latency budget. Lucene in-JVM is ~200ms; ES over HTTP needs ~2000ms. */
	protected abstract long perCaseLatencyBudgetMs();

	@Test
	public void retrievalRecall_perCase() {
		assumeTrue(modelFilesExist(),
		        "set -Dquerystore.eval.modelDir to a model+vocab directory to run");

		for (EvalCase evalCase : evalDataset.getCases()) {
			long start = System.currentTimeMillis();
			List<Integer> retrieved = retrieveTopK(evalCase);
			long elapsed = System.currentTimeMillis() - start;
			double recall = EvalMetrics.recall(retrieved, evalCase.getExpectedRecordIndices());

			log.info("[{}] recall@{}={} latency={}ms expected={} top5={}",
			        evalCase.getId(), TOP_K, String.format(Locale.ROOT, "%.3f", recall), elapsed,
			        evalCase.getExpectedRecordIndices(),
			        retrieved.subList(0, Math.min(5, retrieved.size())));

			assertTrue(elapsed < perCaseLatencyBudgetMs(),
			        evalCase.getId() + ": retrieval should complete in < "
			                + perCaseLatencyBudgetMs() + "ms but took " + elapsed + "ms");
		}
	}

	@Test
	public void retrievalRecall_shouldMeetMinimumThreshold() {
		assumeTrue(modelFilesExist(),
		        "set -Dquerystore.eval.modelDir to a model+vocab directory to run");

		int totalCases = 0;
		double totalRecall = 0;
		Map<String, Double> perCase = new LinkedHashMap<>();
		for (EvalCase evalCase : evalDataset.getCases()) {
			totalCases++;
			List<Integer> retrieved = retrieveTopK(evalCase);
			double recall = EvalMetrics.recall(retrieved, evalCase.getExpectedRecordIndices());
			totalRecall += recall;
			perCase.put(evalCase.getId(), recall);
		}
		double avgRecall = totalCases > 0 ? totalRecall / totalCases : 0;

		log.info("Retrieval eval summary ({}): avgRecall@{}={} across {} cases",
		    getClass().getSimpleName(), TOP_K, String.format(Locale.ROOT, "%.3f", avgRecall), totalCases);
		for (Map.Entry<String, Double> e : perCase.entrySet()) {
			log.info("  {} -> {}", e.getKey(), String.format(Locale.ROOT, "%.3f", e.getValue()));
		}

		assertTrue(avgRecall >= MIN_AVG_RECALL,
		        "Average retrieval recall@" + TOP_K + " should be >= " + MIN_AVG_RECALL
		                + " but was " + String.format(Locale.ROOT, "%.3f", avgRecall));
	}

	// ---------- shared helpers ----------

	protected List<Integer> retrieveTopK(EvalCase evalCase) {
		List<QueryDocument> results = service.search(evalCase.getQuestion(), TOP_K);
		List<Integer> indices = new ArrayList<>(results.size());
		for (QueryDocument doc : results) {
			indices.add(Integer.parseInt(doc.getResourceUuid()));
		}
		return indices;
	}

	protected QueryDocument toDocument(int index, String text) {
		QueryDocument doc = new QueryDocument();
		doc.setPatientUuid(PATIENT_UUID);
		doc.setResourceType(resourceTypeForPrefix(text));
		// String index → reliable mapping back to expectedRecordIndices on the query path.
		doc.setResourceUuid(Integer.toString(index));
		doc.setText(text);
		List<String> synonyms = synonymsForText(text);
		if (!synonyms.isEmpty()) {
			doc.putMetadata(QueryStoreConstants.FIELD_SYNONYMS, synonyms);
		}
		// Order matters: synonyms must be on the document before this line, because
		// getEmbeddingInput() reads the synonyms metadata and appends it to the embed input.
		// Mirrors the production write path in RecordIndexer / TypeBootstrapper — reorder and the
		// embedding silently drops the synonym signal without throwing.
		doc.setEmbedding(provider.embed(doc.getEmbeddingInput()));
		return doc;
	}

	/**
	 * Small medical-abbreviation synonyms map approximating what {@code ConceptNameUtil.getSynonyms}
	 * would return against the CIEL dictionary on a real deployment. The eval dataset's records carry
	 * only the preferred name (e.g., "Respiratory Rate") in their text; without a synonyms list, the
	 * BM25-on-synonyms wiring per ADR Decision 6 has nothing to index and the embedding has to bridge
	 * the abbreviation on its own. Keyed by case-sensitive substring of the record text — the eval
	 * dataset uses canonical capitalisation throughout.
	 *
	 * <p>This is a retrieval-quality test, not a serializer regression test: the lookup deliberately
	 * bypasses {@code ConceptNameUtil} (locale filtering, {@code MAX_SYNONYMS=3} cap, dedupe rules).
	 * A future regression in {@code ConceptNameUtil} would not be caught here; the serializer tests
	 * are the right home for that.
	 *
	 * <p>Insertion order is load-bearing: specific keys precede generic ones so
	 * {@code Diastolic Blood Pressure} matches before any hypothetical bare {@code Blood Pressure}
	 * entry would, and {@code Pulse} comes last so it cannot pre-empt a future longer "Pulse ..."
	 * concept name added above it.
	 */
	private static final Map<String, List<String>> SYNONYMS = synonymsMap();

	private static Map<String, List<String>> synonymsMap() {
		Map<String, List<String>> m = new LinkedHashMap<>();
		m.put("Respiratory Rate", Collections.singletonList("RR"));
		m.put("Systolic Blood Pressure", Arrays.asList("SBP", "BP"));
		m.put("Diastolic Blood Pressure", Arrays.asList("DBP", "BP"));
		m.put("Blood Oxygen Saturation", Arrays.asList("SpO2", "O2 sat"));
		m.put("Temperature (C)", Collections.singletonList("Temp"));
		m.put("CD4 Count", Collections.singletonList("CD4"));
		m.put("Weight (kg)", Collections.singletonList("Wt"));
		m.put("Height (cm)", Collections.singletonList("Ht"));
		m.put("Pulse", Arrays.asList("HR", "Heart Rate", "Pulse Rate"));
		return m;
	}

	protected static List<String> synonymsForText(String text) {
		if (text == null) {
			return Collections.emptyList();
		}
		for (Map.Entry<String, List<String>> e : SYNONYMS.entrySet()) {
			if (text.contains(e.getKey())) {
				return e.getValue();
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Maps the labeled-prose prefix of a serialized record to the querystore resource type the
	 * matching {@code *RecordSerializer} writes. Single source of truth: every new tier in the eval
	 * inherits the same routing, so adding a serializer prefix here flows to every backend at once
	 * rather than drifting per-tier.
	 */
	protected static String resourceTypeForPrefix(String text) {
		if (text.startsWith("Clinical observation:")) {
			return "obs";
		}
		if (text.startsWith("Clinical diagnosis:")) {
			return "diagnosis";
		}
		if (text.startsWith("Medical condition:")) {
			return "condition";
		}
		if (text.startsWith("Medication prescription:")) {
			return "drug_order";
		}
		if (text.startsWith("Patient allergy:")) {
			return "allergy";
		}
		if (text.startsWith("Program enrollment:")) {
			return "program";
		}
		throw new IllegalArgumentException("Unrecognised record prefix in: "
		        + text.substring(0, Math.min(40, text.length())));
	}

	protected static List<String> loadDataset() throws IOException {
		try (InputStream is = AbstractRetrievalQualityEvalTest.class.getClassLoader()
		        .getResourceAsStream("eval/full-patient-dataset.json")) {
			if (is == null) {
				throw new IOException("Dataset not found on classpath: eval/full-patient-dataset.json");
			}
			return new ObjectMapper().readValue(is, new TypeReference<List<String>>() {});
		}
	}

	protected static boolean modelFilesExist() {
		return MODEL_PATH != null && VOCAB_PATH != null
		        && new File(MODEL_PATH).exists() && new File(VOCAB_PATH).exists();
	}
}
