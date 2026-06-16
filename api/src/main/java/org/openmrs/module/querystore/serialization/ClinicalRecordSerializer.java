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

import java.util.Collections;
import java.util.List;

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

	/**
	 * Returns the document for the given record, or {@code null} if the record produces no
	 * document (for example, an obs group parent whose own value is empty — its members are
	 * indexed individually per the ADR decision 6 group obs convention).
	 */
	QueryDocument serialize(T record);

	/**
	 * The set of records to (re)project when {@code root} is saved — the type's projection scope,
	 * used by the sync pipeline ({@link org.openmrs.module.querystore.sync.RecordProjector}) to fan
	 * out from the saved root. Defaults to just {@code root}; {@code Obs} overrides to
	 * include its group members (ADR decision 6), so each member is projected (or deleted, if voided)
	 * in its own right. Module-contributed serializers (ADR Decision 13) may override for their own
	 * nested types.
	 */
	default List<T> collectTree(T root) {
		return Collections.singletonList(root);
	}

	/**
	 * When {@code root} is purged, the {@code patient_uuid} whose every cross-type document must also
	 * be swept from the read store, or {@code null} for no cross-type sweep. Default {@code null}:
	 * purging one record removes only its own document. {@code Patient} overrides to return its uuid,
	 * so purging a patient erases their whole chart (ADR decisions 1 and 10 — purge is a deletion,
	 * unlike void). Consulted by {@link org.openmrs.module.querystore.sync.RecordProjector} only on
	 * purge.
	 */
	default String bulkDeletePatientUuidFor(T root) {
		return null;
	}
}
