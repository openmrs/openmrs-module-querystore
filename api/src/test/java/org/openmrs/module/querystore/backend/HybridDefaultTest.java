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
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.openmrs.module.querystore.model.QueryDocument;

/**
 * Covers the {@link BackendStore#hybrid(SearchRequest)} interface-default method directly so a
 * regression to the pre-Decision-3-cleanup shape (any backend throwing
 * {@code UnsupportedBackendOperationException} from hybrid, or the service layer bypassing the
 * SPI to fuse externally) fails here rather than only in tier-specific integration tests.
 */
public class HybridDefaultTest {

	@Test
	public void hybrid_defaultDelegatesToBm25AndKnnAndRrfFuses() {
		QueryDocument a = doc("A");
		QueryDocument b = doc("B");
		QueryDocument c = doc("C");
		QueryDocument d = doc("D");
		StubBackend backend = new StubBackend(
		    new SearchResult(Arrays.asList(new Hit(a, 0, 1), new Hit(b, 0, 2), new Hit(c, 0, 3))),
		    new SearchResult(Arrays.asList(new Hit(b, 0, 1), new Hit(d, 0, 2), new Hit(a, 0, 3))));

		SearchResult fused = backend.hybrid(SearchRequest.builder()
		    .queryText("q").queryVector(new float[] { 0.1f, 0.2f }).limit(10).build());

		assertTrue("bm25 must be called", backend.bm25Called.get());
		assertTrue("knn must be called when queryVector is present", backend.knnCalled.get());
		assertEquals(Arrays.asList("B", "A", "D", "C"),
		    fused.getHits().stream().map(h -> h.getDocument().getResourceUuid()).collect(java.util.stream.Collectors.toList()));
	}

	@Test
	public void hybrid_skipsKnnWhenNoQueryVector() {
		StubBackend backend = new StubBackend(
		    new SearchResult(Arrays.asList(new Hit(doc("A"), 0, 1))),
		    null);

		SearchResult result = backend.hybrid(SearchRequest.builder().queryText("q").limit(10).build());

		assertTrue("bm25 must be called", backend.bm25Called.get());
		assertEquals("knn must NOT be called when queryVector is null — degrades to BM25-only",
		    false, backend.knnCalled.get());
		assertEquals(1, result.getHits().size());
		assertEquals("A", result.getHits().get(0).getDocument().getResourceUuid());
	}

	@Test
	public void hybrid_skipsKnnWhenCapabilitiesReportNoKnnSupport() {
		// A BM25-only contributed backend (e.g., a PostgreSQL-FTS tier) declares supportsKnn=false
		// in its capabilities. The default must NOT call knn() — that backend isn't required to
		// implement it. Without this gate, the BM25-only backend would have to override hybrid()
		// or implement a stub knn() just to participate in the SPI.
		StubBackend backend = new StubBackend(
		    new SearchResult(Arrays.asList(new Hit(doc("A"), 0, 1))),
		    new SearchResult(Collections.emptyList()));
		backend.supportsKnn = false;

		SearchResult result = backend.hybrid(SearchRequest.builder()
		    .queryText("q").queryVector(new float[] { 0.1f, 0.2f }).limit(10).build());

		assertTrue("bm25 must be called", backend.bm25Called.get());
		assertEquals("knn must NOT be called on a BM25-only backend",
		    false, backend.knnCalled.get());
		assertEquals(1, result.getHits().size());
		assertEquals("A", result.getHits().get(0).getDocument().getResourceUuid());
	}

