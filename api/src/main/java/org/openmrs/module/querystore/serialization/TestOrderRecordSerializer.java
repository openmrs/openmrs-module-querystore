/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.serialization;

import org.openmrs.TestOrder;
import org.openmrs.module.querystore.model.QueryDocument;

/**
 * Serializes a {@link TestOrder} into a {@link QueryDocument} for the {@code querystore_test_order}
 * index. {@code TestOrder} and {@code ReferralOrder} are 100%-field-identical {@link
 * org.openmrs.ServiceOrder} subtypes, so the populate/text/metadata logic lives in {@link
 * AbstractServiceOrderRecordSerializer}; this class only declares the discriminators that pin the
 * subtype to its {@code querystore_test_order} index. Test-order surface: laterality
 * (LEFT/RIGHT/BILATERAL), specimen source (Concept, UUID+name per Decision 9), clinical history
 * (free text, in embedded text), and the shared Order-base fields (action, urgency, care setting,
 * previous order, order number, date stopped, auto-expire date; orderer overrides the
 * encounter-derived provider per the ADR Decision 6 serializer convention).
 */
public class TestOrderRecordSerializer extends AbstractServiceOrderRecordSerializer<TestOrder> {

	@Override
	public String getResourceType() {
		return "test_order";
	}

	@Override
	public Class<TestOrder> getSupportedType() {
		return TestOrder.class;
	}

	@Override
	protected String getTextPrefix() {
		return "Test order: ";
	}
}
