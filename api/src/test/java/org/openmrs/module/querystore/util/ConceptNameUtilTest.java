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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.api.ConceptNameType;

public class ConceptNameUtilTest {

	@Test
	public void getPreferredName_returnsEmpty_whenConceptNull() {
		assertEquals("", ConceptNameUtil.getPreferredName(null));
	}

	@Test
	public void getPreferredName_resolvesAcrossLocaleVariants() {
		// Concept name tagged "en" while default deployment locale is en_GB. The OpenMRS-core
		// fallback chain (Concept.getName(Locale)) does language-level matching when the strict
		// locale lookup fails — this test locks that assumption down.
		Concept c = new Concept();
		c.addName(preferredName("Hypertension"));
		assertEquals("Hypertension", ConceptNameUtil.getPreferredName(c));
	}

	@Test
	public void getSynonyms_returnsEmpty_whenConceptNull() {
		assertTrue(ConceptNameUtil.getSynonyms(null).isEmpty());
	}

	@Test
	public void getSynonyms_capsNonShortAtTen_alphabeticallyFirst() {
		// Non-SHORT synonyms compete for the MAX_NON_SHORT_SYNONYMS=10 cap. Cap is raised from
		// the original conservative 3 because the description-indexing slice established the
		// BM25-only synonym channel — concept dictionaries with rich locale-equivalent FQNs
		// (drug brand/generic/combo forms, region-specific name variants) now keep most of
		// their vocabulary instead of losing it to alphabetic truncation.
		Concept c = new Concept();
		c.addName(preferredName("Primary"));
		c.addName(name("alpha"));
		c.addName(name("beta"));
		c.addName(name("gamma"));
		c.addName(name("delta"));
		c.addName(name("epsilon"));
		c.addName(name("zeta"));
		c.addName(name("eta"));
		c.addName(name("theta"));
		c.addName(name("iota"));
		c.addName(name("kappa"));
		c.addName(name("lambda"));
		c.addName(name("mu"));

		List<String> synonyms = ConceptNameUtil.getSynonyms(c);
		// All 12 non-SHORT names are eligible; cap keeps the 10 alphabetically first.
		assertEquals(Arrays.asList("alpha", "beta", "delta", "epsilon",
				"eta", "gamma", "iota", "kappa", "lambda", "mu"), synonyms);
	}

	@Test
	public void getSynonyms_promotesShortNamesUnconditionally() {
		// Clinical abbreviations like "DTG" (Dolutegravir), "HTN" (Hypertension), "T2DM"
		// (Type 2 Diabetes) are how clinicians actually search. Without SHORT-name promotion
		// they compete for cap slots against locale-equivalent FQN variants and can lose
		// alphabetically — a search for "DTG" then misses the underlying drug record. SHORT
		// names must survive regardless of how many non-SHORT synonyms exist.
		Concept c = new Concept();
		c.addName(preferredName("Dolutegravir"));
		c.addName(shortName("DTG"));
		// 12 non-SHORT names — would normally fill the cap on their own.
		for (char ch = 'a'; ch <= 'l'; ch++) {
			c.addName(name("brand-" + ch));
		}

		List<String> synonyms = ConceptNameUtil.getSynonyms(c);
		// SHORT name is always present, regardless of alphabetic position relative to non-SHORT.
		assertTrue("SHORT name 'DTG' must be promoted unconditionally", synonyms.contains("DTG"));
		// And the non-SHORT cap still applies to the rest.
		long nonShortCount = synonyms.stream().filter(s -> !s.equals("DTG")).count();
		assertEquals("non-SHORT names are still capped at MAX_NON_SHORT_SYNONYMS", 10, nonShortCount);
	}

	@Test
	public void getSynonyms_shortNamesPrecedeNonShortInResultOrder() {
		// Order matters for downstream backends that truncate or stream the list. SHORT-typed
		// abbreviations come first, then non-SHORT synonyms — both alphabetically within their
		// bucket. This locks the contract so a refactor swapping bucket order can't silently
		// flip the visible-prefix slice.
		Concept c = new Concept();
		c.addName(preferredName("Hypertension"));
		c.addName(shortName("HTN"));
		c.addName(name("Arterial hypertension"));
		c.addName(name("High blood pressure"));

		assertEquals(Arrays.asList("HTN", "Arterial hypertension", "High blood pressure"),
				ConceptNameUtil.getSynonyms(c));
	}