	@Test
	public void hybrid_overfetchesCandidateWindowBeforeRrfTruncation() {
		// When limit=1, pre-truncating bm25 and knn to 1 before RRF would never see rank-2
		// candidates that appear in both legs. Over-fetching candidate pools (max(limit * 3, 30))
		// lets RRF fuse the deeper pools before slicing to req.getLimit(). The stub returns no more
		// hits than each leg asks for, so C reaches the fusion only through the deeper pools.
		QueryDocument a = doc("A");
		QueryDocument b = doc("B");
		QueryDocument c = doc("C");
		StubBackend backend = new StubBackend(
		    new SearchResult(Arrays.asList(new Hit(a, 0, 1), new Hit(c, 0, 2))),
		    new SearchResult(Arrays.asList(new Hit(b, 0, 1), new Hit(c, 0, 2))));
		backend.honorLimit = true;

		SearchResult fused = backend.hybrid(SearchRequest.builder()
		    .queryText("q").queryVector(new float[] { 0.1f, 0.2f }).limit(1).build());

		assertEquals(30, backend.lastBm25Limit);
		assertEquals(30, backend.lastKnnLimit);
		assertEquals(1, fused.getHits().size());
		assertEquals("C", fused.getHits().get(0).getDocument().getResourceUuid());
	}

	@Test
	public void hybrid_capsTheCandidateWindowInsteadOfWrappingALargeLimit() {
		// Tripled as an int, 715827883 wraps negative and the floor of 30 would take over.
		StubBackend backend = new StubBackend(new SearchResult(Collections.emptyList()),
		    new SearchResult(Collections.emptyList()));

		backend.hybrid(SearchRequest.builder().queryText("q").queryVector(new float[] { 0.1f, 0.2f })
		    .limit(715827883).build());

		assertEquals(Integer.MAX_VALUE, backend.lastBm25Limit);
		assertEquals(Integer.MAX_VALUE, backend.lastKnnLimit);
	}

	private static QueryDocument doc(String uuid) {
		QueryDocument d = new QueryDocument();
		d.setResourceUuid(uuid);
		return d;
	}

	private static final class StubBackend implements BackendStore {
		final AtomicBoolean bm25Called = new AtomicBoolean();
		final AtomicBoolean knnCalled = new AtomicBoolean();
		int lastBm25Limit = -1;
		int lastKnnLimit = -1;
		boolean supportsKnn = true;
		private final SearchResult bm25Result;
		private final SearchResult knnResult;

		StubBackend(SearchResult bm25Result, SearchResult knnResult) {
			this.bm25Result = bm25Result;
			this.knnResult = knnResult;
		}

		/** When set, each leg returns at most the hits its request asks for, as a real backend does. */
		boolean honorLimit;

		@Override public SearchResult bm25(SearchRequest req) { bm25Called.set(true); lastBm25Limit = req.getLimit(); return limited(bm25Result, req); }
		@Override public SearchResult knn(SearchRequest req) { knnCalled.set(true); lastKnnLimit = req.getLimit(); return limited(knnResult, req); }

		private SearchResult limited(SearchResult result, SearchRequest req) {
			if (!honorLimit || req.getLimit() <= 0 || result.getHits().size() <= req.getLimit()) {
				return result;
			}
			return new SearchResult(result.getHits().subList(0, req.getLimit()));
		}

		@Override public void ensureSchema(String resourceType, SchemaSpec spec) { }
		@Override public void deleteSchema(String resourceType) { }
		@Override public WriteResult upsert(QueryDocument doc) { return null; }
		@Override public WriteResult delete(String resourceType, String resourceUuid) { return null; }
		@Override public BulkWriteResult bulkUpsert(List<QueryDocument> docs) { return null; }
		@Override public BulkWriteResult bulkDelete(String resourceType, List<String> uuids) { return null; }
		@Override public BulkWriteResult bulkDeleteByPatient(String patientUuid) { return null; }
		@Override public boolean existsByPatient(String patientUuid) { return false; }
		@Override public List<QueryDocument> findAllByPatient(String patientUuid) { return java.util.Collections.emptyList(); }
		@Override public BackendCapabilities capabilities() {
			return new BackendCapabilities(supportsKnn, false, false, 1_000_000, EnumSet.allOf(Filter.Kind.class));
		}
		@Override public HealthStatus health() { return null; }
	}
}
