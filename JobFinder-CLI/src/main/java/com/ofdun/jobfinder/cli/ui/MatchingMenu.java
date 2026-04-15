package com.ofdun.jobfinder.cli.ui;

import com.ofdun.jobfinder.cli.CliContext;
import com.ofdun.jobfinder.cli.dto.matching.MatchResult;
import com.ofdun.jobfinder.cli.http.ApiException;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public final class MatchingMenu {
    private final CliContext ctx;

    public void showLoop() {
        var p = ctx.getPrompt();
        while (true) {
            p.println("\n--- Matching (AI) ---");
            p.println("0. Назад");
            p.println(
                    "1. Найти кандидатов для вакансии (GET"
                            + " /matching/vacancies/{vacancyId}/candidates) [auth]");

            int c = p.readInt("> ", false);
            switch (c) {
                case 0 -> {
                    return;
                }
                case 1 -> candidatesForVacancy();
                default -> p.println("Нет такого пункта.");
            }
        }
    }

    private void candidatesForVacancy() {
        var p = ctx.getPrompt();
        Long vacancyId = p.readLong("vacancyId: ", false);
        Integer max = p.readInt("maxAmount (по умолчанию 10): ", true);

        try {
            Map<String, Object> q = new HashMap<>();
            if (max != null) q.put("maxAmount", max);

            MatchResult[] res =
                    ctx.getApi()
                            .get(
                                    "/matching/vacancies/" + vacancyId + "/candidates",
                                    q,
                                    MatchResult[].class,
                                    true);
            if (res == null || res.length == 0) {
                p.println("Ничего не найдено.");
            } else {
                p.println("Найдено кандидатов: " + res.length);
                for (MatchResult r : res) {
                    p.println("- resumeId=" + r.id() + ", score=" + r.score());
                }
            }
        } catch (ApiException e) {
            p.println("Ошибка: " + e.getMessage());
        } finally {
            p.pause();
        }
    }
}
