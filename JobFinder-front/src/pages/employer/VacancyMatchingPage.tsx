import { useMemo, useState } from 'react';
import { useQueries, useQuery } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { getVacancyMatchingCandidates } from '../../shared/api/matchingApi';
import { getResumeById } from '../../shared/api/resumeApi';
import { getVacancyById } from '../../shared/api/vacancyApi';
import { toHumanTitle } from '../../shared/lib/format';
import { QueryStateBlock } from '../../shared/ui/QueryStateBlock';

function cutText(value: string, maxLength: number): string {
  if (value.length <= maxLength) {
    return value;
  }

  return `${value.slice(0, maxLength)}...`;
}

export function VacancyMatchingPage() {
  const params = useParams();
  const vacancyId = Number(params.id);
  const [maxAmount, setMaxAmount] = useState(20);

  const query = useQuery({
    queryKey: ['vacancy-matching', vacancyId, maxAmount],
    queryFn: () => getVacancyMatchingCandidates(vacancyId, maxAmount),
    enabled: Number.isFinite(vacancyId),
  });

  const vacancyQuery = useQuery({
    queryKey: ['matching-vacancy-title', vacancyId],
    queryFn: () => getVacancyById(vacancyId),
    enabled: Number.isFinite(vacancyId),
  });

  const sortedCandidates = useMemo(
    () => [...(query.data ?? [])].sort((left, right) => right.score - left.score),
    [query.data],
  );

  const resumeQueries = useQueries({
    queries: sortedCandidates.map((candidate) => ({
      queryKey: ['resume', candidate.id],
      queryFn: () => getResumeById(candidate.id),
      enabled: query.isSuccess,
    })),
  });

  if (!Number.isFinite(vacancyId)) {
    return <p className="page error">Неверный адрес вакансии.</p>;
  }

  return (
    <main className="page">
      <h1>Подходящие кандидаты: {toHumanTitle(vacancyQuery.data?.description ?? '', 'Вакансия')}</h1>
      <div className="toolbar">
        <label>
          Макс. кандидатов
          <input
            type="number"
            min={1}
            max={100}
            value={maxAmount}
            onChange={(event) => setMaxAmount(Number(event.target.value) || 1)}
          />
        </label>
        <Link to="/me/vacancies">К вакансиям</Link>
      </div>

      {query.isLoading ? <QueryStateBlock variant="loading" message="Считаем matching..." /> : null}
      {query.isError ? (
        <QueryStateBlock variant="error" message="Не удалось получить список кандидатов." onRetry={() => query.refetch()} />
      ) : null}
      {query.data && query.data.length === 0 ? (
        <QueryStateBlock variant="empty" message="Подходящих кандидатов пока нет." />
      ) : null}

      <div className="list">
        {sortedCandidates.map((candidate, index) => {
          const resumeQuery = resumeQueries[index];

          return (
            <article key={candidate.id} className="card">
              <h3>{toHumanTitle(resumeQuery?.data?.description ?? '', 'Кандидат')}</h3>
              <p>Оценка совпадения: {candidate.score.toFixed(4)}</p>
              {resumeQuery?.isLoading ? <p>Загрузка деталей резюме...</p> : null}
              {resumeQuery?.isError ? <p className="error">Не удалось получить детали резюме.</p> : null}
              {resumeQuery?.data ? (
                <>
                  <p>Категория: {resumeQuery.data.category?.name ?? 'Не указана'}</p>
                  <p>{cutText(resumeQuery.data.description, 180)}</p>
                </>
              ) : null}
            </article>
          );
        })}
      </div>
    </main>
  );
}

