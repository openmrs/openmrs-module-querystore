/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.backend.mysql;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.querystore.QueryStoreConstants;
import org.openmrs.module.querystore.backend.BackendCapabilities;
import org.openmrs.module.querystore.backend.BackendStore;
import org.openmrs.module.querystore.backend.BulkWriteResult;
import org.openmrs.module.querystore.backend.DocFailure;
import org.openmrs.module.querystore.backend.Filter;
import org.openmrs.module.querystore.backend.HealthStatus;
import org.openmrs.module.querystore.backend.Hit;
import org.openmrs.module.querystore.backend.SchemaSpec;
import org.openmrs.module.querystore.backend.SearchRequest;
import org.openmrs.module.querystore.backend.SearchResult;
import org.openmrs.module.querystore.backend.UnsupportedBackendOperationException;
import org.openmrs.module.querystore.backend.WriteResult;
import org.openmrs.module.querystore.model.QueryDocument;

/**
 * MySQL reference {@link BackendStore} (Decision 3, "small" tier). Per-type tables in core's
 * database (Decision 4); raw little-endian float32 vectors stored as {@code MEDIUMBLOB};
 * MySQL FULLTEXT for BM25; brute-force cosine kNN streamed in-process. Cross-patient kNN is O(N)
 * by design — fine below ~100k records, painful past a few million per the Decision 3
 * consequences.
 */
public class MysqlBackendStore implements BackendStore {

	private static final Log log = LogFactory.getLog(MysqlBackendStore.class);

	private static final int RECOMMENDED_MAX_CORPUS = 500_000;

	private static final int BATCH_SIZE = 500;

	private static final String UPSERT_COLUMNS = "(resource_uuid, patient_uuid, record_date, text, embedding, metadata_json, last_modified)";

	private static final String[] MUTABLE_COLUMNS = { "patient_uuid", "record_date", "text", "embedding",
	        "metadata_json", "last_modified" };

	// Conditional-upsert guard so a slow projection (bootstrap scan, out-of-order event) cannot
	// overwrite a fresher document. Permits writes when either side lacks a version (last-write-wins
	// fallback) and uses >= so the same version reapplied is idempotent.
	private static final String FRESHNESS_GUARD = "VALUES(last_modified) IS NULL OR last_modified IS NULL"
	        + " OR VALUES(last_modified) >= last_modified";

	// last_modified is the only column compared cross-JVM; bind / read it through an explicit UTC
	// calendar so the DATETIME(3) round-trip preserves the Instant regardless of JVM and MySQL
	// session time zones. Without this, two nodes in different zones could write versions that
	// compare against shifted stored values.
	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	// Per-table upsert SQL cache. The SQL is identical for every write to a given resource type;
	// caching avoids re-running the FRESHNESS_GUARD loop and StringBuilder allocations on the hot
	// write path (steady-state events + bootstrap fan-out at 100s/sec).
	private final Map<String, String> upsertSqlCache = new ConcurrentHashMap<>();

	private static final Comparator<Hit> SCORE_DESC = (a, b) -> Double.compare(b.getRawScore(), a.getRawScore());

	private final DataSource dataSource;

	private final MysqlSchemaManager schemaManager;

	public MysqlBackendStore(DataSource dataSource) {
		this.dataSource = dataSource;
		this.schemaManager = new MysqlSchemaManager(dataSource);
	}

	@Override
	public void ensureSchema(String resourceType, SchemaSpec spec) {
		schemaManager.ensureTable(resourceType);
	}

	@Override
	public void deleteSchema(String resourceType) {
		schemaManager.dropTable(resourceType);
	}

