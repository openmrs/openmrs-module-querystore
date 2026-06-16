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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.querystore.api.impl.QueryStoreServiceImpl;
import org.openmrs.module.querystore.backend.BackendStore;
import org.openmrs.module.querystore.backend.BackendStoreSelector;
import org.openmrs.module.querystore.bootstrap.BootstrapLauncher;
import org.openmrs.module.querystore.sync.AfterCommitDispatcher;
import org.openmrs.module.querystore.model.QueryDocument;

public class QueryStoreActivatorTest {

	private QueryStoreActivator activator;

	private AdministrationService admin;

	@Before
	public void setUp() {
		activator = new QueryStoreActivator();
		admin = mock(AdministrationService.class);
	}

	@Test
	public void isAutostartEnabled_returnsTrueWhenGpIsTrue() {
		when(admin.getGlobalPropertyValue(eq(QueryStoreConstants.GP_BOOTSTRAP_AUTOSTART), eq(Boolean.FALSE)))
		        .thenReturn(Boolean.TRUE);
		assertTrue(activator.isAutostartEnabled(admin));
	}

	@Test
	public void isAutostartEnabled_returnsFalseWhenGpIsFalse() {
		when(admin.getGlobalPropertyValue(eq(QueryStoreConstants.GP_BOOTSTRAP_AUTOSTART), eq(Boolean.FALSE)))
		        .thenReturn(Boolean.FALSE);
		assertFalse(activator.isAutostartEnabled(admin));
	}

	@Test
	public void isAutostartEnabled_returnsFalseWhenGpIsAbsent() {
		// getGlobalPropertyValue returns the default (FALSE) when the property doesn't exist;
		// pin that contract so a future change to a non-Boolean default doesn't silently flip
		// the autostart semantics.
		when(admin.getGlobalPropertyValue(eq(QueryStoreConstants.GP_BOOTSTRAP_AUTOSTART), eq(Boolean.FALSE)))
		        .thenReturn(Boolean.FALSE);
		assertFalse(activator.isAutostartEnabled(admin));
	}

	@Test
	public void setDaemonToken_eagerlyPropagatesToDispatcher_whenLookupSucceeds() {
		// OpenMRS' setDaemonToken typically arrives before started(); the activator must eagerly
		// hand it to the after-commit dispatcher so an events-consumer projection firing between
		// setDaemonToken and started() runs with a UserContext. Without this propagation path,
		// documents in that window are silently dropped by the dispatcher's swallow guard.
		AfterCommitDispatcher dispatcher = mock(AfterCommitDispatcher.class);
		QueryStoreActivator capturingActivator = new QueryStoreActivator() {
			@Override
			AfterCommitDispatcher findSyncDispatcher() {
				return dispatcher;
			}
		};
		DaemonToken token = new DaemonToken("token-eager");
		capturingActivator.setDaemonToken(token);
		verify(dispatcher).setDaemonToken(token);
	}

	@Test
	public void setDaemonToken_swallowsLookupFailure_whenSpringNotReady() {
		// Spring's ServiceContext is not refreshed yet when setDaemonToken first fires on most
		// platform versions. The lookup must fail-soft so the module doesn't fail to start — and
		// started() retries propagation once the context is up. Pin both halves of that contract.
		QueryStoreActivator failingLookup = new QueryStoreActivator() {
			@Override
			AfterCommitDispatcher findSyncDispatcher() {
				throw new IllegalStateException("Spring not ready");
			}
		};
		// Must not throw — the activator survives the early-startup lookup miss.
		failingLookup.setDaemonToken(new DaemonToken("token-early"));
	}

	@Test
	public void wireSyncDaemonToken_propagatesAgain_afterEagerPathRan() {
		// Pin both halves of the dual-propagation contract: setDaemonToken eagerly hands the
		// token to the dispatcher, and wireSyncDaemonToken (called from started()) ALSO hands
		// it over. The second hop is the safety net for platform versions where the eager lookup
		// failed because Spring wasn't refreshed yet. Asserting two calls catches a regression
		// where someone replaces one with the other "to deduplicate."
		AfterCommitDispatcher dispatcher = mock(AfterCommitDispatcher.class);
		QueryStoreActivator activatorWithToken = new QueryStoreActivator() {
			@Override
			AfterCommitDispatcher findSyncDispatcher() {
				return dispatcher;
			}
		};
		DaemonToken token = new DaemonToken("token-dual");
		activatorWithToken.setDaemonToken(token);
		activatorWithToken.wireSyncDaemonToken();
		// Twice with the same token: once from setDaemonToken's eager path, once from started().
		verify(dispatcher, org.mockito.Mockito.times(2)).setDaemonToken(token);
	}

