package com.example.ue_tracker.store;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Two singleton {@code postgres:16} containers for the whole JVM: PRIMARY (NORMAL + CQRS write
 * side + outbox) and READ (CQRS read tables) — mirroring the separate-read-db production setup.
 * Ryuk tears them down at JVM exit.
 */
@SpringBootTest
public abstract class AbstractPostgresTest {

    static final PostgreSQLContainer<?> PRIMARY =
            new PostgreSQLContainer<>("postgres:16").withUrlParam("reWriteBatchedInserts", "true");
    static final PostgreSQLContainer<?> READ =
            new PostgreSQLContainer<>("postgres:16").withUrlParam("reWriteBatchedInserts", "true");

    static {
        PRIMARY.start();
        READ.start();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", PRIMARY::getJdbcUrl);
        r.add("spring.datasource.username", PRIMARY::getUsername);
        r.add("spring.datasource.password", PRIMARY::getPassword);
        r.add("app.read.datasource.url", READ::getJdbcUrl);
        r.add("app.read.datasource.username", READ::getUsername);
        r.add("app.read.datasource.password", READ::getPassword);
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        r.add("spring.flyway.enabled", () -> "true");
        // effectively disable the scheduled auto-drain so tests control projection timing
        r.add("benchmark.projector.fixed-delay", () -> "3600000");
    }

    @Autowired protected JdbcTemplate jdbcTemplate;                       // primary
    @Autowired @Qualifier("readJdbcTemplate") protected JdbcTemplate readJdbcTemplate;

    @BeforeEach
    void truncateAll() {
        jdbcTemplate.execute("TRUNCATE ue_events, ue_events_history, cqrs_write_latest, " +
                "cqrs_write_history, cqrs_outbox");
        readJdbcTemplate.execute("TRUNCATE cqrs_read_latest, cqrs_read_history");
    }
}
