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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.openmrs.module.querystore.QueryStoreConstants;

/**
 * Creates and drops per-type tables ({@code querystore_<type>}) in the OpenMRS database. Tables are
 * created lazily on first {@link #ensureTable(String)} call; existence is cached so repeated
 * upserts don't repeat DDL probes. The {@code querystore_} prefix on every table name (Decision 4)
 * keeps querystore data unambiguously identifiable next to core's tables.
 */
final class MysqlSchemaManager {

	private final DataSource dataSource;

	// v1 single-JVM assumption: `knownTables` reflects DDL issued by this JVM. A sibling node
	// creating a table elsewhere is invisible until `listAllTables()` is called explicitly.
	private final Set<String> knownTables = ConcurrentHashMap.newKeySet();

	MysqlSchemaManager(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/** Idempotently creates the {@code querystore_<resourceType>} table if missing. */
	void ensureTable(String resourceType) {
		String table = tableName(resourceType);
		if (knownTables.contains(table)) {
			return;
		}
		try (Connection conn = dataSource.getConnection()) {
			if (!tableExists(conn, table)) {
				createTable(conn, table);
			}
			knownTables.add(table);
		}
		catch (SQLException e) {
			throw new IllegalStateException("Could not ensure schema for " + table, e);
		}
	}

	void dropTable(String resourceType) {
		String table = tableName(resourceType);
		try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate("DROP TABLE IF EXISTS " + table);
			knownTables.remove(table);
		}
		catch (SQLException e) {
			throw new IllegalStateException("Could not drop " + table, e);
		}
	}

	/**
	 * Returns the cached set of known tables — populated by {@link #ensureTable(String)} and
	 * {@link #listAllTables()}. Cheap; safe to call on hot search paths. Empty until at least one
	 * table has been ensured or enumerated.
	 */
	Set<String> getKnownTables() {
		return new HashSet<>(knownTables);
	}

	/**
	 * Returns the names of every {@code querystore_*} table currently in the database. Used by
	 * cross-table operations like {@link MysqlBackendStore#bulkDeleteByPatient(String)} where the
	 * caller does not know which types contain documents for a given patient.
	 */
	Set<String> listAllTables() {
		Set<String> tables = new HashSet<>();
		try (Connection conn = dataSource.getConnection()) {
			DatabaseMetaData md = conn.getMetaData();
			try (ResultSet rs = md.getTables(conn.getCatalog(), null, QueryStoreConstants.INDEX_PREFIX + "%",
			    new String[] { "TABLE" })) {
				while (rs.next()) {
					tables.add(rs.getString("TABLE_NAME").toLowerCase());
				}
			}
		}
		catch (SQLException e) {
			throw new IllegalStateException("Could not enumerate querystore tables", e);
		}
		knownTables.addAll(tables);
		return tables;
	}

	static String tableName(String resourceType) {
		if (resourceType == null || !resourceType.matches("[a-z][a-z0-9_]*")) {
			throw new IllegalArgumentException("Invalid resource type (must match [a-z][a-z0-9_]*): " + resourceType);
		}
		return QueryStoreConstants.INDEX_PREFIX + resourceType;
	}

	private boolean tableExists(Connection conn, String table) throws SQLException {
		DatabaseMetaData md = conn.getMetaData();
		try (ResultSet rs = md.getTables(conn.getCatalog(), null, table, new String[] { "TABLE" })) {
			return rs.next();
		}
	}

	private void createTable(Connection conn, String table) throws SQLException {
		String ddl = "CREATE TABLE " + table + " ("
		        + "  id BIGINT NOT NULL AUTO_INCREMENT,"
		        + "  resource_uuid CHAR(38) NOT NULL,"
		        + "  patient_uuid CHAR(38) NOT NULL,"
		        + "  record_date DATE NULL,"
		        + "  text MEDIUMTEXT,"
		        + "  embedding MEDIUMBLOB,"
		        + "  metadata_json MEDIUMTEXT,"
		        + "  last_modified DATETIME(3) NULL,"
		        + "  PRIMARY KEY (id),"
		        + "  UNIQUE KEY uq_" + table + "_resource (resource_uuid),"
		        + "  KEY idx_" + table + "_patient (patient_uuid),"
		        + "  KEY idx_" + table + "_date (record_date),"
		        + "  FULLTEXT KEY ft_" + table + "_text (text)"
		        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(ddl);
		}
	}
}
