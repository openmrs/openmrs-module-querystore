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

import org.openmrs.Obs;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;
import org.openmrs.module.querystore.serialization.ObsRecordSerializer;

public class ObsBootstrapper extends HibernateTypeBootstrapper<Obs> {

	private final ObsRecordSerializer serializer;

	public ObsBootstrapper(ObsRecordSerializer serializer, DbSessionFactory sessionFactory) {
		super(sessionFactory);
		this.serializer = serializer;
	}

	@Override
	protected ClinicalRecordSerializer<Obs> getSerializer() {
		return serializer;
	}

	@Override
	protected boolean versionTracksContent() {
		// Every save that changes this record changes its version; see TypeBootstrapper#versionTracksContent.
		return true;
	}

	@Override
	protected String cursorDateExpr() {
		// Obs.hbm.xml does not map dateChanged (OpenMRS 2.8.x); cursor uses dateCreated alone.
		return "e.dateCreated";
	}

	@Override
	protected String patientAssociationExpr() {
		// Obs has only a Person association, not a Patient. Patient extends Person and shares the
		// Person UUID, so filtering by e.person.uuid resolves to the same UUID a patient-scoped
		// caller passes in.
		return "e.person.uuid";
	}
}
