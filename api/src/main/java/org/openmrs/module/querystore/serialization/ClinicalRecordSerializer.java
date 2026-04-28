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

import org.openmrs.module.querystore.model.QueryDocument;

/**
 * Maps a clinical record of type {@code T} to a {@link QueryDocument} populated with the labeled
 * plain text (ADR decision 5) and the structured fields (ADR decisions 6 and 13). Record
 * timestamps are excluded from the embedded text (ADR decision 7); concept names are resolved in
 * the deployment's configured locale (ADR decision 8). The embedding is computed by the
 * querystore pipeline (ADR decision 13) and should be left unset by the serializer.
 */
public interface ClinicalRecordSerializer<T> {

	String getResourceType();

	Class<T> getSupportedType();

	QueryDocument serialize(T record);
}
