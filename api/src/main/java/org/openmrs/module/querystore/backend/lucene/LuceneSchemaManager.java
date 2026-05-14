/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.backend.lucene;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.openmrs.module.querystore.QueryStoreConstants;

/**
 * Owns the per-type {@link IndexWriter} instances. One Lucene directory per resource type
 * ({@code querystore_<type>} per Decision 4); writers are created lazily on first
 * {@link #ensureWriter(String)} call and cached for the JVM lifetime.
 */
final class LuceneSchemaManager implements AutoCloseable {

	private static final Log log = LogFactory.getLog(LuceneSchemaManager.class);

	private final Path indexRoot;

	private final ConcurrentMap<String, IndexWriter> writers = new ConcurrentHashMap<>();

	LuceneSchemaManager(Path indexRoot) {
		this.indexRoot = indexRoot;
	}

	/**
	 * Returns the {@link IndexWriter} for {@code resourceType}, creating it (and the on-disk
	 * directory) if absent. Idempotent.
	 */
	IndexWriter ensureWriter(String resourceType) {
		String name = indexName(resourceType);
		return writers.computeIfAbsent(name, this::openWriter);
	}

	/**
	 * Closes the writer (if open) and deletes the directory contents on disk. Used by tests and
	 * full-rebuild flows. After this returns, {@link #ensureWriter(String)} will recreate.
	 */
	void dropWriter(String resourceType) {
		String name = indexName(resourceType);
		IndexWriter writer = writers.remove(name);
		if (writer != null) {
			try {
				writer.close();
			}
			catch (IOException e) {
				throw new IllegalStateException("Could not close writer for " + name, e);
			}
		}
		Path dir = indexRoot.resolve(name);
		deleteRecursive(dir);
	}

	Set<String> knownIndexNames() {
		return new HashSet<>(writers.keySet());
	}

	/**
	 * Enumerates {@code querystore_*} index directories on disk. Used by cross-type operations
	 * (e.g. {@code bulkDeleteByPatient}) where the caller does not know which types contain
	 * documents for a given patient. Symmetric with the MySQL backend's {@code listAllTables}.
	 *
	 * <p>Side-effect: any directory discovered here that doesn't already have a cached writer gets
	 * one opened eagerly — the caller's next {@link #ensureWriter(String)} for that type is then a
	 * cache hit. Callers that just want a read-only enumeration are not the intended audience.
	 */
	Set<String> listAllIndexes() {
		Set<String> names = new HashSet<>();
		if (!Files.isDirectory(indexRoot)) {
			return names;
		}
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(indexRoot,
		    QueryStoreConstants.INDEX_PREFIX + "*")) {
			for (Path child : stream) {
				if (Files.isDirectory(child)) {
					String name = child.getFileName().toString();
					names.add(name);
					writers.computeIfAbsent(name, this::openWriter);
				}
			}
		}
		catch (IOException e) {
			throw new IllegalStateException("Could not enumerate Lucene indexes under " + indexRoot, e);
		}
		return names;
	}

	@Override
	public void close() {
		for (Map.Entry<String, IndexWriter> entry : writers.entrySet()) {
			try {
				entry.getValue().close();
			}
			catch (IOException e) {
				// Continue closing siblings — but log: a stuck writer here usually means a held
				// index lock, which an operator chasing "module won't restart" needs to see.
				log.warn("Could not close Lucene writer " + entry.getKey() + " during shutdown", e);
			}
		}
		writers.clear();
	}

	static String indexName(String resourceType) {
		if (resourceType == null || !resourceType.matches("[a-z][a-z0-9_]*")) {
			throw new IllegalArgumentException(
			        "Invalid resource type (must match [a-z][a-z0-9_]*): " + resourceType);
		}
		return QueryStoreConstants.INDEX_PREFIX + resourceType;
	}

	private IndexWriter openWriter(String indexName) {
		try {
			Path dir = indexRoot.resolve(indexName);
			Files.createDirectories(dir);
			IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer())
			        .setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			return new IndexWriter(FSDirectory.open(dir), config);
		}
		catch (org.apache.lucene.store.LockObtainFailedException e) {
			// Most common operational failure mode: a previous JVM crashed and left write.lock in
			// place. Name the cause directly so operators know what to clean up rather than
			// digging into the cause chain.
			throw new IllegalStateException("Could not open Lucene index " + indexName
			        + ": write.lock is held (stale lock from a previous JVM crash?)", e);
		}
		catch (IOException e) {
			throw new IllegalStateException("Could not open Lucene index " + indexName
			        + ": " + e.getClass().getSimpleName() + " — " + e.getMessage(), e);
		}
	}

	private static void deleteRecursive(Path dir) {
		if (!Files.exists(dir)) {
			return;
		}
		try (DirectoryStream<Path> children = Files.newDirectoryStream(dir)) {
			for (Path child : children) {
				if (Files.isDirectory(child)) {
					deleteRecursive(child);
				} else {
					Files.deleteIfExists(child);
				}
			}
			Files.deleteIfExists(dir);
		}
		catch (IOException e) {
			throw new IllegalStateException("Could not delete index directory " + dir, e);
		}
	}
}
