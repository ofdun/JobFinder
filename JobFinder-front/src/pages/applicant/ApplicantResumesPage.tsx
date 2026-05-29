import { useMutation, useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { deleteResumeById, searchResumes } from '../../shared/api/resumeApi';
import { toHumanTitle } from '../../shared/lib/format';
import { getCurrentUserId } from '../../shared/session/sessionStore';
import { QueryStateBlock } from '../../shared/ui/QueryStateBlock';

export function ApplicantResumesPage() {
  const userId = getCurrentUserId();

  const query = useQuery({
    queryKey: ['my-resumes', userId],
    queryFn: () => searchResumes({ applicantId: userId as number, limit: 20, offset: 0 }),
    enabled: userId !== null,
  });

  const deleteMutation = useMutation({
    mutationFn: (resumeId: number) => deleteResumeById(resumeId),
    onSuccess: () => query.refetch(),
  });

  if (userId === null) {
    return <p className="page error">Не удалось определить пользователя. Перелогиньтесь.</p>;
  }

  return (
    <main className="page">
      <h1>Мои резюме</h1>
      <div className="toolbar">
        <Link to="/me/resumes/new">Создать резюме</Link>
        <Link to="/me/applicant">Назад в кабинет</Link>
      </div>

      {query.isLoading ? <QueryStateBlock variant="loading" message="Загрузка резюме..." /> : null}
      {query.isError ? (
        <QueryStateBlock
          variant="error"
          message="Не удалось загрузить список резюме."
          onRetry={() => query.refetch()}
        />
      ) : null}

      <div className="list">
        {query.data?.items.map((resume) => (
          <article className="card" key={resume.id}>
            <h3>{toHumanTitle(resume.description, 'Резюме')}</h3>
            <p>{resume.description}</p>
            <p>Категория: {resume.category?.name ?? 'Не указана'}</p>
            <div className="toolbar">
              <Link to={`/me/resumes/${resume.id}/edit`}>Редактировать</Link>
              <button type="button" className="danger" onClick={() => deleteMutation.mutate(resume.id)}>
                Удалить
              </button>
            </div>
          </article>
        ))}
      </div>

      {query.data && query.data.items.length === 0 ? (
        <QueryStateBlock variant="empty" message="Пока нет резюме. Создайте первое." />
      ) : null}
    </main>
  );
}

