import { FormEvent, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link, useNavigate } from "react-router-dom";
import { searchVacancies } from "../../shared/api/vacancyApi";
import { useSession } from "../../shared/session/sessionStore";
import { Icon } from "../../shared/ui/Icon";
import { VacancyCard } from "../../shared/ui/VacancyCard";
import { QueryStateBlock } from "../../shared/ui/QueryStateBlock";
import { ApplicantHomePage } from "../applicant/ApplicantHomePage";
import { EmployerHomePage } from "../employer/EmployerHomePage";

export function HomePage() {
  const session = useSession();
  if (session?.role === "applicant") return <ApplicantHomePage />;
  if (session?.role === "employer") return <EmployerHomePage />;
  return <PublicHome />;
}

function PublicHome() {
  const [query, setQuery] = useState("");
  const navigate = useNavigate();
  const vacancies = useQuery({
    queryKey: ["home-vacancies"],
    queryFn: () =>
      searchVacancies({
        limit: 3,
        offset: 0,
        status: "ACTIVE",
        sortBy: "publicationDate",
        sortDesc: true,
      }),
  });
  function search(event: FormEvent) {
    event.preventDefault();
    navigate(
      `/vacancies/search${query.trim() ? `?q=${encodeURIComponent(query.trim())}` : ""}`,
    );
  }
  return (
    <main className="page landing-page">
      <section className="hero">
        <div className="hero-copy">
          <span className="eyebrow">
            <span className="live-dot" /> НОВЫЙ ШАГ В КАРЬЕРЕ
          </span>
          <h1>
            Работа, в которой
            <br />
            есть{" "}
            <span className="accent-word">
              ваш смысл
              <svg viewBox="0 0 360 20" aria-hidden="true">
                <path d="M4 14Q160 0 352 10" />
              </svg>
            </span>
            .
          </h1>
          <p className="hero-description">
            Найдите своё место. Команду, с которой хочется расти. И дело,
            которое подходит именно вам.
          </p>
          <form className="hero-search" onSubmit={search}>
            <Icon name="search" size={23} />
            <label className="sr-only" htmlFor="hero-query">
              Профессия или навык
            </label>
            <input
              id="hero-query"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Профессия или навык"
            />
            <button className="button-dark" type="submit">
              Найти работу <Icon name="arrow" size={18} />
            </button>
          </form>
          <div className="popular-searches">
            <span>Попробуйте:</span>
            {["Java", "Дизайн", "Маркетинг"].map((term) => (
              <Link
                key={term}
                to={`/vacancies/search?q=${encodeURIComponent(term)}`}
              >
                {term} <span>↗</span>
              </Link>
            ))}
          </div>
        </div>
        <div className="hero-art" aria-hidden="true">
          <div className="orbit orbit-one" />
          <div className="orbit orbit-two" />
          <div className="art-grid" />
          <span className="art-star">✳</span>
          <div className="art-note">
            <span className="art-note-icon">
              <Icon name="check" />
            </span>
            <div>
              Место для вашего таланта<small>Следующий шаг — за вами</small>
            </div>
          </div>
          <div className="art-title">
            MAKE
            <br />
            YOUR
            <br />
            <span>MOVE.</span>
          </div>
          <div className="art-tag">
            <Icon name="spark" size={16} /> Новые возможности
          </div>
          <span className="art-corner">↗</span>
        </div>
      </section>
      <section className="home-section">
        <div className="section-heading">
          <div>
            <span className="eyebrow">ВОЗМОЖНОСТИ РЯДОМ</span>
            <h2>Ваш следующий шаг</h2>
          </div>
          <Link to="/vacancies/search" className="text-link">
            Все вакансии <Icon name="arrow" size={18} />
          </Link>
        </div>
        {vacancies.isLoading && (
          <QueryStateBlock variant="loading" message="Ищем свежие вакансии…" />
        )}
        {vacancies.isError && (
          <QueryStateBlock
            variant="error"
            message="Не удалось загрузить вакансии."
            onRetry={() => vacancies.refetch()}
          />
        )}
        <div className="featured-jobs">
          {vacancies.data?.items.map((vacancy) => (
            <VacancyCard key={vacancy.id} vacancy={vacancy} />
          ))}
        </div>
        {vacancies.data?.items.length === 0 && (
          <QueryStateBlock
            variant="empty"
            message="Новых вакансий пока нет. Загляните чуть позже."
          />
        )}
      </section>
      <section className="journey-section">
        <div>
          <span className="eyebrow">ПРОСТО НАЧНИТЕ</span>
          <h2>
            От «хочу перемен»
            <br />
            до новой работы.
          </h2>
          <Link className="text-link" to="/register?role=applicant">
            Создать аккаунт соискателя <Icon name="arrow" size={18} />
          </Link>
        </div>
        <ol className="journey-steps">
          <li>
            <span>01</span>
            <div>
              <h3>Расскажите о себе</h3>
              <p>Добавьте опыт и навыки в резюме.</p>
            </div>
          </li>
          <li>
            <span>02</span>
            <div>
              <h3>Найдите своё</h3>
              <p>Выберите условия, которые важны вам.</p>
            </div>
          </li>
          <li>
            <span>03</span>
            <div>
              <h3>Сделайте первый шаг</h3>
              <p>Отправьте отклик и следите за ответом.</p>
            </div>
          </li>
        </ol>
      </section>
      <section className="employer-banner">
        <div>
          <span className="eyebrow">ДЛЯ РАБОТОДАТЕЛЕЙ</span>
          <h2>
            Хорошие команды
            <br />
            начинаются с людей.
          </h2>
          <p>
            Создайте отдельный аккаунт компании, публикуйте вакансии и находите
            своих людей.
          </p>
        </div>
        <Link to="/register?role=employer" className="button button-lime">
          Я ищу сотрудников <Icon name="arrow" />
        </Link>
      </section>
      <footer className="site-footer">
        <span className="footer-brand">jobfinder.</span>
        <p>Работа начинается с совпадения.</p>
        <span>Соискатели и работодатели — вместе.</span>
      </footer>
    </main>
  );
}
