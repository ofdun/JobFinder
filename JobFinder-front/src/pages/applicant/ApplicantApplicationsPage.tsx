import { useState } from 'react';
import { useMutation, useQueries } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { deleteApplicationById, getApplicationById } from '../../shared/api/applicationApi';
import { getResumeById } from '../../shared/api/resumeApi';
import { getVacancyById } from '../../shared/api/vacancyApi';
import { formatApplicationStatus, toHumanTitle } from '../../shared/lib/format';
import { getTrackedApplicationIds, untrackApplicationId } from '../../shared/session/trackedApplications';
import { QueryStateBlock } from '../../shared/ui/QueryStateBlock';

export function ApplicantApplicationsPage() {
  const [trackedIds, setTrackedIds] = useState<number[]>(() => getTrackedApplicationIds());

  const queries = useQueries({
    queries: trackedIds.map((id) => ({
      queryKey: ['application', id],
      queryFn: () => getApplicationById(id),
    })),
  });

  const deleteMutation = useMutation({
    mutationFn: (applicationId: number) => deleteApplicationById(applicationId),
    onSuccess: (_, applicationId) => {
      untrackApplicationId(applicationId);
      setTrackedIds((prev) => prev.filter((id) => id !== applicationId));
    },
  });

  const isLoading = queries.some((query) => query.isLoading);
  const hasError = queries.some((query) => query.isError);
  const applications = queries.map((query) => query.data).filter((item) => item !== undefined);

  const vacancyQueries = useQueries({
    queries: applications.map((application) => ({
      queryKey: ['application-vacancy', application.vacancyId],
      queryFn: () => getVacancyById(application.vacancyId),
    })),
  });

  const resumeQueries = useQueries({
    queries: applications.map((application) => ({
      queryKey: ['application-resume', application.resumeId],
      queryFn: () => getResumeById(application.resumeId),
    })),
  });
  const retryAll = () => {
    queries.forEach((query) => {
      void query.refetch();
    });
  };

  return (
    <main className="page">
      <h1>Мои отклики</h1>
      <div className="toolbar">
        <Link to="/me/applicant">В кабинет</Link>
        <Link to="/vacancies/search">Искать вакансии</Link>
      </div>

      {trackedIds.length === 0 ? <QueryStateBlock variant="empty" message="Пока нет сохраненных откликов." /> : null}
      {isLoading ? <QueryStateBlock variant="loading" message="Загрузка откликов..." /> : null}
      {hasError ? (
        <QueryStateBlock variant="error" message="Часть откликов не удалось загрузить." onRetry={retryAll} />
      ) : null}

      <div className="list">
        {applications.map((application, index) => (
          <article key={application.id} className="card">
            <h3>{toHumanTitle(vacancyQueries[index]?.data?.description ?? '', 'Отклик')}</h3>
            <p>
              Вакансия: {toHumanTitle(vacancyQueries[index]?.data?.description ?? '', 'Без названия')}
            </p>
            <p>Резюме: {toHumanTitle(resumeQueries[index]?.data?.description ?? '', 'Без описания')}</p>
            <p>Статус: {formatApplicationStatus(application.applicationStatus)}</p>
            <p>Дата: {new Date(application.applicationDate).toLocaleString('ru-RU')}</p>
            <div className="toolbar">
              <Link to={`/vacancies/${application.vacancyId}`}>К вакансии</Link>
              <button
                type="button"
                className="danger"
                disabled={deleteMutation.isPending}
                onClick={() => deleteMutation.mutate(application.id)}
              >
                Удалить
              </button>
            </div>
          </article>
        ))}
      </div>
    </main>
  );
}


