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
import java.util.Locale;
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
	public void preprocess_preservesFullSentenceContextAndClinicalQualifiers() {
		assertEquals("is the patient well controlled on metformin?",
		        ContextQuestionInterpreter.preprocess("is the patient well controlled on metformin?"));
		assertEquals("is the patient poorly controlled on metformin?",
		        ContextQuestionInterpreter.preprocess("is the patient poorly controlled on metformin?"));
	}

	@Test
	public void preprocess_preservesSingleCharacterEntitiesAndNumerals() {
		assertEquals("history of hepatitis A and cirrhosis",
		        ContextQuestionInterpreter.preprocess("history of hepatitis A and cirrhosis"));
		assertEquals("history of hepatitis B and cirrhosis",
		        ContextQuestionInterpreter.preprocess("history of hepatitis B and cirrhosis"));
		assertEquals("vitamin A deficiency management plan",
		        ContextQuestionInterpreter.preprocess("vitamin A deficiency management plan"));
		assertEquals("vitamin D deficiency management plan",
		        ContextQuestionInterpreter.preprocess("vitamin D deficiency management plan"));
		assertEquals("influenza A positive swab result",
		        ContextQuestionInterpreter.preprocess("influenza A positive swab result"));
		assertEquals("influenza B positive swab result",
		        ContextQuestionInterpreter.preprocess("influenza B positive swab result"));
		assertEquals("blood type A negative transfusion record",
		        ContextQuestionInterpreter.preprocess("blood type A negative transfusion record"));
		assertEquals("blood type O negative transfusion record",
		        ContextQuestionInterpreter.preprocess("blood type O negative transfusion record"));
		assertEquals("management of type I diabetes with insulin",
		        ContextQuestionInterpreter.preprocess("management of type I diabetes with insulin"));
	}

	@Test
	public void preprocess_preservesLateralityPrepositionsAndAnatomy() {
		assertEquals("pain in the right knee", ContextQuestionInterpreter.preprocess("pain in the right knee"));
		assertEquals("pain in the left knee", ContextQuestionInterpreter.preprocess("pain in the left knee"));
		assertEquals("neither aspirin nor warfarin",
		        ContextQuestionInterpreter.preprocess("neither aspirin nor warfarin"));
		assertEquals("is the patient off metformin?", ContextQuestionInterpreter.preprocess("is the patient off metformin?"));
		assertEquals("chronic back pain management plan",
		        ContextQuestionInterpreter.preprocess("chronic back pain management plan"));
		assertEquals("history of low back pain and sciatica",
		        ContextQuestionInterpreter.preprocess("history of low back pain and sciatica"));
		assertEquals("history of Down syndrome and hypothyroidism",
		        ContextQuestionInterpreter.preprocess("history of Down syndrome and hypothyroidism"));
	}

	@Test
	public void preprocess_preservesMultilingualQueriesWithoutEnglishBias() {
		assertEquals("Le patient est-il sous insuline?",
		        ContextQuestionInterpreter.preprocess("Le patient est-il sous insuline?"));
		assertEquals("¿Está el paciente bien controlado?",
		        ContextQuestionInterpreter.preprocess("¿Está el paciente bien controlado?"));
		assertEquals("Je, mgonjwa anatumia dawa ya kisukari?",
		        ContextQuestionInterpreter.preprocess("Je, mgonjwa anatumia dawa ya kisukari?"));
	}

	private static void assertTypes(String question, String... expected) {
		Set<String> expectedTypes = new HashSet<String>(Arrays.asList(expected));
		ContextQuestionInterpreter.Interpretation interpretation = ContextQuestionInterpreter.interpret(question);
		assertEquals(question, expectedTypes, interpretation.types);
		assertFalse(question + " should not imply recency", interpretation.temporal);
	}
}
