/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.api.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Server-side question interpretation and retrieval preprocessing for context slices (ADR
 * Decision 18). Consolidated here — at the owner of the index and the embedder — so the two AI
 * consumers stop duplicating cue routing and query normalization (both had drifted; measured by
 * the harness engine-parity instrument). Interpretation is deliberately mechanical cue matching,
 * not NLU: conservative word-boundary cues, unioned when they co-occur.
 */
final class ContextQuestionInterpreter {

	private ContextQuestionInterpreter() {
	}

	private static final Log log = LogFactory.getLog(ContextQuestionInterpreter.class);

	/** The interpreted question: typed-complete resource types + whether the recency anchor applies. */
	static final class Interpretation {

		final Set<String> types;

		final boolean temporal;

		Interpretation(Set<String> types, boolean temporal) {
			this.types = Collections.unmodifiableSet(types);
			this.temporal = temporal;
		}
	}

	// Typed-scope cues: each maps a family of question phrasings to the resource types whose
	// records must be typed-complete for that question class. Word-boundary, case-insensitive,
	// unioned on co-occurrence ("any drug allergies?" matches medications AND allergies).
	private static final Map<Pattern, String[]> TYPE_CUES;
	static {
		Map<Pattern, String[]> m = new LinkedHashMap<Pattern, String[]>();
		m.put(cues("medications?", "medicines?", "meds", "drugs?", "prescriptions?", "prescribed"),
		        new String[] { "drug_order", "medication_dispense" });
		m.put(cues("allerg(?:y|ies|ic|en|ens)", "adverse", "reactions?", "intoleran(?:t|ce|ces)"),
		        new String[] { "allergy" });
		m.put(cues("programs?", "enrolled", "enrollments?"), new String[] { "program" });
		m.put(cues("conditions?", "diagnos(?:is|es|ed)", "problem list"),
		        new String[] { "condition", "diagnosis" });
		m.put(cues("visits?", "appointments?", "encounters?", "admissions?"),
		        new String[] { "visit", "encounter" });
		m.put(cues("orders?", "ordered"), new String[] { "drug_order", "test_order", "referral_order" });
		TYPE_CUES = Collections.unmodifiableMap(m);
	}

	/** Recency cues: the newest value/event or the recent past — these need the recency anchor. */
	private static final Pattern TEMPORAL_CUES = cues(
	        "most recent", "latest", "newest", "last", "current", "currently", "now", "today",
	        "when was", "when did", "lately", "recently", "since",
	        "(?:over |in |during )?(?:the )?past (?:few )?(?:\\d+ )?(?:days?|weeks?|months?|years?)",
	        "this (?:week|month|year)");

	private static Pattern cues(String... words) {
		return Pattern.compile("\\b(?:" + String.join("|", words) + ")\\b", Pattern.CASE_INSENSITIVE);
	}

	static Interpretation interpret(String question) {
		Set<String> types = new HashSet<String>();
		String text = question == null ? "" : question.trim();
		if (!text.isEmpty()) {
			for (Map.Entry<Pattern, String[]> entry : TYPE_CUES.entrySet()) {
				if (entry.getKey().matcher(text).find()) {
					Collections.addAll(types, entry.getValue());
				}
			}
		}
		boolean temporal = !text.isEmpty() && TEMPORAL_CUES.matcher(text).find();
		return new Interpretation(types, temporal);
	}

	// Lab-panel abbreviations expanded for retrieval: the full panel name is appended after the
	// abbreviation so both surface forms reach the embedding ("last BMP" → "last BMP basic
	// metabolic panel").
	private static final Map<Pattern, String> LAB_PANEL_ABBREVIATIONS;
	static {
		Map<Pattern, String> m = new LinkedHashMap<Pattern, String>();
		m.put(Pattern.compile("\\bBMP\\b", Pattern.CASE_INSENSITIVE), "basic metabolic panel");
		m.put(Pattern.compile("\\bCMP\\b", Pattern.CASE_INSENSITIVE), "comprehensive metabolic panel");
		m.put(Pattern.compile("\\bCBC\\b", Pattern.CASE_INSENSITIVE), "complete blood count");
		m.put(Pattern.compile("\\bLFTs?\\b", Pattern.CASE_INSENSITIVE), "liver function tests");
		m.put(Pattern.compile("\\bRFTs?\\b", Pattern.CASE_INSENSITIVE), "renal function tests");
		m.put(Pattern.compile("\\bABG\\b", Pattern.CASE_INSENSITIVE), "arterial blood gas");
		m.put(Pattern.compile("\\bESR\\b", Pattern.CASE_INSENSITIVE), "erythrocyte sedimentation rate");
		m.put(Pattern.compile("\\bCRP\\b", Pattern.CASE_INSENSITIVE), "C-reactive protein");
		LAB_PANEL_ABBREVIATIONS = Collections.unmodifiableMap(m);
	}

	private static final Set<String> QUERY_STOPWORDS = loadStopwords("context-query-stopwords.txt");

	/**
	 * Retrieval preprocessing for the slice's similarity leg: expand lab-panel abbreviations,
	 * then strip stopwords so phrasing variants embed to the same query vector. Idempotent for
	 * input a caller already preprocessed. Null/blank pass through unchanged.
	 */
	static String preprocess(String question) {
		if (question == null || question.trim().isEmpty()) {
			return question;
		}
		return stripStopwords(expandLabPanels(question));
	}

	private static String expandLabPanels(String question) {
		String expanded = question;
		for (Map.Entry<Pattern, String> entry : LAB_PANEL_ABBREVIATIONS.entrySet()) {
			expanded = entry.getKey().matcher(expanded)
			        .replaceAll("$0 " + Matcher.quoteReplacement(entry.getValue()));
		}
		return expanded;
	}

	private static String stripStopwords(String question) {
		String[] words = question.toLowerCase().replaceAll("'s\\b", "").replaceAll("[?!.,;:']", "")
		        .trim().split("\\s+");
		List<String> contentWords = new ArrayList<String>();
		List<String> allClean = new ArrayList<String>();
		for (String word : words) {
			if (!word.isEmpty()) {
				allClean.add(word);
				if (!QUERY_STOPWORDS.contains(word)) {
					contentWords.add(word);
				}
			}
		}
		// Too few content words → keep every cleaned word: the fuller sentence embeds more
		// specifically than one bare term.
		List<String> keep = contentWords.size() >= 2 ? contentWords : allClean;
		return keep.isEmpty() ? question.toLowerCase().trim() : String.join(" ", keep);
	}

	private static Set<String> loadStopwords(String resourceName) {
		Set<String> stopwords = new HashSet<String>();
		InputStream is = ContextQuestionInterpreter.class.getClassLoader().getResourceAsStream(resourceName);
		if (is == null) {
			log.warn("Stopword resource " + resourceName + " missing — similarity preprocessing degrades"
			        + " to abbreviation expansion only.");
			return stopwords;
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String word = line.trim().toLowerCase();
				if (!word.isEmpty() && !word.startsWith("#")) {
					stopwords.add(word);
				}
			}
		}
		catch (IOException e) {
			log.warn("Failed reading stopword resource " + resourceName, e);
		}
		return stopwords;
	}
}
