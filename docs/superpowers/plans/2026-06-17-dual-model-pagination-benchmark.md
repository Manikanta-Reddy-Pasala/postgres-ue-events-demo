# Dual-Model Pagination Benchmark Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the UE-events demo into a benchmark that compares two persistence models (NORMAL 2-table vs CQRS 5-table async-projection) across two pagination strategies (OFFSET vs KEYSET) over millions of rows, with server-measured timing shown in the UI.

**Architecture:** Spring Boot 3.2 / Java 21 / Postgres 16. Two `EventStore` implementations behind an enum-keyed dispatch map. Bulk load via Postgres `COPY` into a staging temp table, then SQL upsert/insert into model tables (history hash-partitioned by IMSI). CQRS read side filled by a `@Scheduled` outbox projector using `FOR UPDATE SKIP LOCKED`. Flyway owns schema (`ddl-auto=validate`). React frontend adds model + strategy toggles, a generate-with-count control, and query-time badges. Protobuf transport extended with timing + cursor fields.

**Tech Stack:** Spring Boot 3.2.3, Spring Data JPA, Postgres 16, Flyway, Testcontainers (tests), protobuf-java 3.25.3, React 18 + MUI 5 + protobufjs.

---

## File Structure

**Backend — create:**
- `backend/src/main/resources/db/migration/V1__benchmark_schema.sql` — all tables, partitions, indexes, sequences.
- `backend/src/main/java/com/example/ue_tracker/model/EventModel.java` — enum NORMAL, CQRS.
- `backend/src/main/java/com/example/ue_tracker/model/PaginationStrategy.java` — enum OFFSET, KEYSET.
- `backend/src/main/java/com/example/ue_tracker/pagination/Cursor.java` — record.
- `backend/src/main/java/com/example/ue_tracker/pagination/CursorCodec.java` — encode/decode.
- `backend/src/main/java/com/example/ue_tracker/store/PageResult.java` — read result carrier.
- `backend/src/main/java/com/example/ue_tracker/store/EventStore.java` — interface.
- `backend/src/main/java/com/example/ue_tracker/store/NormalEventStore.java`.
- `backend/src/main/java/com/example/ue_tracker/store/CqrsEventStore.java`.
- `backend/src/main/java/com/example/ue_tracker/store/CopySupport.java` — shared COPY/staging helper.
- `backend/src/main/java/com/example/ue_tracker/generator/EventFactory.java` — random event builder.
- `backend/src/main/java/com/example/ue_tracker/generator/GenerationService.java` — chunked orchestration.
- `backend/src/main/java/com/example/ue_tracker/cqrs/CqrsProjectorService.java` — @Scheduled projector.
- `backend/src/main/java/com/example/ue_tracker/cqrs/OutboxRepository.java`.

**Backend — modify:**
- `proto/ue_event.proto` — add fields to `UeEventPageResponse`.
- `backend/src/main/resources/application.properties` — Flyway, JDBC, Hikari.
- `backend/build.gradle` — Flyway + Testcontainers deps.
- `backend/src/main/java/com/example/ue_tracker/controller/UeEventController.java` — model/strategy params, timing, generate, projection-status endpoints.
- `backend/src/main/java/com/example/ue_tracker/adapter/UeEventAdapter.java` — `toProto(ResultSet)` / row mapping reused by stores.

**Backend — delete:**
- `backend/src/main/java/com/example/ue_tracker/service/DataGeneratorService.java` (auto-gen removed; logic moves to `EventFactory`).
- `backend/src/main/java/com/example/ue_tracker/service/UeEventService.java` (folds into `NormalEventStore`).
- Old `UeEventEntity`, `UeEventHistoryEntity`, `UeEventRepository`, `UeEventHistoryRepository` — replaced (stores use JDBC + native SQL, not JPA repos). Keep proto-generated classes.

**Frontend — modify:**
- `frontend/src/proto.js`, `frontend/src/proto.d.ts` — regenerate from updated proto.
- `frontend/src/api.ts` — model/strategy params, generate, projection status.
- `frontend/src/Dashboard.tsx` — toggles, count input, query-time badges, cursor nav.

---

## Task 1: Extend protobuf transport

**Files:**
- Modify: `proto/ue_event.proto`

- [ ] **Step 1: Add timing + cursor fields to `UeEventPageResponse`**

Replace the `UeEventPageResponse` message (lines 58-63) with:

```proto
message UeEventPageResponse {
  repeated UeEvent events = 1;
  int32 total_pages = 2;
  int64 total_elements = 3;
  int32 current_page = 4;
  int64 query_time_ms = 5;
  string next_cursor = 6; // keyset: opaque cursor for next page; empty when none
  bool has_next = 7;
}
```

- [ ] **Step 2: Verify protobuf compiles via Gradle**

Run: `cd backend && ./gradlew generateProto`
Expected: BUILD SUCCESSFUL; regenerated sources under `build/generated/source/proto/main/java`.

- [ ] **Step 3: Commit**

```bash
git add proto/ue_event.proto
git commit -m "feat: add timing and cursor fields to UeEventPageResponse proto"
```

---

## Task 2: Build dependencies and config

**Files:**
- Modify: `backend/build.gradle`
- Modify: `backend/src/main/resources/application.properties`

- [ ] **Step 1: Add Flyway + Testcontainers to `build.gradle`**

In the `dependencies { … }` block add:

```gradle
	implementation 'org.flywaydb:flyway-core'
	implementation 'org.flywaydb:flyway-database-postgresql'
	testImplementation 'org.springframework.boot:spring-boot-testcontainers'
	testImplementation 'org.testcontainers:postgresql'
	testImplementation 'org.testcontainers:junit-jupiter'
```

- [ ] **Step 2: Update `application.properties`**

Replace the whole file with:

```properties
spring.datasource.url=jdbc:postgresql://postgres:5432/uetracker?reWriteBatchedInserts=true
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# Hikari sized for concurrent generate + read
spring.datasource.hikari.maximum-pool-size=16
spring.datasource.hikari.minimum-idle=4

# Flyway owns schema; JPA only validates
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.jpa.show-sql=false

# Projector cadence (ms)
benchmark.projector.fixed-delay=1000
benchmark.projector.batch-size=2000
benchmark.generation.chunk-size=50000
```

- [ ] **Step 3: Verify dependencies resolve**

Run: `cd backend && ./gradlew dependencies --configuration runtimeClasspath | grep -i flyway`
Expected: lists `flyway-core` and `flyway-database-postgresql`.

- [ ] **Step 4: Commit**

```bash
git add backend/build.gradle backend/src/main/resources/application.properties
git commit -m "build: add Flyway + Testcontainers, tune JDBC/Hikari, ddl-auto=validate"
```

---

## Task 3: Flyway schema migration

**Files:**
- Create: `backend/src/main/resources/db/migration/V1__benchmark_schema.sql`

Column naming: snake_case. History tables hash-partitioned by `imsi_or_supi` into 8 partitions. Surrogate ids from sequences (pooled, never IDENTITY).

- [ ] **Step 1: Write the migration**

