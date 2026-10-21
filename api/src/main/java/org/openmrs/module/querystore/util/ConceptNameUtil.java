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
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
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

	// Cap on the non-SHORT bucket of getSynonyms; SHORT names are returned unconditionally.
	// Trades embedding-input length for BM25 vocabulary breadth — see Decision 6's SHORT-name
	// promotion paragraph for the 4-patient eval that ratified raising this from 3 to 10.
	private static final int MAX_NON_SHORT_SYNONYMS = 10;

	/**
	 * Cap on the number of reference-term names returned from {@link #getMappingNames}. A typical
	 * CIEL concept has 3–8 populated mappings (LOINC, ICD-10, PIH, WHOATC, CIEL drug-class
	 * parents); 10 is the same envelope as {@link #MAX_NON_SHORT_SYNONYMS}, picked so the BM25
	 * mapping-names field doesn't dwarf the preferred name on concepts with pathological mapping
	 * lists. Mapping names are BM25-only signal — they are NOT added to the embedding input — so
	 * the cap is purely an index-size / IDF-normalisation guard, not an embedding-budget guard.
	 */
	public static final int MAX_MAPPING_NAMES = 10;

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
		String fallback = null;
		for (ConceptDescription cd : concept.getDescriptions()) {
			String text = cd.getDescription();
			if (text == null) {
				continue;
			}
			String trimmed = text.trim();
			if (trimmed.isEmpty() || isFormPromptBoilerplate(trimmed)) {
				continue;
			}
			if (cd.getLocale() != null
					&& cd.getLocale().getLanguage().equals(localeLanguage)) {
				return trimmed;
			}
			if (fallback == null) {
				fallback = trimmed;
			}
		}
		return fallback != null ? fallback : "";
	}

	/** Starts a form-prompt boilerplate description, which describes the form rather than the concept. */
	private static final String FORM_PROMPT_DESCRIPTION_PREFIX = "Question on encounter form";

	private static boolean isFormPromptBoilerplate(String description) {
		return description.regionMatches(true, 0,
				FORM_PROMPT_DESCRIPTION_PREFIX, 0, FORM_PROMPT_DESCRIPTION_PREFIX.length());
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

	/**
	 * Returns the names of the concept's reference-term mappings — the external-authority labels
	 * dictionary mapping authors curate (LOINC's "Urea nitrogen [Moles/volume] in Serum or Plasma",
	 * ICD-10's "Chronic kidney disease, unspecified", PIH's "Chronic kidney disease", CIEL's
	 * drug-class parents like "Heparins"). Same BM25-vocabulary-bridge mechanic as
	 * {@link #getDescription}: names that contain category words (kidney, disease, serum,
	 * plasma, fever) which the concept's preferred name doesn't carry surface the record on
	 * those queries.
	 *
	 * <p>Returns empty when the concept is null, has no mappings, or none of its mappings carry
	 * a populated reference-term name (the common case for SNOMED-mapped concepts on demo
	 * deployments — SNOMED licensing prevents OCL from distributing names). Names are
	 * de-duplicated (multiple sources commonly use the same wording), trimmed, alphabetically
	 * sorted for indexer determinism, and capped at {@link #MAX_MAPPING_NAMES}. Retired/voided
	 * mappings and retired terms are skipped — they should not bleed into the BM25 channel.
	 *
	 * <p>Per Decision 6 this field is BM25-only and is NOT included in
	 * {@link org.openmrs.module.querystore.model.QueryDocument#getEmbeddingInput()} — the body
	 * is long and vocabulary-overlaps across related concepts (the same asymmetric-bias concern
	 * that excluded description from embedding input).
	 */
	public static List<String> getMappingNames(Concept concept) {
		if (concept == null || concept.getConceptMappings() == null
				|| concept.getConceptMappings().isEmpty()) {
			return Collections.emptyList();
		}
		TreeSet<String> sorted = new TreeSet<>();
		for (ConceptMap m : concept.getConceptMappings()) {
			if (m == null) {
				continue;
			}
			// ConceptMap has no retired/voided flag of its own — lifecycle is managed by the
			// parent Concept; if the concept is being indexed we trust its mappings are live.
			ConceptReferenceTerm term = m.getConceptReferenceTerm();
			if (term == null || Boolean.TRUE.equals(term.getRetired())) {
				continue;
			}
			String name = term.getName();
			if (name == null) {
				continue;
			}
			String trimmed = name.trim();
			if (!trimmed.isEmpty()) {
				sorted.add(trimmed);
			}
		}
		if (sorted.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<>(Math.min(sorted.size(), MAX_MAPPING_NAMES));
		for (String n : sorted) {
			if (result.size() >= MAX_MAPPING_NAMES) {
				break;
			}
			result.add(n);
		}
		return result;
	}

	/**
	 * The concept's live reference-term codes, each namespaced by its source as {@code SOURCE:CODE}
	 * ({@code CIEL:857}, {@code LOINC:14937-7}, {@code SNOMED-CT:72341003}, whitespace in a source
	 * name written as a hyphen), so a question naming a code can match the record
	 * exactly (ADR Decision 17 §2). A code is kept whether or not its term carries a name: SNOMED
	 * terms on demo deployments carry codes alone. Retired terms, terms without a source, and blank
	 * sources or codes are skipped; the result is de-duplicated and sorted, and not capped: only exact
	 * matching reads codes, so the index-size reason for capping names does not carry over, and an
	 * alphabetical cut cost a concept with many mappings its later-sorting codes (Cough's SNOMED CT
	 * code, behind twelve others). Not embedded, as {@link #getMappingNames} is not.
	 */
	public static List<String> getMappingCodes(Concept concept) {
		if (concept == null || concept.getConceptMappings() == null
				|| concept.getConceptMappings().isEmpty()) {
			return Collections.emptyList();
		}
		TreeSet<String> sorted = new TreeSet<>();
		for (ConceptMap m : concept.getConceptMappings()) {
			ConceptReferenceTerm term = m == null ? null : m.getConceptReferenceTerm();
			if (term == null || Boolean.TRUE.equals(term.getRetired()) || term.getConceptSource() == null) {
				continue;
			}
			String source = term.getConceptSource().getName();
			String code = term.getCode();
			if (source == null || source.trim().isEmpty() || code == null || code.trim().isEmpty()) {
				continue;
			}
			// A question names a code as one token, so whitespace inside a source name becomes a
			// hyphen: CIEL's "SNOMED CT" is stored, and matched, as SNOMED-CT.
			sorted.add(source.trim().replaceAll("\\s+", "-") + ":" + code.trim());
		}
		return new ArrayList<>(sorted);
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
