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

import org.openmrs.ReferralOrder;
import org.openmrs.module.querystore.model.QueryDocument;

/**
 * Serializes a {@link ReferralOrder} into a {@link QueryDocument} for the {@code
 * querystore_referral_order} index. {@code ReferralOrder} is structurally identical to {@code
 * TestOrder} — both extend {@link org.openmrs.ServiceOrder} and add no fields — so the populate
 * logic lives in {@link AbstractServiceOrderRecordSerializer}; this class only declares the
 * discriminators that pin the subtype to its {@code querystore_referral_order} index. The surface
 * actually written today is the Order-base fields plus ServiceOrder's laterality, specimen source,
 * and clinical history. {@code ServiceOrder.frequency}, {@code numberOfRepeats}, and
 * {@code ServiceOrder.location} (the service-location concept, distinct from the encounter's
 * location) are not yet surfaced — tracked under the "ServiceOrder frequency / numberOfRepeats /
 * location surfacing" open question.
 */
public class ReferralOrderRecordSerializer extends AbstractServiceOrderRecordSerializer<ReferralOrder> {

	@Override
	public String getResourceType() {
		return "referral_order";
	}

	@Override
	public Class<ReferralOrder> getSupportedType() {
		return ReferralOrder.class;
	}

	@Override
	protected String getTextPrefix() {
		return "Referral order: ";
	}
}
