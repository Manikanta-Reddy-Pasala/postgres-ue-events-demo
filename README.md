# UE Events — Postgres Pagination Benchmark

Tests how far Postgres pagination holds up over millions of UE (User Equipment) events,
comparing **two persistence models** and **two pagination strategies** side by side — with
server-measured query time shown in the UI.

## What it does

- **Generate** N events (UI button) → loaded into **both** models via Postgres `COPY`.
- **Clear** all data (UI button) → truncates both models.
- Browse latest events + per-IMSI history; every read shows its **query time (ms)**.
- Flip **model** and **pagination strategy** live to compare performance.

## Two models

- **NORMAL** — 2 tables. Write updates both in one transaction.
- **CQRS (read-write separation)** — 5 tables. Writes hit write-tables + an outbox; a
  background projector copies into read-tables; reads only touch read-tables.

## Two pagination strategies

- **OFFSET** — numbered pages, estimated total (no per-page `count(*)`). O(offset) — degrades deep.
- **KEYSET** — cursor on `(updated_at, id)`. O(1) at any depth (Next/Prev).

## Stack

- **Backend** — Spring Boot 3.2 / Java 21 / Postgres 16, Flyway, protobuf transport.
- **Frontend** — React 18 + MUI 5, protobuf decode.
- **DB** — Postgres 16, history tables hash-partitioned by IMSI.

## Run

```bash
./run.sh         # build + start everything, wait until ready, print URLs
./run.sh down    # stop + remove containers and data volume
./run.sh logs    # tail logs
```

- UI  → http://localhost:3000
- API → http://localhost:8080/api

## API

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/generate?count=N` | generate N into both models → `{normalMs, cqrsWriteMs}` |
| POST | `/api/clear` | truncate both models |
| GET | `/api/events/latest?model=&strategy=&page\|cursor&size=` | latest page (protobuf) |
| GET | `/api/events/{imsi}/history?model=&strategy=&page\|cursor&size=` | per-IMSI history (protobuf) |
| GET | `/api/projection/status` | CQRS outbox backlog |

`model` = `NORMAL` \| `CQRS` · `strategy` = `OFFSET` \| `KEYSET`

## Architecture — overall

```
 ┌──────────────┐   protobuf / JSON    ┌───────────────────────────┐   JDBC   ┌────────────┐
 │  React UI    │ ───────────────────► │  Spring Boot backend      │ ───────► │ Postgres16 │
 │  :3000       │ ◄─────────────────── │  :8080  (EventStore x2)   │ ◄─────── │            │
 │ toggles +    │   query_time_ms      │  controller → store map   │   COPY   │ partitions │
 │ Gen / Clear  │                      │  + CQRS projector (sched) │          │            │
 └──────────────┘                      └───────────────────────────┘          └────────────┘
```

## Data mapping layer (proto ⇄ Postgres)

Events travel as **protobuf** over HTTP (the small generate/clear/status responses are JSON).
Conversion to/from the DB is split by direction — note the save path does **not** use the adapter:

```
 SAVE (write):   UeEvent (proto) ──► CopySupport.toCsv ──► CSV ──► COPY into Postgres
 FETCH (read):   Postgres ResultSet ──► UeEventAdapter.fromRow ──► UeEvent (proto) ──► HTTP (protobuf)
```

- `CopySupport.toCsv` — proto getters → CSV rows for bulk `COPY` (write side).
- `UeEventAdapter.fromRow` — `ResultSet` → proto, used by `SeekQuery` (read side only).
- `CursorCodec` — keyset cursor ⇄ opaque base64 token (pagination, not row data).

Full round trip (note: DB stores plain relational columns — no JSON):

```
  UI (React)             Backend (Spring Boot)                 Postgres
  ──────────             ─────────────────────                 ────────
                 proto                       proto → CSV
  Generate  ───────────►  controller  ───────────────────►  COPY ─► columns
            (HTTP body)   GenerationService  (CopySupport)

                 proto                       ResultSet → proto
  Browse    ◄───────────  controller  ◄───────────────────  SELECT ◄ columns
            (HTTP body)   SeekQuery         (UeEventAdapter)

  UI always sends/receives protobuf.  DB I/O is CSV-out / rows-in over
  relational columns.  JSON only on generate/clear/status responses.
```

## Architecture — NORMAL model (2 tables)

```
 generate ─► COPY ─► staging_events
                        │  (one transaction)
                        ├─► INSERT  ue_events_history   (append log, hash-partitioned by IMSI)
                        └─► UPSERT  ue_events           (latest row per IMSI)

 read  ◄── ue_events            (latest list)
 read  ◄── ue_events_history    (per-IMSI history)
```

## Architecture — CQRS model (5 tables, async projection)

```
                WRITE SIDE                          READ SIDE
 generate ─► COPY ─► staging_events
                 │ (one txn)
                 ├─► cqrs_write_history ─┐
                 ├─► cqrs_write_latest   │
                 └─► cqrs_outbox  ───────┘
                          │
                          │  CqrsProjectorService  (@Scheduled, FOR UPDATE SKIP LOCKED)
                          │  join write_history on PK (imsi, id)
                          ▼
                 ┌────────────────────────────┐
                 ├─► cqrs_read_history         │ ◄── read (per-IMSI history)
                 └─► cqrs_read_latest          │ ◄── read (latest list)
                 └────────────────────────────┘

 reads only ever touch the read side; backlog = unprojected outbox rows (shown in UI)
```

## Best-practice techniques applied

- Keyset/seek pagination (flat at any depth) + covering composite indexes.
- Estimated counts via `pg_class.reltuples` (no per-page `count(*)`).
- Bulk load via `COPY` into a staging temp table, then set-based upsert.
- Hash partitioning of history tables by IMSI (per-IMSI queries prune to one partition).
- Transactional outbox + `FOR UPDATE SKIP LOCKED` projector (no double-projection).
- Flyway-managed schema (`ddl-auto=validate`), sequence ids (not IDENTITY), `reWriteBatchedInserts`.

## Docs

- Design spec → `docs/superpowers/specs/`
- Implementation plan → `docs/superpowers/plans/`
