package com.ofdun.jobfinder.features.auth.mfa;

import static org.junit.jupiter.api.Assertions.*;

import com.mongodb.client.MongoClients;
import com.ofdun.jobfinder.features.auth.enums.AccountType;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class SecurityStateStoreIT {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container static final MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @Test
    @Tag("postgres")
    void postgresPersistsStateAndRejectsLostUpdates() {
        var jdbc =
                new JdbcTemplate(
                        new DriverManagerDataSource(
                                postgres.getJdbcUrl(),
                                postgres.getUsername(),
                                postgres.getPassword()));
        jdbc.execute("CREATE SCHEMA IF NOT EXISTS jobfinder");
        jdbc.execute(
                "CREATE TABLE IF NOT EXISTS jobfinder.account_security(id VARCHAR(100) PRIMARY KEY,"
                    + " version BIGINT NOT NULL, payload TEXT NOT NULL)");
        verify(new PostgresSecurityStateStore(jdbc), new PostgresSecurityStateStore(jdbc));
    }

    @Test
    @Tag("mongo")
    void mongoPersistsStateAndRejectsLostUpdates() {
        try (var client = MongoClients.create(mongo.getReplicaSetUrl())) {
            var template = new MongoTemplate(client, "authentication");
            verify(new MongoSecurityStateStore(template), new MongoSecurityStateStore(template));
        }
    }

    private void verify(SecurityStateStore first, SecurityStateStore second) {
        String key = first.key(AccountType.APPLICANT, 42);
        try {
            var initial = first.read(key);
            assertEquals(-1, initial.version());
            initial.state().secret = "encrypted-value";
            initial.state().lastStep = 100;
            assertTrue(first.save(key, initial.version(), initial.state()));
            assertFalse(second.save(key, initial.version(), initial.state()));
            var stale = first.read(key);
            second.update(
                    AccountType.APPLICANT,
                    42,
                    state -> {
                        state.lastStep = 101;
                        return null;
                    });
            assertFalse(first.save(key, stale.version(), stale.state()));
            assertEquals(101, first.get(AccountType.APPLICANT, 42).lastStep);
            assertEquals("encrypted-value", second.get(AccountType.APPLICANT, 42).secret);
            assertNull(second.get(AccountType.EMPLOYER, 42).secret);
        } finally {
            first.delete(key);
        }
        assertEquals(-1, second.read(key).version());
    }
}
