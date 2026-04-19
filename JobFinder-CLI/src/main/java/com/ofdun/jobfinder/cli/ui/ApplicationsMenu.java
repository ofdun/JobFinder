package com.ofdun.jobfinder.cli.ui;

import com.ofdun.jobfinder.cli.CliContext;
import com.ofdun.jobfinder.cli.dto.applications.*;
import com.ofdun.jobfinder.cli.http.ApiException;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ApplicationsMenu {
    private final CliContext ctx;

    public void showLoop() {
        var p = ctx.getPrompt();
        while (true) {
            p.println("\n--- Отклики (Applications) ---");
            p.println("0. Назад");
            p.println("1. Создать отклик (POST /applications) [auth]");
            p.println("2. Получить по id (GET /applications/{id}) [auth]");
            p.println("3. Обновить статус (PUT /applications/{id}) [auth]");
            p.println("4. Удалить (DELETE /applications/{id}) [auth]");

            int c = p.readInt("> ", false);
            switch (c) {
                case 0 -> {
                    return;
                }
                case 1 -> create();
                case 2 -> get();
                case 3 -> update();
                case 4 -> delete();
                default -> p.println("Нет такого пункта.");
            }
        }
    }

    private ApplicationRequest readRequest() {
        var p = ctx.getPrompt();
        Long vacancyId = p.readLong("vacancyId: ", false);
        Long resumeId = p.readLong("resumeId: ", false);
        ApplicationStatus status = p.readEnum("applicationStatus", ApplicationStatus.class, false);
        return new ApplicationRequest(vacancyId, resumeId, status);
    }

    private void create() {
        var p = ctx.getPrompt();
        try {
            ApplicationRequest req = readRequest();
            Long id = ctx.getApi().post("/applications", req, Long.class, true);
            p.println("Создано. id=" + id);
        } catch (ApiException e) {
            p.println("Ошибка: " + e.getMessage());
        } finally {
            p.pause();
        }
    }

    private void get() {
        var p = ctx.getPrompt();
        Long id = p.readLong("id: ", false);
        try {
            Application a =
                    ctx.getApi().get("/applications/" + id, Map.of(), Application.class, true);
            p.println(String.valueOf(a));
        } catch (ApiException e) {
            p.println("Ошибка: " + e.getMessage());
        } finally {
            p.pause();
        }
    }

    private void update() {
        var p = ctx.getPrompt();
        Long id = p.readLong("id: ", false);
        try {
            ApplicationRequest req = readRequest();
            Application a = ctx.getApi().put("/applications/" + id, req, Application.class, true);
            p.println("Обновлено: " + a);
        } catch (ApiException e) {
            p.println("Ошибка: " + e.getMessage());
        } finally {
            p.pause();
        }
    }

    private void delete() {
        var p = ctx.getPrompt();
        Long id = p.readLong("id: ", false);
        try {
            ctx.getApi().delete("/applications/" + id, true);
            p.println("Удалено.");
        } catch (ApiException e) {
            p.println("Ошибка: " + e.getMessage());
        } finally {
            p.pause();
        }
    }
}
