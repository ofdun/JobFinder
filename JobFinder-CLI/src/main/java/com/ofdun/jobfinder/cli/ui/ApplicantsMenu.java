package com.ofdun.jobfinder.cli.ui;

import com.ofdun.jobfinder.cli.CliContext;
import com.ofdun.jobfinder.cli.dto.profiles.Applicant;
import com.ofdun.jobfinder.cli.dto.profiles.ApplicantRequest;
import com.ofdun.jobfinder.cli.http.ApiException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ApplicantsMenu {
    private final CliContext ctx;

    public void showLoop() {
        var p = ctx.getPrompt();
        while (true) {
            p.println("\n--- Соискатели ---");
            p.println("0. Назад");
            p.println("1. Регистрация");
            p.println("2. Получить по id [auth]");
            p.println("3. Обновить по id [auth]");
            p.println("4. Удалить по id [auth]");

            int c = p.readInt("> ", false);
            switch (c) {
                case 0 -> {
                    return;
                }
                case 1 -> register();
                case 2 -> get();
                case 3 -> update();
                case 4 -> delete();
                default -> p.println("Нет такого пункта.");
            }
        }
    }

    private void register() {
        var p = ctx.getPrompt();
        try {
            ApplicantRequest req = readRequest();
            Long id = ctx.getApi().createApplicant(req);
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
            Applicant a = ctx.getApi().getApplicant(id);
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
            ApplicantRequest req = readRequest();
            Applicant a = ctx.getApi().replaceApplicant(id, req);
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
            ctx.getApi().deleteApplicant(id);
            ctx.setSession(ctx.getSession().logout());
            p.println("Удалено.");
        } catch (ApiException e) {
            p.println("Ошибка: " + e.getMessage());
        } finally {
            p.pause();
        }
    }

    private ApplicantRequest readRequest() {
        var p = ctx.getPrompt();
        String name = p.readNonBlank("name: ");
        String email = p.readNonBlank("email: ");
        String password = p.readNonBlank("password: ");
        String address = p.readLine("address (можно пусто): ");
        String phone = p.readLine("phoneNumber (можно пусто): ");
        Long locationId = p.readLong("locationId: ", false);
        return new ApplicantRequest(
                name,
                email,
                password,
                address.isBlank() ? null : address,
                phone.isBlank() ? null : phone,
                locationId);
    }
}
