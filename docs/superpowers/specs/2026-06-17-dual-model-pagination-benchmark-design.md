# Dual-Model Pagination Benchmark — Design

**Date:** 2026-06-17
**Status:** Approved (design phase)

## 1. Purpose

The project tests how far Postgres pagination holds up over millions of UE
(User Equipment) events. Today it auto-generates records into two tables and
paginates with OFFSET. This redesign turns it into a controlled benchmark that
compares **two persistence models** and **two pagination strategies** side by
side, with server-measured timing surfaced in the UI.

Goals:

1. Two selectable persistence models — **NORMAL** (2 tables) and **CQRS /
   read-write separation** (5 tables, async projection).
2. Two selectable pagination strategies — **OFFSET** (numbered pages) and
   **KEYSET** (cursor) — to demonstrate where OFFSET degrades and keyset stays
   flat.
3. A manual **Generate Data** action (user-specified count) that loads into both
   models, reporting load time.
4. Every read and generate operation reports **server-side elapsed time**,
   displayed in the UI.

Non-goals: real read replicas, distributed infra, auth, multi-node. Single
Postgres 16 instance, single backend.

## 2. Current state (baseline)

- `ue_events` — latest event per UE, PK = `imsiOrSupi`.
- `ue_events_history` — append-only log, PK `@GeneratedValue(IDENTITY)`.
- `UeEventService.saveEvent` writes both in one `@Transactional` call.
- Reads use Spring Data `Page` + `PageRequest.of(page,size)` (OFFSET) and return
  `count(*)`-backed totals.
- `DataGeneratorService` bulk-generates 1000 rows at boot (`CommandLineRunner`)
  and 1 row every 2s (`@Scheduled`).
- Protobuf transport: `UeEvent`, `UeEventPageResponse`.
- `application.properties`: `ddl-auto=update`, `batch_size=500`, **no**
  `reWriteBatchedInserts`.

### Baseline flaws (hurt both models)

| # | Flaw | Effect |
|---|------|--------|
| 1 | OFFSET pagination | O(offset); deep pages scan + discard millions |
| 2 | `count(*)` per page (Spring `Page`) | full count on every request |
| 3 | History PK `IDENTITY` | Hibernate silently disables JDBC batch inserts |
| 4 | JDBC URL lacks `reWriteBatchedInserts=true` | `batch_size=500` is a no-op |
| 5 | Indexes not aligned to sort key | no index-only scans; no seek tiebreaker |

## 3. Architecture

### 3.1 Persistence models

#### NORMAL (2 tables)
- `ue_events` — latest, PK `imsiOrSupi`. Write = upsert via `INSERT … ON
  CONFLICT (imsiOrSupi) DO UPDATE` (latest-wins by `updatedAt`).
- `ue_events_history` — append log, **hash-partitioned by `imsiOrSupi`**
  (per-IMSI history query prunes to one partition). Surrogate id from a
  sequence (pooled), not IDENTITY.
- Write path: upsert latest + insert history, single transaction.

#### CQRS / read-write separation (5 tables, async projection)
- Write side: `cqrs_write_latest` (PK imsi), `cqrs_write_history`
  (hash-partitioned by imsi).
- `cqrs_outbox` — transactional outbox: one row per write, carrying the full
  event payload + `imsiOrSupi` + monotonic id, plus a `status`/claim column.
- Read side (denormalized, covering indexes tuned to the two query shapes):
  `cqrs_read_latest` (PK imsi), `cqrs_read_history` (hash-partitioned by imsi).
- Write path: insert into write tables **+** outbox row, single transaction.
  Returns immediately.
- **Projector** (`CqrsProjectorService`, `@Scheduled`): drains outbox in batches
  using `SELECT … FOR UPDATE SKIP LOCKED LIMIT batch`, upserts `cqrs_read_latest`
  (`ON CONFLICT` latest-wins), inserts `cqrs_read_history`, deletes processed
  outbox rows. Eventual consistency; backlog observable.
- Read path: **read tables only**.

### 3.2 Pagination strategies (apply to both models)

- **OFFSET**: numbered pages. Total is **estimated** via `pg_class.reltuples`
  (instant) — no per-page `count(*)`. Still O(offset) by design; this is the
  control that demonstrates degradation.
- **KEYSET (seek)**: cursor on `(updatedAt DESC, id DESC)`.
  `WHERE (updatedAt, id) < (:lastUpdatedAt, :lastId) ORDER BY updatedAt DESC,
  id DESC LIMIT size+1`. Fetch `size+1` to compute `hasNext` and `next_cursor`.
  O(1) at any depth.
- History queries add `imsiOrSupi = :imsi` predicate; same two strategies.

### 3.3 Indexes (covering, aligned to cursor)

- Latest tables: `(updatedAt DESC, id DESC)` (+ `id`/imsi as needed for INCLUDE).
- History tables: `(imsiOrSupi, updatedAt DESC, id DESC)`.
- Read-side history index covers the exact columns the projection serves
  (index-only scan target).

### 3.4 Bulk load — Postgres COPY

`BulkLoader` streams generated rows through `CopyManager.copyIn("COPY … FROM
STDIN")` (unwrap the Hikari connection to `BaseConnection`). Loads millions in
seconds, bypassing JPA. Generation:

