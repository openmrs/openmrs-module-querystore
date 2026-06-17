/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_DESCRIPTION;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_MAPPING_NAMES;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_OBS_GROUP_CONCEPT_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_OBS_GROUP_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_SYNONYMS;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class QueryDocumentTest {

	@Test
	public void getEmbeddingInput_textOnly() {
		QueryDocument doc = new QueryDocument();
		doc.setText("Systolic blood pressure: 120 mmHg");
		assertEquals("Systolic blood pressure: 120 mmHg", doc.getEmbeddingInput());
	}

	@Test
	public void getEmbeddingInput_groupConceptPrefixed() {
		QueryDocument doc = new QueryDocument();
		doc.setText("Systolic blood pressure: 120 mmHg");
		doc.putMetadata(FIELD_OBS_GROUP_CONCEPT_NAME, "Vital signs");
		assertEquals("Vital signs — Systolic blood pressure: 120 mmHg", doc.getEmbeddingInput());
	}

	@Test
	public void getEmbeddingInput_synonymsAppended() {
		QueryDocument doc = new QueryDocument();
		doc.setText("Systolic blood pressure: 120 mmHg");
		doc.putMetadata(FIELD_SYNONYMS, Arrays.asList("SBP", "Systolic BP"));
		assertEquals("Systolic blood pressure: 120 mmHg SBP Systolic BP", doc.getEmbeddingInput());
	}

	@Test
	public void getEmbeddingInput_groupAndSynonymsCombined() {
		QueryDocument doc = new QueryDocument();
		doc.setText("Systolic blood pressure: 120 mmHg");
		doc.putMetadata(FIELD_OBS_GROUP_CONCEPT_NAME, "Vital signs");
		doc.putMetadata(FIELD_SYNONYMS, Collections.singletonList("SBP"));
		assertEquals("Vital signs — Systolic blood pressure: 120 mmHg SBP", doc.getEmbeddingInput());
	}

	@Test
	public void getEmbeddingInput_emptyGroupNameSkipped() {
		QueryDocument doc = new QueryDocument();
		doc.setText("Systolic blood pressure: 120 mmHg");
		doc.putMetadata(FIELD_OBS_GROUP_CONCEPT_NAME, "");
		assertEquals("Systolic blood pressure: 120 mmHg", doc.getEmbeddingInput());
	}

	@Test
	public void getEmbeddingInput_emptySynonymsListSkipped() {
		QueryDocument doc = new QueryDocument();
		doc.setText("Systolic blood pressure: 120 mmHg");
		doc.putMetadata(FIELD_SYNONYMS, Collections.emptyList());
		assertEquals("Systolic blood pressure: 120 mmHg", doc.getEmbeddingInput());
	}

	@Test
	public void getEmbeddingInput_groupPrefixDroppedWhenTextAbsent() {
		QueryDocument doc = new QueryDocument();
		doc.putMetadata(FIELD_OBS_GROUP_CONCEPT_NAME, "Vital signs");
		assertEquals("", doc.getEmbeddingInput());
	}

	@Test
	public void getEmbeddingInput_synonymsCarrySignalWithoutText() {
		QueryDocument doc = new QueryDocument();
		doc.putMetadata(FIELD_SYNONYMS, Arrays.asList("SBP", "Systolic BP"));
		assertEquals("SBP Systolic BP", doc.getEmbeddingInput());
	}

	@Test
	public void getEmbeddingInput_emptyAndNullSynonymsFiltered() {
		QueryDocument doc = new QueryDocument();
		doc.setText("Systolic blood pressure: 120 mmHg");
		doc.putMetadata(FIELD_SYNONYMS, Arrays.asList("SBP", "", null, "Systolic BP"));
		assertEquals("Systolic blood pressure: 120 mmHg SBP Systolic BP", doc.getEmbeddingInput());
	}

	@Test
	public void getEmbeddingInput_descriptionIsExcluded() {
		// ADR Decision 6: description is BM25-only, deliberately NOT in the embedding input,
		// to avoid the asymmetric-bias concern. A refactor that appends description text onto
		// the embedding input would silently break the architectural contract — this test
		// pins the exclusion.
		QueryDocument doc = new QueryDocument();
		doc.setText("Blood urea nitrogen: 100 mg/dL");
		doc.putMetadata(FIELD_DESCRIPTION, "Lab test reflecting kidney function. Used to assess renal status.");
		String embedded = doc.getEmbeddingInput();
		assertEquals("Blood urea nitrogen: 100 mg/dL", embedded);
		assertFalse("description text must not appear in embedding input",
				embedded.contains("kidney") || embedded.contains("renal"));
	}

	@Test
	public void getEmbeddingInput_mappingNamesAreExcluded() {
		// ADR Decision 6: mapping_names is BM25-only, deliberately NOT in the embedding input,
		// same asymmetric-bias rule as description. The slice that adds external-authority
		// vocabulary (LOINC / ICD-10 / PIH / CIEL drug-class parents) into a BM25-only channel
		// would lose its architectural justification if mapping_names started feeding the
		// embedder — embedding-input pairwise discrimination on a small embedder like
		// all-MiniLM-L6-v2 would degrade from vocabulary-overlapping mapping text.
		QueryDocument doc = new QueryDocument();
		doc.setText("Condition: Chronic kidney insufficiency. Status: ACTIVE");
		doc.putMetadata(FIELD_MAPPING_NAMES, Arrays.asList(
				"Chronic kidney disease, unspecified",
				"Chronic kidney disease",
				"Heparins"));
		String embedded = doc.getEmbeddingInput();
		assertEquals("Condition: Chronic kidney insufficiency. Status: ACTIVE", embedded);
		assertFalse("mapping_names list must not feed the embedding input",
				embedded.contains("Heparins") || embedded.contains("unspecified"));
	}

	@Test
	public void obsGroupAccessors_returnTrimmedValuesForAMemberDocument() {
		QueryDocument doc = new QueryDocument();
		doc.putMetadata(FIELD_OBS_GROUP_UUID, "group-uuid-123");
		doc.putMetadata(FIELD_OBS_GROUP_CONCEPT_NAME, "  Vital signs  ");
		assertEquals("group-uuid-123", doc.getObsGroupUuid());
		assertEquals("a clean (trimmed) String view of the group concept name", "Vital signs",
				doc.getObsGroupConceptName());
	}

	@Test
	public void obsGroupAccessors_returnNullForANonMemberDocument() {
		QueryDocument doc = new QueryDocument();
		assertNull("a non-group-member document has no obs_group_uuid", doc.getObsGroupUuid());
		assertNull("a non-group-member document has no obs_group_concept_name", doc.getObsGroupConceptName());
	}

	@Test
	public void obsGroupAccessors_normalizeBlankToNull() {
		// Identity-field contract: blank normalizes to null (distinct from the BM25-text getXxxText
		// accessors, which return ""). A blank cluster key would otherwise collapse non-member and
		// empty-name documents together.
		QueryDocument doc = new QueryDocument();
		doc.putMetadata(FIELD_OBS_GROUP_UUID, "   ");
		doc.putMetadata(FIELD_OBS_GROUP_CONCEPT_NAME, "");
		assertNull("blank obs_group_uuid normalizes to null, not the blank string", doc.getObsGroupUuid());
		assertNull("empty obs_group_concept_name normalizes to null", doc.getObsGroupConceptName());
	}
}
