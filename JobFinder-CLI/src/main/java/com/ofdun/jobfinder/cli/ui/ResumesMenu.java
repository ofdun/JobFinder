package com.ofdun.jobfinder.cli.ui;

import com.ofdun.jobfinder.cli.CliContext;
import com.ofdun.jobfinder.cli.dto.resumes.*;
import com.ofdun.jobfinder.cli.http.ApiException;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public final class ResumesMenu {
    private final CliContext ctx;

    public void showLoop() {
        var p = ctx.getPrompt();
        while (true) {
            p.println("\n--- Резюме ---");
            p.println("0. Назад");
            p.println("1. Создать (POST /resumes) [auth]");
            p.println("2. Получить по id (GET /resumes/{id}) [auth]");
            p.println("3. Обновить по id (PUT /resumes/{id}) [auth]");
            p.println("4. Удалить по id (DELETE /resumes/{id}) [auth]");
            p.println("5. Поиск (GET /resumes/search) [auth employer]");

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

    private EducationCreate readEducation() {
        var p = ctx.getPrompt();
        EducationDegree deg = p.readEnum("educationDegree", EducationDegree.class, false);
        String inst = p.readNonBlank("institutionName: ");
        String faculty = p.readLine("faculty: ");
        String dept = p.readLine("department: ");
        var y = p.readInt("yearOfGraduation: ", false);
        return new EducationCreate(
                deg,
                inst,
                faculty.isBlank() ? null : faculty,
                dept.isBlank() ? null : dept,
                new Date(y));
    }

    private JobExperienceCreate readJobExp() {
        var p = ctx.getPrompt();
        String position = p.readNonBlank("position: ");
        String company = p.readNonBlank("companyName: ");
        String desc = p.readLine("description (можно пусто): ");
        var start = p.readDate("startDate", true);
        var end = p.readDate("endDate", true);
        return new JobExperienceCreate(position, company, desc.isBlank() ? null : desc, start, end);
    }

    private ResumeRequest readCreateRequest() {
        var p = ctx.getPrompt();
        Long applicantId = p.readLong("applicantId: ", false);
        Long categoryId = p.readLong("categoryId: ", true);
        String description = p.readNonBlank("description: ");
        List<Long> skillIds = p.readLongList("skillIds", true);
        List<Long> languageIds = p.readLongList("languageIds", true);

        List<EducationCreate> educations = new ArrayList<>();
        while (true) {
            String add = p.readLine("Добавить образование? (y/n): ");
            if (!add.equalsIgnoreCase("y")) break;
            educations.add(readEducation());
        }

        List<JobExperienceCreate> jobs = new ArrayList<>();
        while (true) {
            String add = p.readLine("Добавить опыт работы? (y/n): ");
            if (!add.equalsIgnoreCase("y")) break;
            jobs.add(readJobExp());
        }

        return new ResumeRequest(
                applicantId,
                categoryId,
                description.isBlank() ? null : description,
                skillIds,
                educations,
                jobs,
                languageIds);
    }

    private ResumeUpdateRequest readUpdateRequest() {
        var p = ctx.getPrompt();
        Long applicantId = p.readLong("applicantId (можно пусто): ", true);
        Long categoryId = p.readLong("categoryId (можно пусто): ", true);
        String description = p.readLine("description (можно пусто): ");
        List<Long> skillIds = p.readLongList("skillIds (пусто = []): ", true);
        List<Long> languageIds = p.readLongList("languageIds (пусто = []): ", true);

        List<EducationCreate> educations = new ArrayList<>();
        String eduAny = p.readLine("Заменить educations? (y/n, иначе оставим пустым списком): ");
        if (eduAny.equalsIgnoreCase("y")) {
            while (true) {
                String add = p.readLine("Добавить образование? (y/n): ");
                if (!add.equalsIgnoreCase("y")) break;
                educations.add(readEducation());
            }
        }

        List<JobExperienceCreate> jobs = new ArrayList<>();
        String jobsAny =
                p.readLine("Заменить jobExperiences? (y/n, иначе оставим пустым списком): ");
        if (jobsAny.equalsIgnoreCase("y")) {
            while (true) {
                String add = p.readLine("Добавить опыт работы? (y/n): ");
                if (!add.equalsIgnoreCase("y")) break;
                jobs.add(readJobExp());
            }
        }

        return new ResumeUpdateRequest(
                applicantId,
                categoryId,
                description.isBlank() ? null : description,
                skillIds,
                educations,
                jobs,
                languageIds);
    }

    private void create() {
        var p = ctx.getPrompt();
        try {
            ResumeRequest req = readCreateRequest();
            Long id = ctx.getApi().post("/resumes", req, Long.class, true);
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
            Resume r = ctx.getApi().get("/resumes/" + id, Map.of(), Resume.class, true);
            p.println(String.valueOf(r));
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
            ResumeUpdateRequest req = readUpdateRequest();
            Resume r = ctx.getApi().put("/resumes/" + id, req, Resume.class, true);
            p.println("Обновлено: " + r);
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
            ctx.getApi().delete("/resumes/" + id, true);
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
            Long applicantId = p.readLong("applicantId (можно пусто): ", true);
            if (applicantId != null) q.put("applicantId", applicantId);
            Long categoryId = p.readLong("categoryId (можно пусто): ", true);
            if (categoryId != null) q.put("categoryId", categoryId);

            var skillIds = p.readLongList("skillIds", true);
            if (!skillIds.isEmpty()) q.put("skillIds", skillIds);
            var languageIds = p.readLongList("languageIds", true);
            if (!languageIds.isEmpty()) q.put("languageIds", languageIds);

            Integer limit = p.readInt("limit (по умолчанию 20): ", true);
            if (limit != null) q.put("limit", limit);
            Integer offset = p.readInt("offset (по умолчанию 0): ", true);
            if (offset != null) q.put("offset", offset);

            ResumePageResponse page =
                    ctx.getApi().get("/resumes/search", q, ResumePageResponse.class, true);
            if (page == null || page.items() == null) {
                p.println("Пустой ответ.");
            } else {
                p.println(
                        "Найдено: "
                                + page.items().size()
                                + " (totalElements="
                                + page.totalElements()
                                + ")");
                for (var r : page.items()) {
                    p.println(
                            "- id="
                                    + r.id()
                                    + ", applicantId="
                                    + r.applicantId()
                                    + ", category="
                                    + (r.category() == null ? null : r.category().name()));
                }
            }
        } catch (ApiException e) {
            p.println("Ошибка: " + e.getMessage());
        } finally {
            p.pause();
        }
    }
}
