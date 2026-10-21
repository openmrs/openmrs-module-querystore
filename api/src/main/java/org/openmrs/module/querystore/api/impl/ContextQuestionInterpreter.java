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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Server-side question interpretation and retrieval preprocessing for context slices (ADR
 * Decision 18). QueryStore owns this behavior alongside the index and embedder so every consumer
 * uses the same cue routing and query normalization. Interpretation is deliberately mechanical
 * cue matching, not NLU: conservative word-boundary cues, unioned when they co-occur.
 */
final class ContextQuestionInterpreter {

	private ContextQuestionInterpreter() {
	}


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
		// Kept entry for entry with ChartSearchAI's QueryScopeRouter, the one caller that sends
		// interpret=true: "regimen" and "what she takes" are how a clinician words the question
		// (its Decision 180), and a cue read there and not here answers a long ART history from
		// whichever orders similarity ranks.
		m.put(cues("medications?", "medicines?", "meds", "drugs?", "prescriptions?", "prescribed", "regimens?",
		        "(?:she|he|they|the patient|this patient) (?:takes|is taking|are taking)"),
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
	        "most recent", "recent", "latest", "newest", "last", "current", "currently", "now", "today",
	        "when was", "when did", "lately", "recently", "since",
	        "(?:over |in |during )?(?:the )?past (?:few )?(?:\\d+ )?(?:days?|weeks?|months?|years?)",
	        "this (?:week|month|year)");

	/**
	 * Everyday idioms that share a word with a cue but name no records ("in order to", "under what
	 * conditions", "last name"). They are blanked before the cues run, so each false cue does not pull
	 * whole types into the typed tier or add a recency anchor. A cue elsewhere in the question still
	 * applies. "At last" is the idiom only with nothing after it: "Viral load at last test?" asks for
	 * the newest reading, as ChartSearchAI's router reads it, and keeps its recency anchor.
	 */
	private static final Pattern IDIOMS = cues(
	        "in order (?:to|that|for|of)", "out of order", "order of",
	        "under (?:what|which|these|those|such|certain|normal|the same) conditions?",
	        "on (?:the )?condition that", "condition of",
	        "in (?:good|poor|fair|stable|critical|serious|bad|satisfactory) condition",
	        "last names?", "at last(?!\\s+[\\p{L}\\p{N}])", "last resort");

	private static Pattern cues(String... words) {
		return Pattern.compile("\\b(?:" + String.join("|", words) + ")\\b", Pattern.CASE_INSENSITIVE);
	}

	static Interpretation interpret(String question) {
		Set<String> types = new HashSet<String>();
		String text = question == null ? "" : IDIOMS.matcher(question).replaceAll(" ").trim();
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
		m.put(panelAbbreviation("BMP", "basic metabolic panel"), "basic metabolic panel");
		m.put(panelAbbreviation("CMP", "comprehensive metabolic panel"), "comprehensive metabolic panel");
		m.put(panelAbbreviation("CBC", "complete blood count"), "complete blood count");
		m.put(panelAbbreviation("LFTs?", "liver function tests"), "liver function tests");
		m.put(panelAbbreviation("RFTs?", "renal function tests"), "renal function tests");
		m.put(panelAbbreviation("ABG", "arterial blood gas"), "arterial blood gas");
		m.put(panelAbbreviation("ESR", "erythrocyte sedimentation rate"), "erythrocyte sedimentation rate");
		m.put(panelAbbreviation("CRP", "C-reactive protein"), "C-reactive protein");
		LAB_PANEL_ABBREVIATIONS = Collections.unmodifiableMap(m);
	}

	private static Pattern panelAbbreviation(String abbreviation, String expansion) {
		return Pattern.compile("\\b(?:" + abbreviation + ")\\b(?!\\s+" + Pattern.quote(expansion) + "\\b)",
		        Pattern.CASE_INSENSITIVE);
	}

	// The chart's own subject. Every record in a per-patient slice is about the patient, so the word
	// tells no record from another, while records that say "Patient reported ..." match it and fill
	// the similarity tier ahead of what the question asks about (Decision 18). Only the English article
	// forms and the possessive, so a question in another language keeps its own words.
	private static final Pattern THE_PATIENT = Pattern.compile(
	        "\\b(?:the|this|that)\\s+patient(?:'s|s'|s)?\\b|\\bpatient's\\b", Pattern.CASE_INSENSITIVE);

	/**
	 * Retrieval preprocessing for the slice's similarity leg: leave out the patient the whole chart is
	 * about, and expand lab-panel abbreviations so both the abbreviation and the full clinical surface
	 * form reach the embedding and BM25 index. Everything else keeps its sentence structure for the
	 * transformer embedder and multilingual queries. Idempotent. Null/blank pass through unchanged, as
	 * does a question that is only the patient.
	 */
	static String preprocess(String question) {
		if (question == null || question.trim().isEmpty()) {
			return question;
		}
		String withoutSubject = THE_PATIENT.matcher(question).replaceAll(" ").replaceAll("\\s{2,}", " ").trim();
		return expandLabPanels(withoutSubject.isEmpty() ? question.trim() : withoutSubject);
	}

	private static String expandLabPanels(String question) {
		String expanded = question;
		for (Map.Entry<Pattern, String> entry : LAB_PANEL_ABBREVIATIONS.entrySet()) {
			expanded = entry.getKey().matcher(expanded)
			        .replaceAll("$0 " + Matcher.quoteReplacement(entry.getValue()));
		}
		return expanded;
	}
}
