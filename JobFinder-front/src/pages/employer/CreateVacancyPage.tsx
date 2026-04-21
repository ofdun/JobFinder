import { FormEvent, useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router-dom';
import { getLanguages } from '../../shared/api/languageApi';
import { getSkills } from '../../shared/api/skillApi';
import { createVacancy } from '../../shared/api/vacancyApi';
import {
  formatEmploymentType,
  formatJobFormat,
  formatLanguageProficiency,
  formatPaymentFrequency,
} from '../../shared/lib/format';
import { getCurrentUserId } from '../../shared/session/sessionStore';
import { EmploymentType, JobFormat, PaymentFrequency } from '../../shared/types/vacancy';
import { LocationTypeahead } from '../../shared/ui/LocationTypeahead';
import { MultiSelectChips } from '../../shared/ui/MultiSelectChips';

const paymentFrequencies: PaymentFrequency[] = ['HOURLY', 'WEEKLY', 'MONTHLY', 'PROJECT'];
const employmentTypes: EmploymentType[] = ['FULL_TIME', 'PART_TIME', 'FREELANCE'];
const jobFormats: JobFormat[] = ['REMOTE', 'OFFICE', 'HYBRID'];

export function CreateVacancyPage() {
  const navigate = useNavigate();
  const userId = getCurrentUserId();

  const [locationId, setLocationId] = useState<number | undefined>(1);
  const [salary, setSalary] = useState(100000);
  const [skillIds, setSkillIds] = useState<number[]>([]);
  const [languageIds, setLanguageIds] = useState<number[]>([]);
  const [paymentFrequency, setPaymentFrequency] = useState<PaymentFrequency>('MONTHLY');
  const [experience, setExperience] = useState('1+ year');
  const [jobFormat, setJobFormat] = useState<JobFormat>('HYBRID');
  const [employmentType, setEmploymentType] = useState<EmploymentType>('FULL_TIME');
  const [description, setDescription] = useState('');
  const [address, setAddress] = useState('');

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

  const mutation = useMutation({
    mutationFn: () =>
      createVacancy({
        employerId: userId as number,
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
      }),
    onSuccess: () => navigate('/me/vacancies'),
  });

  if (userId === null) {
    return <p className="page error">Не удалось определить пользователя. Перелогиньтесь.</p>;
  }

  return (
    <main className="page">
      <h1>Создание вакансии</h1>
      <form
        className="card"
        onSubmit={(event: FormEvent) => {
          event.preventDefault();
          mutation.mutate();
        }}
      >
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

        <label>Опыт<input value={experience} onChange={(event) => setExperience(event.target.value)} required /></label>
        <label>Описание<textarea rows={6} value={description} onChange={(event) => setDescription(event.target.value)} required /></label>
        <label>Адрес<input value={address} onChange={(event) => setAddress(event.target.value)} required /></label>

        {mutation.isError ? <p className="error">Не удалось создать вакансию.</p> : null}

        <div className="toolbar">
          <button type="submit" disabled={mutation.isPending || !locationId}>{mutation.isPending ? 'Создаем...' : 'Создать'}</button>
          <Link to="/me/vacancies">Отмена</Link>
        </div>
      </form>
    </main>
  );
}

