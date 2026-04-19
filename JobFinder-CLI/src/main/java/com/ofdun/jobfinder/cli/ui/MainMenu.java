package com.ofdun.jobfinder.cli.ui;

import com.ofdun.jobfinder.cli.CliContext;
import com.ofdun.jobfinder.cli.auth.UserRole;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class MainMenu {
    public enum Action {
        EXIT,
        AUTH,
        APPLICANTS,
        EMPLOYERS,
        RESUMES,
        VACANCIES,
        APPLICATIONS,
        MATCHING
    }

    private final CliContext ctx;

    public Action show() {
        var p = ctx.getPrompt();
        var s = ctx.getSession();

        String who =
                s.isAuthorized()
                        ? (s.getRole() == UserRole.APPLICANT ? "Соискатель" : "Работодатель")
                        : "Гость";
        p.println("\n----------------------------------------");
        p.println(
                "Статус: "
                        + (s.isAuthorized()
                                ? ("Авторизованы (" + who + ")")
                                : "Вы не авторизованы (Гость)"));
        p.println("----------------------------------------");
        p.println("0. Выход");
        p.println("1. Авторизация");
        p.println("2. Соискатели");
        p.println("3. Работодатели");
        p.println("4. Резюме");
        p.println("5. Вакансии");
        p.println("6. Отклики");
        p.println("7. Matching (AI)");

        while (true) {
            Integer choice = p.readInt("> ", false);
            if (choice == null) continue;
            Action a =
                    switch (choice) {
                        case 0 -> Action.EXIT;
                        case 1 -> Action.AUTH;
                        case 2 -> Action.APPLICANTS;
                        case 3 -> Action.EMPLOYERS;
                        case 4 -> Action.RESUMES;
                        case 5 -> Action.VACANCIES;
                        case 6 -> Action.APPLICATIONS;
                        case 7 -> Action.MATCHING;
                        default -> null;
                    };
            if (a == null) {
                p.println("Нет такого пункта.");
                continue;
            }
            return a;
        }
    }
}
