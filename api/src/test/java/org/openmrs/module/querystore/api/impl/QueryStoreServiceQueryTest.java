/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.api.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_CONCEPT_NAME;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_CONCEPT_UUID;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_VALUE_NUMERIC;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.querystore.api.IndexStatus;
import org.openmrs.module.querystore.api.ProjectionProgress;
import org.openmrs.module.querystore.api.QueryResult;
import org.openmrs.module.querystore.api.QuerySpec;
import org.openmrs.module.querystore.backend.SchemaSpec;
import org.openmrs.module.querystore.backend.lucene.LuceneBackendStore;
import org.openmrs.module.querystore.bootstrap.BootstrapLauncher;
import org.openmrs.module.querystore.bootstrap.BootstrapProgress;
import org.openmrs.module.querystore.bootstrap.BootstrapService;
import org.openmrs.module.querystore.bootstrap.DriftReport;
import org.openmrs.module.querystore.bootstrap.SynchronousLaunchers;
import org.openmrs.module.querystore.embedding.EmbeddingProvider;
import org.openmrs.module.querystore.model.QueryDocument;

/**
 * {@link QueryStoreServiceImpl#query(QuerySpec)} against the embedded Lucene backend, so mode, type
 * filtering, sort, collapse, facets and paging run over real BM25 and kNN results and real metadata
 * round-trips rather than a fake's canned hits.
 */
public class QueryStoreServiceQueryTest {

	private static final String PATIENT = "patient-A";

	private static final String CD4 = "concept-cd4";

	private static final String MALARIA = "concept-malaria";

	private Path indexRoot;

	private LuceneBackendStore backend;

	private QueryStoreServiceImpl service;

	private CountingEmbeddingProvider embedder;

	private FakeLauncher launcher;

	private RecordingBootstrapService bootstrap;

	@Before
	public void setUp() throws IOException {
		indexRoot = Files.createTempDirectory("querystore-query-test-");
		backend = new LuceneBackendStore(indexRoot);
		for (String type : Arrays.asList("obs", "condition", "encounter")) {
			backend.ensureSchema(type, SchemaSpec.builder(4).build());
		}
		embedder = new CountingEmbeddingProvider();
		launcher = new FakeLauncher();
		bootstrap = new RecordingBootstrapService();
		service = new QueryStoreServiceImpl();
		service.setBackend(backend);
		service.setEmbeddingProvider(embedder);
		service.setBootstrapLauncher(launcher);
		service.setBootstrapServiceOverride(bootstrap);
	}

	@After
	public void tearDown() throws IOException {
		backend.close();
		try (Stream<Path> paths = Files.walk(indexRoot)) {
			for (Path p : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
				Files.deleteIfExists(p);
			}
		}
	}

	// ---------- mode ----------

	@Test
	public void keywordMode_neverEncodesTheQuery() {
		index(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"));

		QueryResult result = service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).build());

