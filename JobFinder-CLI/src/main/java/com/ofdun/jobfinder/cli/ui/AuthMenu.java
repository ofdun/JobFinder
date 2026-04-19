package com.ofdun.jobfinder.cli.ui;

import com.ofdun.jobfinder.cli.CliContext;
import com.ofdun.jobfinder.cli.auth.UserRole;
import com.ofdun.jobfinder.cli.dto.auth.LoginRequest;
import com.ofdun.jobfinder.cli.dto.auth.TokenPair;
import com.ofdun.jobfinder.cli.http.ApiException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class AuthMenu {
    private final CliContext ctx;

    public void showLoop() {
        var p = ctx.getPrompt();
        while (true) {
            p.println("\n--- Авторизация ---");
            p.println("0. Назад");
            p.println("1. Войти как соискатель");
            p.println("2. Войти как работодатель");
            p.println("3. Refresh токена");
            p.println("4. Выйти");

            int c = p.readInt("> ", false);
            switch (c) {
                case 0 -> {
                    return;
                }
                case 1 -> login(UserRole.APPLICANT);
                case 2 -> login(UserRole.EMPLOYER);
                case 3 -> refresh();
                case 4 -> logout();
                default -> p.println("Нет такого пункта.");
            }
        }
    }

    private void login(UserRole role) {
        var p = ctx.getPrompt();
        String email = p.readNonBlank("email: ");
        String password = p.readNonBlank("password: ");

        String path = role == UserRole.APPLICANT ? "/auth/applicant/login" : "/auth/employer/login";
        try {
            TokenPair pair =
                    ctx.getApi()
                            .post(path, new LoginRequest(email, password), TokenPair.class, false);
            if (pair == null || pair.accessToken() == null || pair.refreshToken() == null) {
                p.println("Сервер вернул пустой ответ.");
                return;
            }
            var newSession =
                    ctx.getSession().authorized(role, pair.accessToken(), pair.refreshToken());
            ctx.setSession(newSession);
            p.println("Успешный вход. Роль: " + role);
        } catch (ApiException e) {
            p.println("Ошибка: " + e.getMessage());
        } finally {
            p.pause();
        }
    }

    private void refresh() {
        var p = ctx.getPrompt();
        try {
            if (ctx.getSession().getRole() == UserRole.GUEST) {
                p.println("Вы гость. Нечего обновлять.");
                return;
            }
            String path =
                    ctx.getSession().getRole() == UserRole.APPLICANT
                            ? "/auth/applicant/refresh"
                            : "/auth/employer/refresh";
            var pair =
                    ctx.getApi()
                            .post(
                                    path,
                                    new com.ofdun.jobfinder.cli.dto.auth.RefreshRequest(
                                            ctx.getSession().getRefreshToken()),
                                    TokenPair.class,
                                    false);
            if (pair == null || pair.accessToken() == null || pair.refreshToken() == null) {
                p.println("Сервер вернул пустой ответ.");
                return;
            }
            var newSession =
                    ctx.getSession()
                            .authorized(
                                    ctx.getSession().getRole(),
                                    pair.accessToken(),
                                    pair.refreshToken());
            ctx.setSession(newSession);
            p.println("Токены обновлены.");
        } catch (ApiException e) {
            p.println("Ошибка: " + e.getMessage());
        } finally {
            p.pause();
        }
    }

    private void logout() {
        var p = ctx.getPrompt();
        var newSession = ctx.getSession().logout();
        ctx.setSession(newSession);
        p.println("Сессия очищена.");
        p.pause();
    }
}
