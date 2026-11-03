/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.bootstrap;

import org.openmrs.Condition;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;
import org.openmrs.module.querystore.serialization.ConditionRecordSerializer;

public class ConditionBootstrapper extends HibernateTypeBootstrapper<Condition> {

	private final ConditionRecordSerializer serializer;

	public ConditionBootstrapper(ConditionRecordSerializer serializer, DbSessionFactory sessionFactory) {
		super(sessionFactory);
		this.serializer = serializer;
	}

	@Override
	protected ClinicalRecordSerializer<Condition> getSerializer() {
		return serializer;
	}

	@Override
	protected boolean versionTracksContent() {
		// Every save that changes this record changes its version; see TypeBootstrapper#versionTracksContent.
		return true;
	}
}