	@Test
	public void getSynonyms_nonShortNameTypesRouteToNonShortBucket() {
		// The SHORT check is "type == ConceptNameType.SHORT" — it must reject every other
		// possible value (null, FULLY_SPECIFIED, INDEX_TERM) so those names land in the
		// otherNames bucket and obey the cap. Real CIEL data has plenty of null-typed names;
		// a refactor that switches to "type.isShort()" or similar would NPE without this test.
		Concept c = new Concept();
		c.addName(preferredName("Hypertension"));
		// Null-typed name (the default when concept_name_type column is null in CIEL data)
		c.addName(name("untyped-synonym"));
		// Explicit FULLY_SPECIFIED — also non-SHORT
		ConceptName fqn = name("fully-specified-variant");
		fqn.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		c.addName(fqn);
		// INDEX_TERM — also non-SHORT
		ConceptName indexTerm = name("index-term-variant");
		indexTerm.setConceptNameType(ConceptNameType.INDEX_TERM);
		c.addName(indexTerm);
		// SHORT, for contrast — must come first in result.
		c.addName(shortName("ZZZ-short"));

		List<String> synonyms = ConceptNameUtil.getSynonyms(c);
		// SHORT name first (regardless of alphabetic position), then non-SHORT bucket alpha.
		assertEquals(Arrays.asList("ZZZ-short",
				"fully-specified-variant", "index-term-variant", "untyped-synonym"),
				synonyms);
	}

	@Test
	public void getSynonyms_exactlyTenNonShortNames_keepsAllTen() {
		// Boundary test pinning the cap=10 termination. A refactor flipping `if (cap == 0)`
		// to `if (cap <= 0)` would silently include 11 entries; flipping to `if (cap < 1)`
		// would lose entry 10. With exactly MAX_NON_SHORT_SYNONYMS entries the result must
		// contain all of them with no off-by-one drop.
		Concept c = new Concept();
		c.addName(preferredName("Primary"));
		c.addName(name("a"));
		c.addName(name("b"));
		c.addName(name("c"));
		c.addName(name("d"));
		c.addName(name("e"));
		c.addName(name("f"));
		c.addName(name("g"));
		c.addName(name("h"));
		c.addName(name("i"));
		c.addName(name("j"));

		assertEquals(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j"),
				ConceptNameUtil.getSynonyms(c));
	}

	@Test
	public void getSynonyms_locale_filterAppliesToShortNamesToo() {
		// SHORT-name promotion must not bypass the locale filter — a German SHORT name is
		// still German vocabulary and would pollute an English-locale deployment's BM25
		// channel with non-locale terms. The filter sits before the type check so a refactor
		// that swaps the order silently breaks this; the test pins the precedence down.
		Concept c = new Concept();
		c.addName(preferredName("Hypertension"));
		ConceptName germanShort = localizedName("Bluthochdruck-Abk", Locale.GERMAN);
		germanShort.setConceptNameType(ConceptNameType.SHORT);
		c.addName(germanShort);
		c.addName(shortName("HTN"));

		// Default locale is en_GB (no Context); only the English SHORT name survives.
		assertEquals(Arrays.asList("HTN"), ConceptNameUtil.getSynonyms(c));
	}

	@Test
	public void getSynonyms_dedupesSameStringBetweenShortAndNonShortBuckets() {
		// Real-world dictionaries occasionally register the same string twice — once tagged
		// SHORT and once with a null/SYNONYM type. The SHORT bucket already carries the entry;
		// the non-SHORT pass must skip it to prevent the result list from double-counting.
		Concept c = new Concept();
		c.addName(preferredName("Hypertension"));
		c.addName(shortName("HTN"));
		c.addName(name("HTN"));
		c.addName(name("High blood pressure"));

		assertEquals(Arrays.asList("HTN", "High blood pressure"),
				ConceptNameUtil.getSynonyms(c));
	}

	@Test
	public void getSynonyms_excludesPreferredName() {
		Concept c = new Concept();
		c.addName(preferredName("Hypertension"));
		c.addName(name("HTN"));

		assertEquals(Arrays.asList("HTN"), ConceptNameUtil.getSynonyms(c));
	}

	@Test
	public void getSynonyms_dropsDuplicateOfPreferredAcrossNameInstances() {
		// Same name string can appear twice in concept.getNames() (e.g., once preferred, once as
		// a synonym in the same dictionary). Our string-level dedupe should drop the duplicate.
		Concept c = new Concept();
		c.addName(preferredName("Hypertension"));
		c.addName(name("Hypertension"));
		c.addName(name("HTN"));

		assertEquals(Arrays.asList("HTN"), ConceptNameUtil.getSynonyms(c));
	}

