import { useEffect, useState } from 'react';
import { useMutation, useQueries, useQuery } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { getResumeById } from '../../shared/api/resumeApi';
import { getVacancyById } from '../../shared/api/vacancyApi';
import { formatApplicationStatus, toHumanTitle } from '../../shared/lib/format';
import { getVacancyApplications, updateApplicationStatusById } from '../../shared/api/applicationApi';
import { ApplicationStatus } from '../../shared/types/application';
import { QueryStateBlock } from '../../shared/ui/QueryStateBlock';

const statuses: ApplicationStatus[] = ['NEW', 'INVITATION', 'REJECTION'];

export function VacancyApplicationsPage() {
  const params = useParams();
  const vacancyId = Number(params.id);
  const [statusById, setStatusById] = useState<Record<number, ApplicationStatus>>({});

  const query = useQuery({
    queryKey: ['vacancy-applications', vacancyId],
    queryFn: () => getVacancyApplications(vacancyId),
    enabled: Number.isFinite(vacancyId),
  });

  const vacancyQuery = useQuery({
    queryKey: ['vacancy-applications-title', vacancyId],
    queryFn: () => getVacancyById(vacancyId),
    enabled: Number.isFinite(vacancyId),
  });

  const resumeQueries = useQueries({
    queries: (query.data ?? []).map((application) => ({
      queryKey: ['vacancy-application-resume', application.resumeId],
      queryFn: () => getResumeById(application.resumeId),
    })),
  });

  useEffect(() => {
    if (!query.data) {
      return;
    }

    const nextState: Record<number, ApplicationStatus> = {};
    query.data.forEach((application) => {
      nextState[application.id] = application.applicationStatus;
    });
    setStatusById(nextState);
  }, [query.data]);

  const updateMutation = useMutation({
    mutationFn: ({ applicationId, nextStatus }: { applicationId: number; nextStatus: ApplicationStatus }) => {
      return updateApplicationStatusById(applicationId, {
        applicationStatus: nextStatus,
      });
    },
    onSuccess: () => query.refetch(),
  });

  if (!Number.isFinite(vacancyId)) {
    return <p className="page error">Неверный адрес вакансии.</p>;
  }

  return (
    <main className="page">
      <h1>Отклики: {toHumanTitle(vacancyQuery.data?.description ?? '', 'Вакансия')}</h1>
      <div className="toolbar">
        <Link to="/me/vacancies">К вакансиям</Link>
      </div>

      {query.isLoading ? <QueryStateBlock variant="loading" message="Загрузка откликов..." /> : null}
      {query.isError ? (
        <QueryStateBlock variant="error" message="Не удалось загрузить отклики." onRetry={() => query.refetch()} />
      ) : null}
      {query.data && query.data.length === 0 ? <QueryStateBlock variant="empty" message="Откликов пока нет." /> : null}

      <div className="list">
        {query.data?.map((application, index) => (
          <article key={application.id} className="card">
            <h3>{toHumanTitle(resumeQueries[index]?.data?.description ?? '', 'Отклик кандидата')}</h3>
            <p>Резюме: {toHumanTitle(resumeQueries[index]?.data?.description ?? '', 'Без описания')}</p>
            <p>Текущий статус: {formatApplicationStatus(application.applicationStatus)}</p>
            <p>Дата: {new Date(application.applicationDate).toLocaleString('ru-RU')}</p>

            <div className="toolbar">
              <select
                value={statusById[application.id] ?? application.applicationStatus}
                onChange={(event) =>
                  setStatusById((prev) => ({
                    ...prev,
                    [application.id]: event.target.value as ApplicationStatus,
                  }))
                }
              >
                {statuses.map((status) => (
                  <option key={status} value={status}>{formatApplicationStatus(status)}</option>
                ))}
              </select>
              <button
                type="button"
                onClick={() =>
                  updateMutation.mutate({
                    applicationId: application.id,
                    nextStatus: statusById[application.id] ?? application.applicationStatus,
                  })
                }
                disabled={updateMutation.isPending}
              >
                {updateMutation.isPending ? 'Обновляем...' : 'Обновить статус'}
              </button>
            </div>
          </article>
        ))}
      </div>
    </main>
  );
}

