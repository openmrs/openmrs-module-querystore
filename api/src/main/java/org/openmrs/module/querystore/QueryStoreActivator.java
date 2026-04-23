package org.openmrs.module.querystore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.BaseModuleActivator;

public class QueryStoreActivator extends BaseModuleActivator {

	private static final Log log = LogFactory.getLog(QueryStoreActivator.class);

	@Override
	public void started() {
		log.info("Query Store module started");
	}

	@Override
	public void stopped() {
		log.info("Query Store module stopped");
	}
}