	@Override
	public WriteResult upsert(QueryDocument doc) {
		validate(doc);
		schemaManager.ensureTable(doc.getResourceType());
		String table = MysqlSchemaManager.tableName(doc.getResourceType());
		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(cachedUpsertSql(table))) {
			bindUpsertParams(ps, doc);
			ps.executeUpdate();
			return WriteResult.success();
		}
		catch (SQLException e) {
			log.warn("upsert failed for " + doc.getResourceType() + "/" + doc.getResourceUuid(), e);
			return WriteResult.failed(new DocFailure(doc.getResourceType(), doc.getResourceUuid(),
			        e.getMessage(), isRetryable(e)));
		}
	}

	@Override
	public WriteResult delete(String resourceType, String resourceUuid) {
		String table = MysqlSchemaManager.tableName(resourceType);
		try (Connection conn = dataSource.getConnection();
		        PreparedStatement ps = conn.prepareStatement("DELETE FROM " + table + " WHERE resource_uuid = ?")) {
			ps.setString(1, resourceUuid);
			ps.executeUpdate();
			return WriteResult.success();
		}
		catch (SQLException e) {
			log.warn("delete failed for " + resourceType + "/" + resourceUuid, e);
			return WriteResult.failed(new DocFailure(resourceType, resourceUuid, e.getMessage(), isRetryable(e)));
		}
	}

	@Override
	public BulkWriteResult bulkUpsert(List<QueryDocument> docs) {
		// Group by resource type so each underlying PreparedStatement can be batched against one
		// table; the alternative (per-doc upsert) pays a connection acquisition per row.
		Map<String, List<QueryDocument>> byType = new LinkedHashMap<>();
		for (QueryDocument doc : docs) {
			validate(doc);
			byType.computeIfAbsent(doc.getResourceType(), k -> new ArrayList<>()).add(doc);
		}
		List<DocFailure> failures = new ArrayList<>();
		int succeeded = 0;
		for (Map.Entry<String, List<QueryDocument>> entry : byType.entrySet()) {
			succeeded += batchUpsert(entry.getKey(), entry.getValue(), failures);
		}
		return new BulkWriteResult(docs.size(), succeeded, failures);
	}

	@Override
	public BulkWriteResult bulkDelete(String resourceType, List<String> resourceUuids) {
		String table = MysqlSchemaManager.tableName(resourceType);
		List<DocFailure> failures = new ArrayList<>();
		int succeeded = 0;
		try (Connection conn = dataSource.getConnection();
		        PreparedStatement ps = conn.prepareStatement("DELETE FROM " + table + " WHERE resource_uuid = ?")) {
			for (List<String> chunk : ListUtils.partition(resourceUuids, BATCH_SIZE)) {
				for (String uuid : chunk) {
					ps.setString(1, uuid);
					ps.addBatch();
				}
				succeeded += countSucceeded(ps.executeBatch());
			}
		}
		catch (SQLException e) {
			log.warn("bulkDelete failed for " + resourceType, e);
			for (String uuid : resourceUuids) {
				failures.add(new DocFailure(resourceType, uuid, e.getMessage(), isRetryable(e)));
			}
		}
		return new BulkWriteResult(resourceUuids.size(), succeeded, failures);
	}

	@Override
	public BulkWriteResult bulkDeleteByPatient(String patientUuid) {
		Set<String> tables = schemaManager.listAllTables();
		List<DocFailure> failures = new ArrayList<>();
		int totalDeleted = 0;
		try (Connection conn = dataSource.getConnection()) {
			for (String table : tables) {
				try (PreparedStatement ps = conn.prepareStatement(
				    "DELETE FROM " + table + " WHERE patient_uuid = ?")) {
					ps.setString(1, patientUuid);
					totalDeleted += ps.executeUpdate();
				}
				catch (SQLException e) {
					log.warn("bulkDeleteByPatient failed for table " + table, e);
					failures.add(new DocFailure(stripPrefix(table), patientUuid, e.getMessage(), isRetryable(e)));
				}
			}
		}
		catch (SQLException e) {
			log.warn("bulkDeleteByPatient could not acquire connection", e);
			failures.add(new DocFailure(null, patientUuid, e.getMessage(), isRetryable(e)));
		}
		// totalRequested is unknown a priori on this overload (caller addresses by patient, not by
		// document UUID); use the post-hoc total of deletes + per-table failures so callers can
		// reason about coverage.
		return new BulkWriteResult(totalDeleted + failures.size(), totalDeleted, failures);
	}

	@Override
	public SearchResult bm25(SearchRequest req) {
		if (StringUtils.isBlank(req.getQueryText()) || req.getLimit() <= 0) {
			return SearchResult.empty();
		}
		PriorityQueue<Hit> heap = topKHeap(req.getLimit());
		for (String table : resolveTables(req)) {
			for (Hit h : bm25SingleTable(table, req)) {
				offerToHeap(heap, h, req.getLimit());
			}
		}
		return materialiseRankedFromHeap(heap, req.getLimit());
	}

	@Override
	public SearchResult knn(SearchRequest req) {
		if (req.getQueryVector() == null || req.getLimit() <= 0) {
			return SearchResult.empty();
		}
		PriorityQueue<Hit> heap = topKHeap(req.getLimit());
		for (String table : resolveTables(req)) {
			knnSingleTable(table, req, heap);
		}
		return materialiseRankedFromHeap(heap, req.getLimit());
	}

	@Override
	public SearchResult hybrid(SearchRequest req) {
		throw new UnsupportedBackendOperationException(
		        "MySQL backend has no native hybrid query; service layer must fuse bm25() + knn() ranks");
	}

	@Override
	public BackendCapabilities capabilities() {
		return new BackendCapabilities(
		        true,
		        false,
		        false,
		        RECOMMENDED_MAX_CORPUS,
		        EnumSet.of(Filter.Kind.TERM, Filter.Kind.IN, Filter.Kind.RANGE, Filter.Kind.PATIENT_SCOPE));
	}

	@Override
	public HealthStatus health() {
		try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement();
		        ResultSet rs = stmt.executeQuery("SELECT 1")) {
			rs.next();
			return HealthStatus.healthy();
		}
		catch (SQLException e) {
			return HealthStatus.unhealthy(e.getMessage());
		}
	}

	// ---------- internals ----------

	private int batchUpsert(String resourceType, List<QueryDocument> docs, List<DocFailure> failures) {
		schemaManager.ensureTable(resourceType);
		String table = MysqlSchemaManager.tableName(resourceType);
		int succeeded = 0;
		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(cachedUpsertSql(table))) {
			for (List<QueryDocument> chunk : ListUtils.partition(docs, BATCH_SIZE)) {
				for (QueryDocument doc : chunk) {
					bindUpsertParams(ps, doc);
					ps.addBatch();
				}
				succeeded += countSucceeded(ps.executeBatch());
			}
		}
		catch (SQLException e) {
			log.warn("bulkUpsert failed for " + resourceType, e);
			for (QueryDocument doc : docs) {
				failures.add(new DocFailure(resourceType, doc.getResourceUuid(), e.getMessage(), isRetryable(e)));
			}
		}
		return succeeded;
	}

	// Returns up to `limit` per-table hits — SQL FULLTEXT does the top-K already, so the caller
	// only has to merge across tables. (Contrast {@link #knnSingleTable}, which has no SQL LIMIT
	// and streams into the heap because cosine is brute-force in-process.)
	private List<Hit> bm25SingleTable(String table, SearchRequest req) {
		MysqlFilterTranslator translator = new MysqlFilterTranslator();
		translator.translate(req.getFilters());
		String filterSql = translator.getSql();

		StringBuilder sql = new StringBuilder();
		// Embedding column omitted: BM25 callers do not consume the vector and decoding it per row
		// is wasted work on every hybrid-search hit.
		sql.append("SELECT resource_uuid, patient_uuid, record_date, text, metadata_json, last_modified, ")
		        .append("MATCH(text) AGAINST (? IN NATURAL LANGUAGE MODE) AS score FROM ").append(table)
		        .append(" WHERE MATCH(text) AGAINST (? IN NATURAL LANGUAGE MODE)");
		appendFilterClause(sql, filterSql);
		sql.append(" ORDER BY score DESC LIMIT ?");

		List<Hit> hits = new ArrayList<>();
		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			int idx = 1;
			ps.setString(idx++, req.getQueryText());
			ps.setString(idx++, req.getQueryText());
			idx = bindFilterParams(ps, idx, translator.getParams());
			ps.setInt(idx, req.getLimit());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					QueryDocument doc = readDocument(table, rs, false);
					hits.add(new Hit(doc, rs.getDouble("score"), 0));
				}
			}
		}
		catch (SQLException e) {
			throw new IllegalStateException("BM25 query on " + table + " failed", e);
		}
		return hits;
	}

	private void knnSingleTable(String table, SearchRequest req, PriorityQueue<Hit> heap) {
		MysqlFilterTranslator translator = new MysqlFilterTranslator();
		translator.translate(req.getFilters());
		String filterSql = translator.getSql();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT resource_uuid, patient_uuid, record_date, text, embedding, metadata_json, last_modified FROM ").append(table)
		        .append(" WHERE embedding IS NOT NULL");
		appendFilterClause(sql, filterSql);

		float[] queryVector = req.getQueryVector();
		double queryNorm = MysqlVectorCodec.norm(queryVector);

		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			ps.setFetchSize(1000);
			bindFilterParams(ps, 1, translator.getParams());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					byte[] storedBytes = rs.getBytes("embedding");
					double score = MysqlVectorCodec.cosineFromBytes(queryVector, queryNorm, storedBytes);
					// Heap admission before materialising the document. On a 100k-corpus query with
					// limit=10 this skips ~99 990 QueryDocument constructions and float[] decodes.
					if (heap.size() >= req.getLimit() && score <= heap.peek().getRawScore()) {
						continue;
					}
					QueryDocument doc = readDocument(table, rs, false);
					doc.setEmbedding(MysqlVectorCodec.decode(storedBytes));
					offerToHeap(heap, new Hit(doc, score, 0), req.getLimit());
				}
			}
		}
		catch (SQLException e) {
			throw new IllegalStateException("kNN scan on " + table + " failed", e);
		}
	}

	private List<String> resolveTables(SearchRequest req) {
		if (req.getResourceTypes().isEmpty()) {
			// Wildcard read: prefer the cache populated by ensureTable / prior enumeration. A cold
			// JVM (no schema work yet) falls back to the metadata probe and seeds the cache. Stale
			// vs. another JVM's writes is acceptable on the read path — unknown types just return
			// zero hits.
			Set<String> cached = schemaManager.getKnownTables();
			return new ArrayList<>(cached.isEmpty() ? schemaManager.listAllTables() : cached);
		}
		List<String> tables = new ArrayList<>(req.getResourceTypes().size());
		for (String type : req.getResourceTypes()) {
			schemaManager.ensureTable(type);
			tables.add(MysqlSchemaManager.tableName(type));
		}
		return tables;
	}

	private static PriorityQueue<Hit> topKHeap(int limit) {
		return new PriorityQueue<>(limit + 1, (a, b) -> Double.compare(a.getRawScore(), b.getRawScore()));
	}

	private static void offerToHeap(PriorityQueue<Hit> heap, Hit hit, int limit) {
		heap.offer(hit);
		if (heap.size() > limit) {
			heap.poll();
		}
	}

	private static SearchResult materialiseRankedFromHeap(PriorityQueue<Hit> heap, int limit) {
		List<Hit> ordered = new ArrayList<>(heap);
		ordered.sort(SCORE_DESC);
		int n = Math.min(ordered.size(), limit);
		List<Hit> ranked = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			Hit h = ordered.get(i);
			ranked.add(new Hit(h.getDocument(), h.getRawScore(), i + 1));
		}
		return new SearchResult(ranked);
	}

	private QueryDocument readDocument(String table, ResultSet rs, boolean includeEmbedding) throws SQLException {
		QueryDocument doc = new QueryDocument();
		doc.setResourceType(stripPrefix(table));
		doc.setResourceUuid(rs.getString("resource_uuid"));
		doc.setPatientUuid(rs.getString("patient_uuid"));
		Date d = rs.getDate("record_date");
		if (d != null) {
			doc.setDate(d.toLocalDate());
		}
		doc.setText(rs.getString("text"));
		if (includeEmbedding) {
			doc.setEmbedding(MysqlVectorCodec.decode(rs.getBytes("embedding")));
		}
		Timestamp ts = rs.getTimestamp("last_modified", Calendar.getInstance(UTC));
		if (ts != null) {
			doc.setLastModified(ts.toInstant());
		}
		Map<String, Object> meta = MysqlMetadataCodec.decode(rs.getString("metadata_json"));
		for (Map.Entry<String, Object> entry : meta.entrySet()) {
			doc.putMetadata(entry.getKey(), entry.getValue());
		}
		return doc;
	}

	private String cachedUpsertSql(String table) {
		return upsertSqlCache.computeIfAbsent(table, MysqlBackendStore::buildUpsertSql);
	}

	private static String buildUpsertSql(String table) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ").append(table).append(" ").append(UPSERT_COLUMNS)
		        .append(" VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE ");
		for (int i = 0; i < MUTABLE_COLUMNS.length; i++) {
			if (i > 0) {
				sql.append(", ");
			}
			String c = MUTABLE_COLUMNS[i];
			sql.append(c).append(" = IF(").append(FRESHNESS_GUARD)
			        .append(", VALUES(").append(c).append("), ").append(c).append(")");
		}
		return sql.toString();
	}

	private static void appendFilterClause(StringBuilder sql, String filterSql) {
		if (!filterSql.isEmpty()) {
			sql.append(" AND ").append(filterSql);
		}
	}

	private static int bindFilterParams(PreparedStatement ps, int startIndex, List<Object> params) throws SQLException {
		int idx = startIndex;
		for (Object p : params) {
			ps.setObject(idx++, toJdbcValue(p));
		}
		return idx;
	}

	private static void bindUpsertParams(PreparedStatement ps, QueryDocument doc) throws SQLException {
		ps.setString(1, doc.getResourceUuid());
		ps.setString(2, doc.getPatientUuid());
		if (doc.getDate() != null) {
			ps.setDate(3, Date.valueOf(doc.getDate()));
		} else {
			ps.setNull(3, Types.DATE);
		}
		ps.setString(4, doc.getText());
		ps.setBytes(5, MysqlVectorCodec.encode(doc.getEmbedding()));
		ps.setString(6, MysqlMetadataCodec.encode(doc.getMetadata()));
		if (doc.getLastModified() != null) {
			ps.setTimestamp(7, Timestamp.from(doc.getLastModified()), Calendar.getInstance(UTC));
		} else {
			ps.setNull(7, Types.TIMESTAMP);
		}
	}

	private static int countSucceeded(int[] batchResult) {
		int n = 0;
		for (int r : batchResult) {
			if (r >= 0 || r == Statement.SUCCESS_NO_INFO) {
				n++;
			}
		}
		return n;
	}

	private static Object toJdbcValue(Object o) {
		if (o instanceof LocalDate) {
			return Date.valueOf((LocalDate) o);
		}
		return o;
	}

	private static String stripPrefix(String table) {
		return StringUtils.removeStart(table, QueryStoreConstants.INDEX_PREFIX);
	}

	private static boolean isRetryable(SQLException e) {
		String state = e.getSQLState();
		return state != null && (state.startsWith("08") || state.startsWith("40"));
	}

	private static void validate(QueryDocument doc) {
		if (doc == null || doc.getResourceType() == null || doc.getResourceUuid() == null
		        || doc.getPatientUuid() == null) {
			throw new IllegalArgumentException("QueryDocument must have resourceType, resourceUuid, and patientUuid");
		}
	}
}
