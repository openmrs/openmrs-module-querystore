/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.backend;

import static org.junit.Assert.assertEquals;

import java.util.PriorityQueue;

import org.junit.Test;
import org.openmrs.module.querystore.model.QueryDocument;

public class TopKHitsTest {

	/** The slice's similarity leg can ask for a limit near Integer.MAX_VALUE (715827883 tripled). */
	@Test
	public void aLimitAtIntegerMaxValueStillRanksTheHitsItIsOffered() {
		int limit = Integer.MAX_VALUE;
		PriorityQueue<Hit> heap = TopKHits.heap(limit);
		TopKHits.offer(heap, hit("low", 0.1), limit);
		TopKHits.offer(heap, hit("high", 0.9), limit);

		SearchResult result = TopKHits.materialise(heap, limit);

		assertEquals(2, result.getHits().size());
		assertEquals("high", result.getHits().get(0).getDocument().getResourceUuid());
		assertEquals(1, result.getHits().get(0).getRank());
	}

	private static Hit hit(String uuid, double score) {
		QueryDocument doc = new QueryDocument();
		doc.setResourceType("obs");
		doc.setResourceUuid(uuid);
		return new Hit(doc, score, 0);
	}
}
