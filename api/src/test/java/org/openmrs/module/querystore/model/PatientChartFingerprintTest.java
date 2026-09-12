/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 */
package org.openmrs.module.querystore.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.junit.Test;

public class PatientChartFingerprintTest {

	@Test
	public void snapshotId_canonicalizesUnorderedMetadataCollections() {
		QueryDocument first = documentWithAliases(new LinkedHashSet<String>(Arrays.asList("beta", "alpha")));
		QueryDocument second = documentWithAliases(new LinkedHashSet<String>(Arrays.asList("alpha", "beta")));

		assertEquals(PatientChartFingerprint.snapshotId(Collections.singletonList(first), false),
		        PatientChartFingerprint.snapshotId(Collections.singletonList(second), false));
	}

	private static QueryDocument documentWithAliases(LinkedHashSet<String> aliases) {
		QueryDocument document = new QueryDocument();
		document.setResourceType("obs");
		document.setResourceUuid("obs-1");
		document.setText("Observation");
		document.putMetadata("aliases", aliases);
		return document;
	}
}
