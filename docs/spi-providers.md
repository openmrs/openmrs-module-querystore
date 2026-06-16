# Contributing custom resource types via the provider SPI

This walkthrough shows how a module — for example `billing` or `appointments` — contributes its own clinical resource type to the query store. The concrete example builds a `billing_bill` provider end-to-end; substitute your own moduleid and type name where appropriate.

The recipe is verified by [`ProviderEndToEndTest`](../api/src/test/java/org/openmrs/module/querystore/bootstrap/ProviderEndToEndTest.java) — when this document drifts from the SPI, that test fails first.

## What you build

Three small classes in your own module:

1. A **serializer** (`ClinicalRecordSerializer<YourEntity>`) that maps your domain entity to a `QueryDocument` with the cross-cutting fields contract.
2. A **bootstrapper** (`TypeBootstrapper<YourEntity>`) that backfills historical records on first install.
3. A **provider bean** (`ResourceTypeProvider`) that bundles 1 + 2 and lets querystore discover the contribution.

Steady-state indexing is no longer something you build: querystore's events consumer indexes any type with a registered serializer whenever core's #6084 `*ServiceEvent`s fire for it — so if your write service qualifies (Step 3), saves/voids/purges are picked up for free.

What you _do not_ build: schema migrations, table/index creation, embedding, search routing, patient-cascade deletion, authorization checks. All of that is automatic.

## What you get for free

- **Schema lifecycle.** All three reference backends (MySQL, Lucene, Elasticsearch) lazy-create your per-type table/index on first write. No Liquibase, no per-tier mapping.
- **Cross-type search.** `QueryStoreService.search(...)` already enumerates every `querystore_*` store. The moment your provider's first document lands, it's part of the cross-type query surface.
- **Patient-scoped retrieval.** `QueryStoreService.searchByPatient(uuid, q, limit)` filters by your documents' `patient_uuid` field uniformly.
- **Patient cascade.** `BackendStore.bulkDeleteByPatient(uuid)` — used by void / merge paths — iterates every `querystore_*` store, including yours.
- **Embedding.** `RecordIndexer` (steady-state writes) and `BootstrapServiceImpl` (initial backfill) embed your documents' `text` field with the deployment-configured `EmbeddingProvider`.
- **Authorization.** Consumer reads go through `QueryStoreService`'s `@Authorized(GET_PATIENTS)` surface. Your provider writes; you do not gate access.

## Prerequisite — confirm your type passes the indexing criterion

