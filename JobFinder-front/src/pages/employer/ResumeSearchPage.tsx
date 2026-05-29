import { useQuery } from '@tanstack/react-query';
import { Link, useSearchParams } from 'react-router-dom';
import { getCategories } from '../../shared/api/categoryApi';
import { getLanguages } from '../../shared/api/languageApi';
import { getSkills } from '../../shared/api/skillApi';
import { searchResumes } from '../../shared/api/resumeApi';
import { formatLanguageProficiency, toHumanTitle } from '../../shared/lib/format';
import { useDebouncedValue } from '../../shared/lib/useDebouncedValue';
import { MultiSelectChips } from '../../shared/ui/MultiSelectChips';
import { QueryStateBlock } from '../../shared/ui/QueryStateBlock';

const PAGE_SIZE = 10;

function parsePage(value: string | null): number {
  const parsed = Number(value);
  if (!Number.isInteger(parsed) || parsed < 1) {
    return 1;
  }

  return parsed;
}

function parseOptionalNumber(value: string | null): number | undefined {
  if (!value || !value.trim()) {
    return undefined;
  }

  const parsed = Number(value);
  if (!Number.isFinite(parsed)) {
    return undefined;
  }

  return parsed;
}

function parseIdList(value: string | null): number[] | undefined {
  if (!value || !value.trim()) {
    return undefined;
  }

  const items = value
    .split(',')
    .map((item) => Number(item.trim()))
    .filter((item) => Number.isInteger(item) && item > 0);

  return items.length > 0 ? items : undefined;
}

function parseIdListForUi(value: string | null): number[] {
  return parseIdList(value) ?? [];
}

function stringifyIdList(value: number[]): string {
  return value.join(',');
}

export function ResumeSearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  const q = searchParams.get('q') ?? '';
  const applicantId = searchParams.get('applicantId') ?? '';
  const categoryId = searchParams.get('categoryId') ?? '';
  const creationDateFrom = searchParams.get('creationDateFrom') ?? '';
  const creationDateTo = searchParams.get('creationDateTo') ?? '';
  const skillIdsText = searchParams.get('skillIds') ?? '';
  const languageIdsText = searchParams.get('languageIds') ?? '';
  const sortBy = searchParams.get('sortBy') ?? '';
  const sortDesc = searchParams.get('sortDesc') === 'true';
  const page = parsePage(searchParams.get('page'));

  const debouncedQ = useDebouncedValue(q, 350);

  const setParam = (key: string, value: string, resetPage = true) => {
    const next = new URLSearchParams(searchParams);

    if (value.trim()) {
      next.set(key, value.trim());
    } else {
      next.delete(key);
    }

    if (resetPage) {
      next.set('page', '1');
    }

    if (!next.has('page')) {
      next.set('page', '1');
    }

    setSearchParams(next);
  };

  const query = useQuery({
    queryKey: [
      'employer-resume-search',
      debouncedQ,
      applicantId,
      categoryId,
      creationDateFrom,
      creationDateTo,
      skillIdsText,
      languageIdsText,
      sortBy,
      sortDesc,
      page,
    ],
    queryFn: () =>
      searchResumes({
        q: debouncedQ || undefined,
        applicantId: parseOptionalNumber(applicantId),
        categoryId: parseOptionalNumber(categoryId),
        creationDateFrom: creationDateFrom || undefined,
        creationDateTo: creationDateTo || undefined,
        skillIds: parseIdList(skillIdsText),
        languageIds: parseIdList(languageIdsText),
        sortBy: sortBy || undefined,
        sortDesc,
        limit: PAGE_SIZE,
        offset: (page - 1) * PAGE_SIZE,
      }),
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

  const categoriesQuery = useQuery({
    queryKey: ['categories'],
    queryFn: getCategories,
    staleTime: 1000 * 60 * 60,
  });

  return (
    <main className="page">
      <h1>Поиск резюме</h1>

      <div className="resume-layout">
        <section className="resume-main">
          {query.isLoading ? <QueryStateBlock variant="loading" message="Загрузка резюме..." /> : null}
          {query.isError ? (
            <QueryStateBlock variant="error" message="Не удалось загрузить резюме." onRetry={() => query.refetch()} />
          ) : null}
          {query.data && query.data.items.length === 0 ? (
            <QueryStateBlock variant="empty" message="По заданным фильтрам резюме не найдены." />
          ) : null}

          <div className="list">
            {query.data?.items.map((resume) => (
              <article key={resume.id} className="card">
                <h3>{toHumanTitle(resume.description, 'Резюме')}</h3>
                <p>Категория: {resume.category?.name ?? 'Не указана'}</p>
                <p>{resume.description}</p>
              </article>
            ))}
          </div>

          <div className="toolbar pagination-toolbar">
            <button type="button" disabled={page === 1} onClick={() => setParam('page', String(page - 1), false)}>
              Назад
            </button>
            <span>Страница {page}</span>
            <button
              type="button"
              disabled={!query.data || query.data.totalPages === 0 || page >= query.data.totalPages}
              onClick={() => setParam('page', String(page + 1), false)}
            >
              Вперед
            </button>
          </div>
        </section>

        <aside className="resume-sidebar card resume-filter-card">
          <h3>Фильтры</h3>
          <label>
            Поиск по описанию
            <input
              aria-label="Поиск по описанию резюме"
              value={q}
              onChange={(event) => setParam('q', event.target.value)}
              placeholder="Например: React, Java, QA"
            />
          </label>

          <div className="toolbar">
          <label>
            Соискатель (код)
            <input type="number" min={1} value={applicantId} onChange={(event) => setParam('applicantId', event.target.value)} />
          </label>
          <label>
            Категория
            <select value={categoryId} onChange={(event) => setParam('categoryId', event.target.value)}>
              <option value="">Любая</option>
              {categoriesQuery.data?.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </label>
          </div>

          <div className="toolbar">
          <label>
            Дата создания от
            <input
              type="datetime-local"
              value={creationDateFrom}
              onChange={(event) => setParam('creationDateFrom', event.target.value)}
            />
          </label>
          <label>
            Дата создания до
            <input
              type="datetime-local"
              value={creationDateTo}
              onChange={(event) => setParam('creationDateTo', event.target.value)}
            />
          </label>
          </div>

          <MultiSelectChips
            label="Навыки"
            options={skillsQuery.data ?? []}
            selectedIds={parseIdListForUi(skillIdsText)}
            onChange={(next) => setParam('skillIds', stringifyIdList(next))}
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
            selectedIds={parseIdListForUi(languageIdsText)}
            onChange={(next) => setParam('languageIds', stringifyIdList(next))}
            placeholder="Добавить язык"
          />
          {languagesQuery.isLoading ? <p>Загрузка языков...</p> : null}
          {languagesQuery.isError ? <p className="error">Не удалось загрузить языки.</p> : null}

          <div className="toolbar">
          <label>
            Сортировка
            <select value={sortBy} onChange={(event) => setParam('sortBy', event.target.value)}>
              <option value="">Без сортировки</option>
              <option value="date">По дате</option>
              <option value="id">По порядку добавления</option>
            </select>
          </label>

          <label className="inline-checkbox">
            <input
              type="checkbox"
              checked={sortDesc}
              onChange={(event) => setParam('sortDesc', String(event.target.checked))}
            />
            По убыванию
          </label>
          <Link to="/me/vacancies">К вакансиям</Link>
          </div>
        </aside>
      </div>
    </main>
  );
}

