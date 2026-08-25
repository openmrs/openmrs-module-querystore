/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 */
package org.openmrs.module.querystore.api.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/** Direct contract coverage for the deterministic context-slice interpreter. */
public class ContextQuestionInterpreterTest {

	@Test
	public void interpret_recognizesEveryTypedCueFamily() {
		assertTypes("Which medications and prescriptions are active?", "drug_order", "medication_dispense");
		assertTypes("Any drug allergies or adverse reactions?", "drug_order", "medication_dispense", "allergy");
		assertTypes("Which programs is the patient enrolled in?", "program");
		assertTypes("What diagnoses are on the problem list?", "condition", "diagnosis");
		assertTypes("Show visits and encounters", "visit", "encounter");
		assertTypes("Which tests were ordered?", "drug_order", "test_order", "referral_order");
	}

	@Test
	public void interpret_recognizesTemporalCuesWithoutInventingTypes() {
		ContextQuestionInterpreter.Interpretation interpretation =
		        ContextQuestionInterpreter.interpret("What was the most recent result in the past 6 months?");

		assertTrue(interpretation.temporal);
		assertTrue(interpretation.types.isEmpty());
	}

	@Test
	public void preprocess_expandsEverySupportedPanelOnlyOnce() {
		for (String abbreviation : Arrays.asList("BMP", "CMP", "CBC", "LFT", "LFTs", "RFT", "RFTs", "ABG", "ESR", "CRP")) {
			String once = ContextQuestionInterpreter.preprocess("latest " + abbreviation + " results");
			assertEquals("preprocessing must be idempotent for " + abbreviation, once,
			        ContextQuestionInterpreter.preprocess(once));
		}
	}

	@Test
	public void preprocess_keepsAllTermsWhenStopwordRemovalWouldLeaveOne() {
		assertEquals("the and", ContextQuestionInterpreter.preprocess("the and"));
	}

	private static void assertTypes(String question, String... expected) {
		Set<String> expectedTypes = new HashSet<String>(Arrays.asList(expected));
		ContextQuestionInterpreter.Interpretation interpretation = ContextQuestionInterpreter.interpret(question);
		assertEquals(question, expectedTypes, interpretation.types);
		assertFalse(question + " should not imply recency", interpretation.temporal);
	}
}