1. Build N random `UeEvent`s (reuse existing generator logic, extracted from
   `DataGeneratorService`).
2. COPY into NORMAL tables (latest = dedup last-per-imsi; history = all).
3. COPY into CQRS write tables + outbox.
4. Return elapsed ms per model (`normalMs`, `cqrsWriteMs`). Read side fills
   asynchronously via the projector; backlog visible in UI.

`latest`/`read_latest` after a bulk load: COPY into a staging set then upsert,
or COPY history and derive latest with `DISTINCT ON (imsiOrSupi) … ORDER BY
updatedAt DESC`. Implementation detail deferred to plan; requirement: latest
tables reflect the newest event per imsi after generation.

### 3.5 Schema management — Flyway

Hash-partitioned tables and the CQRS tables cannot be created by
`ddl-auto=update`. Switch to **Flyway** migrations with `ddl-auto=validate`.
JPA entities map to Flyway-created tables. Partitioned tables created with
`PARTITION BY HASH (imsiOrSupi)` + a fixed number of partitions (e.g. 8).

### 3.6 Config changes

- JDBC URL: append `?reWriteBatchedInserts=true`.
- HikariCP: explicit `maximum-pool-size` sized for concurrent generate + read
  (e.g. 10–20).
- `ddl-auto=validate`; add Flyway dependency.
- Remove boot bulk-gen `CommandLineRunner` and the every-2s `@Scheduled`
  generator (clean benchmark). Keep the projector scheduler.

## 4. Backend structure

```
EventModel (enum: NORMAL, CQRS)
PaginationStrategy (enum: OFFSET, KEYSET)
EventStore (interface)
  PageResult generate(int count)            // returns elapsed + rows
  PageResult getLatest(strategy, page|cursor, size)
  PageResult getHistory(imsi, strategy, page|cursor, size)
NormalEventStore implements EventStore
CqrsEventStore  implements EventStore
CqrsProjectorService (@Scheduled)
BulkLoader (COPY via CopyManager)
UeEventController -> Map<EventModel, EventStore> dispatch; measures elapsed
UeEventAdapter (reused; extended for new entities)
```

- Controller resolves `model` + `strategy` params, dispatches to the store,
  wraps the call in `System.nanoTime()` for `query_time_ms`.
- Each store owns its own repositories/SQL. Keyset queries use native SQL or
  Spring Data with explicit `WHERE` + `LIMIT`.

## 5. API

| Method | Path | Params | Returns |
|--------|------|--------|---------|
| POST | `/api/generate` | `count` | JSON `{normalMs, cqrsWriteMs, count}` |
| GET | `/api/events/latest` | `model`, `strategy`, `page` or `cursor`, `size` | protobuf `UeEventPageResponse` |
| GET | `/api/events/{imsi}/history` | `model`, `strategy`, `page` or `cursor`, `size` | protobuf `UeEventPageResponse` |
| GET | `/api/projection/status` | — | JSON `{outboxBacklog}` |

Proto additions to `UeEventPageResponse`:
- `int64 query_time_ms = 5;`
- `string next_cursor = 6;` (keyset; empty when no more)
- `bool has_next = 7;`

OFFSET responses keep `total_pages` / `total_elements` (estimated). KEYSET
responses populate `next_cursor` / `has_next` and leave totals 0/estimated.

`cursor` encoding: opaque base64 of `updatedAtEpochMicros + ":" + id` (and imsi
for history is implied by path). Decoded server-side.

## 6. Frontend

- **Model toggle**: `Normal` | `CQRS (Read-Write Sep)`.
- **Strategy toggle**: `Offset` | `Keyset`.
- **Count input + Generate** button → `Generated N — normal: Xms · cqrs-write:
  Yms`.
- Latest table + History dialog read the selected model+strategy.
  - Offset → numbered `Pagination`.
  - Keyset → `Prev` / `Next` cursor nav (stack of cursors for Prev).
  - Each shows a **query-time badge** (`Latest: 12 ms`, `History: 4 ms`).
- **CQRS projection-backlog badge** (polls `/api/projection/status`) — shows
  eventual-consistency lag.
- Replace auto-refresh `setInterval` with a manual **Refresh** button (removes
  timing noise).

## 7. Testing

- Projector drains outbox correctly; read tables converge to write tables.
- `FOR UPDATE SKIP LOCKED` prevents double-projection under concurrent runs.
- Offset and keyset return the same rows in the same order for the same data.
- Both models hold identical data for the same generated input.
- `query_time_ms` populated and > 0 on all read paths.
- Cursor encode/decode round-trips; keyset boundary (last page) sets
  `has_next=false`.
- Bulk COPY load of a moderate N (e.g. 100k) completes and counts match.

## 8. Migration / cleanup

- Existing `UeEventService` logic folds into `NormalEventStore`.
- `DataGeneratorService` random-event builder extracted to a shared component;
  its `CommandLineRunner` + `@Scheduled` generation removed.
- New Flyway baseline migration creates all tables (normal + CQRS + partitions)
  and indexes.

## 9. Open implementation details (resolved in plan)

- Exact COPY-to-latest derivation (staging vs `DISTINCT ON`).
- Number of hash partitions (default 8).
- Prev-cursor handling in UI (client cursor stack vs bidirectional cursors).
