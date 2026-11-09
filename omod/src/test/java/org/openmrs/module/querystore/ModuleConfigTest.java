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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.openmrs.module.ModuleUtil;

/**
 * The module needs core 2.9: it listens for the {@code org.openmrs.aop.event} service events that
 * first shipped there (ADR Decision 12). Core 2.8.x installs it and then fails to start it, so
 * config.xml must refuse 2.8 up front.
 */
public class ModuleConfigTest {

	@Test
	public void requireVersion_admitsCore29AndLater_andRefuses28() throws Exception {
		String required = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		        .parse(new File("src/main/resources/config.xml")).getElementsByTagName("require_version").item(0)
		        .getTextContent().trim();

		assertTrue(required, ModuleUtil.matchRequiredVersions("2.9.0-SNAPSHOT", required));
		assertTrue(required, ModuleUtil.matchRequiredVersions("2.9.0", required));
		assertTrue(required, ModuleUtil.matchRequiredVersions("2.10.0", required));
		assertFalse(required, ModuleUtil.matchRequiredVersions("2.8.7", required));
	}
}
