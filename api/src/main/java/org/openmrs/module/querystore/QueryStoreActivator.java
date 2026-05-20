/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
import org.openmrs.module.querystore.api.impl.QueryStoreServiceImpl;
import org.openmrs.module.querystore.backend.BackendStore;
import org.openmrs.module.querystore.backend.BackendStoreSelector;
import org.openmrs.module.querystore.bootstrap.BootstrapService;
import org.openmrs.module.querystore.bridge.AfterCommitDispatcher;

public class QueryStoreActivator extends BaseModuleActivator implements DaemonTokenAware {

	private static final Log log = LogFactory.getLog(QueryStoreActivator.class);

	private DaemonToken daemonToken;

	@Override
	public void setDaemonToken(DaemonToken token) {
		this.daemonToken = token;
		// OpenMRS' module lifecycle calls setDaemonToken close to started() but the exact ordering
		// has shifted between platform versions. Propagate eagerly so a late-arriving token still
		// lands on the dispatcher; the lookup is guarded for the case where Spring isn't up yet
		// (in which case started() will redo the propagation once the context is refreshed).
		try {
			findBridgeDispatcher().setDaemonToken(token);
		}
		catch (RuntimeException ex) {
			log.debug("Deferring bridge daemon-token wiring until started(); Spring not ready yet", ex);
		}
	}

	@Override
	public void started() {
		log.info("Query Store module started");
		wireBackend(
		    Context.getRegisteredComponent("querystore.backend.selector", BackendStoreSelector.class),
		    Context.getRegisteredComponent("queryStoreService", QueryStoreServiceImpl.class));
		// The bridge dispatcher's pool threads have no UserContext; embedder reads of querystore
		// global properties (provider bean, model/vocab paths) need one, so hand it the daemon
		// token before any AOP advice can fire. Activator owns the token (via DaemonTokenAware);
		// the dispatcher is constructed by Spring without it, so propagation lives here.
		wireBridgeDaemonToken();
		if (isAutostartEnabled(Context.getAdministrationService())) {
			triggerBootstrap();
		}
	}

	void wireBridgeDaemonToken() {
		if (daemonToken == null) {
			log.warn("Daemon token unavailable; bridge AOP projection will run without a"
			        + " UserContext until the token is wired. Documents created in this window"
			        + " are silently dropped by the dispatcher's swallow guard — re-run the"
			        + " bootstrap (or restart the module) once the token arrives to reconcile.");
			return;
		}
		findBridgeDispatcher().setDaemonToken(daemonToken);
	}

	/**
	 * Visible-for-testing seam over the static {@link Context#getRegisteredComponent} call so the
	 * activator's two daemon-token propagation paths (eager from {@link #setDaemonToken} and
	 * deferred from {@link #started()}) can be unit-tested without standing up a Spring context.
	 *
	 * <p>Package-private deliberately: the override mechanism relies on a subclass in the same
	 * package. A test that needs to substitute this seam from a different package must either
	 * move into {@code org.openmrs.module.querystore} or pull the substitution into a public
	 * setter — do not widen visibility without that decision.
	 */
	AfterCommitDispatcher findBridgeDispatcher() {
		return Context.getRegisteredComponent(
		    "querystore.bridge.dispatcher", AfterCommitDispatcher.class);
	}

	/**
	 * Reads {@code querystore.backend} via {@link BackendStoreSelector} and injects the chosen
	 * {@link BackendStore} into the service. Done here rather than at Spring wiring time because
	 * reading a GP during bean construction self-deadlocks against {@code ServiceContext}'s
	 * in-progress refresh (issue #10). Any failure (missing selector bean, unknown GP value with no
	 * default candidate wired) propagates out and fails module startup — preferable to silently
	 * leaving {@code backend == null}, which the service's null-checks would mask as
	 * empty-result-forever. The service is looked up via {@code getRegisteredComponent} (raw Spring
	 * bean) rather than {@code Context.getService} (AOP proxy implementing only the interface) so
	 * the {@code setBackend} internal seam stays reachable without a {@code ClassCastException}.
	 */
	void wireBackend(BackendStoreSelector selector, QueryStoreServiceImpl service) {
		service.setBackend(selector.getStore());
	}

	@Override
	public void stopped() {
		// A daemon-thread bootstrap started on `started()` may still be running here; we don't
		// interrupt it. The version-by-lastModified invariant (ADR Decision 3) protects the
		// indexed documents from cross-restart races, but a fresh start that re-runs autostart
		// could overlap the previous run's progress-table writes. Bounded risk on the progress
		// table only; proper shutdown coordination is a follow-up if real deployments hit it.
		log.info("Query Store module stopped");
	}

	/** True when the autostart property is set to {@code "true"} (case-insensitive); the GP is
	 *  registered declaratively in {@code omod/src/main/resources/config.xml}. */
	boolean isAutostartEnabled(AdministrationService admin) {
		return Boolean.TRUE.equals(admin.getGlobalPropertyValue(
		        QueryStoreConstants.GP_BOOTSTRAP_AUTOSTART, Boolean.FALSE));
	}

	/**
	 * Kicks off {@link BootstrapService#bootstrap()} in an OpenMRS daemon thread so module startup
	 * isn't blocked by what is often a multi-hour scan on a real corpus.
	 * {@code BootstrapServiceImpl.bootstrap()} catches per-type failures internally and never
	 * rethrows, so the runnable doesn't wrap it in a try/catch — uncaught throws would surface via
	 * the framework's thread-exception handler.
	 */
	private void triggerBootstrap() {
		if (daemonToken == null) {
			log.warn("Daemon token unavailable; skipping bootstrap autostart "
			        + "(BootstrapService.bootstrap() can still be called programmatically)");
			return;
		}
		BootstrapService bootstrap = Context.getService(BootstrapService.class);
		Daemon.runInDaemonThread(() -> {
			log.info("Auto-bootstrap starting");
			bootstrap.bootstrap();
			log.info("Auto-bootstrap completed");
		}, daemonToken);
	}
}
