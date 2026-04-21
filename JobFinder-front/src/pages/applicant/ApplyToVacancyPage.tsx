import { FormEvent, useMemo, useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { createApplication } from '../../shared/api/applicationApi';
import { searchResumes } from '../../shared/api/resumeApi';
import { getVacancyById } from '../../shared/api/vacancyApi';
import { toHumanTitle } from '../../shared/lib/format';
import { getCurrentUserId } from '../../shared/session/sessionStore';
import { trackApplicationId } from '../../shared/session/trackedApplications';
import { QueryStateBlock } from '../../shared/ui/QueryStateBlock';

export function ApplyToVacancyPage() {
  const params = useParams();
  const vacancyId = Number(params.id);
  const userId = getCurrentUserId();
  const [resumeId, setResumeId] = useState('');

  const resumesQuery = useQuery({
    queryKey: ['apply-resumes', userId],
    queryFn: () => searchResumes({ applicantId: userId as number, limit: 50, offset: 0 }),
    enabled: userId !== null,
  });

  const vacancyQuery = useQuery({
    queryKey: ['apply-vacancy-title', vacancyId],
    queryFn: () => getVacancyById(vacancyId),
    enabled: Number.isFinite(vacancyId),
  });

  const resumeOptions = useMemo(() => resumesQuery.data?.items ?? [], [resumesQuery.data]);

  const mutation = useMutation({
    mutationFn: () =>
      createApplication({
        vacancyId,
        resumeId: Number(resumeId),
        applicationStatus: 'NEW',
      }),
    onSuccess: (applicationId) => {
      trackApplicationId(applicationId);
    },
  });

  if (!Number.isFinite(vacancyId)) {
    return <p className="page error">Неверный адрес вакансии.</p>;
  }

  if (userId === null) {
    return <p className="page error">Не удалось определить пользователя. Перелогиньтесь.</p>;
  }

  return (
    <main className="page">
      <h1>Отклик на вакансию: {toHumanTitle(vacancyQuery.data?.description ?? '', 'Без названия')}</h1>
      <form
        className="card"
        onSubmit={(event: FormEvent) => {
          event.preventDefault();
          mutation.mutate();
        }}
      >
        <label>
          Выберите резюме
          <select value={resumeId} onChange={(event) => setResumeId(event.target.value)} required>
            <option value="">-- выберите --</option>
            {resumeOptions.map((resume) => (
              <option key={resume.id} value={String(resume.id)}>{toHumanTitle(resume.description, 'Резюме', 60)}</option>
            ))}
          </select>
        </label>

        {resumesQuery.isLoading ? <QueryStateBlock variant="loading" message="Загрузка резюме..." /> : null}
        {resumesQuery.isError ? (
          <QueryStateBlock variant="error" message="Не удалось загрузить резюме." onRetry={() => resumesQuery.refetch()} />
        ) : null}
        {!resumesQuery.isLoading && !resumesQuery.isError && resumeOptions.length === 0 ? (
          <QueryStateBlock variant="empty" message="У вас пока нет резюме. Сначала создайте его." />
        ) : null}
        {mutation.isError ? <p className="error">Не удалось отправить отклик.</p> : null}
        {mutation.isSuccess ? <p>Отклик отправлен.</p> : null}

        <div className="toolbar">
          <button type="submit" disabled={mutation.isPending || resumeOptions.length === 0}>
            {mutation.isPending ? 'Отправляем...' : 'Отправить отклик'}
          </button>
          <Link to={`/vacancies/${vacancyId}`}>К вакансии</Link>
          <Link to="/me/applications">Мои отклики</Link>
        </div>
      </form>
    </main>
  );
}

