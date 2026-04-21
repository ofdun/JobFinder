import { FormEvent, useEffect, useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router-dom';
import { getCategories } from '../../shared/api/categoryApi';
import { getLanguages } from '../../shared/api/languageApi';
import { getSkills } from '../../shared/api/skillApi';
import { createResume } from '../../shared/api/resumeApi';
import { formatLanguageProficiency } from '../../shared/lib/format';
import { getCurrentUserId } from '../../shared/session/sessionStore';
import { MultiSelectChips } from '../../shared/ui/MultiSelectChips';

export function CreateResumePage() {
  const navigate = useNavigate();
  const userId = getCurrentUserId();
  const [categoryId, setCategoryId] = useState(1);
  const [description, setDescription] = useState('');
  const [skillIds, setSkillIds] = useState<number[]>([]);
  const [languageIds, setLanguageIds] = useState<number[]>([]);

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

  const mutation = useMutation({
    mutationFn: () =>
      createResume({
        applicantId: userId as number,
        categoryId,
        description,
        skillIds,
        languageIds,
      }),
    onSuccess: () => navigate('/me/resumes'),
  });

  if (userId === null) {
    return <p className="page error">Не удалось определить пользователя. Перелогиньтесь.</p>;
  }

  return (
    <main className="page">
      <h1>Создание резюме</h1>
      <form
        className="card"
        onSubmit={(event: FormEvent) => {
          event.preventDefault();
          mutation.mutate();
        }}
      >
        <label>
          Категория
          <select value={categoryId} onChange={(event) => setCategoryId(Number(event.target.value))} required>
            {categoriesQuery.data?.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </label>
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
        <label>Описание<textarea value={description} onChange={(event) => setDescription(event.target.value)} rows={6} required /></label>
        {mutation.isError ? <p className="error">Не удалось создать резюме.</p> : null}
        <div className="toolbar">
          <button type="submit" disabled={mutation.isPending}>{mutation.isPending ? 'Создаем...' : 'Создать'}</button>
          <Link to="/me/resumes">Отмена</Link>
        </div>
      </form>
    </main>
  );
}

