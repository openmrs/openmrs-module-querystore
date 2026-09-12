/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 */
package org.openmrs.module.querystore.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ContextSliceModelTest {

	@Test
	public void constructorCopiesTheRecordList() {
		List<ContextSliceRecord> records = new ArrayList<ContextSliceRecord>();
		records.add(new ContextSliceRecord(new QueryDocument(), "mandatory"));
		ContextSlice slice = new ContextSlice(records, 1, false);

		records.clear();

		assertEquals("later caller mutation must not change a published slice",
		        1, slice.getRecords().size());
	}

	@Test
	public void convenienceConstructorDoesNotInventProjectionCompleteness() {
		ContextSlice slice = new ContextSlice(new ArrayList<ContextSliceRecord>(), 0, false);

		assertFalse("a caller that supplied no projection evidence must fail closed",
		        slice.isProjectionComplete());
	}
}
