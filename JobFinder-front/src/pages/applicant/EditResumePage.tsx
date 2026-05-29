import { FormEvent, useEffect, useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { getCategories } from '../../shared/api/categoryApi';
import { getLanguages } from '../../shared/api/languageApi';
import { getSkills } from '../../shared/api/skillApi';
import { deleteResumeById, getResumeById, updateResumeById } from '../../shared/api/resumeApi';
import { formatLanguageProficiency } from '../../shared/lib/format';
import { MultiSelectChips } from '../../shared/ui/MultiSelectChips';

export function EditResumePage() {
  const params = useParams();
  const resumeId = Number(params.id);
  const navigate = useNavigate();

  const query = useQuery({
    queryKey: ['resume', resumeId],
    queryFn: () => getResumeById(resumeId),
    enabled: Number.isFinite(resumeId),
  });

  const categoriesQuery = useQuery({
    queryKey: ['categories'],
    queryFn: getCategories,
    staleTime: 1000 * 60 * 60,
  });

  const skillsQuery = useQuery({
    queryKey: ['skills'],
    queryFn: getSkills,
    staleTime: 1000 * 60 * 60,
  });

  const languagesQuery = useQuery({
    queryKey: ['languages'],
    queryFn: getLanguages,
    staleTime: 1000 * 60 * 60,
  });

  const [categoryId, setCategoryId] = useState(1);
  const [skillIds, setSkillIds] = useState<number[]>([]);
  const [languageIds, setLanguageIds] = useState<number[]>([]);
  const [description, setDescription] = useState('');

  useEffect(() => {
    if (!query.data) {
      return;
    }

    setCategoryId(query.data.category?.id ?? 1);
    setSkillIds(query.data.skills?.map((skill) => skill.id) ?? []);
    setLanguageIds(query.data.languages?.map((language) => language.id) ?? []);
    setDescription(query.data.description ?? '');
  }, [query.data]);

  useEffect(() => {
    if (!categoriesQuery.data || categoriesQuery.data.length === 0) {
      return;
    }

    setCategoryId((current) => {
      if (categoriesQuery.data.some((category) => category.id === current)) {
        return current;
      }

      return categoriesQuery.data[0].id;
    });
  }, [categoriesQuery.data]);

  const updateMutation = useMutation({
    mutationFn: () => updateResumeById(resumeId, { categoryId, description, skillIds, languageIds }),
    onSuccess: () => query.refetch(),
  });

  const deleteMutation = useMutation({
    mutationFn: () => deleteResumeById(resumeId),
    onSuccess: () => navigate('/me/resumes'),
  });

  if (!Number.isFinite(resumeId)) {
    return <p className="page error">Неверный адрес резюме.</p>;
  }

  return (
    <main className="page">
      <h1>Редактирование резюме</h1>
      {query.isLoading ? <p>Загрузка...</p> : null}
      {query.isError ? <p className="error">Не удалось загрузить резюме.</p> : null}

      <form
        className="card"
        onSubmit={(event: FormEvent) => {
          event.preventDefault();
          updateMutation.mutate();
        }}
      >
        {categoriesQuery.isLoading ? <p>Загрузка категорий...</p> : null}
        {categoriesQuery.isError ? <p className="error">Не удалось загрузить категории.</p> : null}
        <MultiSelectChips
          label="Навыки"
          options={skillsQuery.data ?? []}
          selectedIds={skillIds}
          onChange={setSkillIds}
          placeholder="Добавить навык"
        />
        {skillsQuery.isLoading ? <p>Загрузка навыков...</p> : null}
        {skillsQuery.isError ? <p className="error">Не удалось загрузить навыки.</p> : null}
        <MultiSelectChips
          label="Языки"
          options={languagesQuery.data?.map((language) => ({
            id: language.id,
            name: `${language.name} (${formatLanguageProficiency(language.proficiencyLevel)})`,
          })) ?? []}
          selectedIds={languageIds}
          onChange={setLanguageIds}
          placeholder="Добавить язык"
        />
        {languagesQuery.isLoading ? <p>Загрузка языков...</p> : null}
        {languagesQuery.isError ? <p className="error">Не удалось загрузить языки.</p> : null}
        {!categoriesQuery.isError && categoriesQuery.data ? (
          <label>
            Категория
            <select value={categoryId} onChange={(event) => setCategoryId(Number(event.target.value))}>
              {categoriesQuery.data.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </label>
        ) : null}
        <label>Описание<textarea value={description} onChange={(event) => setDescription(event.target.value)} rows={6} required /></label>
        <div className="toolbar">
          <button type="submit" disabled={updateMutation.isPending}>{updateMutation.isPending ? 'Сохраняем...' : 'Сохранить'}</button>
          <button type="button" className="danger" onClick={() => deleteMutation.mutate()} disabled={deleteMutation.isPending}>
            {deleteMutation.isPending ? 'Удаляем...' : 'Удалить'}
          </button>
          <Link to="/me/resumes">Назад</Link>
        </div>
      </form>
    </main>
  );
}

