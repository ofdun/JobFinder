package com.ofdun.jobfinder.features.auth.mfa;

import com.ofdun.jobfinder.features.api.ApiFailure;
import com.ofdun.jobfinder.features.auth.enums.AccountType;

import tools.jackson.databind.json.JsonMapper;

import java.util.function.Function;

public interface SecurityStateStore {
    record Snapshot(long version, SecurityState state) {}

    Snapshot read(String key);

    boolean save(String key, long version, SecurityState state);

    void delete(String key);

    default String key(AccountType type, long id) {
        return type.name() + ":" + id;
    }

    default SecurityState get(AccountType type, long id) {
        return read(key(type, id)).state();
    }

    default <T> T update(AccountType type, long id, Function<SecurityState, T> action) {
        String key = key(type, id);
        for (int attempt = 0; attempt < 20; attempt++) {
            var current = read(key);
            T result = action.apply(current.state());
            if (save(key, current.version(), current.state())) return result;
        }
        throw new ApiFailure("AUTH_BUSY", 409, "Повторите запрос");
    }

    static String encode(SecurityState state) {
        return JsonMapper.builder().build().writeValueAsString(state);
    }

    static SecurityState decode(String value) {
        return JsonMapper.builder().build().readValue(value, SecurityState.class);
    }
}
