/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.Test;

/**
 * WARN reaches production logs by default, so no WARN line may carry a patient's or an encounter's
 * uuid (those go to DEBUG, like the [QSEVAL] lines). This scans every {@code log.warn(...)} statement
 * in the api and omod sources for a variable that holds one; a record named by type and uuid goes
 * through {@link SkipLogFormat#loggableUuid}, which omits the identifying types.
 */
public class WarnLogHygieneTest {

	private static final Pattern WARN_STATEMENT = Pattern.compile("log\\.warn\\((.*?)\\);", Pattern.DOTALL);

	private static final Pattern WARN_OR_ERROR_STATEMENT = Pattern.compile("log\\.(warn|error)\\((.*?)\\);", Pattern.DOTALL);

	private static final Pattern SAFELOG_CALL = Pattern.compile("SafeLog\\.(warn|error)\\((.*?)\\);", Pattern.DOTALL);

	private static final Pattern CATCH_PARAMETER = Pattern.compile("catch\\s*\\([^)]*?\\b(\\w+)\\s*\\)");

	private static final Pattern IDENTIFYING_VARIABLE = Pattern.compile("(?i)\\b\\w*(patient|encounter)uuid\\w*\\b");

	@Test
	public void noWarnStatementLogsAPatientOrEncounterUuid() throws IOException {
		List<String> offenders = new ArrayList<>();
		int scanned = 0;
		for (Path file : sourceFiles()) {
			Matcher warn = WARN_STATEMENT.matcher(new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
			while (warn.find()) {
				scanned++;
				if (IDENTIFYING_VARIABLE.matcher(warn.group(1)).find()) {
					offenders.add(file.getFileName() + ": log.warn(" + warn.group(1).replaceAll("\\s+", " ") + ")");
				}
			}
		}
		assertTrue("the scan found no WARN statements, so it isn't scanning anything", scanned > 20);
		assertEquals("WARN statements that log a patient or encounter uuid", new ArrayList<String>(), offenders);
	}

	/**
	 * A WARN or ERROR line must not carry an exception's stack or raw message: either can quote any
	 * uuid. Failures go through {@link SafeLog}, which scrubs the message and puts the stack at DEBUG;
	 * a message spliced into the text goes through {@link SkipLogFormat#scrubUuids} (or
	 * {@link SkipLogFormat#format}, which scrubs its reason).
	 */
	@Test
	public void noWarnOrErrorStatementCarriesARawException() throws IOException {
		List<String> offenders = new ArrayList<>();
		int scanned = 0;
		for (Path file : sourceFiles()) {
			String source = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
			List<String> catchParameters = new ArrayList<>();
			Matcher c = CATCH_PARAMETER.matcher(source);
			while (c.find()) {
				catchParameters.add(c.group(1));
			}
			Matcher statement = WARN_OR_ERROR_STATEMENT.matcher(source);
			while (statement.find()) {
				scanned++;
				String args = statement.group(2);
				String last = args.contains(",") ? args.substring(args.lastIndexOf(',') + 1).trim() : "";
				boolean passesException = catchParameters.contains(last);
				for (String parameter : catchParameters) {
					// Spliced into the text: "..." + e, or e.toString(), carries the raw message.
					if (Pattern.compile("\\+\\s*" + parameter + "\\b(?!\\s*\\.)|\\b" + parameter + "\\.toString\\(")
					        .matcher(args).find()) {
						passesException = true;
					}
				}
				boolean splicesRawMessage = args.contains("getMessage()") && !args.contains("scrubUuids(")
				        && !args.contains("SkipLogFormat.format(");
				if (passesException || splicesRawMessage) {
					offenders.add(file.getFileName() + ": log." + statement.group(1) + "(" + args.replaceAll("\\s+", " ") + ")");
				}
			}
		}
		assertTrue("the scan found no WARN or ERROR statements, so it isn't scanning anything", scanned > 20);
		assertEquals("WARN/ERROR statements that carry a raw exception; use SafeLog", new ArrayList<String>(), offenders);
	}

	/**
	 * SafeLog scrubs uuids from its message at runtime; this keeps the call sites honest too, so a
	 * patient or encounter uuid, or an exception's raw text, is not handed to it in the first place.
	 * The message is every argument between the owner class and the throwable.
	 */
	@Test
	public void noSafeLogMessageCarriesAPatientOrEncounterUuidOrARawException() throws IOException {
		List<String> offenders = new ArrayList<>();
		int scanned = 0;
		for (Path file : sourceFiles()) {
			String source = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
			List<String> catchParameters = new ArrayList<>();
			Matcher c = CATCH_PARAMETER.matcher(source);
			while (c.find()) {
				catchParameters.add(c.group(1));
			}
			Matcher call = SAFELOG_CALL.matcher(source);
			while (call.find()) {
				scanned++;
				String args = call.group(2);
				int first = args.indexOf(',');
				int last = args.lastIndexOf(',');
				String message = first >= 0 && last > first ? args.substring(first + 1, last) : "";
				boolean splices = false;
				for (String parameter : catchParameters) {
					if (Pattern.compile("\\+\\s*" + parameter + "\\b(?!\\s*\\.)|\\b" + parameter + "\\.toString\\(")
					        .matcher(message).find()) {
						splices = true;
					}
				}
				boolean rawMessage = message.contains("getMessage()") && !message.contains("scrubUuids(")
				        && !message.contains("SkipLogFormat.format(");
				if (IDENTIFYING_VARIABLE.matcher(message).find() || splices || rawMessage) {
					offenders.add(file.getFileName() + ": SafeLog." + call.group(1) + "(" + args.replaceAll("\\s+", " ") + ")");
				}
			}
		}
		assertTrue("the scan found no SafeLog calls, so it isn't scanning anything", scanned > 20);
		assertEquals("SafeLog messages that carry a patient or encounter uuid or an exception's raw text",
		    new ArrayList<String>(), offenders);
	}

	/** Every main source file in api and omod; fails if none is found, so a clean scan means something. */
	private static List<Path> sourceFiles() throws IOException {
		List<Path> files = new ArrayList<>();
		for (String root : new String[] { "src/main/java", "../omod/src/main/java" }) {
			if (Files.isDirectory(Paths.get(root))) {
				try (Stream<Path> walk = Files.walk(Paths.get(root))) {
					walk.filter(p -> p.toString().endsWith(".java")).forEach(files::add);
				}
			}
		}
		assertTrue("expected to run from the api module directory and find its sources", !files.isEmpty());
		return files;
	}
}