```sql
-- ============ shared sequences ============
CREATE SEQUENCE ue_events_history_seq;
CREATE SEQUENCE cqrs_write_history_seq;
CREATE SEQUENCE cqrs_read_history_seq;
CREATE SEQUENCE cqrs_outbox_seq;

-- ============ column macro (repeated inline) ============
-- All event tables share these event columns:
--   imei, msisdn, guti, tmsi, rssi, action_taken, reject_cause, rat,
--   frequency_band, arfcn, tracking_area_code, downlink_band_width,
--   plmn_mcc, plmn_mnc, provider_name, mission_id, sensor_id, subsystem_id,
--   trx_command_id, created_at, updated_at, country_iso_alpha2, country_name,
--   target, capture_count, timing_advance, distance_in_meters

-- ============ NORMAL: latest ============
CREATE TABLE ue_events (
    imsi_or_supi VARCHAR(64) PRIMARY KEY,
    imei VARCHAR(32), msisdn VARCHAR(32), guti VARCHAR(64), tmsi VARCHAR(32),
    rssi INT, action_taken VARCHAR(32), reject_cause INT, rat VARCHAR(32),
    frequency_band INT, arfcn INT, tracking_area_code INT, downlink_band_width VARCHAR(32),
    plmn_mcc INT, plmn_mnc INT, provider_name VARCHAR(64), mission_id VARCHAR(64),
    sensor_id VARCHAR(64), subsystem_id VARCHAR(64), trx_command_id VARCHAR(64),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ,
    country_iso_alpha2 VARCHAR(4), country_name VARCHAR(64), target BOOLEAN,
    capture_count INT, timing_advance INT, distance_in_meters INT
);
CREATE INDEX idx_ue_events_seek ON ue_events (updated_at DESC, imsi_or_supi DESC);

-- ============ NORMAL: history (hash-partitioned by imsi) ============
CREATE TABLE ue_events_history (
    id BIGINT NOT NULL DEFAULT nextval('ue_events_history_seq'),
    imsi_or_supi VARCHAR(64) NOT NULL,
    imei VARCHAR(32), msisdn VARCHAR(32), guti VARCHAR(64), tmsi VARCHAR(32),
    rssi INT, action_taken VARCHAR(32), reject_cause INT, rat VARCHAR(32),
    frequency_band INT, arfcn INT, tracking_area_code INT, downlink_band_width VARCHAR(32),
    plmn_mcc INT, plmn_mnc INT, provider_name VARCHAR(64), mission_id VARCHAR(64),
    sensor_id VARCHAR(64), subsystem_id VARCHAR(64), trx_command_id VARCHAR(64),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ,
    country_iso_alpha2 VARCHAR(4), country_name VARCHAR(64), target BOOLEAN,
    capture_count INT, timing_advance INT, distance_in_meters INT,
    PRIMARY KEY (imsi_or_supi, id)
) PARTITION BY HASH (imsi_or_supi);
DO $$ BEGIN
  FOR i IN 0..7 LOOP
    EXECUTE format('CREATE TABLE ue_events_history_p%s PARTITION OF ue_events_history FOR VALUES WITH (MODULUS 8, REMAINDER %s)', i, i);
  END LOOP;
END $$;
CREATE INDEX idx_ue_history_seek ON ue_events_history (imsi_or_supi, updated_at DESC, id DESC);

-- ============ CQRS write: latest ============
CREATE TABLE cqrs_write_latest (LIKE ue_events INCLUDING ALL);

-- ============ CQRS write: history (partitioned) ============
CREATE TABLE cqrs_write_history (
    id BIGINT NOT NULL DEFAULT nextval('cqrs_write_history_seq'),
    imsi_or_supi VARCHAR(64) NOT NULL,
    imei VARCHAR(32), msisdn VARCHAR(32), guti VARCHAR(64), tmsi VARCHAR(32),
    rssi INT, action_taken VARCHAR(32), reject_cause INT, rat VARCHAR(32),
    frequency_band INT, arfcn INT, tracking_area_code INT, downlink_band_width VARCHAR(32),
    plmn_mcc INT, plmn_mnc INT, provider_name VARCHAR(64), mission_id VARCHAR(64),
    sensor_id VARCHAR(64), subsystem_id VARCHAR(64), trx_command_id VARCHAR(64),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ,
    country_iso_alpha2 VARCHAR(4), country_name VARCHAR(64), target BOOLEAN,
    capture_count INT, timing_advance INT, distance_in_meters INT,
    PRIMARY KEY (imsi_or_supi, id)
) PARTITION BY HASH (imsi_or_supi);
DO $$ BEGIN
  FOR i IN 0..7 LOOP
    EXECUTE format('CREATE TABLE cqrs_write_history_p%s PARTITION OF cqrs_write_history FOR VALUES WITH (MODULUS 8, REMAINDER %s)', i, i);
  END LOOP;
END $$;
CREATE INDEX idx_cqrs_write_history_seek ON cqrs_write_history (imsi_or_supi, updated_at DESC, id DESC);

-- ============ CQRS read: latest (denormalized, covering seek) ============
CREATE TABLE cqrs_read_latest (LIKE ue_events INCLUDING ALL);
CREATE INDEX idx_cqrs_read_latest_seek ON cqrs_read_latest (updated_at DESC, imsi_or_supi DESC);

-- ============ CQRS read: history (partitioned) ============
CREATE TABLE cqrs_read_history (
    id BIGINT NOT NULL DEFAULT nextval('cqrs_read_history_seq'),
    imsi_or_supi VARCHAR(64) NOT NULL,
    imei VARCHAR(32), msisdn VARCHAR(32), guti VARCHAR(64), tmsi VARCHAR(32),
    rssi INT, action_taken VARCHAR(32), reject_cause INT, rat VARCHAR(32),
    frequency_band INT, arfcn INT, tracking_area_code INT, downlink_band_width VARCHAR(32),
    plmn_mcc INT, plmn_mnc INT, provider_name VARCHAR(64), mission_id VARCHAR(64),
    sensor_id VARCHAR(64), subsystem_id VARCHAR(64), trx_command_id VARCHAR(64),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ,
    country_iso_alpha2 VARCHAR(4), country_name VARCHAR(64), target BOOLEAN,
    capture_count INT, timing_advance INT, distance_in_meters INT,
    PRIMARY KEY (imsi_or_supi, id)
) PARTITION BY HASH (imsi_or_supi);
DO $$ BEGIN
  FOR i IN 0..7 LOOP
    EXECUTE format('CREATE TABLE cqrs_read_history_p%s PARTITION OF cqrs_read_history FOR VALUES WITH (MODULUS 8, REMAINDER %s)', i, i);
  END LOOP;
END $$;
CREATE INDEX idx_cqrs_read_history_seek ON cqrs_read_history (imsi_or_supi, updated_at DESC, id DESC);

-- ============ CQRS outbox ============
CREATE TABLE cqrs_outbox (
    seq BIGINT PRIMARY KEY DEFAULT nextval('cqrs_outbox_seq'),
    imsi_or_supi VARCHAR(64) NOT NULL,
    write_history_id BIGINT NOT NULL,
    created_at_ts TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_cqrs_outbox_seq ON cqrs_outbox (seq);
```

- [ ] **Step 2: Verify migration applies against a real Postgres**

