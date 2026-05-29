import { useQuery } from '@tanstack/react-query';
import { Link, useSearchParams } from 'react-router-dom';
import { getLanguages } from '../../shared/api/languageApi';
import { getSkills } from '../../shared/api/skillApi';
import { searchVacancies } from '../../shared/api/vacancyApi';
import {
  formatDate,
  formatEmploymentType,
  formatJobFormat,
  formatLanguageProficiency,
  formatPaymentFrequency,
  formatSalary,
} from '../../shared/lib/format';
import { useDebouncedValue } from '../../shared/lib/useDebouncedValue';
import { EmploymentType, JobFormat, PaymentFrequency } from '../../shared/types/vacancy';
import { LocationTypeahead } from '../../shared/ui/LocationTypeahead';
import { MultiSelectChips } from '../../shared/ui/MultiSelectChips';
import { QueryStateBlock } from '../../shared/ui/QueryStateBlock';

const PAGE_SIZE = 10;
const paymentFrequencies: PaymentFrequency[] = ['HOURLY', 'WEEKLY', 'MONTHLY', 'PROJECT'];
const employmentTypes: EmploymentType[] = ['FULL_TIME', 'PART_TIME', 'FREELANCE'];
const jobFormats: JobFormat[] = ['REMOTE', 'OFFICE', 'HYBRID'];

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

function parsePaymentFrequency(value: string): PaymentFrequency | undefined {
  if (value === 'HOURLY' || value === 'WEEKLY' || value === 'MONTHLY' || value === 'PROJECT') {
    return value;
  }

  return undefined;
}

function parseEmploymentType(value: string): EmploymentType | undefined {
  if (value === 'FULL_TIME' || value === 'PART_TIME' || value === 'FREELANCE') {
    return value;
  }

  return undefined;
}

function parseWorkFormat(value: string): JobFormat | undefined {
  if (value === 'REMOTE' || value === 'OFFICE' || value === 'HYBRID') {
    return value;
  }

  return undefined;
}

function buildCardTitle(description: string): string {
  const normalized = description.trim();
  if (!normalized) {
    return 'Вакансия';
  }

  if (normalized.length <= 80) {
    return normalized;
  }

  return `${normalized.slice(0, 80)}...`;
}

