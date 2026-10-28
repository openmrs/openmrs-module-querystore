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

import org.openmrs.ReferralOrder;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;
import org.openmrs.module.querystore.serialization.ReferralOrderRecordSerializer;

public class ReferralOrderBootstrapper extends HibernateTypeBootstrapper<ReferralOrder> {

	private final ReferralOrderRecordSerializer serializer;

	public ReferralOrderBootstrapper(ReferralOrderRecordSerializer serializer, DbSessionFactory sessionFactory) {
		super(sessionFactory);
		this.serializer = serializer;
	}

	@Override
	protected ClinicalRecordSerializer<ReferralOrder> getSerializer() {
		return serializer;
	}

	@Override
	protected String cursorDateExpr() {
		// Order.hbm.xml does not map dateChanged; cursor uses dateCreated alone.
		return "e.dateCreated";
	}

	@Override
	protected String[] additionalNonNullExprs() {
		// Order.encounter is not-null + eager (Order.hbm.xml) — same orphan guard as Diagnosis; see
		// additionalNonNullExprs(). Without it a dump-orphaned encounter FK fails the whole type.
		return new String[] { "e.encounter.uuid" };
	}
}