	@Test
	public void getSynonyms_filtersByLanguage() {
		Concept c = new Concept();
		c.addName(preferredName("Hypertension"));
		c.addName(name("HTN"));
		c.addName(localizedName("Hypertonie", Locale.GERMAN));

		// Default locale resolves to en_GB (no Context); German synonym is filtered out.
		assertEquals(Arrays.asList("HTN"), ConceptNameUtil.getSynonyms(c));
	}

	@Test
	public void getDescription_returnsEmpty_whenConceptNull() {
		assertEquals("", ConceptNameUtil.getDescription(null));
	}

	@Test
	public void getDescription_returnsEmpty_whenNoDescriptions() {
		// Common case in real data: ~40-60% of CIEL concepts have no description. Empty-string
		// return means putConceptFields skips writing the metadata key — keeps the doc compact.
		Concept c = new Concept();
		c.addName(preferredName("Asthma"));
		assertEquals("", ConceptNameUtil.getDescription(c));
	}

	@Test
	public void getDescription_returnsLanguageMatch() {
		Concept c = new Concept();
		c.addName(preferredName("Hypertension"));
		c.addDescription(description("Persistently high arterial blood pressure.", Locale.ENGLISH));
		assertEquals("Persistently high arterial blood pressure.",
				ConceptNameUtil.getDescription(c));
	}

	@Test
	public void getDescription_fallsBackToAnyLocale_whenNoLanguageMatch() {
		// CIEL frequently ships only "en" descriptions; deployments may run with non-English
		// default locales. Returning the only available description beats no description at all
		// for BM25 vocabulary — the alternative is dropping retrieval signal entirely.
		Concept c = new Concept();
		c.addName(preferredName("Hypertension"));
		c.addDescription(description("Hypertonie - persistierend hoher Blutdruck.", Locale.GERMAN));
		// Default locale resolves to en_GB (no Context); no language match → fall back to the only
		// available description (German), keeping the BM25 signal rather than dropping it.
		assertEquals("Hypertonie - persistierend hoher Blutdruck.",
				ConceptNameUtil.getDescription(c));
	}

	@Test
	public void getDescription_prefersLanguageMatchOverFirstEntry() {
		// When a concept has descriptions in multiple locales, the active-language match wins
		// over insertion order — locking the per-locale routing down so a future change to
		// LinkedHashSet ordering can't silently swap which description gets indexed.
		Concept c = new Concept();
		c.addName(preferredName("Hypertension"));
		c.addDescription(description("Hypertonie.", Locale.GERMAN));
		c.addDescription(description("Persistently high arterial blood pressure.", Locale.ENGLISH));
		c.addDescription(description("Hypertension artérielle.", Locale.FRENCH));
		assertEquals("Persistently high arterial blood pressure.",
				ConceptNameUtil.getDescription(c));
	}

	@Test
	public void getDescription_skipsNullAndEmptyTextEntries() {
		// Real OpenMRS data has been seen with placeholder/null description rows. Skipping them
		// and continuing the scan ensures a usable description from another row is still found.
		Concept c = new Concept();
		c.addName(preferredName("Hypertension"));
		c.addDescription(description(null, Locale.ENGLISH));
		c.addDescription(description("", Locale.ENGLISH));
		c.addDescription(description("Persistently high arterial blood pressure.", Locale.ENGLISH));
		assertEquals("Persistently high arterial blood pressure.",
				ConceptNameUtil.getDescription(c));
	}

	@Test
	public void getDescription_skipsFormPromptInstructionBoilerplate() {
		// CIEL Question-class concepts carry form-authoring prompt instructions ("Question on
		// encounter form: 'Does the patient have any of the following conditions?'") that stuff
		// conversational query words ("does", "patient", "have", "any") into the BM25 channel.
		Concept formPromptOnly = new Concept();
		formPromptOnly.addName(preferredName("Adult WHO condition query"));
		formPromptOnly.addDescription(description(
				"Question on encounter form: 'Does the patient have any of the following conditions?'",
				Locale.ENGLISH));
		assertEquals("", ConceptNameUtil.getDescription(formPromptOnly));

		Concept withClinicalFallback = new Concept();
		withClinicalFallback.addName(preferredName("Serum creatinine"));
		withClinicalFallback.addDescription(description(
				"Question on encounter form: 'Enter latest serum creatinine'", Locale.ENGLISH));
		withClinicalFallback.addDescription(description(
				"Laboratory test that determines a blood serum creatinine level. Often used with BUN to assess kidney function.",
				Locale.ENGLISH));
		assertEquals(
				"Laboratory test that determines a blood serum creatinine level. Often used with BUN to assess kidney function.",
				ConceptNameUtil.getDescription(withClinicalFallback));
	}

