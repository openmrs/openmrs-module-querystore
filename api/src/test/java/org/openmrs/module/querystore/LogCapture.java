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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;

/**
 * Captures what one class logs, at {@code DEBUG} and above, for the duration of a test. A line whose
 * event carried a throwable ends with {@link #THROWN}. The test
 * log4j2 config turns the root logger off, so this adds a logger config for the class with its own
 * appender and removes it on {@link #close()}.
 */
public final class LogCapture implements AutoCloseable {

	/** Appended to a captured line whose event carried a throwable. */
	public static final String THROWN = " <thrown>";

	private final List<String> lines = new CopyOnWriteArrayList<>();

	private final List<String> sources = new CopyOnWriteArrayList<>();

	private final String loggerName;

	private final AbstractAppender appender;

	private LogCapture(String loggerName) {
		this.loggerName = loggerName;
		this.appender = new AbstractAppender("capture-" + loggerName, null, null, true, Property.EMPTY_ARRAY) {

			@Override
			public void append(LogEvent event) {
				lines.add(event.getLevel() + " " + event.getMessage().getFormattedMessage()
				        + (event.getThrown() != null ? THROWN : ""));
				StackTraceElement source = event.getSource();
				sources.add(source == null ? "" : source.getClassName());
			}
		};
		appender.start();
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		Configuration config = context.getConfiguration();
		LoggerConfig loggerConfig = new LoggerConfig(loggerName, Level.DEBUG, false);
		loggerConfig.addAppender(appender, Level.DEBUG, null);
		config.addLogger(loggerName, loggerConfig);
		context.updateLoggers();
	}

	public static LogCapture of(Class<?> type) {
		return new LogCapture(type.getName());
	}

	/** Captures a logger by name, and every logger under it that has no config of its own. */
	public static LogCapture of(String loggerName) {
		return new LogCapture(loggerName);
	}

	/** Every captured line as {@code "LEVEL message"}. */
	public List<String> lines() {
		return new ArrayList<>(lines);
	}

	/** The class each captured line was logged from, as the log pattern's location would print it. */
	public List<String> sources() {
		return new ArrayList<>(sources);
	}

	/** Captured lines at the given level, message only. */
	public List<String> at(String level) {
		List<String> out = new ArrayList<>();
		for (String line : lines) {
			if (line.startsWith(level + " ")) {
				out.add(line.substring(level.length() + 1));
			}
		}
		return out;
	}

	@Override
	public void close() {
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		context.getConfiguration().removeLogger(loggerName);
		context.updateLoggers();
		appender.stop();
	}
}
