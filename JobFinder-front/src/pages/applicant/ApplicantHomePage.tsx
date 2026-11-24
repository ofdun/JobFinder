import { Link } from "react-router-dom";
import { Icon } from "../../shared/ui/Icon";

export function ApplicantHomePage() {
  return (
    <main className="page dashboard-page">
      <span className="eyebrow">
        <span className="live-dot" /> КАБИНЕТ СОИСКАТЕЛЯ
      </span>
      <section className="dashboard-hero">
        <div>
          <h1>
            Следующий шаг
            <br />в вашей карьере.
          </h1>
          <p>Ваш опыт, ваши интересы — ваши возможности.</p>
          <Link to="/vacancies/search" className="button button-dark">
            Найти работу <Icon name="arrow" />
          </Link>
        </div>
        <span className="dashboard-art" aria-hidden="true">
          ↗
        </span>
      </section>
      <div className="workspace-grid">
        <Link className="workspace-card" to="/me/resumes">
          <span className="workspace-icon">
            <Icon name="resume" size={28} />
          </span>
          <h2>Мои резюме</h2>
          <p>Опыт, навыки и всё, что делает вас собой.</p>
          <span className="text-link">
            Открыть резюме <Icon name="arrow" />
          </span>
        </Link>
        <Link className="workspace-card" to="/me/applications">
          <span className="workspace-icon">
            <Icon name="send" size={28} />
          </span>
          <h2>Мои отклики</h2>
          <p>Следите за ответами и приглашениями работодателей.</p>
          <span className="text-link">
            Посмотреть отклики <Icon name="arrow" />
          </span>
        </Link>
        <Link className="workspace-card" to="/me/applicant">
          <span className="workspace-icon">
            <Icon name="person" size={28} />
          </span>
          <h2>Мой профиль</h2>
          <p>Контакты и личная информация для связи с вами.</p>
          <span className="text-link">
            Настроить профиль <Icon name="arrow" />
          </span>
        </Link>
      </div>
      <section className="next-step">
        <div>
          <h2>Дайте опыту заговорить.</h2>
          <p>Подготовьте резюме перед первым откликом.</p>
        </div>
        <Link to="/me/resumes/new" className="button button-dark">
          <Icon name="plus" /> Создать резюме
        </Link>
      </section>
    </main>
  );
}
