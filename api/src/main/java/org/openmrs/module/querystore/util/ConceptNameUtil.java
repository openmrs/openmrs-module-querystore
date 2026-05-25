/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import org.openmrs.Concept;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.context.Context;
import org.openmrs.util.LocaleUtility;

/**
 * Resolves a concept's preferred name and same-locale synonyms. The preferred name goes into the
 * stored {@code text} field; synonyms go into the {@code synonyms} metadata field for BM25 and
 * are concatenated onto the embedding input by the indexer (ADR decision 6, Synonyms and group
 * obs convention). Querystore deliberately does not inline synonyms into {@code text} — that
 * keeps citations clean and avoids forcing every consumer to strip parenthetical noise.
 */
public final class ConceptNameUtil {

	private static final int MAX_NON_SHORT_SYNONYMS = 10;

	private ConceptNameUtil() {
	}

	public static String getPreferredName(Concept concept) {
		if (concept == null) {
			return "";
		}
		Locale locale = resolveLocale();
		ConceptName name = concept.getPreferredName(locale);
		if (name == null) {
			name = concept.getName(locale);
		}
		return name != null && name.getName() != null ? name.getName() : "";
	}

	/**
	 * Variant of {@link #getPreferredName(Concept)} that returns {@code null} when the concept is
	 * null or has no usable name in the active locale, instead of an empty string. Callers that
	 * write the name to optional metadata fields prefer this shape — they can null-check once
	 * and skip both the lookup and the write.
	 */
	public static String getPreferredNameOrNull(Concept concept) {
		String name = getPreferredName(concept);
		return name.isEmpty() ? null : name;
	}

	public static List<String> getSynonyms(Concept concept) {
		return getSynonyms(concept, getPreferredName(concept));
	}

	/**
	 * Returns the concept's free-text description for the active locale, falling back to any other
	 * available description when no language-matching description exists. Returns the empty string
	 * when the concept is null or has no description. Same language-only fallback rationale as
	 * {@link #getSynonyms(Concept, String)} — CIEL/OCL dictionaries usually tag descriptions as
	 * {@code en} while deployments run as {@code en_GB}/{@code en_US}.
	 *
	 * <p>Concept descriptions are authored by dictionary maintainers (CIEL, AMPATH, Bahmni) and
	 * naturally contain the clinical-category vocabulary patients use in queries — e.g. "Blood urea
	 * nitrogen" has a description that explicitly mentions "kidney". Indexing this text gives BM25
	 * a vocabulary bridge between natural-language questions and records whose preferred name
	 * doesn't carry the category word. Per the read-side contract this is searched by BM25 only;
	 * it is NOT included in {@link QueryDocument#getEmbeddingInput()} to avoid the asymmetric-bias
	 * concern documented in chartsearchai's {@code ChartSearchAiUtils.extractCategoryHints}.
	 *
	 * <p><b>Precedence:</b> (1) first description whose locale language matches the active
	 * locale's language; (2) otherwise first non-empty description encountered in
	 * {@code concept.getDescriptions()} iteration order. The fallback's iteration order is
	 * Hibernate-determined ({@code PersistentSet}, no {@code @OrderBy}) — in practice CIEL/OCL
	 * concepts have one description so this is rarely exercised; if a deployment needs
	 * deterministic cross-locale fallback, sort the descriptions collection upstream. Returned
	 * text is trimmed; whitespace-only descriptions are dropped to avoid indexing empty tokens.
	 */
	public static String getDescription(Concept concept) {
		if (concept == null || concept.getDescriptions() == null
				|| concept.getDescriptions().isEmpty()) {
			return "";
		}
		String localeLanguage = resolveLocale().getLanguage();
		ConceptDescription fallback = null;
		for (ConceptDescription cd : concept.getDescriptions()) {
			String text = cd.getDescription();
			if (text == null || text.trim().isEmpty()) {
				continue;
			}
			if (cd.getLocale() != null
					&& cd.getLocale().getLanguage().equals(localeLanguage)) {
				return text.trim();
			}
			if (fallback == null) {
				fallback = cd;
			}
		}
		return fallback != null && fallback.getDescription() != null
				? fallback.getDescription().trim() : "";
	}

	/**
	 * Variant for callers that have already resolved the preferred name (typically in the same
	 * serializer pass that composes {@code text}). Avoids re-walking the names collection and
	 * re-resolving the locale. {@code preferredName} must equal what {@link #getPreferredName}
	 * would return for the active locale; passing a different string corrupts the dedupe filter
	 * and can leak the preferred name into the synonyms list.
	 *
	 * <p>SHORT-typed names ({@link ConceptNameType#SHORT}) are returned unconditionally —
	 * clinical abbreviations like {@code DTG}, {@code HTN}, {@code T2DM} are how clinicians
	 * actually type queries, and a single alphabetic cap would let them lose slots to
	 * locale-equivalent FQN variants. Non-SHORT synonyms are still capped at
	 * {@link #MAX_NON_SHORT_SYNONYMS} so concepts with many alternate name variants don't skew
	 * BM25 term-frequency normalization; cap is on the non-SHORT bucket only. Same-string
	 * names appearing in both buckets are deduplicated.
	 */
	public static List<String> getSynonyms(Concept concept, String preferredName) {
		if (concept == null || concept.getNames() == null) {
			return Collections.emptyList();
		}
		// Match by language only rather than full locale equality. Concept dictionaries (CIEL,
		// OCL) typically tag names as "en" while deployment locales are often "en_GB" or "en_US";
		// Concept.getSynonyms(Locale)'s strict Locale.equals would silently drop those.
		String localeLanguage = resolveLocale().getLanguage();
		TreeSet<String> shortNames = new TreeSet<>();
		TreeSet<String> otherNames = new TreeSet<>();
		for (ConceptName cn : concept.getNames()) {
			String name = cn.getName();
			if (name == null || name.equals(preferredName)) {
				continue;
			}
			if (cn.getLocale() != null && !cn.getLocale().getLanguage().equals(localeLanguage)) {
				continue;
			}
			if (cn.getConceptNameType() == ConceptNameType.SHORT) {
				shortNames.add(name);
			} else {
				otherNames.add(name);
			}
		}
		List<String> result = new ArrayList<>(
				shortNames.size() + Math.min(otherNames.size(), MAX_NON_SHORT_SYNONYMS));
		result.addAll(shortNames);
		int cap = MAX_NON_SHORT_SYNONYMS;
		for (String name : otherNames) {
			if (cap == 0) {
				break;
			}
			// Same string registered with both SHORT and non-SHORT types is rare but possible
			// in messy real-world dictionaries; the SHORT bucket already carries the entry, so
			// the non-SHORT pass must skip it to avoid the result list double-counting.
			if (shortNames.contains(name)) {
				continue;
			}
			result.add(name);
			cap--;
		}
		return result;
	}

	private static Locale resolveLocale() {
		// LocaleUtility.getDefaultLocale() guards Context.isSessionOpen() internally and falls
		// back to en_GB when no Context is available — safe to call from event handlers,
		// backfill tasks, and tests alike.
		if (Context.isSessionOpen()) {
			Locale userLocale = Context.getLocale();
			if (userLocale != null) {
				return userLocale;
			}
		}
		return LocaleUtility.getDefaultLocale();
	}
}
