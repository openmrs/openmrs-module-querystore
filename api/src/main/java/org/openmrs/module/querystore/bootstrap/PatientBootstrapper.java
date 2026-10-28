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

import org.openmrs.Patient;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;
import org.openmrs.module.querystore.serialization.PatientRecordSerializer;

/**
 * The default {@code e.dateChanged} cursor on Patient resolves to the {@code patient} table's
 * audit column (per {@code Patient.hbm.xml}), not the underlying Person's. Person-level edits —
 * names, addresses, birthdate, person attributes, all of which {@link PatientRecordSerializer}
 * projects — touch {@code person.date_changed} (mapped as {@code personDateChanged}) and are
 * invisible to this cursor. A from-scratch bootstrap still projects every patient once with
 * current state (Hibernate loads the live entity), so initial scans are correct. The gap shows up
 * on incremental top-up of a {@code COMPLETED} progress row: Person-only edits between completion
 * and re-run aren't re-projected here. {@link org.openmrs.module.querystore.api.QueryStoreService}
 * event handlers (Decision 12) catch those edits in steady state.
 */
public class PatientBootstrapper extends HibernateTypeBootstrapper<Patient> {

	private final PatientRecordSerializer serializer;

	public PatientBootstrapper(PatientRecordSerializer serializer, DbSessionFactory sessionFactory) {
		super(sessionFactory);
		this.serializer = serializer;
	}

	@Override
	protected ClinicalRecordSerializer<Patient> getSerializer() {
		return serializer;
	}

	@Override
	protected String patientAssociationExpr() {
		// Patient is the entity itself — no patient association to traverse. Filter by uuid; the
		// per-patient page returns at most one row.
		return "e.uuid";
	}
}
