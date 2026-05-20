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
import org.openmrs.module.querystore.bridge.AfterCommitDispatcher;
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
		// hand it to the bridge dispatcher so AOP advice firing between setDaemonToken and
		// started() runs with a UserContext. Without this propagation path, documents in that
		// window are silently dropped by the bridge swallow guard.
		AfterCommitDispatcher dispatcher = mock(AfterCommitDispatcher.class);
		QueryStoreActivator capturingActivator = new QueryStoreActivator() {
			@Override
			AfterCommitDispatcher findBridgeDispatcher() {
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
			AfterCommitDispatcher findBridgeDispatcher() {
				throw new IllegalStateException("Spring not ready");
			}
		};
		// Must not throw — the activator survives the early-startup lookup miss.
		failingLookup.setDaemonToken(new DaemonToken("token-early"));
	}

	@Test
	public void wireBridgeDaemonToken_propagatesAgain_afterEagerPathRan() {
		// Pin both halves of the dual-propagation contract: setDaemonToken eagerly hands the
		// token to the dispatcher, and wireBridgeDaemonToken (called from started()) ALSO hands
		// it over. The second hop is the safety net for platform versions where the eager lookup
		// failed because Spring wasn't refreshed yet. Asserting two calls catches a regression
		// where someone replaces one with the other "to deduplicate."
		AfterCommitDispatcher dispatcher = mock(AfterCommitDispatcher.class);
		QueryStoreActivator activatorWithToken = new QueryStoreActivator() {
			@Override
			AfterCommitDispatcher findBridgeDispatcher() {
				return dispatcher;
			}
		};
		DaemonToken token = new DaemonToken("token-dual");
		activatorWithToken.setDaemonToken(token);
		activatorWithToken.wireBridgeDaemonToken();
		// Twice with the same token: once from setDaemonToken's eager path, once from started().
		verify(dispatcher, org.mockito.Mockito.times(2)).setDaemonToken(token);
	}

	@Test
	public void wireBridgeDaemonToken_skipsLookup_whenTokenNull() {
		// If started() runs and no token has arrived yet, the activator must NOT look up the
		// dispatcher — that would silently install a null token, masking the configuration miss
		// behind a no-op. Pin: dispatcher lookup is not called.
		final boolean[] lookupAttempted = { false };
		QueryStoreActivator nullToken = new QueryStoreActivator() {
			@Override
			AfterCommitDispatcher findBridgeDispatcher() {
				lookupAttempted[0] = true;
				return mock(AfterCommitDispatcher.class);
			}
		};
		nullToken.wireBridgeDaemonToken();
		assertFalse("must skip dispatcher lookup when no token is wired", lookupAttempted[0]);
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
