import { FormEvent, useEffect, useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { getLanguages } from '../../shared/api/languageApi';
import { getSkills } from '../../shared/api/skillApi';
import {
  applyVacancyDraft,
  createVacancyDraft,
  deleteVacancyById,
  deleteVacancyDraft,
  getVacancyById,
  getVacancyDrafts,
  updateVacancyById,
} from '../../shared/api/vacancyApi';
import {
  formatEmploymentType,
  formatJobFormat,
  formatLanguageProficiency,
  formatPaymentFrequency,
  formatVacancyStatus,
  formatDateTimeSeconds,
  toHumanTitle,
} from '../../shared/lib/format';
import { EmploymentType, JobFormat, PaymentFrequency, VacancyStatus } from '../../shared/types/vacancy';
import { LocationTypeahead } from '../../shared/ui/LocationTypeahead';
import { MultiSelectChips } from '../../shared/ui/MultiSelectChips';

const paymentFrequencies: PaymentFrequency[] = ['HOURLY', 'WEEKLY', 'MONTHLY', 'PROJECT'];
const employmentTypes: EmploymentType[] = ['FULL_TIME', 'PART_TIME', 'FREELANCE'];
const jobFormats: JobFormat[] = ['REMOTE', 'OFFICE', 'HYBRID'];
const vacancyStatuses: VacancyStatus[] = ['ACTIVE', 'INACTIVE'];

function toIdArray(values: Array<{ id: number }> | undefined): number[] {
  if (!values || values.length === 0) {
    return [];
  }

  return values.map((value) => value.id);
}

export function EditVacancyPage() {
  const params = useParams();
  const vacancyId = Number(params.id);
  const navigate = useNavigate();

  const query = useQuery({
    queryKey: ['vacancy-edit', vacancyId],
    queryFn: () => getVacancyById(vacancyId),
    enabled: Number.isFinite(vacancyId),
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

  const draftsQuery = useQuery({
    queryKey: ['vacancy-drafts', vacancyId],
    queryFn: () => getVacancyDrafts(vacancyId),
    enabled: Number.isFinite(vacancyId),
  });

  const [employerId, setEmployerId] = useState(1);
  const [locationId, setLocationId] = useState<number | undefined>(1);
  const [salary, setSalary] = useState(0);
  const [skillIds, setSkillIds] = useState<number[]>([]);
  const [languageIds, setLanguageIds] = useState<number[]>([]);
  const [paymentFrequency, setPaymentFrequency] = useState<PaymentFrequency>('MONTHLY');
  const [experience, setExperience] = useState('');
  const [jobFormat, setJobFormat] = useState<JobFormat>('HYBRID');
  const [employmentType, setEmploymentType] = useState<EmploymentType>('FULL_TIME');
  const [description, setDescription] = useState('');
  const [address, setAddress] = useState('');
  const [status, setStatus] = useState<VacancyStatus>('ACTIVE');

  useEffect(() => {
    if (!query.data) {
      return;
    }

    setEmployerId(query.data.employerId ?? 1);
    setLocationId(query.data.location?.id);
    setSalary(query.data.salary ?? 0);
    setSkillIds(toIdArray(query.data.skills));
    setLanguageIds(toIdArray(query.data.languages));
    setPaymentFrequency(query.data.paymentFrequency ?? 'MONTHLY');
    setExperience(query.data.experience ?? '');
    setJobFormat(query.data.jobFormat ?? 'HYBRID');
    setEmploymentType(query.data.employmentType ?? 'FULL_TIME');
    setDescription(query.data.description ?? '');
    setAddress(query.data.address ?? '');
    setStatus(query.data.status ?? 'ACTIVE');
  }, [query.data]);

  const updateMutation = useMutation({
    mutationFn: () =>
      updateVacancyById(vacancyId, {
        employerId,
        locationId: locationId as number,
        salary,
        skillIds,
        languageIds,
        paymentFrequency,
        experience,
        jobFormat,
        employmentType,
        description,
        address,
        status,
      }),
    onSuccess: () => query.refetch(),
  });

  const draftMutation = useMutation({
    mutationFn: () => createVacancyDraft(vacancyId),
    onSuccess: () => draftsQuery.refetch(),
  });

  const applyDraftMutation = useMutation({
    mutationFn: (draftId: number) => applyVacancyDraft(vacancyId, draftId),
    onSuccess: () => {
      query.refetch();
      draftsQuery.refetch();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => deleteVacancyById(vacancyId),
    onSuccess: () => navigate('/me/vacancies'),
  });

  const deleteDraftMutation = useMutation({
    mutationFn: (draftId: number) => deleteVacancyDraft(vacancyId, draftId),
    onSuccess: () => draftsQuery.refetch(),
  });

  if (!Number.isFinite(vacancyId)) {
    return <p className="page error">Неверный адрес вакансии.</p>;
  }

  return (
    <main className="page">
      <h1>{toHumanTitle(query.data?.description ?? '', 'Редактирование вакансии')}</h1>
      {query.isLoading ? <p>Загрузка...</p> : null}
      {query.isError ? <p className="error">Не удалось загрузить вакансию.</p> : null}

      <form
        className="card"
        onSubmit={(event: FormEvent) => {
          event.preventDefault();
          updateMutation.mutate();
        }}
      >
        <label>Работодатель (код)<input type="number" min={1} value={employerId} onChange={(event) => setEmployerId(Number(event.target.value))} required /></label>
        <LocationTypeahead label="Локация" value={locationId} onChange={setLocationId} />
        <label>Зарплата<input type="number" min={0} value={salary} onChange={(event) => setSalary(Number(event.target.value))} required /></label>
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

        <label>
          Частота выплаты
          <select value={paymentFrequency} onChange={(event) => setPaymentFrequency(event.target.value as PaymentFrequency)}>
            {paymentFrequencies.map((option) => (
              <option key={option} value={option}>{formatPaymentFrequency(option)}</option>
            ))}
          </select>
        </label>

        <label>
          Формат
          <select value={jobFormat} onChange={(event) => setJobFormat(event.target.value as JobFormat)}>
            {jobFormats.map((option) => (
              <option key={option} value={option}>{formatJobFormat(option)}</option>
            ))}
          </select>
        </label>

        <label>
          Тип занятости
          <select value={employmentType} onChange={(event) => setEmploymentType(event.target.value as EmploymentType)}>
            {employmentTypes.map((option) => (
              <option key={option} value={option}>{formatEmploymentType(option)}</option>
            ))}
          </select>
        </label>

        <label>
          Статус
          <select value={status} onChange={(event) => setStatus(event.target.value as VacancyStatus)}>
            {vacancyStatuses.map((option) => (
              <option key={option} value={option}>{formatVacancyStatus(option)}</option>
            ))}
          </select>
        </label>

        <label>Опыт<input value={experience} onChange={(event) => setExperience(event.target.value)} required /></label>
        <label>Описание<textarea rows={6} value={description} onChange={(event) => setDescription(event.target.value)} required /></label>
        <label>Адрес<input value={address} onChange={(event) => setAddress(event.target.value)} required /></label>

        <section className="card">
          <h2>Версии вакансии</h2>
          {draftsQuery.isLoading ? <p>Загрузка версий...</p> : null}
          {draftsQuery.isError ? <p className="error">Не удалось загрузить версии.</p> : null}
          {draftsQuery.data && draftsQuery.data.length > 0 ? (
            <ul>
              {draftsQuery.data.map((draft) => (
                <li key={draft.id}>
                  <span>{formatDateTimeSeconds(draft.versionTimestamp)}</span>
                  <button
                    type="button"
                    className="button-small"
                    onClick={() => applyDraftMutation.mutate(draft.id)}
                    disabled={applyDraftMutation.isPending}
                  >
                    {applyDraftMutation.isPending ? 'Загружаем...' : 'Загрузить версию'}
                  </button>
                  <button
                    type="button"
                    className="danger button-small"
                    onClick={() => deleteDraftMutation.mutate(draft.id)}
                    disabled={deleteDraftMutation.isPending}
                  >
                    {deleteDraftMutation.isPending ? 'Удаляем...' : 'Удалить версию'}
                  </button>
                </li>
              ))}
            </ul>
          ) : (
            <p>Версий пока нет.</p>
          )}
          <button type="button" onClick={() => draftMutation.mutate()} disabled={draftMutation.isPending}>
            {draftMutation.isPending ? 'Сохраняем...' : 'Сохранить версию'}
          </button>
        </section>

        <div className="toolbar">
          <button type="submit" disabled={updateMutation.isPending || !locationId}>{updateMutation.isPending ? 'Сохраняем...' : 'Сохранить'}</button>
          <button type="button" className="danger" disabled={deleteMutation.isPending} onClick={() => deleteMutation.mutate()}>
            {deleteMutation.isPending ? 'Удаляем...' : 'Удалить'}
          </button>
          <Link to="/me/vacancies">Назад</Link>
        </div>
      </form>
    </main>
  );
}

