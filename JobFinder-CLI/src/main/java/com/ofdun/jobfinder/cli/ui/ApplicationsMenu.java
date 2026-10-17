package com.ofdun.jobfinder.cli.ui;

import com.ofdun.jobfinder.cli.CliContext;
import com.ofdun.jobfinder.cli.dto.applications.*;
import com.ofdun.jobfinder.cli.http.ApiException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ApplicationsMenu {
    private final CliContext ctx;

    public void showLoop() {
        var p = ctx.getPrompt();
        while (true) {
            p.println("\n--- Отклики (Applications) ---");
            p.println("0. Назад");
            p.println("1. Создать отклик [auth]");
            p.println("2. Получить по id [auth]");
            p.println("3. Обновить статус [auth]");
            p.println("4. Удалить [auth]");
            p.println("5. Мои отклики [соискатель]");
            p.println("6. Отклики на вакансию [работодатель]");

            int c = p.readInt("> ", false);
            switch (c) {
                case 0 -> {
                    return;
                }
                case 1 -> create();
                case 2 -> get();
                case 3 -> update();
                case 4 -> delete();
                case 5 -> list(false);
                case 6 -> list(true);
                default -> p.println("Нет такого пункта.");
            }
        }
    }

    private void create() {
        var p = ctx.getPrompt();
        try {
            Long vacancyId = p.readLong("vacancyId: ", false);
            Long resumeId = p.readLong("resumeId: ", false);
            Long id = ctx.getApi().createApplication(vacancyId, resumeId);
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
            Application a = ctx.getApi().getApplication(id);
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
            ApplicationStatus status =
                    p.readEnum("applicationStatus", ApplicationStatus.class, false);
            Application a = ctx.getApi().updateApplicationStatus(id, status);
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
            ctx.getApi().deleteApplication(id);
            p.println("Удалено.");
        } catch (ApiException e) {
            p.println("Ошибка: " + e.getMessage());
        } finally {
            p.pause();
        }
    }

    private void list(boolean forVacancy) {
        var p = ctx.getPrompt();
        try {
            Long vacancyId = forVacancy ? p.readLong("vacancyId: ", false) : null;
            Integer limit = p.readInt("limit (по умолчанию 20): ", true);
            Integer offset = p.readInt("offset (по умолчанию 0): ", true);
            int size = limit == null ? 20 : limit;
            int start = offset == null ? 0 : offset;
            Application[] applications =
                    forVacancy
                            ? ctx.getApi().vacancyApplications(vacancyId, size, start)
                            : ctx.getApi().myApplications(size, start);
            if (applications.length == 0) p.println("Откликов нет.");
            for (Application application : applications) p.println(application.toString());
        } catch (ApiException error) {
            p.println("Ошибка: " + error.getMessage());
        } finally {
            p.pause();
        }
    }
}
