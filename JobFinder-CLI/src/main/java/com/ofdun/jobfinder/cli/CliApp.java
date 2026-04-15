package com.ofdun.jobfinder.cli;

import com.ofdun.jobfinder.cli.auth.Session;
import com.ofdun.jobfinder.cli.http.ApiClient;
import com.ofdun.jobfinder.cli.ui.*;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class CliApp {
    private final Prompt prompt = new Prompt();

    public void run() {
        Session session = Session.guest();
        var client = new ApiClient(session);

        var ctx = new CliContext(session, client, prompt);

        while (true) {
            var mainMenu = new MainMenu(ctx);
            var action = mainMenu.show();
            switch (action) {
                case EXIT -> {
                    return;
                }
                case AUTH -> new AuthMenu(ctx).showLoop();
                case RESUMES -> new ResumesMenu(ctx).showLoop();
                case VACANCIES -> new VacanciesMenu(ctx).showLoop();
                case MATCHING -> new MatchingMenu(ctx).showLoop();
                case APPLICANTS -> new ApplicantsMenu(ctx).showLoop();
                case EMPLOYERS -> new EmployersMenu(ctx).showLoop();
                case APPLICATIONS -> new ApplicationsMenu(ctx).showLoop();
            }
        }
    }
}
