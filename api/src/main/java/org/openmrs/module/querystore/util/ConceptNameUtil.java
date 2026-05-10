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
import org.openmrs.ConceptName;
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

	private static final int MAX_SYNONYMS = 3;

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

	public static List<String> getSynonyms(Concept concept) {
		return getSynonyms(concept, getPreferredName(concept));
	}

	/**
	 * Variant for callers that have already resolved the preferred name (typically in the same
	 * serializer pass that composes {@code text}). Avoids re-walking the names collection and
	 * re-resolving the locale. {@code preferredName} must equal what {@link #getPreferredName}
	 * would return for the active locale; passing a different string corrupts the dedupe filter
	 * and can leak the preferred name into the synonyms list.
	 */
	public static List<String> getSynonyms(Concept concept, String preferredName) {
		if (concept == null || concept.getNames() == null) {
			return Collections.emptyList();
		}
		// Match by language only rather than full locale equality. Concept dictionaries (CIEL,
		// OCL) typically tag names as "en" while deployment locales are often "en_GB" or "en_US";
		// Concept.getSynonyms(Locale)'s strict Locale.equals would silently drop those.
		String localeLanguage = resolveLocale().getLanguage();
		TreeSet<String> sorted = new TreeSet<>();
		for (ConceptName cn : concept.getNames()) {
			String name = cn.getName();
			if (name == null || name.equals(preferredName)) {
				continue;
			}
			if (cn.getLocale() != null && !cn.getLocale().getLanguage().equals(localeLanguage)) {
				continue;
			}
			sorted.add(name);
		}
		List<String> result = new ArrayList<>(Math.min(sorted.size(), MAX_SYNONYMS));
		for (String name : sorted) {
			if (result.size() >= MAX_SYNONYMS) {
				break;
			}
			result.add(name);
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
