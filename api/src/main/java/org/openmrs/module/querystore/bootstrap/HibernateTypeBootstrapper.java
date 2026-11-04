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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.querystore.SafeLog;
import org.openmrs.module.querystore.SkipLogFormat;

/**
 * {@link TypeBootstrapper} implementation that fetches pages via HQL against core's session. The
 * 12 core resource types share the same shape — {@link org.openmrs.BaseOpenmrsData} entity, paginate
 * by {@code dateChanged ?? dateCreated} ascending with {@code uuid} as tie-breaker, skip voided —
 * so the HQL is parameterized by entity name and lives here once instead of being copy-pasted into
 * every leaf. Subtypes provide only the serializer; the entity name is read from
 * {@code getSerializer().getSupportedType().getSimpleName()}.
 */
public abstract class HibernateTypeBootstrapper<T> extends TypeBootstrapper<T> {

	private static final Log log = LogFactory.getLog(HibernateTypeBootstrapper.class);

	private final DbSessionFactory sessionFactory;

	protected HibernateTypeBootstrapper(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * HQL expression for the cursor's effective date, referenced as {@code e.<field>}. The default
	 * {@code COALESCE(e.dateChanged, e.dateCreated)} works for entities whose Hibernate mapping
	 * exposes both audit columns. {@code Obs.hbm.xml} and {@code Order.hbm.xml} (in OpenMRS 2.8+)
	 * do not map {@code dateChanged} — those subclasses must override to return {@code "e.dateCreated"}
	 * (or another monotonic timestamp) or HQL will throw {@code QueryException} at first fetch.
	 */
	protected String cursorDateExpr() {
		return "COALESCE(e.dateChanged, e.dateCreated)";
	}

	/**
	 * HQL expression resolving the entity's patient UUID, referenced as {@code e.<path>.uuid}. Used
	 * by the per-patient fetch path on lazy projection (ADR Open Question: Initial backfill /
	 * bootstrap). Default {@code e.patient.uuid} fits every type whose Hibernate mapping exposes a
	 * {@code patient} association ({@link org.openmrs.Encounter}, {@link org.openmrs.Visit}, all
	 * {@link org.openmrs.Order} subtypes, {@link org.openmrs.Condition}, {@link org.openmrs.Diagnosis},
	 * {@link org.openmrs.Allergy}, {@link org.openmrs.PatientProgram},
	 * {@link org.openmrs.MedicationDispense}). {@link org.openmrs.Obs} has no {@code patient}
	 * association — only {@code person} — and overrides to {@code e.person.uuid}; the Person/Patient
	 * UUID coincidence (Patient extends Person) is what makes the filter resolve correctly.
	 * {@link org.openmrs.Patient} is itself the entity and overrides to {@code e.uuid}.
	 */
	protected String patientAssociationExpr() {
		return "e.patient.uuid";
	}

	/**
	 * Nav-expressions (e.g. {@code "e.encounter.uuid"}) for MANDATORY eager to-one associations
	 * beyond the patient/person scope that a SQL-dump load can leave dangling. Each is applied as an
	 * {@code AND <expr> IS NOT NULL} guard (see {@link #firstPageHql}), forcing the inner join that
	 * drops the orphaned row at fetch instead of letting {@code q.list()} eager-materialize a missing
	 * association and throw {@code FetchNotFoundException} — which fails the whole type, since the
	 * per-record skip in {@code serializeAndEmbed} can't catch a failure that happens at page fetch
	 * (the poison-page fallback in {@link TypeBootstrapper#fetchPageSkippingPoison} is what recovers it).
	 *
	 * <p>Default none: the patient/person guard already covers the common dump orphan. Override for a
	 * type with an additional non-null eager association — e.g. {@link org.openmrs.Diagnosis}, whose
	 * {@code encounter} is {@code @ManyToOne(optional=false)} and eager. Only {@code optional=false}
	 * associations belong here, so the guard never drops a legitimately-null association.
	 */
	protected String[] additionalNonNullExprs() {
		return new String[0];
	}

	@Override
	protected final List<T> fetchPage(Instant afterDateChanged, String afterUuid, int pageSize) {
		Class<T> entityType = getSerializer().getSupportedType();
		String entityName = entityType.getSimpleName();
		String dateExpr = cursorDateExpr();
		// Use the modern parameterized Query API via the underlying SessionFactory; DbSession's
		// createQuery returns the legacy raw type.
		String patientExpr = patientAssociationExpr();
		Session session = sessionFactory.getHibernateSessionFactory().getCurrentSession();
		String[] extra = additionalNonNullExprs();
		Query<T> q;
		if (afterDateChanged == null) {
			q = session.createQuery(firstPageHql(entityName, dateExpr, patientExpr, extra), entityType);
		} else {
			q = session.createQuery(afterCursorHql(entityName, dateExpr, patientExpr, extra), entityType);
			q.setParameter("cursor", Date.from(afterDateChanged));
			q.setParameter("afterUuid", afterUuid != null ? afterUuid : "");
		}
		q.setMaxResults(pageSize);
		return q.list();
	}

	@Override
	protected final List<T> fetchPageForPatient(String patientUuid, Instant afterDateChanged, String afterUuid,
	                                            int pageSize) {
		Class<T> entityType = getSerializer().getSupportedType();
		String entityName = entityType.getSimpleName();
		String dateExpr = cursorDateExpr();
		String patientExpr = patientAssociationExpr();
		Session session = sessionFactory.getHibernateSessionFactory().getCurrentSession();
		String[] extra = additionalNonNullExprs();
		Query<T> q;
		if (afterDateChanged == null) {
			q = session.createQuery(firstPagePerPatientHql(entityName, dateExpr, patientExpr, extra), entityType);
		} else {
			q = session.createQuery(afterCursorPerPatientHql(entityName, dateExpr, patientExpr, extra), entityType);
			q.setParameter("cursor", Date.from(afterDateChanged));
			q.setParameter("afterUuid", afterUuid != null ? afterUuid : "");
		}
		q.setParameter("patientUuid", patientUuid);
		q.setMaxResults(pageSize);
		return q.list();
	}

	/**
	 * Poison-page recovery for the global scan. The inner-join guards ({@code patient.uuid},
	 * {@code encounter.uuid}) already drop the orphans they cover, so this fires only for a dangling FK
	 * NOT covered by a guard — a nullable association with a non-null-but-missing target, or a mandatory
	 * metadata FK ({@code Obs.concept}) — which {@link #fetchPage}'s {@code q.list()} eager-materializes
	 * and throws on. Strategy: select {@code (uuid, cursorDate)} as a SCALAR projection (Hibernate does
	 * not hydrate associations for a projection, so the dangling FK can't throw here), then load each
	 * entity individually by uuid; the poison row throws on its own load and is skipped, while its
	 * descriptor still advances the cursor so the scan moves past it. The window's last descriptor is the
	 * next cursor even when every row in it was poison, so an all-orphan window cannot loop forever.
	 */
	@Override
	protected PageResult<T> fetchPageSkippingPoison(Instant afterDateChanged, String afterUuid, int pageSize,
	                                                RuntimeException pageFetchError) {
		Class<T> entityType = getSerializer().getSupportedType();
		String entityName = entityType.getSimpleName();
		String dateExpr = cursorDateExpr();
		String patientExpr = patientAssociationExpr();
		String[] extra = additionalNonNullExprs();
		Session session = sessionFactory.getHibernateSessionFactory().getCurrentSession();

		Query<Object[]> dq;
		if (afterDateChanged == null) {
			dq = session.createQuery("SELECT e.uuid, " + dateExpr + " "
			        + firstPageHql(entityName, dateExpr, patientExpr, extra), Object[].class);
		} else {
			dq = session.createQuery("SELECT e.uuid, " + dateExpr + " "
			        + afterCursorHql(entityName, dateExpr, patientExpr, extra), Object[].class);
			dq.setParameter("cursor", Date.from(afterDateChanged));
			dq.setParameter("afterUuid", afterUuid != null ? afterUuid : "");
		}
		dq.setMaxResults(pageSize);
		List<Object[]> descriptors = dq.list();
		if (descriptors.isEmpty()) {
			// The bulk fetch threw but this window holds no rows to hydrate — so the failure wasn't a
			// poison row (more likely a DB/HQL error). Surface the original rather than finishing silently.
			throw pageFetchError;
		}

		List<T> loaded = new ArrayList<T>(descriptors.size());
		int skipped = 0;
		for (Object[] descriptor : descriptors) {
			String uuid = (String) descriptor[0];
			try {
				T entity = session.createQuery("FROM " + entityName + " e WHERE e.uuid = :uuid", entityType)
				        .setParameter("uuid", uuid).uniqueResult();
				if (entity != null) {
					loaded.add(entity);
				}
			}
			catch (RuntimeException rowError) {
				skipped++;
				SafeLog.warn(HibernateTypeBootstrapper.class, SkipLogFormat.format("bootstrap", getResourceType(), uuid, null,
				    "skipped dump-orphaned record (dangling FK): " + rowError.getMessage()), rowError);
			}
		}
		Object[] last = descriptors.get(descriptors.size() - 1);
		log.warn("Poison-page recovery for " + getResourceType() + ": loaded " + loaded.size() + ", skipped "
		        + skipped + " orphan(s) of " + descriptors.size() + " in window");
		return new PageResult<T>(loaded, toInstant(last[1]), (String) last[0]);
	}

	/** Converts the cursor-date column of a descriptor projection to an {@link Instant}; null/foreign
	 *  types fall back to {@link Instant#EPOCH}, matching {@code TypeBootstrapper.getDateChanged}. */
	private static Instant toInstant(Object cursorDate) {
		return cursorDate instanceof Date ? ((Date) cursorDate).toInstant() : Instant.EPOCH;
	}

	@Override
	public long countIndexable() {
		Class<T> entityType = getSerializer().getSupportedType();
		Session session = sessionFactory.getHibernateSessionFactory().getCurrentSession();
		return session.createQuery(
		        countHql(entityType.getSimpleName(), patientAssociationExpr(), additionalNonNullExprs()),
		        Long.class).getSingleResult();
	}

	// Package-private so a unit test can pin the HQL shape without invoking Hibernate.
	//
	// The "AND <patientExpr> IS NOT NULL" clause excludes rows whose patient/person FK is dangling —
	// an orphan a SQL-dump load can leave behind (a diagnosis/visit pointing at a patient_id with no
	// patient row). Navigating the association forces an INNER JOIN that drops such rows at the SQL
	// level. Without it, q.list() eager-materializes the missing association and throws
	// FetchNotFoundException, failing the ENTIRE type (per-record skip in serializeAndEmbed can't help —
	// the failure is at page fetch, before projection; fetchPageSkippingPoison recovers it). The
	// per-patient scan is already orphan-safe for
	// the same reason (it navigates the same expr via "= :patientUuid"); this brings the global scan
	// to parity. Orphan rows are unindexable anyway (no patient to scope them to), so excluding them
	// is the correct "skip the bad record" behavior.
	//
	// extraNonNullExprs applies the SAME inner-join orphan guard to ADDITIONAL mandatory eager to-one
	// associations a type declares via additionalNonNullExprs() — e.g. Diagnosis.encounter
	// (@ManyToOne(optional=false), eager). A diagnosis with a valid patient but a dangling encounter FK
	// survives the patient guard, then q.list() eager-materializes the missing encounter and throws
	// FetchNotFoundException, failing the whole type. Navigating each extra expr forces the inner join
	// that drops it too. Only optional=false associations belong here, so the guard never drops a
	// legitimately-null one. Zero extras leaves the HQL byte-for-byte as the patient-guard-only form.
	static String firstPageHql(String entityName, String dateExpr, String patientExpr, String... extraNonNullExprs) {
		return "FROM " + entityName + " e WHERE e.voided = false "
		        + "AND " + patientExpr + " IS NOT NULL "
		        + extraGuards(extraNonNullExprs)
		        + "ORDER BY " + dateExpr + " ASC, e.uuid ASC";
	}

	// Drift detection's "expected" count: the same WHERE as firstPageHql (voided + patient/person guard
	// + extra orphan guards), counted instead of paged — so it matches exactly what the scan indexes.
	static String countHql(String entityName, String patientExpr, String... extraNonNullExprs) {
		return "SELECT count(e) FROM " + entityName + " e WHERE e.voided = false "
		        + "AND " + patientExpr + " IS NOT NULL "
		        + extraGuards(extraNonNullExprs);
	}

	static String afterCursorHql(String entityName, String dateExpr, String patientExpr, String... extraNonNullExprs) {
		// COALESCE in WHERE so a record whose dateChanged is null still progresses past the cursor on
		// dateCreated alone; the uuid tie-breaker handles records sharing the same effective timestamp.
		return "FROM " + entityName + " e WHERE e.voided = false "
		        + "AND " + patientExpr + " IS NOT NULL "
		        + extraGuards(extraNonNullExprs)
		        + "AND ("
		        + dateExpr + " > :cursor "
		        + "OR (" + dateExpr + " = :cursor AND e.uuid > :afterUuid)) "
		        + "ORDER BY " + dateExpr + " ASC, e.uuid ASC";
	}

	static String firstPagePerPatientHql(String entityName, String dateExpr, String patientExpr, String... extraNonNullExprs) {
		return "FROM " + entityName + " e WHERE e.voided = false "
		        + "AND " + patientExpr + " = :patientUuid "
		        + extraGuards(extraNonNullExprs)
		        + "ORDER BY " + dateExpr + " ASC, e.uuid ASC";
	}

	static String afterCursorPerPatientHql(String entityName, String dateExpr, String patientExpr, String... extraNonNullExprs) {
		return "FROM " + entityName + " e WHERE e.voided = false "
		        + "AND " + patientExpr + " = :patientUuid "
		        + extraGuards(extraNonNullExprs)
		        + "AND ("
		        + dateExpr + " > :cursor "
		        + "OR (" + dateExpr + " = :cursor AND e.uuid > :afterUuid)) "
		        + "ORDER BY " + dateExpr + " ASC, e.uuid ASC";
	}

	/** Builds the additional {@code AND <expr> IS NOT NULL} orphan guards declared by
	 *  {@link #additionalNonNullExprs()}. Each navigates a mandatory eager to-one association so a
	 *  dangling FK is dropped by the forced inner join at fetch, rather than throwing
	 *  FetchNotFoundException (which fails the whole type). Empty input yields the empty string so the
	 *  patient-guard-only HQL is unchanged. */
	private static String extraGuards(String[] extraNonNullExprs) {
		if (extraNonNullExprs == null || extraNonNullExprs.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String expr : extraNonNullExprs) {
			sb.append("AND ").append(expr).append(" IS NOT NULL ");
		}
		return sb.toString();
	}
}
