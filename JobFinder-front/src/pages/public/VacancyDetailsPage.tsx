import { useQuery } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { ApiError } from '../../shared/api/httpClient';
import { getVacancyById } from '../../shared/api/vacancyApi';
import { formatDate, formatEmploymentType, formatJobFormat, formatSalary, toHumanTitle } from '../../shared/lib/format';
import { getSession } from '../../shared/session/sessionStore';
import { QueryStateBlock } from '../../shared/ui/QueryStateBlock';

export function VacancyDetailsPage() {
  const params = useParams();
  const vacancyId = Number(params.id);
  const session = getSession();

  const query = useQuery({
    queryKey: ['vacancy', vacancyId],
    queryFn: () => getVacancyById(vacancyId),
    enabled: Number.isFinite(vacancyId),
  });

  if (!Number.isFinite(vacancyId)) {
    return (
      <main className="page">
        <QueryStateBlock variant="error" message="Неверный идентификатор вакансии." />
      </main>
    );
  }

  const errorMessage = query.error instanceof ApiError ? query.error.message : 'Вакансия не найдена или недоступна.';

  return (
    <main className="page">
      <Link to="/vacancies/search">Назад к списку</Link>
      {query.isLoading ? <QueryStateBlock variant="loading" message="Загрузка вакансии..." /> : null}
      {query.isError ? (
        <QueryStateBlock variant="error" message={errorMessage} onRetry={() => query.refetch()} />
      ) : null}
      {!query.isLoading && !query.isError && !query.data ? (
        <QueryStateBlock variant="empty" message="Вакансия не найдена или недоступна." />
      ) : null}
      {query.data ? (
      <article className="card">
        <h1>{toHumanTitle(query.data.description, 'Вакансия')}</h1>
        <p>{query.data.description}</p>
        <p>Формат: {formatJobFormat(query.data.jobFormat)}</p>
        <p>Тип занятости: {formatEmploymentType(query.data.employmentType)}</p>
        <p>Опыт: {query.data.experience}</p>
        <p>Адрес: {query.data.address}</p>
        <p>Зарплата: {formatSalary(query.data)}</p>
        <p>Дата публикации: {formatDate(query.data.publicationDate)}</p>
        {session?.role === 'applicant' ? (
          <Link to={`/vacancies/${query.data.id}/apply`}>Откликнуться</Link>
        ) : (
          <Link to="/login" state={{ from: `/vacancies/${query.data.id}/apply` }}>
            Войти, чтобы откликнуться
          </Link>
        )}
      </article>
      ) : null}
    </main>
  );
}

