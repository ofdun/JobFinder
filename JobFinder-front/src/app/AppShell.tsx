import { ThemeToggle } from "../shared/ui/ThemeToggle";
import { NavLink } from "react-router-dom";
import { LogoutButton } from "../features/auth/LogoutButton";
import { useSession } from "../shared/session/sessionStore";
import { Icon, IconName } from "../shared/ui/Icon";

export function AppShell() {
  const session = useSession();
  const links: { to: string; label: string; icon: IconName }[] =
    session?.role === "employer"
      ? [
          { to: "/", label: "Обзор", icon: "grid" },
          { to: "/me/vacancies", label: "Мои вакансии", icon: "briefcase" },
          {
            to: "/me/resumes/search",
            label: "Найти кандидата",
            icon: "search",
          },
        ]
      : session?.role === "applicant"
        ? [
            { to: "/", label: "Обзор", icon: "grid" },
            { to: "/vacancies/search", label: "Найти работу", icon: "search" },
            { to: "/me/resumes", label: "Мои резюме", icon: "resume" },
            { to: "/me/applications", label: "Отклики", icon: "send" },
          ]
        : [{ to: "/vacancies/search", label: "Найти работу", icon: "search" }];
  return (
    <header className="app-shell">
      <div className="app-shell-inner">
        <NavLink className="brand-link" to="/" aria-label="JobFinder — главная">
          <span className="brand-symbol">
            <Icon name="spark" size={23} />
          </span>
          jobfinder<span className="brand-dot">.</span>
        </NavLink>
        <nav className="app-shell-nav" aria-label="Основная навигация">
          {links.map((link) => (
            <NavLink key={link.to} to={link.to} end={link.to === "/"}>
              <Icon name={link.icon} size={16} />
              {link.label}
            </NavLink>
          ))}
        </nav>
        <div className="shell-account">
          <ThemeToggle />
          {session ? (
            <>
              <NavLink
                className="account-link"
                to={
                  session.role === "applicant"
                    ? "/me/applicant"
                    : "/me/employer"
                }
              >
                <span className="account-avatar">
                  <Icon name="person" size={17} />
                </span>
                <span>
                  {session.role === "applicant" ? "Соискатель" : "Работодатель"}
                </span>
              </NavLink>
              <LogoutButton className="button-quiet" />
            </>
          ) : (
            <>
              <NavLink to="/login" className="login-link">
                Войти
              </NavLink>
              <NavLink to="/register" className="button button-dark">
                Начать <Icon name="arrow" size={17} />
              </NavLink>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
