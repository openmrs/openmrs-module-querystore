/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.embedding;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmrs.module.querystore.SkipLogFormat;

/**
 * Resolves a configured model-file path against the OpenMRS application data directory, with
 * path-traversal protection (rejects {@code ..}, confirms the resolved path stays within the
 * data directory) and existence verification. Also strips the macOS {@code com.apple.quarantine}
 * extended attribute when present so that ONNX native loaders can open downloaded model files
 * without a Gatekeeper block.
 */
final class ModelFileResolver {

	private ModelFileResolver() {
	}

	private static final Logger log = LoggerFactory.getLogger(ModelFileResolver.class);

	/**
	 * Resolves a model path relative to the OpenMRS application data directory. Rejects paths
	 * containing {@code ..} to prevent path traversal and verifies the resolved path stays within
	 * the application data directory.
	 *
	 * @param relativePath the relative path from the global property
	 * @param globalPropertyName the global property name, used in error messages
	 * @return the absolute path to the model file
	 * @throws IllegalStateException if the path is invalid, traverses outside the data directory,
	 *         or the file does not exist
	 */
	static String resolveModelPath(String relativePath, String globalPropertyName) {
		if (StringUtils.isBlank(relativePath)) {
			throw new IllegalStateException(
					"Model path is not configured: " + globalPropertyName);
		}
		if (relativePath.contains("..")) {
			throw new IllegalStateException(
					"Model path must not contain '..': " + globalPropertyName);
		}

		File appDataDir = new File(OpenmrsUtil.getApplicationDataDirectory());
		File modelFile = new File(appDataDir, relativePath);

		try {
			String canonicalPath = modelFile.getCanonicalPath();
			String canonicalDataDir = appDataDir.getCanonicalPath();
			if (!canonicalPath.startsWith(canonicalDataDir + File.separator)) {
				throw new IllegalStateException(
						"Model path must resolve to within the OpenMRS application data directory: "
								+ globalPropertyName);
			}
		}
		catch (IOException e) {
			throw new IllegalStateException(
					"Failed to resolve model path for " + globalPropertyName, e);
		}

		if (!modelFile.exists()) {
			throw new IllegalStateException(
					"Model file not found: " + modelFile.getAbsolutePath()
							+ ". Set the correct relative path in " + globalPropertyName);
		}

		removeQuarantineAttribute(modelFile.toPath());

		return modelFile.getAbsolutePath();
	}

	/**
	 * Removes the macOS quarantine extended attribute from a file if present. Downloaded files
	 * on macOS are tagged with {@code com.apple.quarantine}, which prevents native libraries
	 * (e.g. ONNX Runtime) from loading them. Uses the {@code xattr} command because Java's
	 * {@code UserDefinedFileAttributeView} only covers the {@code user.} namespace and cannot
	 * access Apple system attributes.
	 */
	static void removeQuarantineAttribute(Path path) {
		if (!SystemUtils.IS_OS_MAC) {
			return;
		}
		try {
			Process process = new ProcessBuilder(
					"xattr", "-d", "com.apple.quarantine", path.toString())
					.redirectErrorStream(true)
					.start();
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				log.info("Removed macOS quarantine attribute from {}", path);
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("Interrupted removing macOS quarantine attribute from {}", path);
		}
		catch (IOException e) {
			log.warn("Failed to remove macOS quarantine attribute from {}: {}",
					path, SkipLogFormat.scrubUuids(e.getMessage()));
		}
	}
}