	@Test
	public void getMappingNames_returnsEmpty_whenConceptNull() {
		assertTrue(ConceptNameUtil.getMappingNames(null).isEmpty());
	}

	@Test
	public void getMappingNames_returnsEmpty_whenNoMappings() {
		// Common case in real data: ~13% of demo concepts have zero mappings. Empty return means
		// putMappingNames skips writing the metadata key, keeping the doc compact.
		Concept c = new Concept();
		c.addName(preferredName("Asthma"));
		assertTrue(ConceptNameUtil.getMappingNames(c).isEmpty());
	}

	@Test
	public void getMappingNames_returnsEmpty_whenAllTermsLackNames() {
		// SNOMED mappings on this demo are code-only (term.name is null across all 17,037 rows)
		// because OCL doesn't redistribute SNOMED's PT/FSN. The slice must degrade to "no
		// signal" rather than indexing null/empty strings into BM25.
		Concept c = conceptWithMappings("Blood urea nitrogen",
				mappingTo(term(null, "72341003")),
				mappingTo(term("", "857")),
				mappingTo(term("   ", "857")));
		assertTrue(ConceptNameUtil.getMappingNames(c).isEmpty());
	}

	@Test
	public void getMappingNames_returnsPopulatedNames_alphabeticallySorted() {
		// Mirror the empirical data on the demo: BUN has LOINC, PIH, CIEL, SNOMED mappings.
		// Three carry names (LOINC, PIH, CIEL), one does not (SNOMED). Result is alphabetic so
		// indexer output is deterministic across runs.
		Concept c = conceptWithMappings("Blood urea nitrogen",
				mappingTo(term("Urea nitrogen [Moles/volume] in Serum or Plasma", "14937-7")),
				mappingTo(term("Urea (BUN)", "857")),
				mappingTo(term("Blood urea nitrogen", "857")),
				mappingTo(term(null, "72341003")));

		assertEquals(Arrays.asList(
				"Blood urea nitrogen",
				"Urea (BUN)",
				"Urea nitrogen [Moles/volume] in Serum or Plasma"),
				ConceptNameUtil.getMappingNames(c));
	}

	@Test
	public void getMappingNames_trimsWhitespaceAndDropsBlanks() {
		// Real-world data has been seen with names that are just whitespace (placeholder rows
		// during bulk imports). Trim and drop so the BM25 channel doesn't index empty tokens.
		Concept c = conceptWithMappings("Hypertension",
				mappingTo(term("   Essential hypertension   ", "I10")),
				mappingTo(term("\n\t  \n", "999")));

		assertEquals(Arrays.asList("Essential hypertension"),
				ConceptNameUtil.getMappingNames(c));
	}

	@Test
	public void getMappingNames_dedupesSameStringAcrossSources() {
		// PIH and CIEL frequently both ship the literal "Chronic kidney disease". TreeSet-based
		// dedupe drops the duplicate so a single common phrase doesn't get double TF weight.
		Concept c = conceptWithMappings("Chronic kidney insufficiency",
				mappingTo(term("Chronic kidney disease", "3699")),
				mappingTo(term("Chronic kidney disease", "N18.9")),
				mappingTo(term("Chronic kidney disease, unspecified", "N18.9-alt")));

		assertEquals(Arrays.asList(
				"Chronic kidney disease",
				"Chronic kidney disease, unspecified"),
				ConceptNameUtil.getMappingNames(c));
	}

	@Test
	public void getMappingNames_skipsRetiredReferenceTerms() {
		// A retired reference term carries stale vocabulary by design. Excluding it keeps the
		// BM25 channel current with the dictionary's living concept-to-code authority.
		ConceptReferenceTerm liveTerm = term("Live mapping", "A00.0");
		ConceptReferenceTerm retiredTerm = term("Stale mapping", "A00.1");
		retiredTerm.setRetired(Boolean.TRUE);
		Concept c = conceptWithMappings("Some condition",
				mappingTo(liveTerm),
				mappingTo(retiredTerm));

		assertEquals(Arrays.asList("Live mapping"),
				ConceptNameUtil.getMappingNames(c));
	}

