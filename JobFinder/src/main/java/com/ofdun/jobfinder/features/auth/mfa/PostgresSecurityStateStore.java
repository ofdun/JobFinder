package com.ofdun.jobfinder.features.auth.mfa;

import com.ofdun.jobfinder.features.auth.enums.AccountType;
import java.util.function.Function;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "postgres")
public class PostgresSecurityStateStore implements SecurityStateStore {
    private final JdbcTemplate jdbc;

    public PostgresSecurityStateStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public SecurityState get(AccountType type, long id) {
        return SecurityStateStore.super.get(type, id);
    }

    @Override
    public <T> T update(AccountType type, long id, Function<SecurityState, T> action) {
        return SecurityStateStore.super.update(type, id, action);
    }

    public Snapshot read(String key) {
        return jdbc
                .query(
                        "SELECT version, payload FROM jobfinder.account_security WHERE id = ?",
                        (rs, row) ->
                                new Snapshot(
                                        rs.getLong(1), SecurityStateStore.decode(rs.getString(2))),
                        key)
                .stream()
                .findFirst()
                .orElseGet(() -> new Snapshot(-1, new SecurityState()));
    }

    public boolean save(String key, long version, SecurityState state) {
        if (version == -1) {
            return jdbc.update(
                            "INSERT INTO jobfinder.account_security(id, version, payload) VALUES"
                                    + " (?, 0, ?) ON CONFLICT DO NOTHING",
                            key,
                            SecurityStateStore.encode(state))
                    == 1;
        }
        return jdbc.update(
                        "UPDATE jobfinder.account_security SET version = version + 1, payload = ?"
                                + " WHERE id = ? AND version = ?",
                        SecurityStateStore.encode(state),
                        key,
                        version)
                == 1;
    }

    public void delete(String key) {
        jdbc.update("DELETE FROM jobfinder.account_security WHERE id = ?", key);
    }
}
