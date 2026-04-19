package com.ofdun.jobfinder.cli.ui;

import com.ofdun.jobfinder.cli.CliContext;
import com.ofdun.jobfinder.cli.dto.vacancies.*;
import com.ofdun.jobfinder.cli.http.ApiException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class VacanciesMenu {
    private final CliContext ctx;

    public void showLoop() {
        var p = ctx.getPrompt();
        while (true) {
            p.println("\n--- Вакансии ---");
            p.println("0. Назад");
            p.println("1. Создать (POST /vacancies) [auth]");
            p.println("2. Получить по id (GET /vacancies/{id})");
            p.println("3. Обновить по id (PUT /vacancies/{id}) [auth]");
            p.println("4. Удалить по id (DELETE /vacancies/{id}) [auth]");
            p.println("5. Поиск (GET /vacancies/search)");

            int c = p.readInt("> ", false);
            switch (c) {
                case 0 -> {
                    return;
                }
                case 1 -> create();
                case 2 -> get();
                case 3 -> update();
                case 4 -> delete();
                case 5 -> search();
                default -> p.println("Нет такого пункта.");
            }
        }
    }

    private VacancyRequest readRequest() {
        var p = ctx.getPrompt();
        Long employerId = p.readLong("employerId: ", true);
        Long locationId = p.readLong("locationId: ", true);
        Double salary = p.readDouble("salary: ", true);
        var skillIds = p.readLongList("skillIds", true);
        var languageIds = p.readLongList("languageIds", true);
        PaymentFrequency pf = p.readEnum("paymentFrequency", PaymentFrequency.class, true);
        String experience = p.readLine("experience (можно пусто): ");
        JobFormat jf = p.readEnum("jobFormat", JobFormat.class, true);
        EmploymentType et = p.readEnum("employmentType", EmploymentType.class, true);
        String description = p.readLine("description (можно пусто): ");
        String address = p.readLine("address (можно пусто): ");

        return new VacancyRequest(
                employerId,
                locationId,
                salary,
                skillIds,
                languageIds,
                pf,
                experience.isBlank() ? null : experience,
                jf,
                et,
                description.isBlank() ? null : description,
                address.isBlank() ? null : address);
    }

    private void create() {
        var p = ctx.getPrompt();
        try {
            VacancyRequest req = readRequest();
            Long id = ctx.getApi().post("/vacancies", req, Long.class, true);
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
            Vacancy v = ctx.getApi().get("/vacancies/" + id, Map.of(), Vacancy.class, false);
            p.println(String.valueOf(v));
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
            VacancyRequest req = readRequest();
            Vacancy v = ctx.getApi().put("/vacancies/" + id, req, Vacancy.class, true);
            p.println("Обновлено: " + v);
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
            ctx.getApi().delete("/vacancies/" + id, true);
            p.println("Удалено.");
        } catch (ApiException e) {
            p.println("Ошибка: " + e.getMessage());
        } finally {
            p.pause();
        }
    }

    private void search() {
        var p = ctx.getPrompt();
        try {
            Map<String, Object> q = new HashMap<>();
            String text = p.readLine("q (подстрока, можно пусто): ");
            if (!text.isBlank()) q.put("q", text);
            Long employerId = p.readLong("employerId (можно пусто): ", true);
            if (employerId != null) q.put("employerId", employerId);
            Long locationId = p.readLong("locationId (можно пусто): ", true);
            if (locationId != null) q.put("locationId", locationId);
            Double salaryMin = p.readDouble("salaryMin (можно пусто): ", true);
            if (salaryMin != null) q.put("salaryMin", salaryMin);
            Double salaryMax = p.readDouble("salaryMax (можно пусто): ", true);
            if (salaryMax != null) q.put("salaryMax", salaryMax);

            PaymentFrequency pf = p.readEnum("paymentFrequency", PaymentFrequency.class, true);
            if (pf != null) q.put("paymentFrequency", pf.name());
            EmploymentType et = p.readEnum("employmentType", EmploymentType.class, true);
            if (et != null) q.put("employmentType", et.name());
            JobFormat jf = p.readEnum("workFormat", JobFormat.class, true);
            if (jf != null) q.put("workFormat", jf.name());

            var skillIds = p.readLongList("skillIds", true);
            if (!skillIds.isEmpty()) q.put("skillIds", skillIds);
            var languageIds = p.readLongList("languageIds", true);
            if (!languageIds.isEmpty()) q.put("languageIds", languageIds);

            Integer limit = p.readInt("limit (по умолчанию 20): ", true);
            if (limit != null) q.put("limit", limit);
            Integer offset = p.readInt("offset (по умолчанию 0): ", true);
            if (offset != null) q.put("offset", offset);
            String sortBy = p.readLine("sortBy (можно пусто): ");
            if (!sortBy.isBlank()) q.put("sortBy", sortBy);
            String sortDesc = p.readLine("sortDesc true/false (можно пусто): ");
            if (!sortDesc.isBlank()) q.put("sortDesc", Boolean.parseBoolean(sortDesc));

            VacancyPageResponse page =
                    ctx.getApi().get("/vacancies/search", q, VacancyPageResponse.class, false);
            if (page == null || page.items() == null) {
                p.println("Пустой ответ.");
            } else {
                p.println(
                        "Найдено: "
                                + page.items().size()
                                + " (totalElements="
                                + page.totalElements()
                                + ")");
                for (var v : page.items()) {
                    p.println(
                            "- id="
                                    + v.id()
                                    + ", salary="
                                    + v.salary()
                                    + ", employmentType="
                                    + v.employmentType()
                                    + ", jobFormat="
                                    + v.jobFormat());
                }
            }
        } catch (ApiException e) {
            p.println("Ошибка: " + e.getMessage());
        } finally {
            p.pause();
        }
    }
}
