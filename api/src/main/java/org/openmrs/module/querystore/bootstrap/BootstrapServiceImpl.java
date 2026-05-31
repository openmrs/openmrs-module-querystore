/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.backend.BackendStoreSelector;
import org.openmrs.module.querystore.embedding.EmbeddingProvider;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;
import org.openmrs.module.querystore.spi.ResourceTypeNames;
import org.openmrs.module.querystore.spi.ResourceTypeProvider;

/**
 * Default {@link BootstrapService}: registers a {@link TypeBootstrapper} per resource type at
 * Spring wiring time and dispatches {@link #bootstrap(String)} to the matching one. Bootstrappers
 * run sequentially per type — embedding is CPU-bound and concurrency belongs in the
 * {@link EmbeddingProvider}, not the dispatch loop.
 *
 * <p>Module-provided bootstrappers (ADR Decision 13) are picked up via
 * {@link ResourceTypeProvider} beans discovered from the global OpenMRS Spring context at
 * {@link #bootstrap()} / {@link #bootstrap(String)} time, so a providing module installed after
 * querystore is included without restart. Providers run after all core types because core obs
 * typically dwarfs every other type; deferring providers keeps them from being delayed behind a
 * long-running core scan only when the provider corpus is small, which is the common case.
 */
public class BootstrapServiceImpl extends BaseOpenmrsService implements BootstrapService {

	private static final Log log = LogFactory.getLog(BootstrapServiceImpl.class);

	private final Map<String, TypeBootstrapper<?>> bootstrappers = new LinkedHashMap<>();

	// Per-resource-type lock so concurrent bootstrap(type) calls serialize. Without it, two
	// callers (admin + scheduled job, or double-click) share the same progress row and trample
	// each other's cursor and counts. Document storage stays correct via Slice A's version
	// protection, but the progress bookkeeping silently corrupts.
	private final Map<String, Object> typeLocks = new ConcurrentHashMap<>();

	// Per-patient lock so concurrent ensureIndexed(uuid) calls for the same patient serialize. The
	// cross-JVM race is absorbed by the Decision 3 version invariant (conditional upsert on
	// last_modified); this lock prevents redundant projection work within one JVM. Map grows by one
	// entry per ever-touched patient and is bounded in practice by deployment patient count — see
	// the ADR "Lazy per-patient projection" sub-question for the trade-off discussion.
	private final Map<String, Object> patientLocks = new ConcurrentHashMap<>();

	private BootstrapProgressDao progressDao;

	private QueryStoreService queryStoreService;

	private EmbeddingProvider embeddingProvider;

	private BackendStoreSelector backendSelector;

	// Test override: when non-null, replaces the Spring-context discovery path so tests can pin
	// the provider set without a live OpenMRS context. Normal runtime leaves this null and
	// discovers via Context.getRegisteredComponents on each bootstrap call.
	private List<ResourceTypeProvider> providersOverride;

	// Test seam parallel to providersOverride: when non-null, replaces the
	// BackendStoreSelector.currentBackendName() lookup so tests can exercise the backend-mismatch
	// reset path without standing up a real selector + GP wiring. Normal runtime leaves this null
	// and consults the injected selector.
	private Supplier<String> backendNameSupplierOverride;

	public void setProgressDao(BootstrapProgressDao progressDao) {
		this.progressDao = progressDao;
	}

	public void setQueryStoreService(QueryStoreService queryStoreService) {
		this.queryStoreService = queryStoreService;
	}

	public void setEmbeddingProvider(EmbeddingProvider embeddingProvider) {
		this.embeddingProvider = embeddingProvider;
	}

	public void setBackendSelector(BackendStoreSelector backendSelector) {
		this.backendSelector = backendSelector;
	}

	/** Test seam — see {@link #backendNameSupplierOverride}. Package-private; production code wires
	 *  the {@link BackendStoreSelector} via {@link #setBackendSelector}. */
	void setBackendNameSupplierOverride(Supplier<String> backendNameSupplier) {
		this.backendNameSupplierOverride = backendNameSupplier;
	}