export function VacancySearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const q = searchParams.get('q') ?? '';
  const salaryMin = searchParams.get('salaryMin') ?? '';
  const salaryMax = searchParams.get('salaryMax') ?? '';
  const locationId = searchParams.get('locationId') ?? '';
  const publicationDateFrom = searchParams.get('publicationDateFrom') ?? '';
  const publicationDateTo = searchParams.get('publicationDateTo') ?? '';
  const skillIdsText = searchParams.get('skillIds') ?? '';
  const languageIdsText = searchParams.get('languageIds') ?? '';
  const paymentFrequency = searchParams.get('paymentFrequency') ?? '';
  const employmentType = searchParams.get('employmentType') ?? '';
  const workFormat = searchParams.get('workFormat') ?? '';
  const sortBy = searchParams.get('sortBy') ?? '';
  const sortDesc = searchParams.get('sortDesc') === 'true';
  const page = parsePage(searchParams.get('page'));
  const debouncedQ = useDebouncedValue(q, 350);

  const setQueryState = (key: string, value: string, nextPage = 1) => {
    const next = new URLSearchParams(searchParams);

    if (value.trim()) {
      next.set(key, value.trim());
    } else {
      next.delete(key);
    }

    next.set('page', String(nextPage));
    setSearchParams(next);
  };

  const query = useQuery({
    queryKey: [
      'vacancies',
      debouncedQ,
      salaryMin,
      salaryMax,
      locationId,
      publicationDateFrom,
      publicationDateTo,
      skillIdsText,
      languageIdsText,
      paymentFrequency,
      employmentType,
      workFormat,
      sortBy,
      sortDesc,
      page,
    ],
    queryFn: () =>
      searchVacancies({
        q: debouncedQ || undefined,
        salaryMin: parseOptionalNumber(salaryMin),
        salaryMax: parseOptionalNumber(salaryMax),
        locationId: parseOptionalNumber(locationId),
        publicationDateFrom: publicationDateFrom || undefined,
        publicationDateTo: publicationDateTo || undefined,
        skillIds: parseIdList(skillIdsText),
        languageIds: parseIdList(languageIdsText),
        paymentFrequency: parsePaymentFrequency(paymentFrequency),
        employmentType: parseEmploymentType(employmentType),
        workFormat: parseWorkFormat(workFormat),
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

  return (
    <main className="page">
      <h1>Поиск вакансий</h1>
      <div className="vacancy-layout">
        <section className="vacancy-main">
          <div className="toolbar">
            <input
              value={q}
              onChange={(event) => setQueryState('q', event.target.value)}
              placeholder="Поиск по описанию"
            />
          </div>

          {query.isLoading ? <QueryStateBlock variant="loading" message="Загрузка вакансий..." /> : null}
          {query.isError ? (
            <QueryStateBlock
              variant="error"
              message={query.error.message || 'Ошибка загрузки вакансий.'}
              onRetry={() => query.refetch()}
            />
          ) : null}
          {!query.isLoading && !query.isError && query.data && query.data.items.length === 0 ? (
            <QueryStateBlock variant="empty" message="По вашему запросу вакансии не найдены." />
          ) : null}

          <div className="list">
            {query.data?.items.map((vacancy) => (
              <article key={vacancy.id} className="card">
                <h3>{buildCardTitle(vacancy.description)}</h3>
                <p>Формат: {formatJobFormat(vacancy.jobFormat)}</p>
                <p>Занятость: {formatEmploymentType(vacancy.employmentType)}</p>
                <p>Зарплата: {formatSalary(vacancy)}</p>
                <p>Опубликовано: {formatDate(vacancy.publicationDate)}</p>
                <Link to={`/vacancies/${vacancy.id}`}>Подробнее</Link>
              </article>
            ))}
          </div>

          <div className="toolbar pagination-toolbar">
            <button type="button" disabled={page === 1} onClick={() => setQueryState('page', String(page - 1), page - 1)}>
              Назад
            </button>
            <span>Страница {page}</span>
            <button
              type="button"
              disabled={!query.data || query.data.totalPages === 0 || page >= query.data.totalPages}
              onClick={() => setQueryState('page', String(page + 1), page + 1)}
            >
              Вперед
            </button>
          </div>
        </section>

        <aside className="vacancy-sidebar card vacancy-filter-card">
          <h3>Фильтры</h3>
          <div className="toolbar">
          <label>
            Зарплата от
            <input type="number" min={0} value={salaryMin} onChange={(event) => setQueryState('salaryMin', event.target.value)} />
          </label>
          <label>
            Зарплата до
            <input type="number" min={0} value={salaryMax} onChange={(event) => setQueryState('salaryMax', event.target.value)} />
          </label>
          <label>
            Локация
            <LocationTypeahead
              label=""
              value={parseOptionalNumber(locationId)}
              onChange={(next) => setQueryState('locationId', next ? String(next) : '')}
              placeholder="Введите город или страну"
            />
          </label>
          </div>

          <div className="toolbar">
          <label>
            Дата публикации от
            <input
              type="datetime-local"
              value={publicationDateFrom}
              onChange={(event) => setQueryState('publicationDateFrom', event.target.value)}
            />
          </label>
          <label>
            Дата публикации до
            <input
              type="datetime-local"
              value={publicationDateTo}
              onChange={(event) => setQueryState('publicationDateTo', event.target.value)}
            />
          </label>
          </div>

          <MultiSelectChips
            label="Навыки"
            options={skillsQuery.data ?? []}
            selectedIds={parseIdListForUi(skillIdsText)}
            onChange={(next) => setQueryState('skillIds', stringifyIdList(next))}
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
            onChange={(next) => setQueryState('languageIds', stringifyIdList(next))}
            placeholder="Добавить язык"
          />
          {languagesQuery.isLoading ? <p>Загрузка языков...</p> : null}
          {languagesQuery.isError ? <p className="error">Не удалось загрузить языки.</p> : null}

          <div className="toolbar">
          <label>
            Частота выплат
            <select value={paymentFrequency} onChange={(event) => setQueryState('paymentFrequency', event.target.value)}>
              <option value="">Любая</option>
              {paymentFrequencies.map((option) => (
                <option key={option} value={option}>
                  {formatPaymentFrequency(option)}
                </option>
              ))}
            </select>
          </label>
          <label>
            Занятость
            <select value={employmentType} onChange={(event) => setQueryState('employmentType', event.target.value)}>
              <option value="">Любой</option>
              {employmentTypes.map((option) => (
                <option key={option} value={option}>
                  {formatEmploymentType(option)}
                </option>
              ))}
            </select>
          </label>
          <label>
            Формат работы
            <select value={workFormat} onChange={(event) => setQueryState('workFormat', event.target.value)}>
              <option value="">Любой</option>
              {jobFormats.map((option) => (
                <option key={option} value={option}>
                  {formatJobFormat(option)}
                </option>
              ))}
            </select>
          </label>
          </div>

          <div className="toolbar">
          <label>
            Сортировка
            <select value={sortBy} onChange={(event) => setQueryState('sortBy', event.target.value)}>
              <option value="">Без сортировки</option>
              <option value="publicationDate">По дате публикации</option>
              <option value="salary">По зарплате</option>
            </select>
          </label>

          <label className="inline-checkbox">
            <input
              type="checkbox"
              checked={sortDesc}
              onChange={(event) => setQueryState('sortDesc', String(event.target.checked))}
            />
            По убыванию
          </label>
          </div>
        </aside>
      </div>
    </main>
  );
}

