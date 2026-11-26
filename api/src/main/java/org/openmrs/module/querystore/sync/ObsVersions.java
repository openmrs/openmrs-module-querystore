/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.sync;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.openmrs.Obs;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;

/**
 * Resolves an obs to what the read store should hold for it, from committed state. Core never edits
 * an obs in place: saving a changed, persisted obs clones it into a new obs (new uuid,
 * {@code previous_version} pointing back) and voids the original. So a voided obs is replaced by its
 * live successors, found by following {@code previous_version} forward.
 *
 * <p>Runs on the sync executor's daemon thread after commit, against that thread's session, the way
 * the bootstrappers query core.
 */
public class ObsVersions {

	// A previous_version chain longer than this is an anomaly, not a run of edits in one commit.
	public static final int MAX_CHAIN = 20;

	private static final Log log = LogFactory.getLog(ObsVersions.class);

	private final DbSessionFactory sessionFactory;

	public ObsVersions(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/** What to write for one obs: documents to index and uuids to delete. */
	public static final class Resolution {

		private final List<QueryDocument> toIndex;

		private final List<String> toDelete;

		public Resolution(List<QueryDocument> toIndex, List<String> toDelete) {
			this.toIndex = Collections.unmodifiableList(toIndex);
			this.toDelete = Collections.unmodifiableList(toDelete);
		}

		public List<QueryDocument> getToIndex() {
			return toIndex;
		}

		public List<String> getToDelete() {
			return toDelete;
		}
	}

	/**
	 * Reads the obs with this uuid as committed. Live: index it. Voided: delete it and index each live
	 * obs that superseded it, with its group members. Gone (purged since): delete it.
	 */
	public Resolution resolve(String uuid, ClinicalRecordSerializer<Obs> serializer) {
		List<QueryDocument> toIndex = new ArrayList<>();
		List<String> toDelete = new ArrayList<>();
		Obs obs = session().createQuery("from Obs o where o.uuid = :uuid", Obs.class).setParameter("uuid", uuid)
		        .uniqueResult();
		if (obs == null) {
			toDelete.add(uuid);
		} else if (!Boolean.TRUE.equals(obs.getVoided())) {
			addIfSerialized(serializer.serialize(obs), toIndex);
		} else {
			toDelete.add(uuid);
			for (Obs successor : liveSuccessors(obs)) {
				for (Obs node : serializer.collectTree(successor)) {
					if (!Boolean.TRUE.equals(node.getVoided())) {
						addIfSerialized(serializer.serialize(node), toIndex);
					}
				}
			}
		}
		return new Resolution(toIndex, toDelete);
	}

	/**
	 * The live obs that replaced this one, following previous_version past any voided in between. Walks
	 * at most {@link #MAX_CHAIN} obs; past that it stops, logs, and returns what it found, so the
	 * caller still deletes the original and a live successor further on stays unindexed until it is
	 * next saved.
	 */
	List<Obs> liveSuccessors(Obs original) {
		List<Obs> live = new ArrayList<>();
		Set<Integer> seen = new HashSet<>();
		Deque<Obs> frontier = new ArrayDeque<>();
		frontier.add(original);
		int walked = 0;
		while (!frontier.isEmpty()) {
			Obs node = frontier.poll();
			if (!seen.add(node.getObsId())) {
				continue;
			}
			if (walked++ == MAX_CHAIN) {
				log.warn("Obs previous_version chain is longer than " + MAX_CHAIN
				        + "; live successors past it are not indexed (obs uuid at DEBUG)");
				log.debug("Obs previous_version chain truncated from obs/" + original.getUuid());
				break;
			}
			for (Obs next : session().createQuery("from Obs o where o.previousVersion = :prev", Obs.class)
			        .setParameter("prev", node).list()) {
				if (Boolean.TRUE.equals(next.getVoided())) {
					frontier.add(next);
				} else {
					live.add(next);
				}
			}
		}
		return live;
	}

	private Session session() {
		return sessionFactory.getHibernateSessionFactory().getCurrentSession();
	}

	private static void addIfSerialized(QueryDocument doc, List<QueryDocument> out) {
		if (doc != null) {
			out.add(doc);
		}
	}
}