	public void setBootstrappers(List<TypeBootstrapper<?>> list) {
		bootstrappers.clear();
		if (list != null) {
			for (TypeBootstrapper<?> b : list) {
				bootstrappers.put(b.getResourceType(), b);
			}
		}
	}

	/** Test hook to inject providers without a live OpenMRS Spring context. Validation happens at
	 *  discovery time (per-provider, with skip-on-failure isolation) so this seam can also be used
	 *  to wire in deliberately-malformed providers when exercising the isolation behavior.
	 *  Package-private — tests live alongside the implementation; production code uses Spring discovery. */
	void setProvidersOverride(List<ResourceTypeProvider> providers) {
		this.providersOverride = providers;
	}

	@Override
	public void bootstrap() {
		Map<String, ResourceTypeProvider> providers = discoverProviders();
		for (String resourceType : allResourceTypes(providers)) {
			try {
				runOne(resourceType, providers);
			}
			catch (RuntimeException e) {
				log.warn("Bootstrap of " + resourceType + " failed; continuing with remaining types", e);
			}
		}
	}

	@Override
	public void bootstrap(String resourceType) {
		runOne(resourceType, discoverProviders());
	}

	@Override
	public void ensureIndexed(String patientUuid) {
		if (patientUuid == null || patientUuid.isEmpty()) {
			return;
		}
		Object lock = patientLocks.computeIfAbsent(patientUuid, k -> new Object());
		synchronized (lock) {
			// Re-probe inside the lock: callers (QueryStoreServiceImpl.searchByPatient /
			// getPatientChart) test existsByPatient BEFORE entering this method, so two concurrent
			// cold-touches on the same never-indexed patient both see false and both invoke
			// ensureIndexed. Without this check, the lock would serialize them but the second
			// thread would re-run the entire type-by-type projection (re-serialize, re-embed,
			// re-upsert every record) — the version-guard upserts converge to the right index
			// state but the embedding CPU cost is paid twice. A second probe inside the lock lets
			// the late waiter short-circuit. existsByPatient is sub-linear (Decision 3 SPI
			// invariant 2), so the cost of this guard is negligible compared to even one
			// re-projection avoided.
			if (backendSelector != null && backendSelector.getStore().existsByPatient(patientUuid)) {
				return;
			}
			projectAllTypesForPatient(patientUuid);
		}
	}

	@Override
	public void reindexPatient(String patientUuid) {
		if (patientUuid == null || patientUuid.isEmpty()) {
			return;
		}
		Object lock = patientLocks.computeIfAbsent(patientUuid, k -> new Object());
		synchronized (lock) {
			// Force a full re-projection (repairs a partially-indexed patient, which ensureIndexed's
			// existsByPatient gate refuses to touch). Delete FIRST: this clears stale rows AND means
			// the projection isn't gated by existing docs. Order is load-bearing — projecting before
			// deleting would re-add the patient's records and then wipe them, leaving the patient
			// empty. Same per-patient lock as ensureIndexed, so a concurrent cold-touch projection
			// can't interleave with the delete. queryStoreService is called unconditionally (as
			// elsewhere in this class): a null here is a wiring fault that must fail loudly, not
			// silently skip the delete and leave stale docs alongside the re-projected ones.
			queryStoreService.bulkDeleteByPatient(patientUuid);
			projectAllTypesForPatient(patientUuid);
		}
	}

	/** The per-type per-patient projection loop shared by {@link #ensureIndexed} and
	 *  {@link #reindexPatient}. Per-type and per-record failures are isolated and logged so one
	 *  type's failure doesn't abort the rest. Caller holds the per-patient lock. */
	private void projectAllTypesForPatient(String patientUuid) {
		Map<String, ResourceTypeProvider> providers = discoverProviders();
		for (String resourceType : allResourceTypes(providers)) {
			try {
				runOneForPatient(resourceType, patientUuid, providers);
			}
			catch (UnsupportedOperationException noPerPatientImpl) {
				// Bootstrapper declared no per-patient story (TypeBootstrapper.fetchPageForPatient
				// default throw). Legitimate no-op for reference-data SPI types that don't carry a
				// patient association — log at debug so a deployment with several such providers
				// doesn't spam warnings on every cold searchByPatient.
				if (log.isDebugEnabled()) {
					log.debug("Skipping " + resourceType + " for patient " + patientUuid
					        + " — no per-patient projection implemented");
				}
			}
			catch (RuntimeException e) {
				log.warn("Per-patient projection of " + resourceType + " for patient " + patientUuid
				        + " failed; continuing with remaining types", e);
			}
		}
	}