Before following the steps below, confirm your resource type meets [Decision 13's *Criteria for contributing a metadata resource type*](./adr.md#criteria-for-contributing-a-metadata-resource-type). The walkthrough is the cheap part; deciding *whether* to index is the load-bearing decision. The criterion bites hardest for types extending `BaseOpenmrsMetadata` (catalog/reference data), where most candidates fail the iff rule — structured catalog filtering, "never referenced" queries, and aggregations are better served by SQL or a downstream analytical projection (e.g., MambaETL where deployed), and dictionary lookups belong in OCL / FHIR terminology services. Clinical-data types extending `BaseOpenmrsData` (the SPI's current type bound, see [issue #16](https://github.com/openmrs/openmrs-module-querystore/issues/16)) generally satisfy the criterion by carrying free-text clinical content over the patient record.

## Prerequisite — entity must extend `BaseOpenmrsData`

Your domain entity must be a Hibernate-mapped subclass of `org.openmrs.BaseOpenmrsData`. The projection pipeline (`RecordProjector.project(...)`) is bounded on this for two reasons: the per-node voided routing reads `getVoided()`, and the bootstrap cursor reads `getDateChanged() ?? getDateCreated()`. Domain entities that don't extend `BaseOpenmrsData` cannot use the SPI as-is — that's a deliberate scope limit, not an accident.

## Naming rule

Your resource type name must be `<moduleid>_<type>`, lowercase, with internal segments matching `[a-z][a-z0-9]*`. No leading, trailing, or consecutive underscores. Examples:

- `appointments_appointment`
- `billing_bill`
- `billing_payment_method`
- `radiology_imaging_study`

The corresponding index is `querystore_<moduleid>_<type>` per ADR Decision 4. Querystore validates this at discovery; a malformed name causes your provider bean to be logged and skipped while well-formed peers continue.

## Step 1 — serializer

Implement `ClinicalRecordSerializer<YourEntity>`. Populate the cross-cutting fields from ADR Decision 6:

| Field | Required | Notes |
|---|---|---|
| `resource_type` | yes | Set to your provider's resource type name. See the gotchas section on serializer/provider name drift — the discovery validator rejects providers whose serializer reports a different name. |
| `resource_uuid` | yes | Your entity's UUID. |
| `patient_uuid` | yes if the record is patient-scoped | Skip only for non-patient resources (e.g., a knowledge-base type). |
| `last_modified` | yes | `dateChanged ?? dateCreated`. Used by the backend's conditional-upsert race guard. |
| `record_date` | yes | The record's effective clinical date (not the modification timestamp). |
| `text` | yes | Labeled prose; this is what gets indexed for BM25 and embedded for kNN. |
| `embedding` | leave unset | Querystore embeds at write time. |

Type-specific fields go in `putMetadata(key, value)`. Encounter / visit / location / provider denormalizations belong here when applicable.

If your records carry a primary coded concept, also populate these via `putMetadata` so the framework enriches the embedding input per [ADR Decision 6's Synonyms-and-group-obs convention](./adr.md#synonyms-and-group-obs-convention):

- `synonyms` — a `List<String>` of locale-aware concept synonyms (use `ConceptNameUtil.getSynonyms(...)`). Concatenated onto the embedding input so vectors are synonym-aware on every backend; also BM25-indexed by the Lucene and Elasticsearch tiers as a top-level companion of `text` so an "HTN" query hits a doc whose preferred name is "Hypertension" via keyword match too. The MySQL FULLTEXT tier covers only `text`; semantic-path synonym matching still works there via the embedding-input enrichment.
- `obs_group_concept_name` — the parent group concept's name, for records that are members of a group obs. Prepended to the embedding input (but not the stored `text`) so group-level semantic queries match member vectors.

```java
public class BillSerializer implements ClinicalRecordSerializer<Bill> {

    @Override public String getResourceType() { return "billing_bill"; }

    @Override public Class<Bill> getSupportedType() { return Bill.class; }

    @Override
    public QueryDocument serialize(Bill bill) {
        QueryDocument doc = new QueryDocument();
        doc.setResourceType("billing_bill");
        doc.setResourceUuid(bill.getUuid());
        doc.setPatientUuid(bill.getPatient().getUuid());
        Instant modified = (bill.getDateChanged() != null ? bill.getDateChanged() : bill.getDateCreated())
                .toInstant();
        doc.setLastModified(modified);
        doc.setDate(LocalDate.ofInstant(modified, ZoneOffset.UTC));
        doc.setText("Bill: " + bill.getDescription()
                + ". Amount: " + bill.getAmount() + " " + bill.getCurrency() + ".");
        doc.putMetadata("amount_cents", bill.getAmountCents());
        doc.putMetadata("status", bill.getStatus().name());
        return doc;
    }
}
```

**Locale.** If your entity carries coded concepts, resolve their names in the deployment's configured locale at serialization time, then store both the UUID and the resolved name (ADR Decision 9). See `ObsRecordSerializer` for the pattern.

**Voiding.** Do not check `voided` in `serialize` — return the document unconditionally. The bridge advice routes voided records to delete instead of upsert (ADR Decision 10).

## Step 2 — bootstrapper

Implement `TypeBootstrapper<YourEntity>` to backfill historical records. The recommended base class is `HibernateTypeBootstrapper<YourEntity>` — it provides paginated HQL with cursor resumption (sorted by `dateChanged ?? dateCreated` + `uuid`, voided records excluded) and only requires you to plug in the serializer:

```java
public class BillBootstrapper extends HibernateTypeBootstrapper<Bill> {

    private final BillSerializer serializer;

    public BillBootstrapper(BillSerializer serializer, DbSessionFactory sessionFactory) {
        super(sessionFactory);
        this.serializer = serializer;
    }

    @Override
    protected ClinicalRecordSerializer<Bill> getSerializer() { return serializer; }
}
```

If your entity's Hibernate mapping omits `dateChanged` (as `Obs` and `Order` do in OpenMRS 2.8+), override `cursorDateExpr()` to return `"e.dateCreated"`.

The bootstrapper is optional — see Step 7 for when to return null from `getBootstrapper()`.

## Step 3 — steady-state indexing (events)

You no longer write an indexing advice — the AOP migration bridge was removed (ADR Decision 12). querystore's `CoreServiceEventListener` consumes core's #6084 `*ServiceEvent`s and projects any entity for which a serializer (Step 1) is registered, through the same `RecordProjector` that does per-node voided routing (voided → delete, non-voided → serialize + index), after-commit dispatch, and per-entity failure isolation. So once your serializer is registered, steady-state saves/voids/purges of your type are indexed **for free** — *if* your write service emits those events.

It does, automatically, when your service meets core's #6084 pointcut ([Decision 13](./adr.md#decision-13-module-extension-spi-service-provider-interface-for-custom-resource-types) §3): it implements `org.openmrs.api.OpenmrsService`, the mutator is named `save* / create* / void* / unvoid* / retire* / unretire* / purge*`, `args[0]` is an `OpenmrsObject`, and the call is external (self-invocation bypasses the proxy). This is **opt-in by verification, not assumption** — confirm an event actually fires for your service at runtime (the default assumption is *no* coverage).

If your service does not qualify (non-`OpenmrsService`, non-standard method names, or self-invocation), contribute your own listener: a Spring `@EventListener` for the relevant `*ServiceEvent`, or — for a service that doesn't emit at all — a small AOP `@Around` shim on your service that calls `RecordProjector.project(serializer, entity, isPurge, indexer, dispatcher)` (the `querystore.sync.indexer` and `querystore.sync.dispatcher` beans are reachable via `Context.getRegisteredComponent(...)`). Until then, the bootstrap (Step 2) is the only thing keeping your type current.

## Step 4 — provider bean

Bundle the serializer and bootstrapper behind the SPI:

```java
public class BillProvider implements ResourceTypeProvider {

    private final BillSerializer serializer;
    private final BillBootstrapper bootstrapper;

    public BillProvider(BillSerializer serializer, BillBootstrapper bootstrapper) {
        this.serializer = serializer;
        this.bootstrapper = bootstrapper;
    }

    @Override public String getResourceType() { return "billing_bill"; }
    @Override public ClinicalRecordSerializer<?> getSerializer() { return serializer; }
    @Override public TypeBootstrapper<?> getBootstrapper() { return bootstrapper; }
}
```

## Step 5 — wire the beans in your module

In your module's `omod/src/main/resources/moduleApplicationContext.xml`, register the three Spring beans:

```xml
<bean id="billing.serializer.bill"
      class="org.openmrs.module.billing.querystore.BillSerializer"/>

<bean id="billing.bootstrapper.bill"
      class="org.openmrs.module.billing.querystore.BillBootstrapper">
    <constructor-arg ref="billing.serializer.bill"/>
    <constructor-arg ref="dbSessionFactory"/>
</bean>

<bean id="billing.provider.bill"
      class="org.openmrs.module.billing.querystore.BillProvider">
    <constructor-arg ref="billing.serializer.bill"/>
    <constructor-arg ref="billing.bootstrapper.bill"/>
</bean>
```

(Steady-state indexing needs no wiring here — it rides core's #6084 events per Step 3. If your service doesn't emit them and you added your own `@EventListener` or AOP shim, register/wire that bean too.)

Querystore discovers `billing.provider.bill` at the next `bootstrap()` invocation via `Context.getRegisteredComponents(ResourceTypeProvider.class)` — no further wiring on the querystore side is needed.

## Step 6 — module dependency

In your module's `pom.xml`:

```xml
<dependency>
    <groupId>org.openmrs.module</groupId>
    <artifactId>querystore-api</artifactId>
    <version>${querystore.version}</version>
    <scope>provided</scope>
</dependency>
```

And in your `omod/src/main/resources/config.xml`:

```xml
<require_modules>
    <require_module version="${querystore.version}">org.openmrs.module.querystore</require_module>
</require_modules>
```

## Step 7 — backfill on install

When your module is installed _after_ querystore has already completed its initial bootstrap, your provider's historical records need a one-time backfill. The simplest convention is to trigger it from your module's activator:

```java
public class BillingActivator extends BaseModuleActivator {

    @Override
    public void started() {
        Context.getService(BootstrapService.class).bootstrap("billing_bill");
    }
}
```

`bootstrap("x")` is idempotent — if your provider already has a complete progress row, it resumes from the cursor and finds no new work. Safe to call on every module start.

If your provider has no historical data to backfill (the type is new and starts empty), return `null` from `getBootstrapper()` and skip this step. An admin invoking `bootstrap("billing_bill")` against a null-bootstrapper provider gets an explicit "no historical backfill" error rather than a silent no-op.

## Verification

After your module loads and bootstraps:

1. **Discovery.** `Context.getRegisteredComponents(ResourceTypeProvider.class)` should include your bean. If it doesn't, check that the bean is declared in `moduleApplicationContext.xml` and that the module loaded without errors.

2. **Index creation.** The backend creates `querystore_billing_bill` on the first write — a Lucene directory under `<appdata>/querystore/lucene/`, a MySQL table `querystore_billing_bill`, or an Elasticsearch index `querystore_billing_bill`.

3. **Bootstrap progress.** `BootstrapService.getStatus("billing_bill")` returns a `BootstrapProgress` with `COMPLETED` status and a non-zero `documentsIndexed` count.

4. **Steady-state writes.** Save a bill through your service; within ~1 second after commit, `service.searchByPatient(patientUuid, "<query against the bill text>", 10)` returns the bill.

5. **Cross-type search.** `service.search("<broad query>", 10)` returns the bill alongside any matching core-type documents.

6. **Patient cascade.** When the patient is voided, `BackendStore.bulkDeleteByPatient(uuid)` removes the bill from your index along with all other documents for that patient.

## Limitations and gotchas

- **Self-invocation bypass.** Core's #6084 events (and any AOP shim you add) fire only when your service's methods are called through Spring's proxy. An internal `this.foo()` call from inside the bean bypasses them. The bootstrap pass is the safety net.
- **Hibernate cascade saves.** If a save on parent entity X cascades to child entity Y through a Hibernate mapping, no `*ServiceEvent` fires for Y (there's no service call), so the events consumer doesn't see it. Same shape as `transferEncounter` on the core side — an accepted events-path limitation per Decision 12; reconciliation/bootstrap catches it.
- **`dateChanged ?? dateCreated` cursor.** If your entity's Hibernate mapping omits `dateChanged`, override `cursorDateExpr()` in your bootstrapper to return `"e.dateCreated"` or HQL throws at first fetch.
- **Embedding model identifier.** Every consumer issuing kNN queries against your documents must embed its query with the same model querystore used at index time. Decision 8 and Decision 13 fix this as part of the public contract; query-time consumers read the model identifier from querystore configuration.
- **One provider per resource type.** If two providers claim the same name, the first one wins at discovery and the second is logged and dropped. Two modules cannot independently index the same `<moduleid>_<type>`.
- **One serializer-resource-type per provider.** If your serializer's `getResourceType()` doesn't match your provider's, discovery rejects the provider. The two paths (bootstrap + bridge) must route to the same store.

## Reference implementations

The 14 core-type contributions in this module follow the same shape (minus the SPI bean — they're wired directly in querystore's own `moduleApplicationContext.xml`). The closest siblings to copy from:

| If your type looks like… | Look at… |
|---|---|
| A patient-scoped clinical observation | `ConditionRecordSerializer` + `ConditionBootstrapper` |
| An order against a patient (drug, lab, referral) | `DrugOrderRecordSerializer` + its bootstrapper |
| An encounter-attached event | `EncounterRecordSerializer` + its bootstrapper |
| A patient entity with denormalized identifiers | `PatientRecordSerializer` + its bootstrapper |
| A program enrollment / lifecycle | `PatientProgramRecordSerializer` + its bootstrapper + advice |

The canonical worked example is [`ProviderEndToEndTest`](../api/src/test/java/org/openmrs/module/querystore/bootstrap/ProviderEndToEndTest.java), already referenced at the top of this document.