	@Test
	public void getMappingNames_retiredTermDedupeBoundary() {
		// Two mappings carry the literal same name "Chronic kidney disease", but one term is
		// retired. The retired-filter must run BEFORE TreeSet dedupe so the surviving entry is
		// the live one, not whichever happens to win an undefined comparison. A refactor that
		// re-orders the filter passes (e.g. dedupes first, then filters retired) would silently
		// drop the live entry and keep nothing — this test pins the precedence.
		ConceptReferenceTerm liveTerm = term("Chronic kidney disease", "3699");
		ConceptReferenceTerm retiredTerm = term("Chronic kidney disease", "N18.9");
		retiredTerm.setRetired(Boolean.TRUE);
		Concept c = conceptWithMappings("Condition",
				mappingTo(liveTerm),
				mappingTo(retiredTerm));

		assertEquals(Arrays.asList("Chronic kidney disease"),
				ConceptNameUtil.getMappingNames(c));
	}

	@Test
	public void getMappingNames_elevenEntries_dropsTheAlphabeticallyLastOne() {
		// Cap+1 boundary. Eleven distinct names exercise the loop's termination condition: with
		// `cap == MAX_MAPPING_NAMES` and a `break` at `cap == 0`, the eleventh alphabetic entry
		// must be dropped. A refactor flipping `==` to `<=` would silently include 11; flipping
		// to `<` would drop entry 10. Neither boundary is caught by the size-15 or size-10
		// tests on their own.
		ConceptMap[] mappings = new ConceptMap[11];
		for (int i = 0; i < 11; i++) {
			char ch = (char) ('a' + i);
			mappings[i] = mappingTo(term("name-" + ch, "Z" + i));
		}
		Concept c = conceptWithMappings("Boundary", mappings);

		List<String> result = ConceptNameUtil.getMappingNames(c);
		assertEquals(10, result.size());
		assertTrue("entry 11 ('name-k') is alphabetically last and must be dropped",
				!result.contains("name-k"));
	}

	@Test
	public void getMappingNames_capsAtMaxMappingNames() {
		// Pathological concept with 15 distinct populated mapping names exercises the cap.
		// Result keeps the alphabetically-first 10; locks the order so a refactor that
		// switches data structures must preserve the determinism contract.
		ConceptMap[] mappings = new ConceptMap[15];
		for (int i = 0; i < 15; i++) {
			char ch = (char) ('a' + i);
			mappings[i] = mappingTo(term("name-" + ch, "Z" + i));
		}
		Concept c = conceptWithMappings("Pathological", mappings);

		assertEquals(Arrays.asList(
				"name-a", "name-b", "name-c", "name-d", "name-e",
				"name-f", "name-g", "name-h", "name-i", "name-j"),
				ConceptNameUtil.getMappingNames(c));
	}

	@Test
	public void getMappingNames_exactlyCapEntries_keepsAllOfThem() {
		// Boundary test for the cap loop. A refactor flipping the limit check off-by-one would
		// either drop entry 10 or include entry 11; with exactly MAX_MAPPING_NAMES entries
		// (10) the result must contain all of them.
		ConceptMap[] mappings = new ConceptMap[10];
		for (int i = 0; i < 10; i++) {
			char ch = (char) ('a' + i);
			mappings[i] = mappingTo(term("name-" + ch, "Z" + i));
		}
		Concept c = conceptWithMappings("Boundary", mappings);
		assertEquals(10, ConceptNameUtil.getMappingNames(c).size());
		assertEquals("name-j",
				ConceptNameUtil.getMappingNames(c).get(9));
	}

	@Test
	public void getMappingCodes_namespacesEachLiveCodeBySourceSortedAndDeduplicated() {
		// SNOMED terms on the demo carry a code and no name, so the code is the only handle a
		// question naming "SNOMED-CT:72341003" or "CIEL:857" has on the concept.
		ConceptReferenceTerm retired = sourcedTerm("CIEL", "999", null);
		retired.setRetired(true);
		Concept c = conceptWithMappings("Blood urea nitrogen",
				mappingTo(sourcedTerm("LOINC", "14937-7", "Urea nitrogen [Moles/volume] in Serum or Plasma")),
				mappingTo(sourcedTerm("CIEL", "857", "Blood urea nitrogen")),
				mappingTo(sourcedTerm("PIH", "857", "Urea (BUN)")),
				mappingTo(sourcedTerm("SNOMED-CT", "72341003", null)),
				mappingTo(sourcedTerm("CIEL", " 857 ", null)),
				mappingTo(sourcedTerm(null, "123", null)),
				mappingTo(sourcedTerm("CIEL", "  ", null)),
				mappingTo(retired));

		assertEquals(Arrays.asList("CIEL:857", "LOINC:14937-7", "PIH:857", "SNOMED-CT:72341003"),
				ConceptNameUtil.getMappingCodes(c));
		assertTrue(ConceptNameUtil.getMappingCodes(null).isEmpty());
	}

