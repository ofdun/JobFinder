import { useMutation, useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { deleteVacancyById, searchVacancies } from '../../shared/api/vacancyApi';
import { getCurrentUserId } from '../../shared/session/sessionStore';
import { formatDate, formatEmploymentType, formatJobFormat, formatSalary, toHumanTitle } from '../../shared/lib/format';
import { QueryStateBlock } from '../../shared/ui/QueryStateBlock';

export function EmployerVacanciesPage() {
  const userId = getCurrentUserId();

  const query = useQuery({
    queryKey: ['employer-vacancies', userId],
    queryFn: () => searchVacancies({ employerId: userId as number, limit: 50, offset: 0 }),
    enabled: userId !== null,
  });

  const deleteMutation = useMutation({
    mutationFn: (vacancyId: number) => deleteVacancyById(vacancyId),
    onSuccess: () => query.refetch(),
  });

  if (userId === null) {
    return <p className="page error">Не удалось определить пользователя. Перелогиньтесь.</p>;
  }

  return (
    <main className="page">
      <h1>Мои вакансии</h1>
      <div className="toolbar">
        <Link to="/me/vacancies/new">Создать вакансию</Link>
        <Link to="/me/employer">В кабинет</Link>
      </div>

      {query.isLoading ? <QueryStateBlock variant="loading" message="Загрузка вакансий..." /> : null}
      {query.isError ? (
        <QueryStateBlock
          variant="error"
          message="Не удалось загрузить список вакансий."
          onRetry={() => query.refetch()}
        />
      ) : null}

      <div className="list">
        {query.data?.items.map((vacancy) => (
          <article key={vacancy.id} className="card">
            <h3>{toHumanTitle(vacancy.description, 'Вакансия')}</h3>
            <p>{vacancy.description}</p>
            <p>Формат: {formatJobFormat(vacancy.jobFormat)}</p>
            <p>Занятость: {formatEmploymentType(vacancy.employmentType)}</p>
            <p>Зарплата: {formatSalary(vacancy)}</p>
            <p>Дата публикации: {formatDate(vacancy.publicationDate)}</p>
            <div className="toolbar">
              <Link to={`/me/vacancies/${vacancy.id}/edit`}>Редактировать</Link>
              <Link to={`/me/vacancies/${vacancy.id}/applications`}>Отклики</Link>
              <Link to={`/me/vacancies/${vacancy.id}/matching`}>Matching</Link>
              <button
                type="button"
                className="danger"
                disabled={deleteMutation.isPending}
                onClick={() => deleteMutation.mutate(vacancy.id)}
              >
                Удалить
              </button>
            </div>
          </article>
        ))}
      </div>

      {query.data && query.data.items.length === 0 ? (
        <QueryStateBlock variant="empty" message="Пока нет вакансий. Создайте первую." />
      ) : null}
    </main>
  );
}

