# Query Store REST API

The query store's primary in-JVM consumer surface is the Java `QueryStoreService`; see
[ADR Decision 14](./adr.md#decision-14-authorization-and-consumer-api-surface). The REST API adds:

- a thin read adapter for authorized non-JVM clients and external services such as med-agent-hub; and
- operational endpoints for observing and repairing index state.

The read adapter delegates to the existing service methods. It does not add per-request
reconciliation, completeness claims, or a second read policy.

All endpoints are served under the OpenMRS REST namespace (the module requires the
`webservices.rest` module):

```
/ws/rest/v1/querystore/...
```

Authenticate with any OpenMRS REST mechanism (HTTP Basic shown below).

---

## GET `/ws/rest/v1/querystore/patientrecord`

Returns query-store records through the existing `QueryStoreService` behavior:

| Parameters | Service method | Ordering |
|---|---|---|
| `patient=<uuid>` | `getPatientChart(patient)` | Full materialized patient chart, newest first |
| `patient=<uuid>&q=<text>` | `searchByPatient(patient, q, k)` | Ranked patient results |
| `q=<text>` | `search(q, k)` | Ranked cross-patient results |

- **Privilege:** `Get Patients`.
- **Paging:** `limit` defaults to `50`; `startIndex` defaults to `0`.
- **Shape:** `results`, `totalCount`, and `links`. Full-chart reads carry the materialized result
  count. Ranked top-K reads use `null` because the service does not expose a browseable total.
- **Records:** `resourceType`, `resourceUuid`, ISO `date`, `text`, and `metadata`. Ranked records
  also carry a 1-based `rank`.
- **Excluded:** embeddings and backend scores are never returned.

The endpoint intentionally does not report a snapshot or `complete` flag. Index readiness and
repair remain the responsibility of `/indexingstatus`, `/drift`, and `/reindex`; ordinary reads do
not trigger a full patient rebuild.

```bash
curl -s -u patient-reader:secret \
  'https://your-server/openmrs/ws/rest/v1/querystore/patientrecord?patient=patient-uuid&limit=500'
```

**Errors:** missing `patient` and `q`, invalid paging, or malformed parameters return `400`; an
unknown patient returns `404`; failed authentication/authorization returns `401`/`403`.

---

## GET `/ws/rest/v1/querystore/indexingstatus`

Returns the per-resource-type bootstrap (initial-backfill) status, plus a single derived
`complete` flag answering **"is this deployment fully indexed?"**.

- **Privilege:** `Get Patients` — a `failureMessage` can contain internal record/patient
  identifiers, so it is gated like the query store's read API.
- **Request body:** none.

**Response `200`:**

```json
{
  "complete": false,
  "types": [
    {
      "resourceType": "obs",
      "status": "RUNNING",
      "documentsIndexed": 320795,
      "cursorDateChanged": "2006-05-25T12:41:31Z",
      "startedAt": "2026-05-30T19:35:29.849Z",
      "completedAt": null,
      "failureMessage": null,
      "backend": "lucene"
    },
    {
      "resourceType": "diagnosis",
      "status": "FAILED",
      "documentsIndexed": 0,
      "cursorDateChanged": null,
      "startedAt": "2026-05-29T13:31:01.404Z",
      "completedAt": null,
      "failureMessage": "FetchNotFoundException: Entity `org.openmrs.Patient` with identifier value `49` does not exist",
      "backend": "lucene"
    }
  ]
}
```

| Field | Meaning |
|---|---|
| `complete` | `true` **only** when at least one type is tracked **and every** tracked type is `COMPLETED`. Any `RUNNING`/`FAILED`/`NOT_STARTED` type — or an empty progress table — yields `false`. This is the headline "fully indexed?" answer. |
| `types[].status` | `NOT_STARTED` \| `RUNNING` \| `COMPLETED` \| `FAILED`. |
| `types[].documentsIndexed` | Documents written for that type so far. |
| `types[].cursorDateChanged` | The backfill resume cursor (ascending `dateChanged`); records changed after this are not yet indexed. ISO-8601, or `null`. |
| `types[].startedAt` / `completedAt` | ISO-8601 timestamps, or `null`. |
| `types[].failureMessage` | Populated when `status` is `FAILED` (e.g. an orphaned row referencing a deleted patient). |
| `types[].backend` | The backend that row was written against (`lucene` \| `mysql` \| `elasticsearch`). |

```bash
curl -s -u admin:Admin123 \
  https://your-server/openmrs/ws/rest/v1/querystore/indexingstatus
```

---

## POST `/ws/rest/v1/querystore/reindex`

Re-indexes the read store in one of three scopes, selected by the request body. All are destructive,
expensive maintenance operations gated by **`Manage Global Properties`** (stricter than the read
endpoint's `Get Patients`).

### Per-patient — `{"patient": "<uuid>"}`

Forces a full re-projection of **one patient** — deletes that patient's read-store documents and
re-indexes every resource type from core — then reports the resulting document count. This is the
only no-restart way to repair a **partially-indexed** patient (e.g. one whose recent records
arrived via a SQL dump): the lazy cold-touch projection refuses such patients because they already
have *some* documents.

- **Synchronous:** runs in the request thread; latency scales with the patient's record count
  (each record is re-embedded). Bounded to one patient.

**Response `200`:**

```json
{ "patient": "dd9836d7-1691-11df-97a5-7038c432aabf", "documentsIndexed": 154 }
```

```bash
curl -s -X POST -u admin:Admin123 -H 'Content-Type: application/json' \
  -d '{"patient":"dd9836d7-1691-11df-97a5-7038c432aabf"}' \
  https://your-server/openmrs/ws/rest/v1/querystore/reindex
```

### Global — `{"scope": "all"}`

Launches the **full backfill** over every patient and resource type — the same scan the
`querystore.bootstrap.autostart` startup path runs — on a daemon thread, and returns immediately.
This is the no-restart way to kick off (or resume) the global bootstrap on a live, already-running
instance; previously it could only start at module startup. Poll
[`GET /indexingstatus`](#get-wsrestv1querystoreindexingstatus) until `complete` is `true`.

- **Asynchronous:** a full-corpus scan can't run in a request thread, so the call returns `202
  Accepted` and the work proceeds in the background. Overlapping `scope:"all"` requests collapse to
  a single in-flight run via an in-flight guard in the launcher; even if two runs did overlap, the
  per-resource-type locks keep the progress bookkeeping safe.
- **Idempotent overlap:** if a Querystore reindex is already running, another `scope:"all"` request
  is accepted without launching a second worker.
- **Opt-in by design:** the global scope is requested *explicitly* with `scope:"all"` (matched
  case-insensitively), never by omitting `patient` — an absent field overlaps with a malformed
  request, and the global scan is the most expensive operation in the module.

**Response `202`:**

```json
{ "accepted": true }
```

```bash
curl -s -X POST -u admin:Admin123 -H 'Content-Type: application/json' \
  -d '{"scope":"all"}' \
  https://your-server/openmrs/ws/rest/v1/querystore/reindex
```

### Per-type — `{"scope": "type", "resourceType": "<type>"}`

Reconciliation remediation: forces a full re-walk of **one resource type** from core to correct
drift surfaced by [`GET /drift`](#get-wsrestv1querystoredrift), without the cost of a global
rebuild. Resets that type's bootstrap cursor and re-scans it from the start, so records the original
scan skipped (poison rows) or never saw (lost events, post-bootstrap arrivals before the cursor) are
picked up. Poll [`GET /indexingstatus`](#get-wsrestv1querystoreindexingstatus) for that type's
status; re-check `GET /drift` afterward.

- **Asynchronous:** a full single-type scan can't run in a request thread, so the call returns `202
  Accepted` and the work proceeds on a daemon thread.
- **Re-walk only:** corrects **under-indexing** (positive drift — the common case). It does **not**
  delete *stale extras* (negative drift: documents whose core record was voided/deleted but never
  evented out), since the scan only visits live core records.
- **Validated:** `resourceType` is checked against the registered indexed types; an unknown value is
  rejected with `400` synchronously, rather than a `202` that fails silently on the daemon thread.

**Response `202`:**

```json
{ "accepted": true, "resourceType": "obs" }
```

```bash
curl -s -X POST -u admin:Admin123 -H 'Content-Type: application/json' \
  -d '{"scope":"type","resourceType":"obs"}' \
  https://your-server/openmrs/ws/rest/v1/querystore/reindex
```

### Errors

| Status | Body | When |
|---|---|---|
| `400` | `{"error": "patient or scope (\"all\" or \"type\") is required"}` | neither `patient` nor `scope` supplied |
| `400` | `{"error": "Unknown scope '<x>'; supported scopes are \"all\" and \"type\""}` | `scope` set to anything other than `all` or `type` |
| `400` | `{"error": "Specify only one of patient, scope:\"all\", or scope:\"type\" + resourceType"}` | `scope:"all"` supplied together with `patient` or `resourceType` (ambiguous) |
| `400` | `{"error": "Specify either patient or scope:\"type\", not both"}` | both `patient` and `scope:"type"` supplied (ambiguous) |
| `400` | `{"error": "scope:\"type\" requires a resourceType"}` | `scope:"type"` without a `resourceType` |
| `400` | `{"error": "Unknown resourceType '<x>'"}` | `scope:"type"` with a `resourceType` that is not a registered indexed type |
| `503` | `{"error": "Cannot start reindex: the bootstrap daemon is not yet available"}` | `scope:"all"` requested while the module's daemon token is unavailable — normally a misconfiguration, since the token is wired during module startup, before the server serves requests |
| `503` | `{"error": "Cannot start re-sync: the bootstrap daemon is not yet available"}` | `scope:"type"` requested while the daemon token is unavailable (same cause as above) |

**Semantics & caveats:**

- **Delete-before-reproject, under a per-patient lock** (per-patient scope). The delete and
  re-projection are atomic with respect to the lazy cold-touch projection (they share the same
  per-patient lock), so a concurrent first-touch can't interleave with the delete.
- **Transient partial reads** (per-patient scope). A concurrent search for the *same* patient may
  briefly observe partial or empty results while the rebuild is in flight (the read path's existence
  probe runs outside the lock). The full chart is restored by the time this call returns. Avoid
  triggering a reindex during active charting on that patient.
- **Concurrent bridge writes converge.** A live `saveObs` (etc.) during either scope's reindex is
  reconciled by the version-protection invariant (newer write wins, older dropped) — no corruption.
- **Single-type backfill.** To advance just one resource type in-process, call
  `BootstrapService.bootstrap(resourceType)`; external operators use the REST `scope:"type"` trigger
  documented above.