	@Test
	public void wireSyncDaemonToken_skipsLookup_whenTokenNull() {
		// If started() runs and no token has arrived yet, the activator must NOT look up the
		// dispatcher — that would silently install a null token, masking the configuration miss
		// behind a no-op. Pin: dispatcher lookup is not called.
		final boolean[] lookupAttempted = { false };
		QueryStoreActivator nullToken = new QueryStoreActivator() {
			@Override
			AfterCommitDispatcher findSyncDispatcher() {
				lookupAttempted[0] = true;
				return mock(AfterCommitDispatcher.class);
			}
		};
		nullToken.wireSyncDaemonToken();
		assertFalse("must skip dispatcher lookup when no token is wired", lookupAttempted[0]);
	}

	@Test
	public void setDaemonToken_eagerlyPropagatesToBootstrapLauncher_whenLookupSucceeds() {
		// The on-demand reindex endpoint (scope:"all") and bootstrap autostart both launch the
		// global bootstrap on a daemon thread, which needs the token's UserContext. The activator
		// must hand the token to the launcher the same way it does the sync dispatcher.
		BootstrapLauncher launcher = mock(BootstrapLauncher.class);
		QueryStoreActivator capturingActivator = new QueryStoreActivator() {
			@Override
			BootstrapLauncher findBootstrapLauncher() {
				return launcher;
			}
		};
		DaemonToken token = new DaemonToken("token-launcher-eager");
		capturingActivator.setDaemonToken(token);
		verify(launcher).setDaemonToken(token);
	}

	@Test
	public void wireBootstrapLauncherToken_propagatesAgain_afterEagerPathRan() {
		// Mirrors the dispatcher dual-propagation contract: setDaemonToken eagerly wires the
		// launcher, and wireBootstrapLauncherToken (from started()) wires it again as the safety
		// net for platform versions where the eager lookup fired before Spring was refreshed.
		BootstrapLauncher launcher = mock(BootstrapLauncher.class);
		QueryStoreActivator activatorWithToken = new QueryStoreActivator() {
			@Override
			BootstrapLauncher findBootstrapLauncher() {
				return launcher;
			}
		};
		DaemonToken token = new DaemonToken("token-launcher-dual");
		activatorWithToken.setDaemonToken(token);
		activatorWithToken.wireBootstrapLauncherToken();
		verify(launcher, org.mockito.Mockito.times(2)).setDaemonToken(token);
	}

	@Test
	public void wireBootstrapLauncherToken_skipsLookup_whenTokenNull() {
		// If started() runs before any token arrives, the activator must NOT look up the launcher
		// and install a null token — that would mask the configuration miss behind a no-op launch.
		final boolean[] lookupAttempted = { false };
		QueryStoreActivator nullToken = new QueryStoreActivator() {
			@Override
			BootstrapLauncher findBootstrapLauncher() {
				lookupAttempted[0] = true;
				return mock(BootstrapLauncher.class);
			}
		};
		nullToken.wireBootstrapLauncherToken();
		assertFalse("must skip launcher lookup when no token is wired", lookupAttempted[0]);
	}

	@Test
	public void triggerBootstrap_delegatesToTheLauncher() {
		// Autostart used to run Daemon.runInDaemonThread(bootstrap) inline; it now delegates to the
		// launcher so autostart and the on-demand reindex endpoint share one launch path. Pin the
		// delegation so a refactor that drops the launchAsync() call (silently stopping autostart
		// from bootstrapping) is caught.
		BootstrapLauncher launcher = mock(BootstrapLauncher.class);
		when(launcher.launchAsync()).thenReturn(Boolean.TRUE);
		QueryStoreActivator activatorWithLauncher = new QueryStoreActivator() {
			@Override
			BootstrapLauncher findBootstrapLauncher() {
				return launcher;
			}
		};
		activatorWithLauncher.triggerBootstrap();
		verify(launcher).launchAsync();
	}

	@Test
	public void wireBackend_injectsSelectorsChoiceIntoTheServiceImpl() {
		// Pins the issue #10 contract: the activator resolves the selector's chosen backend and
		// hands it directly to the impl, with no proxy-cast in between. Asserts by observation —
		// after wiring, a subsequent index() call must reach the chosen backend.
		BackendStoreSelector selector = mock(BackendStoreSelector.class);
		BackendStore chosen = mock(BackendStore.class);
		when(selector.getStore()).thenReturn(chosen);
		QueryStoreServiceImpl service = new QueryStoreServiceImpl();

		activator.wireBackend(selector, service);

		QueryDocument doc = new QueryDocument();
		doc.setResourceType("obs");
		doc.setResourceUuid("u-1");
		service.index(doc);
		verify(chosen).upsert(doc);
	}
}
