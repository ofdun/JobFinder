package com.ofdun.jobfinder.cli.ui;

import com.ofdun.jobfinder.cli.CliContext;
import com.ofdun.jobfinder.cli.dto.profiles.Employer;
import com.ofdun.jobfinder.cli.dto.profiles.EmployerRequest;
import com.ofdun.jobfinder.cli.http.ApiException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class EmployersMenu {
    private final CliContext ctx;

    public void showLoop() {
        var p = ctx.getPrompt();
        while (true) {
            p.println("\n--- Работодатели ---");
            p.println("0. Назад");
            p.println("1. Регистрация (POST /employers)");
            p.println("2. Получить по id (GET /employers/{id}) [auth]");
            p.println("3. Обновить по id (PUT /employers/{id}) [auth]");
            p.println("4. Удалить по id (DELETE /employers/{id}) [auth]");

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
            EmployerRequest req = readRequest();
            Long id = ctx.getApi().post("/employers", req, Long.class, false);
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
            Employer a =
                    ctx.getApi().get("/employers/" + id, java.util.Map.of(), Employer.class, true);
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
            EmployerRequest req = readRequest();
            Employer a = ctx.getApi().put("/employers/" + id, req, Employer.class, true);
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
            ctx.getApi().delete("/employers/" + id, true);
            ctx.getSession().logout();
            p.println("Удалено.");
        } catch (ApiException e) {
            p.println("Ошибка: " + e.getMessage());
        } finally {
            p.pause();
        }
    }

    private EmployerRequest readRequest() {
        var p = ctx.getPrompt();
        String name = p.readNonBlank("name: ");
        String email = p.readNonBlank("email: ");
        String password = p.readNonBlank("password: ");
        String description = p.readLine("description (можно пусто): ");
        String address = p.readLine("address (можно пусто): ");
        String website = p.readLine("websiteLink (можно пусто): ");
        Long locationId = p.readLong("locationId (можно пусто): ", true);

        return new EmployerRequest(
                name,
                email,
                password,
                description.isBlank() ? null : description,
                address.isBlank() ? null : address,
                website.isBlank() ? null : website,
                locationId);
    }
}
