import { Link } from "react-router-dom";
import { Icon } from "../../shared/ui/Icon";

export function EmployerHomePage() {
  return (
    <main className="page dashboard-page">
      <span className="eyebrow">
        <span className="live-dot" /> КАБИНЕТ РАБОТОДАТЕЛЯ
      </span>
      <section className="dashboard-hero">
        <div>
          <h1>
            Ваша команда.
            <br />
            Следующая глава.
          </h1>
          <p>Находите людей, с которыми можно больше.</p>
          <Link to="/me/vacancies/new" className="button button-dark">
            Создать вакансию <Icon name="plus" />
          </Link>
        </div>
        <span className="dashboard-art" aria-hidden="true">
          ✳
        </span>
      </section>
      <div className="workspace-grid">
        <Link className="workspace-card" to="/me/vacancies">
          <span className="workspace-icon">
            <Icon name="briefcase" size={28} />
          </span>
          <h2>Мои вакансии</h2>
          <p>Публикации, изменения и отклики кандидатов.</p>
          <span className="text-link">
            Управлять вакансиями <Icon name="arrow" />
          </span>
        </Link>
        <Link className="workspace-card" to="/me/resumes/search">
          <span className="workspace-icon">
            <Icon name="search" size={28} />
          </span>
          <h2>Поиск кандидатов</h2>
          <p>Выберите опыт и навыки для вашей команды.</p>
          <span className="text-link">
            Посмотреть резюме <Icon name="arrow" />
          </span>
        </Link>
        <Link className="workspace-card" to="/me/employer">
          <span className="workspace-icon">
            <Icon name="person" size={28} />
          </span>
          <h2>Профиль компании</h2>
          <p>Расскажите о компании и оставьте контакты.</p>
          <span className="text-link">
            Редактировать профиль <Icon name="arrow" />
          </span>
        </Link>
      </div>
      <section className="next-step">
        <div>
          <h2>Нужный человек может быть рядом.</h2>
          <p>
            Откройте свою вакансию, чтобы посмотреть отклики и подбор
            кандидатов.
          </p>
        </div>
        <Link to="/me/vacancies" className="button button-dark">
          К моим вакансиям <Icon name="arrow" />
        </Link>
      </section>
    </main>
  );
}