	@Override
	public List<BootstrapProgress> getStatus() {
		return progressDao.findAll();
	}

	@Override
	public BootstrapProgress getStatus(String resourceType) {
		return progressDao.find(resourceType);
	}

	private void runOne(String resourceType, Map<String, ResourceTypeProvider> providers) {
		TypeBootstrapper<?> bootstrapper = bootstrappers.get(resourceType);
		if (bootstrapper == null) {
			ResourceTypeProvider provider = providers.get(resourceType);
			if (provider == null) {
				throw new IllegalArgumentException("No bootstrapper registered for resource type: " + resourceType);
			}
			bootstrapper = provider.getBootstrapper();
			if (bootstrapper == null) {
				// Provider exists but declared no historical backfill — surface the no-op so an
				// admin who explicitly invoked bootstrap("foo") sees there's nothing to do for
				// that name, distinct from "no such type."
				throw new IllegalArgumentException(
				        "Provider for resource type '" + resourceType
				                + "' declared no bootstrapper (no historical backfill)");
			}
		}
		Object lock = typeLocks.computeIfAbsent(resourceType, k -> new Object());
		synchronized (lock) {
			BootstrapProgress progress = progressDao.find(resourceType);
			String currentBackend = currentBackendName();
			if (progress == null) {
				progress = new BootstrapProgress(resourceType);
				progress.setBackend(currentBackend);
			} else if (!Objects.equals(progress.getBackend(), currentBackend)) {
				// A querystore.backend GP flip leaves the new backend's storage empty (or holding
				// whatever a prior session wrote there), while the persisted cursor points past
				// records that were written into the old backend. Inheriting that cursor would
				// declare the new backend "completed" without it ever having seen any data — the
				// silent-data-loss class of bug. Reset the row so the bootstrap loop walks the
				// corpus afresh against the current backend. Null on the stored side (rows written
				// before the backend column existed) is treated as a mismatch, so one re-bootstrap
				// follows the schema migration.
				//
				// Caveat operators should know: this reset reconciles the historical corpus only.
				// QueryStoreServiceImpl.backend is wired once in QueryStoreActivator.started(); a
				// runtime GP flip without a module restart means AOP writes between flip and
				// restart still go to the previous backend and are lost to the new one. The reset
				// path makes the static corpus correct on the next start but cannot recover those
				// in-flight writes. If runtime backend swaps become a supported workflow, a
				// GlobalPropertyListener that re-wires QueryStoreServiceImpl is the next fix.
				log.info("Resetting bootstrap progress for " + resourceType
				        + " (backend changed: " + progress.getBackend() + " -> " + currentBackend + ")");
				resetProgressForBackend(progress, currentBackend);
				progressDao.save(progress);
			}
			bootstrapper.run(progress, queryStoreService, embeddingProvider, progressDao);
		}
	}

	/**
	 * Fail-fast: the production path must always wire {@link #backendSelector} via Spring; tests
	 * either inject a selector or install {@link #backendNameSupplierOverride}. A silently-null
	 * answer would let one untouched dispatch stamp the row with {@code null}, then a subsequent
	 * properly-wired dispatch would see "null != lucene", reset, re-walk — and the row would now
	 * be stamped with the real name. The third dispatch matches and is fine. The damage is a
	 * spurious extra full re-bootstrap on the run after the misconfiguration is fixed — bounded,
	 * but only if we don't fail-fast here. With this guard, a misconfigured deployment surfaces
	 * the wiring gap in the activator's daemon thread instead.
	 */
	private String currentBackendName() {
		if (backendNameSupplierOverride != null) {
			return backendNameSupplierOverride.get();
		}
		if (backendSelector == null) {
			throw new IllegalStateException(
			        "BackendStoreSelector not wired into BootstrapServiceImpl; "
			                + "production deployments must wire it via moduleApplicationContext.xml, "
			                + "tests via setBackendSelector or setBackendNameSupplierOverride");
		}
		return backendSelector.currentBackendName();
	}