	@Test
	public void getMappingCodes_writesASourceNameWithSpacesAsOneTypableToken() {
		// CIEL-based dictionaries name sources "SNOMED CT" and "IMO ProblemIT". A question can only
		// name a code as one token, so the stored namespace is that token: "SNOMED-CT:72341003".
		Concept c = conceptWithMappings("Hypertension",
				mappingTo(sourcedTerm("SNOMED CT", "38341003", null)),
				mappingTo(sourcedTerm("IMO  ProblemIT", "10000", null)));

		assertEquals(Arrays.asList("IMO-ProblemIT:10000", "SNOMED-CT:38341003"), ConceptNameUtil.getMappingCodes(c));
	}

	@Test
	public void getMappingCodes_keepsEveryLiveCodeHoweverManyMappingsAConceptHas() {
		// Only exact matching reads codes, and an alphabetical cap of ten cost Cough (13 live mappings)
		// its SNOMED CT code on a refapp standalone, so a question naming it found no exact match.
		ConceptMap[] mappings = new ConceptMap[13];
		for (int i = 0; i < 12; i++) {
			mappings[i] = mappingTo(sourcedTerm("CIEL", String.valueOf(100 + i), null));
		}
		mappings[12] = mappingTo(sourcedTerm("SNOMED CT", "49727002", null));

		java.util.List<String> codes = ConceptNameUtil.getMappingCodes(conceptWithMappings("Cough", mappings));

		assertEquals(13, codes.size());
		assertTrue(codes.contains("SNOMED-CT:49727002"));
	}

	private static ConceptReferenceTerm sourcedTerm(String source, String code, String name) {
		ConceptReferenceTerm t = term(name, code);
		if (source != null) {
			org.openmrs.ConceptSource conceptSource = new org.openmrs.ConceptSource();
			conceptSource.setName(source);
			t.setConceptSource(conceptSource);
		}
		return t;
	}

	private static ConceptReferenceTerm term(String name, String code) {
		ConceptReferenceTerm t = new ConceptReferenceTerm();
		t.setName(name);
		t.setCode(code);
		// ConceptReferenceSource construction is heavyweight to stub; tests don't read source
		// off the term so we leave conceptSource null. The slice never dereferences source from
		// the term — it pulls names directly off the term itself.
		return t;
	}

	private static ConceptMap mappingTo(ConceptReferenceTerm term) {
		ConceptMap m = new ConceptMap();
		m.setConceptReferenceTerm(term);
		return m;
	}

	private static Concept conceptWithMappings(String preferred, ConceptMap... mappings) {
		// Concept.addConceptMapping touches Context.getConceptService() through ConceptMap.equals
		// on the LinkedHashSet add path — fine in production but breaks unit tests with no Spring
		// context. Setting the mappings collection directly bypasses the set-equality path while
		// preserving the iteration order the production code relies on.
		Concept c = new Concept();
		c.addName(preferredName(preferred));
		Set<ConceptMap> set = new LinkedHashSet<>();
		for (ConceptMap m : mappings) {
			set.add(m);
		}
		c.setConceptMappings(set);
		return c;
	}

	private static ConceptDescription description(String text, Locale locale) {
		ConceptDescription d = new ConceptDescription();
		d.setDescription(text);
		d.setLocale(locale);
		return d;
	}

	private static ConceptName name(String text) {
		return localizedName(text, Locale.ENGLISH);
	}

	private static ConceptName shortName(String text) {
		ConceptName cn = name(text);
		cn.setConceptNameType(ConceptNameType.SHORT);
		return cn;
	}

	private static ConceptName preferredName(String text) {
		ConceptName cn = name(text);
		cn.setLocalePreferred(Boolean.TRUE);
		return cn;
	}

	private static ConceptName localizedName(String text, Locale locale) {
		ConceptName cn = new ConceptName();
		cn.setName(text);
		cn.setLocale(locale);
		return cn;
	}
}