		assertEquals(1, result.getHits().size());
		assertEquals("keyword mode must not run the query encoder", 0, embedder.embedQueryCount.get());
	}

	@Test
	public void hybridMode_encodesTheQueryAndFindsVectorOnlyMatches() {
		// The encoder maps every text to the same vector, so kNN returns the weight obs although BM25
		// can't match "cd4" against it: a hit only hybrid mode can produce.
		index(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"));
		index(obs("weight-1", "concept-weight", "Weight: 61.0 kg", 61.0, "2026-03-12"));

		QueryResult keyword = service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).build());
		QueryResult hybrid = service.query(spec("cd4").mode(QuerySpec.Mode.HYBRID).build());

		assertEquals(1, keyword.getHits().size());
		assertEquals(2, hybrid.getHits().size());
		assertEquals(1, embedder.embedQueryCount.get());
	}

	// ---------- types ----------

	@Test
	public void types_restrictTheSearchToThoseResourceTypes() {
		index(obs("obs-1", MALARIA, "Diagnosis: Malaria", null, "2026-01-01"));
		index(doc("condition", "cond-1", MALARIA, "Condition: Malaria, active", "2026-01-02"));

		QueryResult all = service.query(spec("malaria").mode(QuerySpec.Mode.KEYWORD).build());
		QueryResult conditions = service.query(spec("malaria").mode(QuerySpec.Mode.KEYWORD)
		        .types(Collections.singletonList("condition")).build());

		assertEquals(2, all.getHits().size());
		assertEquals(1, conditions.getHits().size());
		assertEquals("condition", conditions.getHits().get(0).getDocument().getResourceType());
	}

	@Test
	public void unknownType_isRefused_andCreatesNoIndex() throws IOException {
		// The Lucene backend creates an index directory for any well-formed type a search names, so an
		// unchecked type from a caller would create one.
		index(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"));
		List<String> before = indexDirectories();

		try {
			service.query(spec("cd4").types(Collections.singletonList("faketype")).build());
			fail("expected IllegalArgumentException for an unregistered type");
		}
		catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage().contains("faketype"));
		}
		assertEquals("no index was created for the unknown type", before, indexDirectories());
		assertEquals(1, service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD)
		        .types(Collections.singletonList("obs")).build()).getHits().size());
	}

	private List<String> indexDirectories() throws IOException {
		try (Stream<Path> children = Files.list(indexRoot)) {
			return children.map(p -> p.getFileName().toString()).sorted().collect(Collectors.toList());
		}
	}

	// ---------- sort ----------

	@Test
	public void recentSort_ordersHitsNewestFirst() {
		index(obs("g-old", "concept-glucose", "Fasting blood glucose: 5.1 mmol/L", 5.1, "2025-01-01"));
		index(obs("g-new", "concept-glucose", "Fasting blood glucose: 7.9 mmol/L", 7.9, "2026-06-01"));
		index(obs("g-mid", "concept-glucose", "Fasting blood glucose: 6.4 mmol/L", 6.4, "2025-09-01"));

		QueryResult result = service.query(spec("glucose").mode(QuerySpec.Mode.KEYWORD)
		        .sort(QuerySpec.Sort.RECENT).build());

		assertEquals(Arrays.asList("g-new", "g-mid", "g-old"), uuids(result.getHits()));
	}

	@Test
	public void recencyBrowse_withoutQueryText_returnsNewestDocumentsOfTheGivenTypes() {
		index(obs("o-1", CD4, "CD4 count: 300.0 cells/µL", 300.0, "2025-01-01"));
		index(obs("o-2", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-01-01"));
		index(doc("condition", "c-1", MALARIA, "Condition: Malaria, active", "2026-02-01"));

		QueryResult result = service.query(QuerySpec.forPatient(PATIENT).sort(QuerySpec.Sort.RECENT).limit(2).build());
		QueryResult obsOnly = service.query(QuerySpec.forPatient(PATIENT).sort(QuerySpec.Sort.RECENT)
		        .types(Collections.singletonList("obs")).limit(5).build());

		assertEquals(Arrays.asList("c-1", "o-2"), uuids(result.getHits()));
		assertEquals(Arrays.asList("o-2", "o-1"), uuids(obsOnly.getHits()));
		assertEquals("a browse never encodes anything", 0, embedder.embedQueryCount.get());
	}

	// ---------- collapse ----------

	@Test
	public void collapseByConcept_returnsOneGroupPerConceptWithLatestCountAndRecentValues() {
		for (int month = 1; month <= 7; month++) {
			index(obs("cd4-" + month, CD4, "CD4 count: " + (300 + month) + ".0 cells/µL", 300.0 + month,
			    "2026-0" + month + "-15"));
		}

		QueryResult result = service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD)
		        .collapse(QuerySpec.Collapse.CONCEPT).build());

		assertTrue("a collapsed result has no flat hits", result.getHits().isEmpty());
		assertEquals(1, result.getGroups().size());
		QueryResult.ConceptGroup group = result.getGroups().get(0);
		assertEquals(CD4, group.getConceptUuid());
		assertEquals("CD4 count", group.getConceptName());
		assertEquals(7, group.getCount());
		assertEquals("cd4-7", group.getLatest().getDocument().getResourceUuid());
		assertEquals("five newest values, newest first", Arrays.asList("cd4-7", "cd4-6", "cd4-5", "cd4-4", "cd4-3"),
		    group.getRecent().stream().map(QueryDocument::getResourceUuid).collect(Collectors.toList()));
		assertTrue(group.getScore() != null && group.getScore() > 0);
	}

	@Test
	public void collapseByConcept_takesLatestAndCountFromTheWholeChart_notJustTheMatches() {
		// The newest CD4's text doesn't contain "count", so it can't match the query, but the row must
		// still show it as the latest value.
		index(obs("cd4-old", CD4, "CD4 count: 300.0 cells/µL", 300.0, "2025-01-01"));
		index(obs("cd4-new", CD4, "CD4: 410.0 cells/µL", 410.0, "2026-05-01"));

		QueryResult result = service.query(spec("count").mode(QuerySpec.Mode.KEYWORD)
		        .collapse(QuerySpec.Collapse.CONCEPT).build());

		QueryResult.ConceptGroup group = result.getGroups().get(0);
		assertEquals("cd4-new", group.getLatest().getDocument().getResourceUuid());
		assertEquals(2, group.getCount());
		assertNull("the latest wasn't a match, so it carries no score of its own", group.getLatest().getScore());
	}

	@Test
	public void collapseByConcept_breaksASameDayTieByWhenTheRecordWasEntered() {
		// Two CD4s on the same visit date: record_date is a day, so it ties. The one entered later is
		// the latest, though resource_type/uuid order (the chart's own tie-break) would pick the other.
		QueryDocument enteredFirst = obs("cd4-a", CD4, "CD4 count: 349.0 cells/µL", 349.0, "2006-03-29");
		enteredFirst.setLastModified(java.time.Instant.parse("2006-04-04T10:00:00Z"));
		QueryDocument enteredLater = obs("cd4-z", CD4, "CD4 count: 431.0 cells/µL", 431.0, "2006-03-29");
		enteredLater.setLastModified(java.time.Instant.parse("2006-05-04T10:00:00Z"));
		index(enteredFirst);
		index(enteredLater);

		QueryResult.ConceptGroup group = service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD)
		        .collapse(QuerySpec.Collapse.CONCEPT).build()).getGroups().get(0);

		assertEquals("cd4-z", group.getLatest().getDocument().getResourceUuid());
		assertEquals(Arrays.asList("cd4-z", "cd4-a"),
		    group.getRecent().stream().map(QueryDocument::getResourceUuid).collect(Collectors.toList()));
	}

	@Test
	public void recencyBrowse_collapsedByConcept_groupsTheWholeChartNewestFirst() {
		index(obs("cd4-1", CD4, "CD4 count: 300.0 cells/µL", 300.0, "2025-01-01"));
		index(obs("cd4-2", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-01-01"));
		index(doc("condition", "cond-1", MALARIA, "Malaria: active", "2025-06-01"));

		List<QueryResult.ConceptGroup> groups = service.query(QuerySpec.forPatient(PATIENT).sort(QuerySpec.Sort.RECENT)
		        .collapse(QuerySpec.Collapse.CONCEPT).build()).getGroups();

		assertEquals(Arrays.asList(CD4, MALARIA),
		    groups.stream().map(QueryResult.ConceptGroup::getConceptUuid).collect(Collectors.toList()));
		assertEquals(2, groups.get(0).getCount());
		assertEquals("cd4-2", groups.get(0).getLatest().getDocument().getResourceUuid());
	}

	@Test
	public void collapseByConcept_keepsConceptlessDocumentsAsGroupsOfOne() {
		index(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"));
		index(doc("encounter", "enc-1", null, "Encounter: HIV clinic, CD4 drawn", "2026-03-12"));

		QueryResult result = service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD)
		        .collapse(QuerySpec.Collapse.CONCEPT).build());

		assertEquals(2, result.getGroups().size());
		QueryResult.ConceptGroup encounter = result.getGroups().stream().filter(g -> g.getConceptUuid() == null)
		        .findFirst().get();
		assertEquals(1, encounter.getCount());
		assertEquals("enc-1", encounter.getLatest().getDocument().getResourceUuid());
	}

	@Test
	public void collapseByConcept_ordersGroupsByBestMemberRelevance_andPagesOverGroups() {
		// "malaria" twice in the condition text outranks the single mention in the obs, under BM25.
		index(obs("obs-1", "concept-test", "Malaria test result: negative", null, "2026-06-01"));
		index(doc("condition", "cond-1", MALARIA, "Malaria, malaria", "2025-01-01"));

		QueryResult page1 = service.query(spec("malaria").mode(QuerySpec.Mode.KEYWORD)
		        .collapse(QuerySpec.Collapse.CONCEPT).limit(1).build());
		QueryResult page2 = service.query(spec("malaria").mode(QuerySpec.Mode.KEYWORD)
		        .collapse(QuerySpec.Collapse.CONCEPT).limit(1).offset(1).build());
		QueryResult recent = service.query(spec("malaria").mode(QuerySpec.Mode.KEYWORD)
		        .collapse(QuerySpec.Collapse.CONCEPT).sort(QuerySpec.Sort.RECENT).build());

		assertEquals(MALARIA, page1.getGroups().get(0).getConceptUuid());
		assertEquals("concept-test", page2.getGroups().get(0).getConceptUuid());
		assertEquals("recent sort orders groups by their latest", "concept-test",
		    recent.getGroups().get(0).getConceptUuid());
	}

	// ---------- lookup order ----------

	@Test
	public void collapseByConcept_putsTheConceptTheQueryNamesFirst_inBothModes() {
		// The O3 demo data case: all three match the single token "cd4" with the same BM25 score, and
		// the order-set obs came first by insertion. "CD4" is a synonym of CD4 count only.
		index(withNames(doc("obs", "orders-1", "concept-tests-ordered", "Tests ordered: CD4 panel", "2026-03-12"),
		    "Tests ordered"));
		index(withNames(obs("pct-1", "concept-cd4-pct", "CD4%: 21.0", 21.0, "2026-03-12"), "CD4%", "CD4 percent"));
		index(withNames(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"), "CD4 count", "CD4"));

		for (QuerySpec.Mode mode : QuerySpec.Mode.values()) {
			List<QueryResult.ConceptGroup> groups = service.query(spec("cd4").mode(mode)
			        .collapse(QuerySpec.Collapse.CONCEPT).build()).getGroups();
			assertEquals(mode.name(), CD4, groups.get(0).getConceptUuid());
		}
	}

	@Test
	public void collapseByConcept_matchesANameLessItsUnits_soWeightFindsWeightKg() {
		// "Review of systems, general: Weight loss" ties "Weight (kg)" on BM25 for "weight".
		index(withNames(doc("obs", "ros-1", "concept-ros", "Review of systems, general: Weight loss", "2026-03-12"),
		    "Review of systems, general"));
		index(withNames(doc("obs", "loss-1", "concept-weight-loss", "Weight loss: Yes", "2026-03-12"), "Weight loss"));
		index(withNames(obs("wt-1", "concept-weight", "Weight: 61.0 kg", 61.0, "2026-03-12"), "Weight (kg)", "WT"));

		List<QueryResult.ConceptGroup> groups = service.query(spec("weight").mode(QuerySpec.Mode.KEYWORD)
		        .collapse(QuerySpec.Collapse.CONCEPT).build()).getGroups();

		assertEquals("concept-weight", groups.get(0).getConceptUuid());
	}

	@Test
	public void collapseByConcept_breaksAScoreTieTowardANumericValueThenMoreReadings() {
		// Same text, so the same BM25 score; none is named by the query.
		index(withNames(doc("obs", "coded-1", "concept-coded", "Malaria smear: positive", "2026-03-12"), "Smear result"));
		index(withNames(obs("num-1", "concept-num-few", "Malaria smear: 2.0", 2.0, "2026-03-12"), "Smear count"));
		index(withNames(obs("num-2", "concept-num-many", "Malaria smear: 3.0", 3.0, "2026-03-12"), "Smear density"));
		index(withNames(obs("num-3", "concept-num-many", "Parasite density: 4.0", 4.0, "2026-02-01"), "Smear density"));

		List<QueryResult.ConceptGroup> groups = service.query(spec("smear").mode(QuerySpec.Mode.KEYWORD)
		        .collapse(QuerySpec.Collapse.CONCEPT).build()).getGroups();

		assertEquals(Arrays.asList("concept-num-many", "concept-num-few", "concept-coded"),
		    groups.stream().map(QueryResult.ConceptGroup::getConceptUuid).collect(Collectors.toList()));
	}

	@Test
	public void normalizeName_dropsATrailingUnitButKeepsOtherPunctuation() {
		assertEquals("weight", QueryStoreServiceImpl.normalizeName("Weight (kg)"));
		assertEquals("cd4%", QueryStoreServiceImpl.normalizeName("CD4%"));
		assertEquals("blood pressure", QueryStoreServiceImpl.normalizeName("  Blood   Pressure "));
		assertNull(QueryStoreServiceImpl.normalizeName("(kg)"));
	}

	// ---------- facets and paging ----------

	@Test
	public void resourceTypeFacet_countsTheWholePool_notJustThePage() {
		index(obs("obs-1", MALARIA, "Diagnosis: Malaria", null, "2026-01-01"));
		index(obs("obs-2", MALARIA, "Malaria smear: positive", null, "2026-01-03"));
		index(doc("condition", "cond-1", MALARIA, "Condition: Malaria, active", "2026-01-02"));

		QueryResult result = service.query(spec("malaria").mode(QuerySpec.Mode.KEYWORD).limit(1)
		        .facet(QuerySpec.Facet.RESOURCE_TYPE).build());

		assertEquals(1, result.getHits().size());
		assertEquals(Integer.valueOf(2), result.getResourceTypeCounts().get("obs"));
		assertEquals(Integer.valueOf(1), result.getResourceTypeCounts().get("condition"));
		assertNull("facets are absent unless asked for",
		    service.query(spec("malaria").mode(QuerySpec.Mode.KEYWORD).build()).getResourceTypeCounts());
	}

	@Test
	public void hybridMode_recentSortAndFacets_countOnlyKeywordMatches() {
		// kNN returns the weight obs for "cd4" (every vector is the same here), and it is the newest
		// record. Relevance order may show it; a date sort or a count must not treat it as a match.
		index(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"));
		index(obs("weight-1", "concept-weight", "Weight: 61.0 kg", 61.0, "2026-09-01"));

		QueryResult relevance = service.query(spec("cd4").mode(QuerySpec.Mode.HYBRID).build());
		QueryResult recent = service.query(spec("cd4").mode(QuerySpec.Mode.HYBRID).sort(QuerySpec.Sort.RECENT)
		        .facet(QuerySpec.Facet.RESOURCE_TYPE).build());

		assertEquals(2, relevance.getHits().size());
		assertEquals(Collections.singletonList("cd4-1"), uuids(recent.getHits()));
		assertEquals(Collections.singletonMap("obs", 1), recent.getResourceTypeCounts());
	}

	@Test
	public void offsetAndLimit_pageTheHits() {
		index(obs("g-1", "concept-glucose", "Glucose: 5.1 mmol/L", 5.1, "2026-01-01"));
		index(obs("g-2", "concept-glucose", "Glucose: 6.1 mmol/L", 6.1, "2026-02-01"));
		index(obs("g-3", "concept-glucose", "Glucose: 7.1 mmol/L", 7.1, "2026-03-01"));

		QueryResult.Hit[] pages = {
		        only(service.query(spec("glucose").mode(QuerySpec.Mode.KEYWORD).sort(QuerySpec.Sort.RECENT).limit(1)
		                .offset(0).build())),
		        only(service.query(spec("glucose").mode(QuerySpec.Mode.KEYWORD).sort(QuerySpec.Sort.RECENT).limit(1)
		                .offset(2).build())) };

		assertEquals("g-3", pages[0].getDocument().getResourceUuid());
		assertEquals("g-1", pages[1].getDocument().getResourceUuid());
		assertTrue(service.query(spec("glucose").mode(QuerySpec.Mode.KEYWORD).offset(3).build()).getHits().isEmpty());
	}

	// ---------- cold patient ----------

	@Test
	public void coldPatient_withoutWait_warmsInTheBackgroundAndAnswersEmptyAtOnce() {
		QueryResult result = service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD)
		        .collapse(QuerySpec.Collapse.CONCEPT).partialResults(true).wait(false).build());

		assertEquals(IndexStatus.PROJECTING, result.getStatus());
		assertTrue(result.getHits().isEmpty());
		assertTrue("a collapsed cold answer still carries an (empty) groups list", result.getGroups().isEmpty());
		assertEquals(Collections.singletonList(PATIENT), launcher.launched);
		assertTrue("wait=false must not project on the request thread", bootstrap.ensureIndexedCalls.isEmpty());

		// While the warm runs, a second keystroke neither blocks nor launches another.
		launcher.warming = true;
		QueryResult again = service.query(spec("cd4").partialResults(true).wait(false).build());
		assertEquals(IndexStatus.PROJECTING, again.getStatus());
		assertEquals(1, launcher.launched.size());
	}

	@Test
	public void coldPatient_withoutWait_reportsNotIndexed_whenNoWarmCanStart() {
		launcher.accept = false;

		QueryResult result = service.query(spec("cd4").wait(false).build());

		assertEquals(IndexStatus.NOT_INDEXED, result.getStatus());
	}

	@Test
	public void coldPatient_withWait_projectsOnTheRequestThreadThenSearches() {
		bootstrap.onEnsureIndexed = uuid -> index(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"));

		QueryResult result = service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).build());

		assertEquals(Collections.singletonList(PATIENT), bootstrap.ensureIndexedCalls);
		assertEquals(IndexStatus.INDEXED, result.getStatus());
		assertEquals(1, result.getHits().size());
		assertTrue(launcher.launched.isEmpty());
	}

	@Test
	public void indexStatus_readsProjectingWhileAWarmRuns_evenWhenSomeTypesHaveLanded() {
		assertEquals(IndexStatus.NOT_INDEXED, service.getIndexStatus(PATIENT));
		index(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"));
		assertEquals(IndexStatus.INDEXED, service.getIndexStatus(PATIENT));
		launcher.warming = true;
		assertEquals(IndexStatus.PROJECTING, service.getIndexStatus(PATIENT));
	}

	@Test
	public void aProjectingChart_servesTheRowsAlreadyIndexed_withinTheRequestedTypesOnly() {
		// A warm is part way: obs and a condition have landed. The caller may read obs only, so the REST
		// layer asks for obs; the partial answer must not carry the condition.
		index(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"));
		index(doc("condition", "cond-1", CD4, "Condition: CD4 lymphocytopenia", "2026-03-01"));
		launcher.warming = true;
		List<String> obsOnly = Collections.singletonList("obs");

		QueryResult search = service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).types(obsOnly).partialResults(true).wait(false).build());
		QueryResult collapsed = service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).types(obsOnly)
		        .collapse(QuerySpec.Collapse.CONCEPT).partialResults(true).wait(false).build());
		QueryResult recent = service.query(QuerySpec.forPatient(PATIENT).sort(QuerySpec.Sort.RECENT).types(obsOnly)
		        .limit(5).partialResults(true).wait(false).build());
		QueryResult everything = service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).partialResults(true).wait(false).build());

		assertEquals(IndexStatus.PROJECTING, search.getStatus());
		assertEquals(Collections.singletonList("cd4-1"), uuids(search.getHits()));
		assertEquals(IndexStatus.PROJECTING, collapsed.getStatus());
		assertEquals(1, collapsed.getGroups().size());
		assertEquals(1, collapsed.getGroups().get(0).getCount());
		assertEquals(IndexStatus.PROJECTING, recent.getStatus());
		assertEquals(Collections.singletonList("cd4-1"), uuids(recent.getHits()));
		assertEquals(2, everything.getHits().size());
		assertTrue("serving a partial chart starts no second warm", launcher.launched.isEmpty());
	}

	@Test
	public void aProjectingChart_forACallerWhoCannotReadEveryType_getsNoRowsNoGroupsNoFacetsNoProgress_untilIndexed() {
		// sec r1 B1. The caller reads conditions and allergies, not diagnoses, which project between
		// them. Rows landing per type, or progress stepping per type, would let the caller time the
		// diagnosis step, so until the chart is indexed the body is the bare status.
		index(doc("condition", "cond-1", MALARIA, "Condition: Malaria, active", "2026-01-02"));
		bootstrap.progress = new ProjectionProgress(4, 12);
		launcher.warming = true;
		QuerySpec.Builder restricted = spec("malaria").mode(QuerySpec.Mode.KEYWORD)
		        .types(Collections.singletonList("condition")).collapse(QuerySpec.Collapse.CONCEPT)
		        .facet(QuerySpec.Facet.RESOURCE_TYPE).wait(false);

		Map<String, Object> projecting = service.query(restricted.build()).toMap();
		Map<String, Object> recent = service.query(QuerySpec.forPatient(PATIENT).sort(QuerySpec.Sort.RECENT)
		        .types(Collections.singletonList("condition")).wait(false).build()).toMap();

		Map<String, Object> bare = new java.util.LinkedHashMap<>();
		bare.put("status", "projecting");
		bare.put("hits", Collections.emptyList());
		assertEquals(bare, projecting);
		assertEquals(bare, recent);
		assertTrue("and no second warm started", launcher.launched.isEmpty());

		launcher.warming = false;
		QueryResult indexed = service.query(restricted.build());
		assertEquals(IndexStatus.INDEXED, indexed.getStatus());
		assertEquals(1, indexed.getGroups().size());
	}

	@Test
	public void aProjectingChart_carriesItsProgressInTypes_andAnIndexedOneDoesNot() {
		index(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"));
		bootstrap.progress = new ProjectionProgress(4, 12);
		launcher.warming = true;

		Map<String, Object> projecting = service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).partialResults(true).wait(false).build()).toMap();
		launcher.warming = false;
		bootstrap.progress = null;
		Map<String, Object> indexed = service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).partialResults(true).wait(false).build()).toMap();

		assertEquals("projecting", projecting.get("status"));
		@SuppressWarnings("unchecked")
		Map<String, Object> progress = (Map<String, Object>) projecting.get("progress");
		assertEquals(4, progress.get("done"));
		assertEquals(12, progress.get("total"));
		assertEquals("indexed", indexed.get("status"));
		assertFalse(indexed.containsKey("progress"));
	}

	@Test
	public void aProjectingChart_aWaitingSearchThatOutwaitsItsBound_servesTheRowsAlreadyIndexed() {
		List<Runnable> parked = new ArrayList<>();
		BootstrapLauncher launcher = SynchronousLaunchers.parked(1, parked);
		assertTrue(launcher.launchPatientWarm(PATIENT, () -> true));
		service.setBootstrapLauncher(launcher);
		service.setSyncProjectionWaitMillis(20);
		index(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"));

		QueryResult waited = service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).partialResults(true).wait(true).build());

		assertEquals(IndexStatus.PROJECTING, waited.getStatus());
		assertEquals(Collections.singletonList("cd4-1"), uuids(waited.getHits()));
	}

	@Test
	public void warm_onAnIndexedPatient_startsNothing() {
		index(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"));

		assertEquals(IndexStatus.INDEXED, service.warm(PATIENT));
		assertTrue(launcher.launched.isEmpty());
	}

	// ---------- projection bound ----------

	@Test
	public void coldPatient_withNoProjectionSlotFree_isNotProjectedOnTheRequestThread() {
		// The one slot is held by a background warm for another patient. A waiting search on a cold
		// patient must not run a projection of its own past the bound: it gives up after the wait and
		// serves what is indexed. searchByPatient's cold-touch path is bounded the same way.
		List<Runnable> parked = new ArrayList<>();
		BootstrapLauncher launcher = SynchronousLaunchers.parked(1, parked);
		assertTrue(launcher.launchPatientWarm("another-patient", () -> true));
		service.setBootstrapLauncher(launcher);
		service.setSyncProjectionWaitMillis(20);

		QueryResult waited = service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).build());
		service.searchByPatient(PATIENT, "cd4", 5);

		assertTrue("no projection ran past the bound", bootstrap.ensureIndexedCalls.isEmpty());
		assertEquals(IndexStatus.NOT_INDEXED, waited.getStatus());

		parked.get(0).run();
		service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).build());
		assertEquals("with the slot free, the waiting search projects", Collections.singletonList(PATIENT),
		    bootstrap.ensureIndexedCalls);
	}

	@Test
	public void aSynchronousProjection_readsAsProjecting_untilItFinishes() throws Exception {
		// The projection writes its first type, then stalls: the chart exists but is half written. A
		// concurrent status read and a non-waiting search must say projecting, not indexed, and the
		// search must not start a second projection.
		service.setBootstrapLauncher(SynchronousLaunchers.create());
		java.util.concurrent.CountDownLatch written = new java.util.concurrent.CountDownLatch(1);
		java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
		bootstrap.onEnsureIndexed = uuid -> {
			index(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"));
			written.countDown();
			try {
				release.await(10, java.util.concurrent.TimeUnit.SECONDS);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		};
		Thread search = new Thread(() -> service.searchByPatient(PATIENT, "cd4", 5));
		search.start();
		assertTrue("the projection reached its half-written point", written.await(10, java.util.concurrent.TimeUnit.SECONDS));

		assertEquals(IndexStatus.PROJECTING, service.getIndexStatus(PATIENT));
		QueryResult concurrent = service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).partialResults(true).wait(false).build());
		assertEquals(IndexStatus.PROJECTING, concurrent.getStatus());
		assertEquals("what has landed so far, labelled projecting", Collections.singletonList("cd4-1"),
		    uuids(concurrent.getHits()));

		release.countDown();
		search.join(10_000);
		assertEquals(IndexStatus.INDEXED, service.getIndexStatus(PATIENT));
		assertEquals("one projection, not a second from the concurrent search", 1, bootstrap.ensureIndexedCalls.size());
	}

	@Test
	public void candidatePool_isClampedTo1000() {
		service.setCandidatePool(1_000_000);
		for (int i = 0; i < 3; i++) {
			index(obs("g-" + i, "concept-glucose", "Glucose: 5." + i + " mmol/L", 5.0 + i, "2026-01-0" + (i + 1)));
		}
		// Clamped, not refused: a pool of 1000 still finds all three.
		assertEquals(3, service.query(spec("glucose").mode(QuerySpec.Mode.KEYWORD).build()).getHits().size());
		assertEquals(QueryStoreServiceImpl.MAX_CANDIDATE_POOL, service.candidatePool());
	}

	@Test
	public void candidatePool_boundsWhatFacetsCount() {
		index(obs("obs-1", MALARIA, "Diagnosis: Malaria", null, "2026-01-01"));
		index(obs("obs-2", MALARIA, "Malaria smear: positive", null, "2026-01-03"));
		index(doc("condition", "cond-1", MALARIA, "Condition: Malaria, active", "2026-01-02"));
		service.setCandidatePool(1);

		QueryResult result = service.query(spec("malaria").mode(QuerySpec.Mode.KEYWORD)
		        .facet(QuerySpec.Facet.RESOURCE_TYPE).build());

		assertEquals(1, result.getResourceTypeCounts().values().stream().mapToInt(Integer::intValue).sum());
	}

	// ---------- [QSEVAL] logging ----------

	@Test
	public void qsevalLines_haveTheirOwnLogger_soPackageDebugDoesNotEmitThem() {
		index(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"));

		try (org.openmrs.module.querystore.LogCapture pkg = org.openmrs.module.querystore.LogCapture
		        .of("org.openmrs.module.querystore")) {
			service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).build());
			assertFalse(pkg.lines().stream().anyMatch(l -> l.contains("[QSEVAL]")));
		}
		try (org.openmrs.module.querystore.LogCapture qseval = org.openmrs.module.querystore.LogCapture
		        .of("querystore.qseval")) {
			service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).build());
			assertTrue("DEBUG on the qseval logger emits them", qseval.lines().stream().anyMatch(l -> l.contains("[QSEVAL]")));
		}
	}

	// ---------- failed warms ----------

	@Test
	public void warm_thatIndexesNothing_readsFailed_andAKeystrokeDoesNotRetryIt() {
		// A real launcher, run synchronously: the projection "succeeds" but writes nothing (a uuid that
		// is no patient's, or every type failing), which is a failed warm, not a silent not_indexed.
		service.setBootstrapLauncher(SynchronousLaunchers.create());

		assertEquals("the call itself starts the warm", IndexStatus.PROJECTING, service.warm(PATIENT));
		assertEquals(IndexStatus.FAILED, service.getIndexStatus(PATIENT));

		QueryResult keystroke = service.query(spec("cd4").wait(false).build());
		assertEquals(IndexStatus.FAILED, keystroke.getStatus());
		assertTrue(keystroke.getHits().isEmpty());
		assertEquals("a keystroke must not re-run a failing projection", 1, bootstrap.ensureIndexedCalls.size());
		assertTrue(bootstrap.reindexCalls.isEmpty());
	}

	@Test
	public void warm_afterAFailure_retriesWithAFullReprojection_onceTheBackoffRunsOut_andRecovers() {
		long[] now = { 1_000_000L };
		service.setBootstrapLauncher(SynchronousLaunchers.create(() -> now[0]));
		service.warm(PATIENT);
		assertEquals(IndexStatus.FAILED, service.getIndexStatus(PATIENT));

		// Second time round the projection works, but not before the backoff has run out.
		bootstrap.onEnsureIndexed = uuid -> index(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"));
		assertEquals("too soon: nothing starts", IndexStatus.FAILED, service.warm(PATIENT));
		assertTrue(bootstrap.reindexCalls.isEmpty());

		// A retry re-projects in full (reindexPatient), since a failed warm may have left a partial
		// chart that ensureIndexed would skip.
		now[0] += BootstrapLauncher.RETRY_BACKOFF_BASE_MILLIS;
		service.warm(PATIENT);

		assertEquals(Collections.singletonList(PATIENT), bootstrap.reindexCalls);
		assertEquals(IndexStatus.INDEXED, service.getIndexStatus(PATIENT));
		assertEquals(1, service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).wait(false).build()).getHits().size());
	}

	@Test
	public void aFailedPatient_isRetriedByALaterSearch_onceTheBackoffRunsOut() {
		// The omnibar never calls POST warm: its searches are the only warms a failed chart gets.
		long[] now = { 1_000_000L };
		service.setBootstrapLauncher(SynchronousLaunchers.create(() -> now[0]));
		service.warm(PATIENT);
		assertEquals(IndexStatus.FAILED, service.getIndexStatus(PATIENT));
		bootstrap.onEnsureIndexed = uuid -> index(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"));

		now[0] += BootstrapLauncher.RETRY_BACKOFF_BASE_MILLIS;
		service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).wait(false).build());

		assertEquals("the search retried the failed chart in full", Collections.singletonList(PATIENT),
		    bootstrap.reindexCalls);
		QueryResult after = service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).wait(false).build());
		assertEquals(IndexStatus.INDEXED, after.getStatus());
		assertEquals(1, after.getHits().size());
	}

	@Test
	public void aNonWaitingSearchThatStartsAFailedChartsRetry_answersEmptyProjecting_notTheRowsTheRetryWillDelete() {
		// code r1 S1. The retry is reindexPatient, which deletes the chart before it re-projects. Parked,
		// it has not run yet, so the old partial rows are still there; this answer must not carry them.
		long[] now = { 1_000_000L };
		List<Runnable> parked = new ArrayList<>();
		BootstrapLauncher launcher = failedChart(parked, now);
		now[0] += BootstrapLauncher.RETRY_BACKOFF_BASE_MILLIS;

		QueryResult result = service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).collapse(QuerySpec.Collapse.CONCEPT)
		        .partialResults(true).wait(false).build());

		assertEquals(IndexStatus.PROJECTING, result.getStatus());
		assertTrue(result.getHits().isEmpty());
		assertTrue(result.getGroups().isEmpty());
		assertEquals("the retry was started and is parked", 1, parked.size());
		assertTrue(launcher.isWarming(PATIENT));
	}

	@Test
	public void aWaitingSearchThatStartsAFailedChartsRetry_joinsIt_thenSearchesTheRebuiltChart() throws Exception {
		long[] now = { 1_000_000L };
		List<Runnable> parked = new java.util.concurrent.CopyOnWriteArrayList<>();
		failedChart(parked, now);
		now[0] += BootstrapLauncher.RETRY_BACKOFF_BASE_MILLIS;
		service.setSyncProjectionWaitMillis(10_000);
		bootstrap.onEnsureIndexed = uuid -> index(obs("cd4-2", CD4, "CD4 count: 410.0 cells/µL", 410.0, "2026-04-01"));
		Thread runner = new Thread(() -> {
			try {
				while (parked.isEmpty()) {
					Thread.sleep(10);
				}
				Thread.sleep(100);
				parked.remove(0).run();
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		runner.start();

		QueryResult result = service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).partialResults(true).wait(true).build());
		runner.join(10_000);

		assertEquals(IndexStatus.INDEXED, result.getStatus());
		assertTrue(uuids(result.getHits()).contains("cd4-2"));
		assertEquals(Collections.singletonList(PATIENT), bootstrap.reindexCalls);
	}

	/**
	 * A one-slot parked launcher on {@code now}, with the patient's chart partly indexed and its last
	 * warm failed (at {@code now}).
	 */
	private BootstrapLauncher failedChart(List<Runnable> parked, long[] now) {
		BootstrapLauncher launcher = SynchronousLaunchers.parked(1, parked, () -> now[0]);
		service.setBootstrapLauncher(launcher);
		index(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"));
		assertTrue(launcher.launchPatientWarm(PATIENT, () -> {
			throw new IllegalStateException("conditions failed");
		}));
		parked.remove(0).run();
		assertEquals(IndexStatus.FAILED, service.getIndexStatus(PATIENT));
		return launcher;
	}

	@Test
	public void aFailedChartWhoseFailureIsForgotten_readsNotIndexed_notIndexed() {
		// sec r1 S2: the failed warm left a partial chart. When the bounded failure map forgets the
		// patient, that chart must not start reading as indexed: its documents are dropped.
		BootstrapLauncher launcher = SynchronousLaunchers.create();
		service.setBootstrapLauncher(launcher);
		bootstrap.onForget = uuid -> backend.bulkDeleteByPatient(uuid);
		bootstrap.onEnsureIndexed = uuid -> {
			index(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"));
			throw new IllegalStateException("conditions failed");
		};
		service.warm(PATIENT);
		assertEquals(IndexStatus.FAILED, service.getIndexStatus(PATIENT));

		for (int i = 0; i < BootstrapLauncher.MAX_FAILED_PATIENTS; i++) {
			launcher.launchPatientWarm("other-" + i, () -> {
				throw new IllegalStateException("x");
			});
		}

		assertEquals(Collections.singletonList(PATIENT), bootstrap.forgetCalls);
		assertEquals(IndexStatus.NOT_INDEXED, service.getIndexStatus(PATIENT));
	}

	@Test
	public void aForgottenFailure_ofAPatientBeingProjected_leavesItsDocuments() {
		// A projection in flight records its own outcome; dropping its documents mid-run would leave it
		// partial and then reported indexed.
		service.setBootstrapLauncher(launcher);
		launcher.warming = true;

		launcher.forget(PATIENT);

		assertTrue(bootstrap.forgetCalls.isEmpty());
	}

	@Test
	public void aPoisonChart_isRetriedOnItsBackoffSchedule_notOnEveryKeystroke() {
		long[] now = { 1_000_000L };
		service.setBootstrapLauncher(SynchronousLaunchers.create(() -> now[0]));
		bootstrap.onEnsureIndexed = uuid -> {
			throw new IllegalStateException("this chart never projects");
		};
		service.warm(PATIENT);

		// Ten minutes of a keystroke every second, waiting and not, plus a POST warm each time.
		for (int second = 0; second < 600; second++) {
			now[0] += 1000;
			assertEquals(IndexStatus.FAILED, service.query(spec("cd4").wait(second % 2 == 0).build()).getStatus());
			service.warm(PATIENT);
		}

		// Backoff 30 s, 60 s, 120 s, 240 s: retries at about 30, 90, 210 and 450 seconds in.
		assertEquals(4, bootstrap.reindexCalls.size());
		assertEquals(IndexStatus.FAILED, service.getIndexStatus(PATIENT));
	}

	@Test
	public void aFailedPatient_getsNoHits_evenWhenTheSearchWaits() {
		service.setBootstrapLauncher(SynchronousLaunchers.create());
		bootstrap.onEnsureIndexed = uuid -> {
			index(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"));
			throw new org.openmrs.module.querystore.bootstrap.IncompleteProjectionException(
			        "Cold-touch projection was incomplete: 1 type(s) failed, 0 record(s) not projected");
		};
		service.warm(PATIENT);
		assertEquals(IndexStatus.FAILED, service.getIndexStatus(PATIENT));
		int projections = bootstrap.ensureIndexedCalls.size();

		QueryResult waited = service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).wait(true).build());

		assertEquals(IndexStatus.FAILED, waited.getStatus());
		assertTrue("a partial chart is not served", waited.getHits().isEmpty());
		assertEquals("and not re-projected by the search", projections, bootstrap.ensureIndexedCalls.size());
	}

	@Test
	public void reindex_reprojectsInASlot_andClearsAFailure() {
		service.setBootstrapLauncher(SynchronousLaunchers.create());
		bootstrap.onEnsureIndexed = uuid -> {
			throw new IllegalStateException("projection blew up");
		};
		service.warm(PATIENT);
		assertEquals(IndexStatus.FAILED, service.getIndexStatus(PATIENT));

		bootstrap.onEnsureIndexed = uuid -> index(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"));
		assertEquals(IndexStatus.INDEXED, service.reindex(PATIENT));
		assertEquals(IndexStatus.INDEXED, service.getIndexStatus(PATIENT));
		assertEquals(1, service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD).build()).getHits().size());
	}

	@Test
	public void reindex_withNoSlotFree_reportsNotIndexed_andRunsNothing() {
		List<Runnable> parked = new ArrayList<>();
		BootstrapLauncher launcher = SynchronousLaunchers.parked(1, parked);
		assertTrue(launcher.launchPatientWarm("another-patient", () -> true));
		service.setBootstrapLauncher(launcher);
		service.setSyncProjectionWaitMillis(20);

		assertEquals(IndexStatus.NOT_INDEXED, service.reindex(PATIENT));
		assertTrue(bootstrap.reindexCalls.isEmpty());
	}

	@Test
	public void warm_thatThrowsPartway_readsFailedEvenThoughSomeTypesLanded() {
		service.setBootstrapLauncher(SynchronousLaunchers.create());
		bootstrap.onEnsureIndexed = uuid -> {
			index(obs("cd4-1", CD4, "CD4 count: 350.0 cells/µL", 350.0, "2026-03-12"));
			throw new IllegalStateException("conditions failed");
		};

		service.warm(PATIENT);

		assertEquals("a partial chart is not reported as indexed", IndexStatus.FAILED, service.getIndexStatus(PATIENT));
		assertEquals("failed", IndexStatus.FAILED.wireName());
	}

	// ---------- spec validation ----------

	@Test
	public void spec_rejectsAMissingPatient_andARelevanceSortWithNothingToRank() {
		expectInvalid(() -> QuerySpec.forPatient(" ").query("cd4").build());
		expectInvalid(() -> QuerySpec.forPatient(PATIENT).build());
		expectInvalid(() -> QuerySpec.forPatient(PATIENT).query("cd4").limit(0).build());
		expectInvalid(() -> QuerySpec.forPatient(PATIENT).query("cd4").offset(-1).build());
	}

	// ---------- toMap ----------

	@Test
	public void toMap_shapesHitsGroupsAndFacetsForTheRestResponse() {
		for (int month = 1; month <= 2; month++) {
			index(obs("cd4-" + month, CD4, "CD4 count: " + (300 + month) + ".0 cells/µL", 300.0 + month,
			    "2026-0" + month + "-15"));
		}

		Map<String, Object> body = service.query(spec("cd4").mode(QuerySpec.Mode.KEYWORD)
		        .collapse(QuerySpec.Collapse.CONCEPT).facet(QuerySpec.Facet.RESOURCE_TYPE).build()).toMap();

		assertEquals("indexed", body.get("status"));
		assertEquals(Collections.emptyList(), body.get("hits"));
		@SuppressWarnings("unchecked")
		Map<String, Object> group = ((List<Map<String, Object>>) body.get("groups")).get(0);
		assertEquals(CD4, group.get("conceptUuid"));
		assertEquals("CD4 count", group.get("conceptName"));
		assertEquals(2, group.get("count"));
		@SuppressWarnings("unchecked")
		Map<String, Object> latest = (Map<String, Object>) group.get("latest");
		assertEquals("obs", latest.get("resourceType"));
		assertEquals("cd4-2", latest.get("resourceUuid"));
		assertEquals("2026-02-15", latest.get("date"));
		assertEquals("CD4 count: 302.0 cells/µL", latest.get("snippet"));
		assertEquals(CD4, latest.get("conceptUuid"));
		assertEquals(302.0, ((Number) latest.get("valueNumeric")).doubleValue(), 0.0);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> values = (List<Map<String, Object>>) group.get("recentValues");
		assertEquals(Arrays.asList("2026-02-15", "2026-01-15"),
		    values.stream().map(v -> v.get("date")).collect(Collectors.toList()));
		@SuppressWarnings("unchecked")
		Map<String, Object> facet = (Map<String, Object>) ((Map<String, Object>) body.get("facets")).get("resourceType");
		assertEquals(true, facet.get("approximate"));
		assertEquals(Collections.singletonMap("obs", 2), facet.get("counts"));
	}

	// ---------- helpers ----------

	private static QuerySpec.Builder spec(String query) {
		return QuerySpec.forPatient(PATIENT).query(query);
	}

	private void index(QueryDocument d) {
		assertTrue(backend.upsert(d).isSucceeded());
	}

	private static QueryDocument obs(String uuid, String concept, String text, Double value, String date) {
		QueryDocument d = doc("obs", uuid, concept, text, date);
		if (value != null) {
			d.putMetadata(FIELD_VALUE_NUMERIC, value);
		}
		return d;
	}

	private static QueryDocument doc(String type, String uuid, String concept, String text, String date) {
		QueryDocument d = new QueryDocument();
		d.setResourceType(type);
		d.setResourceUuid(uuid);
		d.setPatientUuid(PATIENT);
		d.setDate(LocalDate.parse(date));
		d.setText(text);
		d.setEmbedding(CountingEmbeddingProvider.VECTOR);
		if (concept != null) {
			d.putMetadata(FIELD_CONCEPT_UUID, concept);
			d.putMetadata(FIELD_CONCEPT_NAME, text.substring(0, text.indexOf(':') < 0 ? text.length()
			        : text.indexOf(':')));
		}
		return d;
	}

	/** Sets the concept's preferred name and synonyms, as the serializers write them. */
	private static QueryDocument withNames(QueryDocument d, String name, String... synonyms) {
		d.putMetadata(FIELD_CONCEPT_NAME, name);
		d.putMetadata(org.openmrs.module.querystore.QueryStoreConstants.FIELD_SYNONYMS, Arrays.asList(synonyms));
		return d;
	}

	private static List<String> uuids(List<QueryResult.Hit> hits) {
		List<String> out = new ArrayList<>();
		hits.forEach(h -> out.add(h.getDocument().getResourceUuid()));
		return out;
	}

	private static QueryResult.Hit only(QueryResult result) {
		assertEquals(1, result.getHits().size());
		return result.getHits().get(0);
	}

	private static void expectInvalid(Runnable build) {
		try {
			build.run();
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException expected) {
			// pass
		}
	}

	// ---------- fakes ----------

	/** Records launches instead of spawning daemon threads; {@code warming} stands in for one running. */
	private static final class FakeLauncher extends BootstrapLauncher {

		final List<String> launched = new ArrayList<>();

		boolean accept = true;

		boolean warming;

		@Override
		public boolean launchPatientWarm(String patientUuid, java.util.function.BooleanSupplier work) {
			if (!accept) {
				return false;
			}
			launched.add(patientUuid);
			warming = true;
			return true;
		}

		@Override
		public boolean isWarming(String patientUuid) {
			return warming;
		}

		java.util.function.Consumer<String> forgotten;

		@Override
		public void setOnFailureForgotten(java.util.function.Consumer<String> handler) {
			forgotten = handler;
		}

		/** Forgets a failure as the bound would. */
		void forget(String patientUuid) {
			forgotten.accept(patientUuid);
		}
	}

	private static final class CountingEmbeddingProvider implements EmbeddingProvider {

		static final float[] VECTOR = { 1f, 0f, 0f, 0f };

		final AtomicInteger embedQueryCount = new AtomicInteger();

		@Override public int getDimensions() { return 4; }

		@Override public float[] embed(String text) { return VECTOR; }

		@Override public float[] embedQuery(String text) {
			embedQueryCount.incrementAndGet();
			return VECTOR;
		}

		@Override public String getModelName() { return "fixed"; }
	}

	private static final class RecordingBootstrapService implements BootstrapService {

		final List<String> ensureIndexedCalls = new ArrayList<>();

		final List<String> reindexCalls = new ArrayList<>();

		java.util.function.Consumer<String> onEnsureIndexed;

		ProjectionProgress progress;

		final List<String> forgetCalls = new ArrayList<>();

		java.util.function.Consumer<String> onForget;

		@Override public void forgetPatient(String patientUuid) {
			forgetCalls.add(patientUuid);
			if (onForget != null) {
				onForget.accept(patientUuid);
			}
		}

		@Override public ProjectionProgress getProjectionProgress(String patientUuid) { return progress; }
		@Override public void bootstrap() { }
		@Override public void bootstrap(String resourceType) { }
		@Override public void resyncType(String resourceType) { }
		@Override public List<String> getResourceTypeNames() { return Arrays.asList("obs", "condition", "encounter"); }
		@Override public void ensureIndexed(String patientUuid) {
			ensureIndexedCalls.add(patientUuid);
			if (onEnsureIndexed != null) {
				onEnsureIndexed.accept(patientUuid);
			}
		}
		@Override public void reindexPatient(String patientUuid) {
			reindexCalls.add(patientUuid);
			if (onEnsureIndexed != null) {
				onEnsureIndexed.accept(patientUuid);
			}
		}
		@Override public List<BootstrapProgress> getStatus() { return Collections.emptyList(); }
		@Override public BootstrapProgress getStatus(String resourceType) { return null; }
		@Override public DriftReport getDrift() { return null; }
		@Override public org.openmrs.module.querystore.bootstrap.BootstrapStatusReport getStatusReport() {
			return org.openmrs.module.querystore.bootstrap.BootstrapStatusReport.from(Collections.<BootstrapProgress> emptyList(),
			        getResourceTypeNames(), "test");
		}
		@Override public boolean isPatientProjectionIncomplete(String patientUuid) { return false; }
		@Override public void onStartup() { }
		@Override public void onShutdown() { }
	}
}
