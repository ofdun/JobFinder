import { NavLink } from 'react-router-dom';
import { LogoutButton } from '../features/auth/LogoutButton';
import { useSession } from '../shared/session/sessionStore';

export function AppShell() {
  const session = useSession();

  return (
    <header className="app-shell">
      <div className="app-shell-inner">
        <NavLink className="brand-link" to="/vacancies/search">
          JobFinder
        </NavLink>

        <nav className="app-shell-nav" aria-label="Основная навигация">
          <NavLink to="/vacancies/search" className={({ isActive }) => (isActive ? 'active' : undefined)}>
            Вакансии
          </NavLink>

          {session?.role === 'applicant' ? (
            <>
              <NavLink to="/me/applicant" className={({ isActive }) => (isActive ? 'active' : undefined)}>
                Кабинет соискателя
              </NavLink>
              <NavLink to="/me/resumes" className={({ isActive }) => (isActive ? 'active' : undefined)}>
                Резюме
              </NavLink>
              <NavLink to="/me/applications" className={({ isActive }) => (isActive ? 'active' : undefined)}>
                Отклики
              </NavLink>
            </>
          ) : null}

          {session?.role === 'employer' ? (
            <>
              <NavLink to="/me/employer" className={({ isActive }) => (isActive ? 'active' : undefined)}>
                Кабинет работодателя
              </NavLink>
              <NavLink to="/me/vacancies" className={({ isActive }) => (isActive ? 'active' : undefined)}>
                Мои вакансии
              </NavLink>
              <NavLink to="/me/resumes/search" className={({ isActive }) => (isActive ? 'active' : undefined)}>
                Поиск резюме
              </NavLink>
            </>
          ) : null}

          {!session ? (
            <>
              <NavLink to="/login" className={({ isActive }) => (isActive ? 'active' : undefined)}>
                Вход
              </NavLink>
              <NavLink to="/register" className={({ isActive }) => (isActive ? 'active' : undefined)}>
                Регистрация
              </NavLink>
            </>
          ) : null}
        </nav>

        {session ? <LogoutButton className="app-shell-logout" /> : null}
      </div>
    </header>
  );
}

