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

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Shared min-heap merge for backend search paths that fan out across per-type stores. Both the
 * MySQL and Lucene tiers collect up to {@code limit} hits per resource type and need to merge into
 * a single top-{@code limit} cross-type result; this helper keeps the merge cost at
 * {@code O(N log limit)} rather than the {@code O(N log N)} a full sort would pay. Score order is
 * descending — higher {@link Hit#getRawScore()} ranks first. Rank is assigned at materialisation
 * time and is 1-based per the SPI contract.
 */
public final class TopKHits {

	private TopKHits() {
	}

	public static PriorityQueue<Hit> heap(int limit) {
		// Min-heap on rawScore so the smallest-score outlier sits at the head — that's the one we
		// evict when a stronger hit arrives. No initial capacity: limit + 1 wraps at Integer.MAX_VALUE,
		// and a large limit would allocate its whole array before the first hit.
		return new PriorityQueue<>((a, b) -> Double.compare(a.getRawScore(), b.getRawScore()));
	}

	public static void offer(PriorityQueue<Hit> heap, Hit hit, int limit) {
		heap.offer(hit);
		if (heap.size() > limit) {
			heap.poll();
		}
	}

	public static SearchResult materialise(PriorityQueue<Hit> heap, int limit) {
		List<Hit> ordered = new ArrayList<>(heap);
		ordered.sort((a, b) -> Double.compare(b.getRawScore(), a.getRawScore()));
		int n = Math.min(ordered.size(), limit);
		List<Hit> ranked = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			Hit h = ordered.get(i);
			ranked.add(new Hit(h.getDocument(), h.getRawScore(), i + 1));
		}
		return new SearchResult(ranked);
	}
}
