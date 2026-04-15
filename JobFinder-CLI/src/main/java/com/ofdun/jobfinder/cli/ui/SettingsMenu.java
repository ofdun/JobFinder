package com.ofdun.jobfinder.cli.ui;

import com.ofdun.jobfinder.cli.CliContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SettingsMenu {
    private final CliContext ctx;

    public void showLoop() {
        var p = ctx.getPrompt();
        while (true) {
            p.println("\n--- Настройки ---");
            p.println("Текущий baseUrl: " + ctx.getSession().getBaseUrl());
            p.println("0. Назад");
            p.println("1. Изменить baseUrl");
            p.println("2. Сбросить сессию локально");

            int c = p.readInt("> ", false);
            switch (c) {
                case 0 -> {
                    return;
                }
                case 1 -> {
                    String url =
                            p.readNonBlank(
                                    "Введите baseUrl (например http://localhost:8080/api/v1): ");
                    var newSession = ctx.getSession().withBaseUrl(url);
                    ctx.setSession(newSession);
                    p.println("Сохранено.");
                    p.pause();
                }
                case 2 -> {
                    var newSession = ctx.getSession().logout();
                    ctx.setSession(newSession);
                    p.println("Сессия сброшена.");
                    p.pause();
                }
                default -> p.println("Нет такого пункта.");
            }
        }
    }
}
