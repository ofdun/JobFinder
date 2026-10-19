package com.ofdun.jobfinder.cli.ui;

import com.ofdun.jobfinder.cli.CliContext;
import com.ofdun.jobfinder.cli.dto.vacancies.*;
import com.ofdun.jobfinder.cli.http.ApiException;
import java.math.BigDecimal;
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
            p.println("1. Создать [auth]");
            p.println("2. Получить по id");
            p.println("3. Обновить по id [auth]");
            p.println("4. Удалить по id [auth]");
            p.println("5. Поиск");

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
        Long employerId = p.readLong("employerId: ", false);
        Long locationId = p.readLong("locationId: ", false);
        BigDecimal salary = p.readDecimal("salary: ", false);
        var skillIds = p.readLongList("skillIds", true);
        var languageIds = p.readLongList("languageIds", true);
        PaymentFrequency pf = p.readEnum("paymentFrequency", PaymentFrequency.class, false);
        String experience = p.readNonBlank("experience: ");
        JobFormat jf = p.readEnum("jobFormat", JobFormat.class, false);
        EmploymentType et = p.readEnum("employmentType", EmploymentType.class, false);
        String description = p.readNonBlank("description: ");
        String address = p.readNonBlank("address: ");
        VacancyStatus status =
                p.readEnum("status (по умолчанию ACTIVE)", VacancyStatus.class, true);

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
                address.isBlank() ? null : address,
                status == null ? VacancyStatus.ACTIVE : status);
    }

    private void create() {
        var p = ctx.getPrompt();
        try {
            VacancyRequest req = readRequest();
            Long id = ctx.getApi().createVacancy(req);
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
            Vacancy v = ctx.getApi().getVacancy(id);
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
            Vacancy v = ctx.getApi().replaceVacancy(id, req);
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
            ctx.getApi().deleteVacancy(id);
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
            Long locationId = p.readLong("locationId: ", false);
            if (locationId != null) q.put("locationId", locationId);
            BigDecimal salaryMin = p.readDecimal("salaryMin (можно пусто): ", true);
            if (salaryMin != null) q.put("salaryMin", salaryMin);
            BigDecimal salaryMax = p.readDecimal("salaryMax (можно пусто): ", true);
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
            String sortBy = p.readLine("sortBy (PUBLICATION_DATE, SALARY, ID; можно пусто): ");
            if (!sortBy.isBlank()) q.put("sortBy", sortBy);
            String sortDesc = p.readLine("sortDesc true/false (можно пусто): ");
            if (!sortDesc.isBlank()) q.put("sortDesc", Boolean.parseBoolean(sortDesc));

            VacancyPageResponse page = ctx.getApi().searchVacancies(q);
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
