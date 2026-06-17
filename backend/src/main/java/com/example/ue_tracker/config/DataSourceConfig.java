package com.example.ue_tracker.config;

import com.example.ue_tracker.adapter.UeEventAdapter;
import com.example.ue_tracker.store.EventQuery;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Two datasources:
 * <ul>
 *   <li><b>primary</b> ({@code spring.datasource}) — NORMAL tables, CQRS write tables, outbox.
 *       Takes the full write load. JPA/Flyway auto-config bind to this (it's {@code @Primary}).</li>
 *   <li><b>read</b> ({@code app.read.datasource}) — a separate Postgres holding the CQRS read tables.
 *       The projector writes here; CQRS reads come from here, isolated from the write storm.</li>
 * </ul>
 */
@Configuration
public class DataSourceConfig {

    // ---- primary ----
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    HikariDataSource primaryDataSource(@Qualifier("primaryDataSourceProperties") DataSourceProperties props) {
        return props.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    @Primary
    JdbcTemplate jdbcTemplate(@Qualifier("primaryDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    /**
     * Explicit primary Flyway. Defining any Flyway bean (we need one for the read db) makes
     * Spring Boot's auto-Flyway back off, so the primary migration must be declared too.
     */
    @Bean(initMethod = "migrate")
    @Primary
    Flyway primaryFlyway(@Qualifier("primaryDataSource") DataSource ds) {
        return Flyway.configure().dataSource(ds).locations("classpath:db/migration").load();
    }

    // ---- read (separate Postgres) ----
    @Bean
    @ConfigurationProperties("app.read.datasource")
    DataSourceProperties readDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("app.read.datasource.hikari")
    HikariDataSource readDataSource(@Qualifier("readDataSourceProperties") DataSourceProperties props) {
        return props.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    JdbcTemplate readJdbcTemplate(@Qualifier("readDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    /** Create the schema on the read DB (reuses the same migrations; only cqrs_read_* are used there). */
    @Bean(initMethod = "migrate")
    Flyway readFlyway(@Qualifier("readDataSource") DataSource ds) {
        return Flyway.configure().dataSource(ds).locations("classpath:db/migration").load();
    }

    // ---- query helpers bound to each datasource ----
    @Bean
    @Primary
    EventQuery primaryEventQuery(@Qualifier("primaryDataSource") DataSource ds, UeEventAdapter adapter) {
        return new EventQuery(ds, adapter);
    }

    @Bean
    EventQuery readEventQuery(@Qualifier("readDataSource") DataSource ds, UeEventAdapter adapter) {
        return new EventQuery(ds, adapter);
    }
}
