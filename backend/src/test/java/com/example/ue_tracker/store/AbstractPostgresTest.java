package com.example.ue_tracker.store;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Singleton-container base: one {@code postgres:16} container started for the whole JVM and
 * shared across all test classes. Avoids per-class restart vs Spring context-cache mismatch.
 * Ryuk tears the container down at JVM exit.
 */
@SpringBootTest
public abstract class AbstractPostgresTest {

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16")
                    .withUrlParam("reWriteBatchedInserts", "true");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        r.add("spring.flyway.enabled", () -> "true");
        // effectively disable the scheduled auto-drain so tests control projection timing
        r.add("benchmark.projector.fixed-delay", () -> "3600000");
    }

    @Autowired JdbcTemplate cleanupJdbc;

    @BeforeEach
    void truncateAll() {
        cleanupJdbc.execute("TRUNCATE ue_events, ue_events_history, cqrs_write_latest, " +
                "cqrs_write_history, cqrs_read_latest, cqrs_read_history, cqrs_outbox");
    }
}
