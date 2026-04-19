package com.ofdun.jobfinder.cli;

import com.ofdun.jobfinder.cli.auth.Session;
import com.ofdun.jobfinder.cli.http.ApiClient;
import com.ofdun.jobfinder.cli.ui.Prompt;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public final class CliContext {
    @Getter private Session session;

    @Getter private final ApiClient api;

    @Getter private final Prompt prompt;

    public void setSession(Session session) {
        this.session = session;
        this.api.setSession(session);
    }
}