	private static void resetProgressForBackend(BootstrapProgress p, String backend) {
		p.setStatus(BootstrapStatus.NOT_STARTED);
		p.setCursorDateChanged(null);
		p.setCursorUuid(null);
		p.setDocumentsIndexed(0);
		p.setStartedAt(null);
		p.setCompletedAt(null);
		p.setFailureMessage(null);
		p.setBackend(backend);
	}

	private void runOneForPatient(String resourceType, String patientUuid,
	                              Map<String, ResourceTypeProvider> providers) {
		// Callers iterate allResourceTypes, which already filters providers with a null bootstrapper
		// — so for every resourceType reaching here, the core map or the provider's bootstrapper
		// resolves non-null. No null guards needed.
		TypeBootstrapper<?> bootstrapper = bootstrappers.get(resourceType);
		if (bootstrapper == null) {
			bootstrapper = providers.get(resourceType).getBootstrapper();
		}
		bootstrapper.runForPatient(patientUuid, queryStoreService, embeddingProvider);
	}

	/** Core types first (insertion order), then providers with a non-null bootstrapper. The map is
	 *  a LinkedHashMap so provider insertion order is preserved. */
	private List<String> allResourceTypes(Map<String, ResourceTypeProvider> providers) {
		List<String> types = new ArrayList<>(bootstrappers.keySet());
		for (Map.Entry<String, ResourceTypeProvider> e : providers.entrySet()) {
			if (e.getValue().getBootstrapper() == null) {
				continue;
			}
			if (!types.contains(e.getKey())) {
				types.add(e.getKey());
			}
		}
		return types;
	}

	/** Returns providers keyed by validated name, deduplicated, with bad ones logged and skipped.
	 *  Source is the test override when set, otherwise the OpenMRS Spring context. */
	private Map<String, ResourceTypeProvider> discoverProviders() {
		List<ResourceTypeProvider> discovered = providersOverride;
		if (discovered == null) {
			try {
				discovered = Context.getRegisteredComponents(ResourceTypeProvider.class);
			}
			catch (NullPointerException noContext) {
				// Direct construction without a wired Spring context (test environments, and the
				// narrow window during module activation before context refresh completes) lands
				// here. A real provider-bean misconfiguration surfaces as BeansException instead
				// and propagates.
				return Collections.emptyMap();
			}
		}
		if (discovered == null || discovered.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, ResourceTypeProvider> accepted = new LinkedHashMap<>(discovered.size());
		Set<String> seen = new HashSet<>(discovered.size());
		for (ResourceTypeProvider p : discovered) {
			String name = null;
			try {
				name = p.getResourceType();
				ResourceTypeNames.validateProvided(name);
				if (!seen.add(name)) {
					throw new IllegalStateException("Duplicate provider for resource type '" + name + "'");
				}
				ClinicalRecordSerializer<?> ser = p.getSerializer();
				if (ser != null && !name.equals(ser.getResourceType())) {
					throw new IllegalStateException("Provider declares resource type '" + name
					        + "' but its serializer reports '" + ser.getResourceType()
					        + "' — names must match or routing drifts between bootstrap and bridge paths");
				}
				accepted.put(name, p);
			}
			catch (RuntimeException e) {
				log.warn("Skipping malformed ResourceTypeProvider bean " + p.getClass().getName()
				        + (name != null ? " (resource type '" + name + "')" : "")
				        + " — " + e.getMessage());
			}
		}
		return accepted;
	}

	// Package-private read-only view for tests asserting registration without exercising run().
	Map<String, TypeBootstrapper<?>> getBootstrappers() {
		return Collections.unmodifiableMap(bootstrappers);
	}
}
