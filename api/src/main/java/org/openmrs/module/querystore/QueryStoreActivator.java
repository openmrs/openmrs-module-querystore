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
import org.openmrs.module.querystore.bootstrap.BootstrapLauncher;
import org.openmrs.module.querystore.sync.AfterCommitDispatcher;
import org.openmrs.module.querystore.embedding.EmbeddingProvider;

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
			findSyncDispatcher().setDaemonToken(token);
		}
		catch (RuntimeException ex) {
			log.debug("Deferring sync daemon-token wiring until started(); Spring not ready yet", ex);
		}
		// Same eager-propagate-then-retry contract for the bootstrap launcher (autostart + the
		// on-demand reindex endpoint). Separate try so a dispatcher-lookup failure does not skip
		// the launcher, and vice versa; started() redoes both once Spring is refreshed.
		try {
			findBootstrapLauncher().setDaemonToken(token);
		}
		catch (RuntimeException ex) {
			log.debug("Deferring bootstrap-launcher daemon-token wiring until started(); Spring not ready yet", ex);
		}
	}

	@Override
	public void started() {
		log.info("Query Store module started");
		wireBackend(
		    Context.getRegisteredComponent("querystore.backend.selector", BackendStoreSelector.class),
		    Context.getRegisteredComponent("queryStoreService", QueryStoreServiceImpl.class));
		// The after-commit dispatcher's pool threads have no UserContext; embedder reads of querystore
		// global properties (provider bean, model/vocab paths) need one, so hand it the daemon token
		// before the events consumer can dispatch an index. Activator owns the token (via
		// DaemonTokenAware); the dispatcher is constructed by Spring without it, so propagation lives here.
		wireSyncDaemonToken();
		wireBootstrapLauncherToken();
		warmupQueryEmbedder();
		if (isAutostartEnabled(Context.getAdministrationService())) {
			triggerBootstrap();
		}
	}

	/**
	 * Forces ONNX query-encoder construction (model bytes loaded from disk, session built, first
	 * inference compiled) off the first user search. The first {@code embedQuery} pays a one-time
	 * 3-4 s cost on the all-MiniLM-L6-v2 model (measured on the local standalone); without this
	 * warmup the first search a user issues after a restart blocks for that full duration.
	 * Runs in a daemon thread so module startup is not held by the model load; if the embedder
	 * is unavailable (no provider bean wired, no daemon token), the warmup silently no-ops and
	 * the first real search pays the cold cost — same behavior as before this hook existed.
	 */
	private void warmupQueryEmbedder() {
		if (daemonToken == null) {
			return;
		}
		Daemon.runInDaemonThread(() -> {
			try {
				EmbeddingProvider provider = Context.getRegisteredComponent(
				    "querystore.embedding.dispatcher", EmbeddingProvider.class);
				long t0 = System.currentTimeMillis();
				provider.embedQuery("warmup");
				log.info("Query embedder warmup completed in " + (System.currentTimeMillis() - t0) + " ms");
			}
			catch (RuntimeException e) {
				log.warn("Query embedder warmup failed; first real query will pay cold cost", e);
			}
		}, daemonToken);
	}

	void wireSyncDaemonToken() {
		if (daemonToken == null) {
			log.warn("Daemon token unavailable; the events consumer's after-commit projection will run"
			        + " without a UserContext until the token is wired. Documents created in this window"
			        + " are silently dropped by the dispatcher's swallow guard — re-run the"
			        + " bootstrap (or restart the module) once the token arrives to reconcile.");
			return;
		}
		findSyncDispatcher().setDaemonToken(daemonToken);
	}

	/**
	 * Hands the daemon token to the {@link BootstrapLauncher} so bootstrap autostart and the
	 * on-demand reindex endpoint can launch the global scan on a daemon thread. Skips the lookup
	 * when no token has arrived — installing a null token would mask the configuration miss behind
	 * a launcher that silently refuses to run. Mirrors {@link #wireSyncDaemonToken()}.
	 */
	void wireBootstrapLauncherToken() {
		if (daemonToken == null) {
			log.warn("Daemon token unavailable; bootstrap autostart and the on-demand reindex"
			        + " endpoint cannot run until the token is wired.");
			return;
		}
		findBootstrapLauncher().setDaemonToken(daemonToken);
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
	AfterCommitDispatcher findSyncDispatcher() {
		return Context.getRegisteredComponent(
		    "querystore.sync.dispatcher", AfterCommitDispatcher.class);
	}

	/** Visible-for-testing seam over {@link Context#getRegisteredComponent}, mirroring
	 *  {@link #findSyncDispatcher()} — see its note on the package-private contract. */
	BootstrapLauncher findBootstrapLauncher() {
		return Context.getRegisteredComponent(
		    "querystore.bootstrap.launcher", BootstrapLauncher.class);
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
		// A daemon-thread bootstrap may still be running here — kicked off either by autostart on
		// `started()` or on demand via the reindex endpoint (scope:"all") — and we don't interrupt
		// it. The version-by-lastModified invariant (ADR Decision 3) protects the indexed documents
		// from cross-restart races, but a fresh start that re-runs autostart could overlap the
		// previous run's progress-table writes. Bounded risk on the progress table only; proper
		// shutdown coordination is a follow-up if real deployments hit it.
		log.info("Query Store module stopped");
	}

	/** True when the autostart property is set to {@code "true"} (case-insensitive); the GP is
	 *  registered declaratively in {@code omod/src/main/resources/config.xml}. */
	boolean isAutostartEnabled(AdministrationService admin) {
		return Boolean.TRUE.equals(admin.getGlobalPropertyValue(
		        QueryStoreConstants.GP_BOOTSTRAP_AUTOSTART, Boolean.FALSE));
	}

	/**
	 * Kicks off the global bootstrap on an OpenMRS daemon thread (so module startup isn't blocked by
	 * what is often a multi-hour scan) by delegating to the {@link BootstrapLauncher} — the same
	 * path the on-demand reindex endpoint uses. The launcher returns {@code false} when no daemon
	 * token is wired; autostart logs and skips rather than failing startup.
	 */
	void triggerBootstrap() {
		if (!findBootstrapLauncher().launchAsync()) {
			log.warn("Bootstrap autostart skipped: daemon token unavailable "
			        + "(BootstrapService.bootstrap() can still be called programmatically)");
		}
	}
}
