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

import org.openmrs.Diagnosis;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;
import org.openmrs.module.querystore.serialization.DiagnosisRecordSerializer;

public class DiagnosisBootstrapper extends HibernateTypeBootstrapper<Diagnosis> {

	private final DiagnosisRecordSerializer serializer;

	public DiagnosisBootstrapper(DiagnosisRecordSerializer serializer, DbSessionFactory sessionFactory) {
		super(sessionFactory);
		this.serializer = serializer;
	}

	@Override
	protected ClinicalRecordSerializer<Diagnosis> getSerializer() {
		return serializer;
	}

	@Override
	protected boolean versionTracksContent() {
		// Every save that changes this record changes its version; see TypeBootstrapper#versionTracksContent.
		return true;
	}

	/**
	 * {@link org.openmrs.Diagnosis#getEncounter() Diagnosis.encounter} is
	 * {@code @ManyToOne(optional=false)} and eagerly fetched, so a SQL-dump load that leaves a
	 * diagnosis pointing at a missing encounter makes {@code q.list()} throw
	 * {@code FetchNotFoundException} and fail the ENTIRE diagnosis type — the patient guard doesn't
	 * cover the encounter FK. Guarding it forces the inner join that drops the orphaned diagnosis at
	 * fetch instead. (Observed on the demo: {@code FetchNotFoundException: Encounter 767 does not exist}.)
	 */
	@Override
	protected String[] additionalNonNullExprs() {
		return new String[] { "e.encounter.uuid" };
	}
}
