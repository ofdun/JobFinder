import { Vacancy } from '../types/vacancy';
import { ApplicationStatus } from '../types/application';

const paymentFrequencySalaryLabels: Record<Vacancy['paymentFrequency'], string> = {
  HOURLY: 'в час',
  WEEKLY: 'в неделю',
  MONTHLY: 'в месяц',
  PROJECT: 'за проект',
};

const paymentFrequencyLabels: Record<Vacancy['paymentFrequency'], string> = {
  HOURLY: 'Почасовая',
  WEEKLY: 'Еженедельная',
  MONTHLY: 'Ежемесячная',
  PROJECT: 'За проект',
};

const employmentTypeLabels: Record<Vacancy['employmentType'], string> = {
  FULL_TIME: 'Полная занятость',
  PART_TIME: 'Частичная занятость',
  FREELANCE: 'Фриланс',
};

const jobFormatLabels: Record<Vacancy['jobFormat'], string> = {
  REMOTE: 'Удаленно',
  OFFICE: 'В офисе',
  HYBRID: 'Гибрид',
};

const applicationStatusLabels: Record<ApplicationStatus, string> = {
  NEW: 'Новый',
  INVITATION: 'Приглашение',
  REJECTION: 'Отказ',
};

const languageProficiencyLabels: Record<string, string> = {
  A1: 'A1 (Начальный)',
  A2: 'A2 (Базовый)',
  B1: 'B1 (Средний)',
  B2: 'B2 (Выше среднего)',
  C1: 'C1 (Продвинутый)',
  C2: 'C2 (В совершенстве)',
};

const moneyFormatter = new Intl.NumberFormat('ru-RU');

export function formatSalary(vacancy: Vacancy): string {
  return `${moneyFormatter.format(vacancy.salary)} (${paymentFrequencySalaryLabels[vacancy.paymentFrequency]})`;
}

export function formatPaymentFrequency(value: Vacancy['paymentFrequency'] | string): string {
  return paymentFrequencyLabels[value as Vacancy['paymentFrequency']] ?? value;
}

export function formatEmploymentType(value: Vacancy['employmentType'] | string): string {
  return employmentTypeLabels[value as Vacancy['employmentType']] ?? value;
}

export function formatJobFormat(value: Vacancy['jobFormat'] | string): string {
  return jobFormatLabels[value as Vacancy['jobFormat']] ?? value;
}

export function formatApplicationStatus(value: ApplicationStatus): string {
  return applicationStatusLabels[value];
}

export function formatLanguageProficiency(value: string): string {
  return languageProficiencyLabels[value] ?? value;
}

export function toHumanTitle(value: string, fallback: string, maxLength = 80): string {
  const normalized = value.trim();
  if (!normalized) {
    return fallback;
  }

  if (normalized.length <= maxLength) {
    return normalized;
  }

  return `${normalized.slice(0, maxLength)}...`;
}

export function formatDate(dateIso: string): string {
  return new Intl.DateTimeFormat('ru-RU', {
    dateStyle: 'medium',
  }).format(new Date(dateIso));
}