Run (requires the docker postgres from `run.sh`, or a local one on 5432):
```bash
cd backend && ./gradlew flywayInfo -Dflyway.url=jdbc:postgresql://localhost:5432/uetracker -Dflyway.user=postgres -Dflyway.password=postgres 2>/dev/null || echo "verify in Task 5 integration test instead"
```
Expected: lists `V1` as pending, or fall through to the Task 5 integration test which boots Flyway via Testcontainers.

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/resources/db/migration/V1__benchmark_schema.sql
git commit -m "feat: Flyway schema - normal + CQRS tables, hash partitions, seek indexes"
```

---

## Task 4: Enums, Cursor, PageResult, EventStore interface

**Files:**
- Create: `backend/src/main/java/com/example/ue_tracker/model/EventModel.java`
- Create: `backend/src/main/java/com/example/ue_tracker/model/PaginationStrategy.java`
- Create: `backend/src/main/java/com/example/ue_tracker/pagination/Cursor.java`
- Create: `backend/src/main/java/com/example/ue_tracker/pagination/CursorCodec.java`
- Create: `backend/src/main/java/com/example/ue_tracker/store/PageResult.java`
- Create: `backend/src/main/java/com/example/ue_tracker/store/EventStore.java`
- Test: `backend/src/test/java/com/example/ue_tracker/pagination/CursorCodecTest.java`

- [ ] **Step 1: Write the failing CursorCodec test**

```java
package com.example.ue_tracker.pagination;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class CursorCodecTest {

    private final CursorCodec codec = new CursorCodec();

    @Test
    void roundTripsUpdatedAtAndId() {
        Instant ts = Instant.parse("2026-06-17T10:15:30.123456Z");
        String encoded = codec.encode(new Cursor(ts, 9876543210L));
        Cursor decoded = codec.decode(encoded);
        assertEquals(ts, decoded.updatedAt());
        assertEquals(9876543210L, decoded.id());
    }

    @Test
    void decodesNullOrBlankAsNull() {
        assertNull(codec.decode(null));
        assertNull(codec.decode(""));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend && ./gradlew test --tests CursorCodecTest`
Expected: FAIL — `Cursor` / `CursorCodec` do not exist.

- [ ] **Step 3: Write the enums**

`EventModel.java`:
```java
package com.example.ue_tracker.model;

public enum EventModel { NORMAL, CQRS }
```

`PaginationStrategy.java`:
```java
package com.example.ue_tracker.model;

public enum PaginationStrategy { OFFSET, KEYSET }
```

- [ ] **Step 4: Write Cursor + CursorCodec**

`Cursor.java`:
```java
package com.example.ue_tracker.pagination;

import java.time.Instant;

public record Cursor(Instant updatedAt, long id) {}
```

`CursorCodec.java`:
```java
package com.example.ue_tracker.pagination;

import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Component
public class CursorCodec {

    public String encode(Cursor c) {
        long micros = c.updatedAt().getEpochSecond() * 1_000_000L + c.updatedAt().getNano() / 1_000L;
        String raw = micros + ":" + c.id();
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public Cursor decode(String token) {
        if (token == null || token.isBlank()) return null;
        String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        int sep = raw.indexOf(':');
        long micros = Long.parseLong(raw.substring(0, sep));
        long id = Long.parseLong(raw.substring(sep + 1));
        Instant ts = Instant.ofEpochSecond(micros / 1_000_000L, (micros % 1_000_000L) * 1_000L);
        return new Cursor(ts, id);
    }
}
```

- [ ] **Step 5: Write PageResult + EventStore**

`PageResult.java`:
```java
package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import java.util.List;

public record PageResult(
        List<UeEvent> events,
        long totalElements,   // estimated for OFFSET; 0 for KEYSET
        int totalPages,       // estimated for OFFSET; 0 for KEYSET
        int currentPage,      // OFFSET only
        String nextCursor,    // KEYSET only; empty otherwise
        boolean hasNext
) {}
```

`EventStore.java`:
```java
package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.model.EventModel;
import com.example.ue_tracker.model.PaginationStrategy;
import java.util.List;

public interface EventStore {
    EventModel model();

    /** Bulk-load a chunk; returns DB-side elapsed millis for this chunk. */
    long copyIn(List<UeEvent> chunk);

    PageResult getLatest(PaginationStrategy strategy, int page, String cursor, int size);

    PageResult getHistory(String imsi, PaginationStrategy strategy, int page, String cursor, int size);
}
```

- [ ] **Step 6: Run test to verify it passes**

Run: `cd backend && ./gradlew test --tests CursorCodecTest`
Expected: PASS (2 tests).

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/example/ue_tracker/model/EventModel.java \
        backend/src/main/java/com/example/ue_tracker/model/PaginationStrategy.java \
        backend/src/main/java/com/example/ue_tracker/pagination/ \
        backend/src/main/java/com/example/ue_tracker/store/PageResult.java \
        backend/src/main/java/com/example/ue_tracker/store/EventStore.java \
        backend/src/test/java/com/example/ue_tracker/pagination/CursorCodecTest.java
git commit -m "feat: EventModel/PaginationStrategy enums, cursor codec, store contract"
```

---

## Task 5: EventFactory (random event builder)

**Files:**
- Create: `backend/src/main/java/com/example/ue_tracker/generator/EventFactory.java`
- Test: `backend/src/test/java/com/example/ue_tracker/generator/EventFactoryTest.java`

Moves the random-event logic out of the deleted `DataGeneratorService`. Uses a bounded set of base IMSIs so per-IMSI history grows (keeps history pagination meaningful).

- [ ] **Step 1: Write the failing test**

```java
package com.example.ue_tracker.generator;

import com.example.ue.proto.UeEvent;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EventFactoryTest {

    private final EventFactory factory = new EventFactory();

    @Test
    void buildsRequestedCountWithRequiredFields() {
        List<UeEvent> batch = factory.randomBatch(100);
        assertEquals(100, batch.size());
        for (UeEvent e : batch) {
            assertFalse(e.getImsiOrSupi().isBlank());
            assertFalse(e.getUpdatedAt().isBlank());
            assertNotEquals(0, e.getActionTaken().getNumber() + 1); // enum set
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend && ./gradlew test --tests EventFactoryTest`
Expected: FAIL — `EventFactory` not found.

- [ ] **Step 3: Implement EventFactory**

```java
package com.example.ue_tracker.generator;

import com.example.ue.proto.ActionTaken;
import com.example.ue.proto.RatType;
import com.example.ue.proto.UeEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
public class EventFactory {

    private final Random random = new Random();
    private static final String[] RAT_TYPES = {"RAT_2G", "RAT_3G", "RAT_4G_LTE", "RAT_5G"};
    private static final String[] ACTIONS = {"REJECT", "ATTACH", "SILENT_CALL", "DETACH", "LOCATION_UPDATE", "PAGING"};
    private static final String[] PROVIDERS = {"Etisalat", "Du", "Vodafone", "O2", "T-Mobile"};
    private static final String[] COUNTRIES = {"ae", "us", "uk", "de", "fr"};
    private static final String[] COUNTRY_NAMES = {"United Arab Emirates", "United States", "United Kingdom", "Germany", "France"};
    private static final String[] BASE_IMSIS = {
            "424021478673415", "424021478673416", "424021478673417", "424021478673418", "424021478673419",
            "424021478673420", "424021478673421", "424021478673422", "424021478673423", "424021478673424"
    };

    public List<UeEvent> randomBatch(int count) {
        List<UeEvent> out = new ArrayList<>(count);
        for (int i = 0; i < count; i++) out.add(randomEvent());
        return out;
    }

    public UeEvent randomEvent() {
        String imsi = BASE_IMSIS[random.nextInt(BASE_IMSIS.length)];
        if (random.nextInt(10) > 7) {
            imsi = imsi.substring(0, 10) + String.format("%05d", random.nextInt(99999));
        }
        int c = random.nextInt(COUNTRIES.length);
        String now = Instant.now().toString();
        return UeEvent.newBuilder()
                .setImsiOrSupi(imsi)
                .setImei("35" + String.format("%013d", random.nextLong(10000000000000L)))
                .setMsisdn("+97150" + String.format("%07d", random.nextInt(10000000)))
                .setGuti("424023" + String.format("%014d", random.nextLong(100000000000000L)))
                .setTmsi(String.format("%010d", random.nextLong(10000000000L)))
                .setRssi(-50 - random.nextInt(50))
                .setActionTaken(ActionTaken.valueOf(ACTIONS[random.nextInt(ACTIONS.length)]))
                .setRejectCause(random.nextInt(20))
                .setRat(RatType.valueOf(RAT_TYPES[random.nextInt(RAT_TYPES.length)]))
                .setFrequencyBand(random.nextInt(40) + 1)
                .setArfcn(100 + random.nextInt(1000))
                .setTrackingAreaCode(40000 + random.nextInt(1000))
                .setDownlinkBandWidth(random.nextBoolean() ? "FIVE_MHZ" : "TEN_MHZ")
                .setPlmnMcc(424).setPlmnMnc(2)
                .setProviderName(PROVIDERS[random.nextInt(PROVIDERS.length)])
                .setMissionId(UUID.randomUUID().toString())
                .setSensorId("sensor-" + random.nextInt(100))
                .setSubsystemId("sys-l" + random.nextInt(5) + "-" + random.nextInt(10))
                .setTrxCommandId("cmd_" + random.nextInt(1000))
                .setCreatedAt(now).setUpdatedAt(now)
                .setCountryIsoAlpha2(COUNTRIES[c]).setCountryName(COUNTRY_NAMES[c])
                .setTarget(random.nextInt(100) > 95)
                .setCaptureCount(1 + random.nextInt(20))
                .setTimingAdvance(random.nextInt(100))
                .setDistanceInMeters(100 + random.nextInt(5000))
                .build();
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd backend && ./gradlew test --tests EventFactoryTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/example/ue_tracker/generator/EventFactory.java \
        backend/src/test/java/com/example/ue_tracker/generator/EventFactoryTest.java
git commit -m "feat: EventFactory random event builder"
```

---

## Task 6: CopySupport — COPY into staging + row helpers

**Files:**
- Create: `backend/src/main/java/com/example/ue_tracker/store/CopySupport.java`

Shared helper: COPY a chunk of `UeEvent`s into a per-call TEMP staging table, returning the staging table name and DB elapsed ms. Stores then run their own INSERT/UPSERT from staging.

- [ ] **Step 1: Implement CopySupport**

```java
package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * COPYs a chunk into a session-local TEMP table `staging_events` (created if absent,
 * truncated each call). Columns match the shared event column order used by all tables.
 */
@Component
public class CopySupport {

    // Canonical column order shared by COPY and downstream INSERT...SELECT.
    public static final String COLS =
        "imsi_or_supi,imei,msisdn,guti,tmsi,rssi,action_taken,reject_cause,rat," +
        "frequency_band,arfcn,tracking_area_code,downlink_band_width,plmn_mcc,plmn_mnc," +
        "provider_name,mission_id,sensor_id,subsystem_id,trx_command_id,created_at,updated_at," +
        "country_iso_alpha2,country_name,target,capture_count,timing_advance,distance_in_meters";

    private static final String CREATE_STAGING = """
        CREATE TEMP TABLE IF NOT EXISTS staging_events (
            imsi_or_supi text, imei text, msisdn text, guti text, tmsi text, rssi int,
            action_taken text, reject_cause int, rat text, frequency_band int, arfcn int,
            tracking_area_code int, downlink_band_width text, plmn_mcc int, plmn_mnc int,
            provider_name text, mission_id text, sensor_id text, subsystem_id text,
            trx_command_id text, created_at timestamptz, updated_at timestamptz,
            country_iso_alpha2 text, country_name text, target boolean, capture_count int,
            timing_advance int, distance_in_meters int
        ) ON COMMIT DROP
        """;

    private final DataSource dataSource;

    public CopySupport(DataSource dataSource) { this.dataSource = dataSource; }

    /**
     * Runs `work` inside one transaction that has `staging_events` populated with `chunk`.
     * The connection is shared so the TEMP table and the work see the same session.
     * Returns DB-elapsed millis of the COPY phase only.
     */
    public long withStaging(List<UeEvent> chunk, StagingWork work) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                st.execute(CREATE_STAGING);
                st.execute("TRUNCATE staging_events");
            }
            long start = System.nanoTime();
            CopyManager cm = conn.unwrap(PGConnection.class).getCopyAPI();
            cm.copyIn("COPY staging_events (" + COLS + ") FROM STDIN WITH (FORMAT csv)",
                    new StringReader(toCsv(chunk)));
            long copyMs = (System.nanoTime() - start) / 1_000_000L;
            work.run(conn);
            conn.commit();
            return copyMs;
        } catch (SQLException e) {
            throw new RuntimeException("COPY staging failed", e);
        }
    }

    @FunctionalInterface
    public interface StagingWork { void run(Connection conn) throws SQLException; }

    private String toCsv(List<UeEvent> chunk) {
        StringBuilder sb = new StringBuilder(chunk.size() * 256);
        for (UeEvent e : chunk) {
            sb.append(q(e.getImsiOrSupi())).append(',').append(q(e.getImei())).append(',')
              .append(q(e.getMsisdn())).append(',').append(q(e.getGuti())).append(',')
              .append(q(e.getTmsi())).append(',').append(e.getRssi()).append(',')
              .append(q(e.getActionTaken().name())).append(',').append(e.getRejectCause()).append(',')
              .append(q(e.getRat().name())).append(',').append(e.getFrequencyBand()).append(',')
              .append(e.getArfcn()).append(',').append(e.getTrackingAreaCode()).append(',')
              .append(q(e.getDownlinkBandWidth())).append(',').append(e.getPlmnMcc()).append(',')
              .append(e.getPlmnMnc()).append(',').append(q(e.getProviderName())).append(',')
              .append(q(e.getMissionId())).append(',').append(q(e.getSensorId())).append(',')
              .append(q(e.getSubsystemId())).append(',').append(q(e.getTrxCommandId())).append(',')
              .append(q(e.getCreatedAt())).append(',').append(q(e.getUpdatedAt())).append(',')
              .append(q(e.getCountryIsoAlpha2())).append(',').append(q(e.getCountryName())).append(',')
              .append(e.getTarget()).append(',').append(e.getCaptureCount()).append(',')
              .append(e.getTimingAdvance()).append(',').append(e.getDistanceInMeters())
              .append('\n');
        }
        return sb.toString();
    }

    private String q(String s) {
        if (s == null) return "";
        return '"' + s.replace("\"", "\"\"") + '"';
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `cd backend && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/example/ue_tracker/store/CopySupport.java
git commit -m "feat: CopySupport - COPY chunk into TEMP staging table"
```

---

## Task 7: Row→proto mapping helper

**Files:**
- Modify: `backend/src/main/java/com/example/ue_tracker/adapter/UeEventAdapter.java`

Stores read via `ResultSet`, not JPA. Add a `fromRow` mapper. Keep existing proto methods (still referenced by nothing after JPA removal — they may be deleted, but leaving them is harmless; this step only ADDS).

- [ ] **Step 1: Add `fromRow` to UeEventAdapter**

Add these imports at top:
```java
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
```

Add this method inside the class:
```java
    public UeEvent fromRow(ResultSet rs) throws SQLException {
        UeEvent.Builder b = UeEvent.newBuilder()
                .setImsiOrSupi(nz(rs.getString("imsi_or_supi")))
                .setImei(nz(rs.getString("imei")))
                .setMsisdn(nz(rs.getString("msisdn")))
                .setGuti(nz(rs.getString("guti")))
                .setTmsi(nz(rs.getString("tmsi")))
                .setRssi(rs.getInt("rssi"))
                .setRejectCause(rs.getInt("reject_cause"))
                .setFrequencyBand(rs.getInt("frequency_band"))
                .setArfcn(rs.getInt("arfcn"))
                .setTrackingAreaCode(rs.getInt("tracking_area_code"))
                .setDownlinkBandWidth(nz(rs.getString("downlink_band_width")))
                .setPlmnMcc(rs.getInt("plmn_mcc"))
                .setPlmnMnc(rs.getInt("plmn_mnc"))
                .setProviderName(nz(rs.getString("provider_name")))
                .setMissionId(nz(rs.getString("mission_id")))
                .setSensorId(nz(rs.getString("sensor_id")))
                .setSubsystemId(nz(rs.getString("subsystem_id")))
                .setTrxCommandId(nz(rs.getString("trx_command_id")))
                .setCreatedAt(tsToString(rs.getTimestamp("created_at")))
                .setUpdatedAt(tsToString(rs.getTimestamp("updated_at")))
                .setCountryIsoAlpha2(nz(rs.getString("country_iso_alpha2")))
                .setCountryName(nz(rs.getString("country_name")))
                .setTarget(rs.getBoolean("target"))
                .setCaptureCount(rs.getInt("capture_count"))
                .setTimingAdvance(rs.getInt("timing_advance"))
                .setDistanceInMeters(rs.getInt("distance_in_meters"));
        try { b.setActionTaken(ActionTaken.valueOf(nz(rs.getString("action_taken")))); }
        catch (IllegalArgumentException ex) { b.setActionTaken(ActionTaken.UNKNOWN_ACTION); }
        try { b.setRat(RatType.valueOf(nz(rs.getString("rat")))); }
        catch (IllegalArgumentException ex) { b.setRat(RatType.UNKNOWN_RAT); }
        return b.build();
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private static String tsToString(Timestamp t) { return t == null ? "" : t.toInstant().toString(); }
```

- [ ] **Step 2: Verify it compiles**

Run: `cd backend && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/example/ue_tracker/adapter/UeEventAdapter.java
git commit -m "feat: UeEventAdapter.fromRow ResultSet mapper"
```

---

## Task 8: NormalEventStore

**Files:**
- Create: `backend/src/main/java/com/example/ue_tracker/store/NormalEventStore.java`
- Test: `backend/src/test/java/com/example/ue_tracker/store/AbstractPostgresTest.java`
- Test: `backend/src/test/java/com/example/ue_tracker/store/NormalEventStoreTest.java`

- [ ] **Step 1: Write the Testcontainers base class**

```java
package com.example.ue_tracker.store;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public abstract class AbstractPostgresTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16")
                    .withUrlParam("reWriteBatchedInserts", "true");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        r.add("spring.flyway.enabled", () -> "true");
    }
}
```

- [ ] **Step 2: Write the failing NormalEventStore test**

```java
package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.generator.EventFactory;
import com.example.ue_tracker.model.PaginationStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class NormalEventStoreTest extends AbstractPostgresTest {

    @Autowired NormalEventStore store;
    @Autowired EventFactory factory;

    @Test
    void copyInLoadsHistoryAndLatestThenPaginatesBothWays() {
        List<UeEvent> batch = factory.randomBatch(500);
        long ms = store.copyIn(batch);
        assertTrue(ms >= 0);

        // OFFSET first page
        PageResult offset = store.getLatest(PaginationStrategy.OFFSET, 0, null, 50);
        assertEquals(50, offset.events().size());

        // KEYSET first page then follow cursor
        PageResult k1 = store.getLatest(PaginationStrategy.KEYSET, 0, null, 50);
        assertEquals(50, k1.events().size());
        assertTrue(k1.hasNext());
        assertFalse(k1.nextCursor().isBlank());
        PageResult k2 = store.getLatest(PaginationStrategy.KEYSET, 0, k1.nextCursor(), 50);
        assertFalse(k2.events().isEmpty());
        // pages must not overlap on the seek key
        assertNotEquals(k1.events().get(49).getUpdatedAt() + k1.events().get(49).getImsiOrSupi(),
                        k2.events().get(0).getUpdatedAt() + k2.events().get(0).getImsiOrSupi());

        // history for a known imsi returns rows
        String imsi = batch.get(0).getImsiOrSupi();
        PageResult hist = store.getHistory(imsi, PaginationStrategy.KEYSET, 0, null, 50);
        assertFalse(hist.events().isEmpty());
        assertTrue(hist.events().stream().allMatch(e -> e.getImsiOrSupi().equals(imsi)));
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `cd backend && ./gradlew test --tests NormalEventStoreTest`
Expected: FAIL — `NormalEventStore` not found.

- [ ] **Step 4: Implement NormalEventStore**

```java
package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.adapter.UeEventAdapter;
import com.example.ue_tracker.model.EventModel;
import com.example.ue_tracker.model.PaginationStrategy;
import com.example.ue_tracker.pagination.Cursor;
import com.example.ue_tracker.pagination.CursorCodec;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class NormalEventStore implements EventStore {

    private final CopySupport copy;
    private final UeEventAdapter adapter;
    private final CursorCodec codec;
    private final JdbcTemplate jdbc;
    private final DataSource dataSource;

    NormalEventStore(CopySupport copy, UeEventAdapter adapter, CursorCodec codec,
                     JdbcTemplate jdbc, DataSource dataSource) {
        this.copy = copy; this.adapter = adapter; this.codec = codec;
        this.jdbc = jdbc; this.dataSource = dataSource;
    }

    @Override public EventModel model() { return EventModel.NORMAL; }

    @Override
    public long copyIn(List<UeEvent> chunk) {
        return copy.withStaging(chunk, conn -> {
            try (Statement st = conn.createStatement()) {
                st.execute("INSERT INTO ue_events_history (" + CopySupport.COLS + ") " +
                           "SELECT " + CopySupport.COLS + " FROM staging_events");
                st.execute("""
                    INSERT INTO ue_events (%s)
                    SELECT DISTINCT ON (imsi_or_supi) %s FROM staging_events
                    ORDER BY imsi_or_supi, updated_at DESC
                    ON CONFLICT (imsi_or_supi) DO UPDATE SET
                      updated_at = EXCLUDED.updated_at, action_taken = EXCLUDED.action_taken,
                      rat = EXCLUDED.rat, rssi = EXCLUDED.rssi, provider_name = EXCLUDED.provider_name,
                      country_name = EXCLUDED.country_name, msisdn = EXCLUDED.msisdn,
                      distance_in_meters = EXCLUDED.distance_in_meters
                    WHERE EXCLUDED.updated_at >= ue_events.updated_at
                    """.formatted(CopySupport.COLS, CopySupport.COLS));
            }
        });
    }

    @Override
    public PageResult getLatest(PaginationStrategy strategy, int page, String cursor, int size) {
        return query("ue_events", null, "imsi_or_supi", strategy, page, cursor, size);
    }

    @Override
    public PageResult getHistory(String imsi, PaginationStrategy strategy, int page, String cursor, int size) {
        return query("ue_events_history", imsi, "id", strategy, page, cursor, size);
    }

    /** Shared query logic; tieKey is the seek tiebreaker column (imsi for latest, id for history). */
    private PageResult query(String table, String imsi, String tieKey,
                             PaginationStrategy strategy, int page, String cursorToken, int size) {
        boolean history = imsi != null;
        if (strategy == PaginationStrategy.OFFSET) {
            String sql = "SELECT * FROM " + table +
                    (history ? " WHERE imsi_or_supi = ?" : "") +
                    " ORDER BY updated_at DESC, " + tieKey + " DESC OFFSET ? LIMIT ?";
            List<UeEvent> rows = run(sql, ps -> {
                int i = 1;
                if (history) ps.setString(i++, imsi);
                ps.setInt(i++, page * size);
                ps.setInt(i, size + 1);
            });
            boolean hasNext = rows.size() > size;
            if (hasNext) rows = rows.subList(0, size);
            long total = estimateCount(table);
            int totalPages = (int) Math.max(1, Math.ceil((double) total / size));
            return new PageResult(rows, total, totalPages, page, "", hasNext);
        }
        // KEYSET
        Cursor c = codec.decode(cursorToken);
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(table).append(" WHERE 1=1");
        if (history) sql.append(" AND imsi_or_supi = ?");
        if (c != null) sql.append(" AND (updated_at, ").append(tieKey).append(") < (?, ?)");
        sql.append(" ORDER BY updated_at DESC, ").append(tieKey).append(" DESC LIMIT ?");
        List<UeEvent> rows = run(sql.toString(), ps -> {
            int i = 1;
            if (history) ps.setString(i++, imsi);
            if (c != null) { ps.setTimestamp(i++, Timestamp.from(c.updatedAt())); ps.setLong(i++, c.id()); }
            ps.setInt(i, size + 1);
        });
        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);
        String next = "";
        if (hasNext && !rows.isEmpty()) {
            UeEvent last = rows.get(rows.size() - 1);
            long tie = history ? lastId : 0L; // see note: history uses id; latest uses imsi-as-string seek
            next = codec.encode(new Cursor(Instant.parse(last.getUpdatedAt()),
                    history ? lastRowId.get() : 0L));
        }
        return new PageResult(rows, 0, 0, 0, next, hasNext);
    }

    // capture last row id for keyset cursor (history); latest seek uses (updated_at, imsi) — handled below
    private final ThreadLocal<Long> lastRowId = ThreadLocal.withInitial(() -> 0L);
    private long lastId; // unused placeholder retained for clarity; real value via lastRowId

    private List<UeEvent> run(String sql, PsSetter setter) {
        List<UeEvent> out = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(adapter.fromRow(rs));
                    try { lastRowId.set(rs.getLong("id")); } catch (SQLException ignore) { /* latest table */ }
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }

    private long estimateCount(String table) {
        Long est = jdbc.queryForObject(
                "SELECT reltuples::bigint FROM pg_class WHERE relname = ?", Long.class, table);
        return est == null || est < 0 ? 0 : est;
    }

    @FunctionalInterface private interface PsSetter { void set(PreparedStatement ps) throws SQLException; }
}
```

> **Implementation note for the engineer:** the keyset cursor needs the tiebreaker value of the *last returned row*. For history the tiebreaker is `id` (captured via `lastRowId`). For the `ue_events` latest table there is no `id`; its seek tiebreaker is `imsi_or_supi` (a string). Simplify by giving the latest cursor a string tiebreaker: change `Cursor` to carry the tiebreaker as a `String` for latest and `long` for history is over-engineering — instead, make the latest seek use only `updated_at` plus `imsi_or_supi` text. Concretely: in `query`, when `!history`, build the cursor from `(updated_at, imsi_or_supi)` and add a `CursorCodec.encodeLatest(Instant, String)` / `decodeLatest` pair. Add those two methods to `CursorCodec` (base64 of `micros:imsi`), and in `getLatest` use them. This keeps one seek key type per table. Update `CursorCodecTest` with a round-trip for the string variant.

- [ ] **Step 5: Add the latest-cursor methods to CursorCodec (resolves the note)**

Add to `CursorCodec`:
```java
    public String encodeLatest(Instant updatedAt, String imsi) {
        long micros = updatedAt.getEpochSecond() * 1_000_000L + updatedAt.getNano() / 1_000L;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((micros + ":" + imsi).getBytes(StandardCharsets.UTF_8));
    }

    public String[] decodeLatest(String token) { // returns [microsString, imsi] or null
        if (token == null || token.isBlank()) return null;
        String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        int sep = raw.indexOf(':');
        return new String[]{ raw.substring(0, sep), raw.substring(sep + 1) };
    }
```

Then in `NormalEventStore.query`, branch the KEYSET block: for `history` use `Cursor`/`id` as written; for latest, decode via `decodeLatest`, seek `(updated_at, imsi_or_supi) < (?, ?)`, and encode next via `encodeLatest(Instant.parse(last.getUpdatedAt()), last.getImsiOrSupi())`. Remove the `lastId`/`lastRowId` placeholders for the latest path. Keep `lastRowId` only for history.

- [ ] **Step 6: Run test to verify it passes**

Run: `cd backend && ./gradlew test --tests NormalEventStoreTest`
Expected: PASS. (First run pulls the `postgres:16` image.)

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/example/ue_tracker/store/NormalEventStore.java \
        backend/src/main/java/com/example/ue_tracker/pagination/CursorCodec.java \
        backend/src/test/java/com/example/ue_tracker/store/
git commit -m "feat: NormalEventStore - COPY load, offset + keyset pagination"
```

---

## Task 9: CqrsEventStore + outbox write path

**Files:**
- Create: `backend/src/main/java/com/example/ue_tracker/store/CqrsEventStore.java`
- Test: `backend/src/test/java/com/example/ue_tracker/store/CqrsEventStoreTest.java`

Write path: COPY staging → insert `cqrs_write_history` (returns generated ids), upsert `cqrs_write_latest`, insert `cqrs_outbox` rows referencing the new history ids. Read path: identical query logic to NormalEventStore but against `cqrs_read_*` tables. To avoid duplicating the query method, extract it.

- [ ] **Step 1: Extract the shared query into a helper** 

Create `backend/src/main/java/com/example/ue_tracker/store/SeekQuery.java` holding the `query(...)`, `run(...)`, `estimateCount(...)` logic from Task 8 (move it out of `NormalEventStore`, make it a `@Component` taking `DataSource`, `JdbcTemplate`, `UeEventAdapter`, `CursorCodec`). `NormalEventStore` and `CqrsEventStore` both delegate: `seek.query(latestTable, null, ...)` / `seek.query(historyTable, imsi, ...)`. Update `NormalEventStore` to call `seek` and drop its private query code. Re-run `NormalEventStoreTest` — expected PASS (behavior unchanged).

```java
package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.adapter.UeEventAdapter;
import com.example.ue_tracker.model.PaginationStrategy;
import com.example.ue_tracker.pagination.CursorCodec;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class SeekQuery {
    private final DataSource ds; private final JdbcTemplate jdbc;
    private final UeEventAdapter adapter; private final CursorCodec codec;

    SeekQuery(DataSource ds, JdbcTemplate jdbc, UeEventAdapter adapter, CursorCodec codec) {
        this.ds = ds; this.jdbc = jdbc; this.adapter = adapter; this.codec = codec;
    }

    public PageResult query(String table, String imsi, PaginationStrategy strategy,
                            int page, String cursorToken, int size) {
        boolean history = imsi != null;
        String tieKey = history ? "id" : "imsi_or_supi";
        if (strategy == PaginationStrategy.OFFSET) {
            String sql = "SELECT * FROM " + table + (history ? " WHERE imsi_or_supi = ?" : "")
                    + " ORDER BY updated_at DESC, " + tieKey + " DESC OFFSET ? LIMIT ?";
            long[] lastId = {0};
            List<UeEvent> rows = run(sql, ps -> { int i = 1; if (history) ps.setString(i++, imsi);
                ps.setInt(i++, page * size); ps.setInt(i, size + 1); }, lastId);
            boolean hasNext = rows.size() > size; if (hasNext) rows = rows.subList(0, size);
            long total = estimate(table);
            int totalPages = (int) Math.max(1, Math.ceil((double) total / size));
            return new PageResult(rows, total, totalPages, page, "", hasNext);
        }
        // KEYSET
        long[] lastId = {0};
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(table).append(" WHERE 1=1");
        if (history) sql.append(" AND imsi_or_supi = ?");
        Instant curTs = null; long curId = 0; String curImsi = null; boolean hasCursor = false;
        if (cursorToken != null && !cursorToken.isBlank()) {
            String[] parts = codec.decodeLatest(cursorToken); // [micros, tie]
            long micros = Long.parseLong(parts[0]);
            curTs = Instant.ofEpochSecond(micros / 1_000_000L, (micros % 1_000_000L) * 1_000L);
            if (history) curId = Long.parseLong(parts[1]); else curImsi = parts[1];
            hasCursor = true;
            sql.append(" AND (updated_at, ").append(tieKey).append(") < (?, ?)");
        }
        sql.append(" ORDER BY updated_at DESC, ").append(tieKey).append(" DESC LIMIT ?");
        final Instant cts = curTs; final long cid = curId; final String cimsi = curImsi; final boolean hc = hasCursor;
        List<UeEvent> rows = run(sql.toString(), ps -> {
            int i = 1; if (history) ps.setString(i++, imsi);
            if (hc) { ps.setTimestamp(i++, Timestamp.from(cts)); if (history) ps.setLong(i++, cid); else ps.setString(i++, cimsi); }
            ps.setInt(i, size + 1);
        }, lastId);
        boolean hasNext = rows.size() > size; if (hasNext) rows = rows.subList(0, size);
        String next = "";
        if (hasNext && !rows.isEmpty()) {
            UeEvent last = rows.get(rows.size() - 1);
            String tie = history ? String.valueOf(lastId[0]) : last.getImsiOrSupi();
            next = codec.encodeLatest(Instant.parse(last.getUpdatedAt()), tie);
        }
        return new PageResult(rows, 0, 0, 0, next, hasNext);
    }

    private interface PsSetter { void set(PreparedStatement ps) throws SQLException; }

    private List<UeEvent> run(String sql, PsSetter setter, long[] lastId) {
        List<UeEvent> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { out.add(adapter.fromRow(rs));
                    try { lastId[0] = rs.getLong("id"); } catch (SQLException ignore) {} }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }

    private long estimate(String table) {
        Long e = jdbc.queryForObject("SELECT reltuples::bigint FROM pg_class WHERE relname = ?", Long.class, table);
        return e == null || e < 0 ? 0 : e;
    }
}
```

Then trim `NormalEventStore` to:
```java
    @Override public PageResult getLatest(PaginationStrategy s, int page, String cursor, int size) {
        return seek.query("ue_events", null, s, page, cursor, size);
    }
    @Override public PageResult getHistory(String imsi, PaginationStrategy s, int page, String cursor, int size) {
        return seek.query("ue_events_history", imsi, s, page, cursor, size);
    }
```
(inject `SeekQuery seek` via constructor; remove the moved private methods and the `lastRowId`/`lastId` placeholders).

- [ ] **Step 2: Write the failing CqrsEventStore test**

```java
package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.generator.EventFactory;
import com.example.ue_tracker.model.PaginationStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CqrsEventStoreTest extends AbstractPostgresTest {

    @Autowired CqrsEventStore store;
    @Autowired EventFactory factory;
    @Autowired JdbcTemplate jdbc;

    @Test
    void writePathFillsWriteTablesAndOutboxButNotReadTables() {
        List<UeEvent> batch = factory.randomBatch(300);
        store.copyIn(batch);

        Long writeHist = jdbc.queryForObject("SELECT count(*) FROM cqrs_write_history", Long.class);
        Long outbox = jdbc.queryForObject("SELECT count(*) FROM cqrs_outbox", Long.class);
        Long readHist = jdbc.queryForObject("SELECT count(*) FROM cqrs_read_history", Long.class);

        assertEquals(300L, writeHist);
        assertEquals(300L, outbox);          // one outbox row per write
        assertEquals(0L, readHist);          // projector has not run
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `cd backend && ./gradlew test --tests CqrsEventStoreTest`
Expected: FAIL — `CqrsEventStore` not found.

- [ ] **Step 4: Implement CqrsEventStore**

```java
package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.model.EventModel;
import com.example.ue_tracker.model.PaginationStrategy;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.List;

@Component
public class CqrsEventStore implements EventStore {

    private final CopySupport copy;
    private final SeekQuery seek;

    CqrsEventStore(CopySupport copy, SeekQuery seek) { this.copy = copy; this.seek = seek; }

    @Override public EventModel model() { return EventModel.CQRS; }

    @Override
    public long copyIn(List<UeEvent> chunk) {
        return copy.withStaging(chunk, conn -> {
            try (Statement st = conn.createStatement()) {
                // insert into write history, capturing generated ids into a temp table
                st.execute("CREATE TEMP TABLE IF NOT EXISTS new_ids (id bigint, imsi_or_supi text) ON COMMIT DROP");
                st.execute("TRUNCATE new_ids");
                st.execute("WITH ins AS (" +
                        "INSERT INTO cqrs_write_history (" + CopySupport.COLS + ") " +
                        "SELECT " + CopySupport.COLS + " FROM staging_events RETURNING id, imsi_or_supi) " +
                        "INSERT INTO new_ids (id, imsi_or_supi) SELECT id, imsi_or_supi FROM ins");
                // upsert write latest
                st.execute("""
                    INSERT INTO cqrs_write_latest (%s)
                    SELECT DISTINCT ON (imsi_or_supi) %s FROM staging_events
                    ORDER BY imsi_or_supi, updated_at DESC
                    ON CONFLICT (imsi_or_supi) DO UPDATE SET
                      updated_at = EXCLUDED.updated_at, action_taken = EXCLUDED.action_taken,
                      rat = EXCLUDED.rat, rssi = EXCLUDED.rssi, provider_name = EXCLUDED.provider_name,
                      country_name = EXCLUDED.country_name, msisdn = EXCLUDED.msisdn,
                      distance_in_meters = EXCLUDED.distance_in_meters
                    WHERE EXCLUDED.updated_at >= cqrs_write_latest.updated_at
                    """.formatted(CopySupport.COLS, CopySupport.COLS));
                // one outbox row per written history row
                st.execute("INSERT INTO cqrs_outbox (imsi_or_supi, write_history_id) " +
                        "SELECT imsi_or_supi, id FROM new_ids");
            }
        });
    }

    @Override
    public PageResult getLatest(PaginationStrategy s, int page, String cursor, int size) {
        return seek.query("cqrs_read_latest", null, s, page, cursor, size);
    }

    @Override
    public PageResult getHistory(String imsi, PaginationStrategy s, int page, String cursor, int size) {
        return seek.query("cqrs_read_history", imsi, s, page, cursor, size);
    }
}
```

- [ ] **Step 5: Run both store tests**

Run: `cd backend && ./gradlew test --tests CqrsEventStoreTest --tests NormalEventStoreTest`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/example/ue_tracker/store/SeekQuery.java \
        backend/src/main/java/com/example/ue_tracker/store/NormalEventStore.java \
        backend/src/main/java/com/example/ue_tracker/store/CqrsEventStore.java \
        backend/src/test/java/com/example/ue_tracker/store/CqrsEventStoreTest.java
git commit -m "feat: CqrsEventStore write path + shared SeekQuery"
```

---

## Task 10: CQRS projector (outbox → read tables)

**Files:**
- Create: `backend/src/main/java/com/example/ue_tracker/cqrs/CqrsProjectorService.java`
- Test: `backend/src/test/java/com/example/ue_tracker/cqrs/CqrsProjectorServiceTest.java`

Drains outbox using `FOR UPDATE SKIP LOCKED`, copies the referenced write-history rows into `cqrs_read_history`, upserts `cqrs_read_latest`, deletes drained outbox rows. Exposes `int drainOnce(int batch)` for tests; `@Scheduled` calls it.

- [ ] **Step 1: Write the failing test**

```java
package com.example.ue_tracker.cqrs;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.generator.EventFactory;
import com.example.ue_tracker.store.CqrsEventStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import com.example.ue_tracker.store.AbstractPostgresTest;

class CqrsProjectorServiceTest extends AbstractPostgresTest {

    @Autowired CqrsEventStore store;
    @Autowired CqrsProjectorService projector;
    @Autowired EventFactory factory;
    @Autowired JdbcTemplate jdbc;

    @Test
    void drainsOutboxIntoReadTablesAndClearsBacklog() {
        List<UeEvent> batch = factory.randomBatch(250);
        store.copyIn(batch);
        assertEquals(250L, jdbc.queryForObject("SELECT count(*) FROM cqrs_outbox", Long.class));

        int drained = 0, loops = 0;
        while (jdbc.queryForObject("SELECT count(*) FROM cqrs_outbox", Long.class) > 0 && loops++ < 100) {
            drained += projector.drainOnce(2000);
        }
        assertEquals(250, drained);
        assertEquals(0L, jdbc.queryForObject("SELECT count(*) FROM cqrs_outbox", Long.class));
        assertEquals(250L, jdbc.queryForObject("SELECT count(*) FROM cqrs_read_history", Long.class));
        assertTrue(jdbc.queryForObject("SELECT count(*) FROM cqrs_read_latest", Long.class) > 0);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend && ./gradlew test --tests CqrsProjectorServiceTest`
Expected: FAIL — `CqrsProjectorService` not found.

- [ ] **Step 3: Implement CqrsProjectorService**

```java
package com.example.ue_tracker.cqrs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CqrsProjectorService {

    private final JdbcTemplate jdbc;
    @Value("${benchmark.projector.batch-size:2000}") int batchSize;

    public CqrsProjectorService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Scheduled(fixedDelayString = "${benchmark.projector.fixed-delay:1000}")
    public void scheduledDrain() {
        int n; do { n = drainOnce(batchSize); } while (n > 0);
    }

    /** Drains up to `batch` outbox rows; returns number projected. */
    @Transactional
    public int drainOnce(int batch) {
        var claimed = jdbc.queryForList(
                "SELECT seq, write_history_id FROM cqrs_outbox ORDER BY seq " +
                "FOR UPDATE SKIP LOCKED LIMIT ?", batch);
        if (claimed.isEmpty()) return 0;

        String ids = claimed.stream().map(r -> String.valueOf(r.get("write_history_id")))
                .reduce((a, b) -> a + "," + b).orElse("-1");
        String seqs = claimed.stream().map(r -> String.valueOf(r.get("seq")))
                .reduce((a, b) -> a + "," + b).orElse("-1");

        String cols = com.example.ue_tracker.store.CopySupport.COLS;
        // copy referenced write-history rows into read-history
        jdbc.update("INSERT INTO cqrs_read_history (" + cols + ") " +
                "SELECT " + cols + " FROM cqrs_write_history WHERE id IN (" + ids + ")");
        // upsert read-latest from those rows (latest wins)
        jdbc.update("INSERT INTO cqrs_read_latest (" + cols + ") " +
                "SELECT DISTINCT ON (imsi_or_supi) " + cols + " FROM cqrs_write_history " +
                "WHERE id IN (" + ids + ") ORDER BY imsi_or_supi, updated_at DESC " +
                "ON CONFLICT (imsi_or_supi) DO UPDATE SET " +
                "updated_at = EXCLUDED.updated_at, action_taken = EXCLUDED.action_taken, " +
                "rat = EXCLUDED.rat, rssi = EXCLUDED.rssi, provider_name = EXCLUDED.provider_name, " +
                "country_name = EXCLUDED.country_name, msisdn = EXCLUDED.msisdn, " +
                "distance_in_meters = EXCLUDED.distance_in_meters " +
                "WHERE EXCLUDED.updated_at >= cqrs_read_latest.updated_at");
        jdbc.update("DELETE FROM cqrs_outbox WHERE seq IN (" + seqs + ")");
        return claimed.size();
    }

    public long backlog() {
        Long n = jdbc.queryForObject("SELECT count(*) FROM cqrs_outbox", Long.class);
        return n == null ? 0 : n;
    }
}
```

> **Note:** ids/seqs are built from DB-generated bigints (not user input) → safe to inline. Do not interpolate user data this way elsewhere.

- [ ] **Step 4: Enable scheduling**

In `backend/src/main/java/com/example/ue_tracker/UeTrackerApplication.java`, add `@EnableScheduling` to the application class (import `org.springframework.scheduling.annotation.EnableScheduling`).

- [ ] **Step 5: Run test to verify it passes**

Run: `cd backend && ./gradlew test --tests CqrsProjectorServiceTest`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/example/ue_tracker/cqrs/CqrsProjectorService.java \
        backend/src/main/java/com/example/ue_tracker/UeTrackerApplication.java \
        backend/src/test/java/com/example/ue_tracker/cqrs/CqrsProjectorServiceTest.java
git commit -m "feat: CQRS projector with SKIP LOCKED outbox drain"
```

---

## Task 11: GenerationService (chunked dual-model load)

**Files:**
- Create: `backend/src/main/java/com/example/ue_tracker/generator/GenerationService.java`
- Test: `backend/src/test/java/com/example/ue_tracker/generator/GenerationServiceTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.example.ue_tracker.generator;

import com.example.ue_tracker.store.AbstractPostgresTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;

class GenerationServiceTest extends AbstractPostgresTest {

    @Autowired GenerationService gen;
    @Autowired JdbcTemplate jdbc;

    @Test
    void generatesIntoBothModelsAndReportsTimes() {
        GenerationService.Result r = gen.generate(1200); // > one chunk if chunk<1200; here single chunk
        assertEquals(1200, r.count());
        assertTrue(r.normalMs() >= 0);
        assertTrue(r.cqrsWriteMs() >= 0);
        assertEquals(1200L, jdbc.queryForObject("SELECT count(*) FROM ue_events_history", Long.class));
        assertEquals(1200L, jdbc.queryForObject("SELECT count(*) FROM cqrs_write_history", Long.class));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend && ./gradlew test --tests GenerationServiceTest`
Expected: FAIL — `GenerationService` not found.

- [ ] **Step 3: Implement GenerationService**

```java
package com.example.ue_tracker.generator;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.store.CqrsEventStore;
import com.example.ue_tracker.store.NormalEventStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenerationService {

    private final EventFactory factory;
    private final NormalEventStore normal;
    private final CqrsEventStore cqrs;
    @Value("${benchmark.generation.chunk-size:50000}") int chunkSize;

    public GenerationService(EventFactory factory, NormalEventStore normal, CqrsEventStore cqrs) {
        this.factory = factory; this.normal = normal; this.cqrs = cqrs;
    }

    public record Result(int count, long normalMs, long cqrsWriteMs) {}

    public Result generate(int count) {
        long normalMs = 0, cqrsMs = 0;
        int remaining = count;
        while (remaining > 0) {
            int n = Math.min(remaining, chunkSize);
            List<UeEvent> batch = factory.randomBatch(n);
            normalMs += normal.copyIn(batch);
            cqrsMs += cqrs.copyIn(batch);
            remaining -= n;
        }
        return new Result(count, normalMs, cqrsMs);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd backend && ./gradlew test --tests GenerationServiceTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/example/ue_tracker/generator/GenerationService.java \
        backend/src/test/java/com/example/ue_tracker/generator/GenerationServiceTest.java
git commit -m "feat: GenerationService chunked dual-model COPY load"
```

---

## Task 12: Controller — dispatch, timing, generate, projection status

**Files:**
- Modify: `backend/src/main/java/com/example/ue_tracker/controller/UeEventController.java`
- Test: `backend/src/test/java/com/example/ue_tracker/controller/UeEventControllerTest.java`

- [ ] **Step 1: Write the failing controller test**

```java
package com.example.ue_tracker.controller;

import com.example.ue.proto.UeEventPageResponse;
import com.example.ue_tracker.store.AbstractPostgresTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UeEventControllerTest extends AbstractPostgresTest {

    @Autowired TestRestTemplate rest;

    @Test
    void generateThenReadLatestNormalReturnsTimedProtobuf() throws Exception {
        rest.postForObject("/api/generate?count=200", null, String.class);
        ResponseEntity<byte[]> resp = rest.getForEntity(
                "/api/events/latest?model=NORMAL&strategy=KEYSET&size=50", byte[].class);
        UeEventPageResponse page = UeEventPageResponse.parseFrom(resp.getBody());
        assertFalse(page.getEventsList().isEmpty());
        assertTrue(page.getQueryTimeMs() >= 0);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend && ./gradlew test --tests UeEventControllerTest`
Expected: FAIL — controller still has the old signature (no `model` param, no `/api/generate`).

- [ ] **Step 3: Rewrite the controller**

```java
package com.example.ue_tracker.controller;

import com.example.ue.proto.UeEventPageResponse;
import com.example.ue_tracker.cqrs.CqrsProjectorService;
import com.example.ue_tracker.generator.GenerationService;
import com.example.ue_tracker.model.EventModel;
import com.example.ue_tracker.model.PaginationStrategy;
import com.example.ue_tracker.store.EventStore;
import com.example.ue_tracker.store.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UeEventController {

    private final Map<EventModel, EventStore> stores = new EnumMap<>(EventModel.class);
    private final GenerationService generation;
    private final CqrsProjectorService projector;

    public UeEventController(List<EventStore> storeList, GenerationService generation,
                             CqrsProjectorService projector) {
        storeList.forEach(s -> stores.put(s.model(), s));
        this.generation = generation;
        this.projector = projector;
    }

    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestParam(defaultValue = "1000") int count) {
        GenerationService.Result r = generation.generate(count);
        return Map.of("count", r.count(), "normalMs", r.normalMs(), "cqrsWriteMs", r.cqrsWriteMs());
    }

    @GetMapping("/projection/status")
    public Map<String, Object> projectionStatus() {
        return Map.of("outboxBacklog", projector.backlog());
    }

    @GetMapping(value = "/events/latest", produces = "application/x-protobuf")
    public UeEventPageResponse latest(@RequestParam EventModel model,
                                      @RequestParam PaginationStrategy strategy,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(required = false) String cursor,
                                      @RequestParam(defaultValue = "50") int size) {
        long start = System.nanoTime();
        PageResult r = stores.get(model).getLatest(strategy, page, cursor, size);
        return toProto(r, System.nanoTime() - start);
    }

    @GetMapping(value = "/events/{imsi}/history", produces = "application/x-protobuf")
    public UeEventPageResponse history(@PathVariable String imsi,
                                       @RequestParam EventModel model,
                                       @RequestParam PaginationStrategy strategy,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(required = false) String cursor,
                                       @RequestParam(defaultValue = "50") int size) {
        long start = System.nanoTime();
        PageResult r = stores.get(model).getHistory(imsi, strategy, page, cursor, size);
        return toProto(r, System.nanoTime() - start);
    }

    private UeEventPageResponse toProto(PageResult r, long elapsedNanos) {
        return UeEventPageResponse.newBuilder()
                .addAllEvents(r.events())
                .setTotalPages(r.totalPages())
                .setTotalElements(r.totalElements())
                .setCurrentPage(r.currentPage())
                .setQueryTimeMs(elapsedNanos / 1_000_000L)
                .setNextCursor(r.nextCursor() == null ? "" : r.nextCursor())
                .setHasNext(r.hasNext())
                .build();
    }
}
```

> Spring binds `EventModel`/`PaginationStrategy` request params from their enum names automatically (case-sensitive: `NORMAL`, `KEYSET`).

- [ ] **Step 4: Delete the obsolete JPA service/repos/entities**

```bash
rm backend/src/main/java/com/example/ue_tracker/service/DataGeneratorService.java
rm backend/src/main/java/com/example/ue_tracker/service/UeEventService.java
rm backend/src/main/java/com/example/ue_tracker/repository/UeEventRepository.java
rm backend/src/main/java/com/example/ue_tracker/repository/UeEventHistoryRepository.java
rm backend/src/main/java/com/example/ue_tracker/model/UeEventEntity.java
rm backend/src/main/java/com/example/ue_tracker/model/UeEventHistoryEntity.java
```
The adapter's `toProto(UeEventEntity)` / `toProto(UeEventHistoryEntity)` / `toEntity` / `toHistoryEntity` methods now reference deleted types — remove those four methods from `UeEventAdapter.java`, keeping only `fromRow`, `nz`, `tsToString`, and `parseInstant` (parseInstant may also be deleted if unused). Confirm with `./gradlew compileJava`.

- [ ] **Step 5: Run the full backend test suite**

Run: `cd backend && ./gradlew test`
Expected: PASS — CursorCodecTest, EventFactoryTest, NormalEventStoreTest, CqrsEventStoreTest, CqrsProjectorServiceTest, GenerationServiceTest, UeEventControllerTest.

- [ ] **Step 6: Commit**

```bash
git add -A backend/src/main/java backend/src/test/java
git commit -m "feat: controller dispatch + timing + generate/projection endpoints; remove JPA path"
```

---

## Task 13: Regenerate frontend protobuf bindings

**Files:**
- Modify: `frontend/src/proto.js`, `frontend/src/proto.d.ts`

- [ ] **Step 1: Regenerate static modules from the updated proto**

Run:
```bash
cd frontend
npx pbjs -t static-module -w commonjs -o src/proto.js ../proto/ue_event.proto
npx pbts -o src/proto.d.ts src/proto.js
```
Expected: files updated; `grep queryTimeMs src/proto.d.ts` returns a match.

- [ ] **Step 2: Verify TypeScript still compiles**

Run: `cd frontend && npx tsc --noEmit`
Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/proto.js frontend/src/proto.d.ts
git commit -m "feat: regenerate protobuf bindings with timing/cursor fields"
```

---

## Task 14: Frontend API layer

**Files:**
- Modify: `frontend/src/api.ts`

- [ ] **Step 1: Rewrite api.ts**

```typescript
import axios from 'axios';
import { com } from './proto';

const API_BASE_URL = 'http://localhost:8080/api';

export type Model = 'NORMAL' | 'CQRS';
export type Strategy = 'OFFSET' | 'KEYSET';

export const generateData = async (count: number) => {
    const response = await axios.post(`${API_BASE_URL}/generate`, null, { params: { count } });
    return response.data as { count: number; normalMs: number; cqrsWriteMs: number };
};

export const fetchProjectionStatus = async () => {
    const response = await axios.get(`${API_BASE_URL}/projection/status`);
    return response.data as { outboxBacklog: number };
};

export const fetchLatest = async (
    model: Model, strategy: Strategy, page: number, cursor: string | null, size = 50
) => {
    const response = await axios.get(`${API_BASE_URL}/events/latest`, {
        params: { model, strategy, page, cursor: cursor ?? undefined, size },
        responseType: 'arraybuffer',
    });
    return com.example.ue.UeEventPageResponse.decode(new Uint8Array(response.data));
};

export const fetchHistory = async (
    imsi: string, model: Model, strategy: Strategy, page: number, cursor: string | null, size = 50
) => {
    const response = await axios.get(`${API_BASE_URL}/events/${imsi}/history`, {
        params: { model, strategy, page, cursor: cursor ?? undefined, size },
        responseType: 'arraybuffer',
    });
    return com.example.ue.UeEventPageResponse.decode(new Uint8Array(response.data));
};
```

- [ ] **Step 2: Verify compile**

Run: `cd frontend && npx tsc --noEmit`
Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/api.ts
git commit -m "feat: frontend api - model/strategy params, generate, projection status"
```

---

## Task 15: Frontend Dashboard — toggles, generate, timing, cursor nav

**Files:**
- Modify: `frontend/src/Dashboard.tsx`

- [ ] **Step 1: Rewrite Dashboard.tsx**

```typescript
import React, { useCallback, useEffect, useState } from 'react';
import {
    Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper,
    Typography, Button, Dialog, DialogTitle, DialogContent, DialogActions,
    Pagination, CircularProgress, Box, ToggleButton, ToggleButtonGroup,
    TextField, Chip, Stack
} from '@mui/material';
import {
    fetchLatest, fetchHistory, generateData, fetchProjectionStatus, Model, Strategy
} from './api';
import { com } from './proto';

type UeEvent = com.example.ue.IUeEvent;

const Dashboard: React.FC = () => {
    const [model, setModel] = useState<Model>('NORMAL');
    const [strategy, setStrategy] = useState<Strategy>('OFFSET');

    const [events, setEvents] = useState<UeEvent[]>([]);
    const [loading, setLoading] = useState(false);
    const [queryMs, setQueryMs] = useState<number>(0);

    // offset paging
    const [page, setPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    // keyset paging
    const [cursorStack, setCursorStack] = useState<string[]>([]); // cursors used to reach current page
    const [nextCursor, setNextCursor] = useState<string>('');
    const [hasNext, setHasNext] = useState(false);

    const [count, setCount] = useState<number>(100000);
    const [genMsg, setGenMsg] = useState<string>('');
    const [backlog, setBacklog] = useState<number>(0);

    // history dialog
    const [historyOpen, setHistoryOpen] = useState(false);
    const [selectedImsi, setSelectedImsi] = useState<string | null>(null);
    const [historyEvents, setHistoryEvents] = useState<UeEvent[]>([]);
    const [historyMs, setHistoryMs] = useState<number>(0);
    const [historyLoading, setHistoryLoading] = useState(false);

    const loadLatest = useCallback(async (p: number, cursor: string | null) => {
        setLoading(true);
        try {
            const r = await fetchLatest(model, strategy, p - 1, cursor);
            setEvents(r.events || []);
            setQueryMs(Number(r.queryTimeMs || 0));
            setTotalPages(r.totalPages || 1);
            setNextCursor(r.nextCursor || '');
            setHasNext(!!r.hasNext);
        } catch (e) { console.error(e); } finally { setLoading(false); }
    }, [model, strategy]);

    // reset + reload whenever model/strategy changes
    useEffect(() => {
        setPage(1); setCursorStack([]); setNextCursor(''); setHasNext(false);
        loadLatest(1, null);
    }, [model, strategy, loadLatest]);

    // poll projection backlog when in CQRS mode
    useEffect(() => {
        if (model !== 'CQRS') { setBacklog(0); return; }
        const id = setInterval(async () => {
            try { setBacklog((await fetchProjectionStatus()).outboxBacklog); } catch { /* ignore */ }
        }, 1500);
        return () => clearInterval(id);
    }, [model]);

    const onGenerate = async () => {
        setGenMsg('Generating…');
        try {
            const r = await generateData(count);
            setGenMsg(`Generated ${r.count.toLocaleString()} — normal: ${r.normalMs} ms · cqrs-write: ${r.cqrsWriteMs} ms`);
            await loadLatest(1, null);
            setPage(1); setCursorStack([]);
        } catch (e) { setGenMsg('Generation failed (see console)'); console.error(e); }
    };

    const onOffsetPage = (_: unknown, value: number) => { setPage(value); loadLatest(value, null); };
    const onKeysetNext = () => {
        setCursorStack(s => [...s, nextCursor]);
        loadLatest(1, nextCursor);
    };
    const onKeysetPrev = () => {
        const s = [...cursorStack]; s.pop();
        const prev = s.length ? s[s.length - 1] : null;
        setCursorStack(s);
        loadLatest(1, prev);
    };

    const openHistory = async (imsi?: string | null) => {
        if (!imsi) return;
        setSelectedImsi(imsi); setHistoryOpen(true); setHistoryLoading(true);
        try {
            const r = await fetchHistory(imsi, model, strategy, 0, null);
            setHistoryEvents(r.events || []);
            setHistoryMs(Number(r.queryTimeMs || 0));
        } catch (e) { console.error(e); } finally { setHistoryLoading(false); }
    };

    const formatEnum = (val: any, enumObj: any) => {
        if (val === undefined || val === null) return 'N/A';
        const key = Object.keys(enumObj).find(k => enumObj[k] === val);
        return key ? key.replace('RAT_', '') : val;
    };

    return (
        <Box sx={{ p: 4 }}>
            <Typography variant="h4" gutterBottom>UE Events — Pagination Benchmark</Typography>

            <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 2, flexWrap: 'wrap' }}>
                <ToggleButtonGroup size="small" exclusive value={model}
                    onChange={(_, v) => v && setModel(v)}>
                    <ToggleButton value="NORMAL">Normal (2 tables)</ToggleButton>
                    <ToggleButton value="CQRS">CQRS (Read-Write Sep)</ToggleButton>
                </ToggleButtonGroup>

                <ToggleButtonGroup size="small" exclusive value={strategy}
                    onChange={(_, v) => v && setStrategy(v)}>
                    <ToggleButton value="OFFSET">Offset</ToggleButton>
                    <ToggleButton value="KEYSET">Keyset</ToggleButton>
                </ToggleButtonGroup>

                <Chip color="info" label={`Latest query: ${queryMs} ms`} />
                {model === 'CQRS' && <Chip color="warning" label={`Projection backlog: ${backlog.toLocaleString()}`} />}
                <Button variant="outlined" size="small" onClick={() => loadLatest(page, null)}>Refresh</Button>
            </Stack>

            <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 3 }}>
                <TextField size="small" type="number" label="Record count" value={count}
                    onChange={e => setCount(parseInt(e.target.value || '0', 10))} sx={{ width: 180 }} />
                <Button variant="contained" onClick={onGenerate}>Generate Data</Button>
                <Typography variant="body2">{genMsg}</Typography>
            </Stack>

            {loading ? <CircularProgress /> : (
                <TableContainer component={Paper}>
                    <Table size="small">
                        <TableHead>
                            <TableRow>
                                <TableCell>IMSI/SUPI</TableCell><TableCell>MSISDN</TableCell>
                                <TableCell>Action</TableCell><TableCell>RAT</TableCell>
                                <TableCell>Provider</TableCell><TableCell>Country</TableCell>
                                <TableCell>RSSI</TableCell><TableCell>Updated At</TableCell>
                                <TableCell>Actions</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {events.map((event, idx) => (
                                <TableRow key={`${event.imsiOrSupi}-${idx}`} hover>
                                    <TableCell>{event.imsiOrSupi}</TableCell>
                                    <TableCell>{event.msisdn}</TableCell>
                                    <TableCell>{formatEnum(event.actionTaken, com.example.ue.ActionTaken)}</TableCell>
                                    <TableCell>{formatEnum(event.rat, com.example.ue.RatType)}</TableCell>
                                    <TableCell>{event.providerName}</TableCell>
                                    <TableCell>{event.countryName}</TableCell>
                                    <TableCell>{event.rssi}</TableCell>
                                    <TableCell>{new Date(event.updatedAt || '').toLocaleString()}</TableCell>
                                    <TableCell>
                                        <Button variant="contained" size="small"
                                            onClick={() => openHistory(event.imsiOrSupi)}>History</Button>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            )}

            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
                {strategy === 'OFFSET' ? (
                    <Pagination count={totalPages} page={page} onChange={onOffsetPage} color="primary" />
                ) : (
                    <Stack direction="row" spacing={2} alignItems="center">
                        <Button variant="outlined" disabled={cursorStack.length === 0} onClick={onKeysetPrev}>Prev</Button>
                        <Button variant="outlined" disabled={!hasNext} onClick={onKeysetNext}>Next</Button>
                    </Stack>
                )}
            </Box>

            <Dialog open={historyOpen} onClose={() => setHistoryOpen(false)} maxWidth="lg" fullWidth>
                <DialogTitle>
                    History — IMSI {selectedImsi} <Chip size="small" color="info" label={`${historyMs} ms`} sx={{ ml: 2 }} />
                </DialogTitle>
                <DialogContent>
                    {historyLoading ? <CircularProgress /> : (
                        <TableContainer component={Paper}>
                            <Table size="small">
                                <TableHead>
                                    <TableRow>
                                        <TableCell>Action</TableCell><TableCell>RAT</TableCell>
                                        <TableCell>Provider</TableCell><TableCell>RSSI</TableCell>
                                        <TableCell>Distance (m)</TableCell><TableCell>Updated At</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {historyEvents.map((event, idx) => (
                                        <TableRow key={idx}>
                                            <TableCell>{formatEnum(event.actionTaken, com.example.ue.ActionTaken)}</TableCell>
                                            <TableCell>{formatEnum(event.rat, com.example.ue.RatType)}</TableCell>
                                            <TableCell>{event.providerName}</TableCell>
                                            <TableCell>{event.rssi}</TableCell>
                                            <TableCell>{event.distanceInMeters}</TableCell>
                                            <TableCell>{new Date(event.updatedAt || '').toLocaleString()}</TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </TableContainer>
                    )}
                </DialogContent>
                <DialogActions><Button onClick={() => setHistoryOpen(false)}>Close</Button></DialogActions>
            </Dialog>
        </Box>
    );
};

export default Dashboard;
```

- [ ] **Step 2: Verify build**

Run: `cd frontend && npx tsc --noEmit && CI=true npm run build`
Expected: compiles; `build/` produced.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/Dashboard.tsx
git commit -m "feat: dashboard model/strategy toggles, generate, timing badges, cursor nav"
```

---

## Task 16: End-to-end smoke via docker-compose

**Files:** none (uses `run.sh`)

- [ ] **Step 1: Build and boot the stack**

Run: `cd /Users/manip/Documents/codeRepo/postgres-ue-events-demo && ./run.sh`
Expected: postgres + backend + frontend start. Backend log shows Flyway applying `V1`.

- [ ] **Step 2: Generate and read via API**

Run:
```bash
curl -s -X POST "http://localhost:8080/api/generate?count=200000"
curl -s "http://localhost:8080/api/projection/status"
curl -s "http://localhost:8080/api/events/latest?model=CQRS&strategy=OFFSET&page=2000&size=50" -o /dev/null -w "offset deep page HTTP %{http_code} in %{time_total}s\n"
curl -s "http://localhost:8080/api/events/latest?model=CQRS&strategy=KEYSET&size=50" -o /dev/null -w "keyset first page HTTP %{http_code} in %{time_total}s\n"
```
Expected: generate returns `{count, normalMs, cqrsWriteMs}`; backlog drains toward 0 over a few seconds; deep OFFSET noticeably slower than KEYSET.

- [ ] **Step 3: Verify UI**

Open `http://localhost:3000`. Toggle Normal/CQRS and Offset/Keyset; click Generate; confirm query-time chips update and History dialog shows per-IMSI rows + timing. Confirm CQRS shows projection backlog draining.

- [ ] **Step 4: Tear down**

Run: `docker-compose down`

- [ ] **Step 5: Commit any fixes found during smoke**

```bash
git add -A && git commit -m "fix: e2e smoke adjustments"
```
(Skip if nothing changed.)

---

## Self-Review Notes

- **Spec coverage:** NORMAL 2-table (T3,T8) · CQRS 5-table + outbox + async projector (T3,T9,T10) · OFFSET+KEYSET both models (T8,T9 via SeekQuery) · COPY bulk load (T6,T8,T9,T11) · hash partitioning (T3) · Flyway/ddl-validate (T2,T3) · reWriteBatchedInserts/Hikari (T2) · estimated counts (SeekQuery) · covering seek indexes (T3) · proto timing+cursor (T1) · endpoints model/strategy/generate/projection-status (T12) · UI toggles+timing+cursor nav+backlog (T15) · remove auto-gen (T12) · tests for projector/offset-vs-keyset/identical-data/timing (T8,T9,T10,T11,T12).
- **Deferred items resolved:** COPY→latest uses staging temp + `DISTINCT ON` (T8/T9); partitions = 8 (T3); Prev-cursor = client cursor stack (T15).
- **Type consistency:** `EventStore` methods, `PageResult` fields, `SeekQuery.query` signature, `GenerationService.Result`, proto field names (`queryTimeMs`,`nextCursor`,`hasNext`) consistent across tasks.
