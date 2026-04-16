package com.ofdun.jobfinder.cli;

import com.ofdun.jobfinder.cli.auth.Session;
import com.ofdun.jobfinder.cli.http.ApiClient;
import com.ofdun.jobfinder.cli.logging.StructuredCliLogger;
import com.ofdun.jobfinder.cli.ui.*;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor
public final class CliApp {
    private static final Logger log = LoggerFactory.getLogger(CliApp.class);

    private final Prompt prompt = new Prompt();

    public void run() {
        Session session = Session.guest();
        var client = new ApiClient(session);

        var ctx = new CliContext(session, client, prompt);
        StructuredCliLogger.logUserAction(log, session, "cli.start", l -> l.info("CLI started"));

        while (true) {
            var mainMenu = new MainMenu(ctx);
            var action = mainMenu.show();
            StructuredCliLogger.logUserAction(
                    log,
                    ctx.getSession(),
                    "cli.menu.main.select",
                    l -> l.info("Main menu action selected: {}", action));
            switch (action) {
                case EXIT -> {
                    StructuredCliLogger.logUserAction(
                            log, ctx.getSession(), "cli.exit", l -> l.info("CLI finished"));
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
