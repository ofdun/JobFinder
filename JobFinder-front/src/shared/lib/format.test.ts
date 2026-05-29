import { describe, expect, it } from 'vitest';
import {
  formatDate,
  formatEmploymentType,
  formatJobFormat,
  formatLanguageProficiency,
  formatPaymentFrequency,
  formatSalary,
} from './format';

describe('format helpers', () => {
  it('formats salary in ru locale', () => {
    const salary = formatSalary({
      id: 1,
      employerId: 1,
      location: { id: 1, city: 'Moscow', country: 'RU' },
      salary: 150000,
      skills: [],
      languages: [],
      paymentFrequency: 'MONTHLY',
      experience: '2 years',
      jobFormat: 'REMOTE',
      employmentType: 'FULL_TIME',
      description: 'desc',
      publicationDate: '2026-01-01T00:00:00Z',
      address: 'test',
    });

    expect(salary).toContain('150');
    expect(salary).toContain('в месяц');
  });

  it('formats ISO date', () => {
    expect(formatDate('2026-01-01T00:00:00Z')).toBeTruthy();
  });

  it('formats payment frequency for filters', () => {
    expect(formatPaymentFrequency('MONTHLY')).toBe('Ежемесячная');
    expect(formatPaymentFrequency('HOURLY')).toBe('Почасовая');
    expect(formatPaymentFrequency('WEEKLY')).toBe('Еженедельная');
    expect(formatPaymentFrequency('PROJECT')).toBe('За проект');
    expect(formatPaymentFrequency('payment')).toBe('payment');
  });

  it('formats employment type for filters', () => {
    expect(formatEmploymentType('FULL_TIME')).toBe('Полная занятость');
    expect(formatEmploymentType('PART_TIME')).toBe('Частичная занятость');
    expect(formatEmploymentType('FREELANCE')).toBe('Фриланс');
    expect(formatEmploymentType('unknown')).toBe('unknown');
  });

  it('formats job format for filters', () => {
    expect(formatJobFormat('REMOTE')).toBe('Удаленно');
    expect(formatJobFormat('OFFICE')).toBe('В офисе');
    expect(formatJobFormat('HYBRID')).toBe('Гибрид');
    expect(formatJobFormat('unknown')).toBe('unknown');
  });

  it('formats language proficiency in russian', () => {
    expect(formatLanguageProficiency('B2')).toContain('Выше среднего');
    expect(formatLanguageProficiency('UNKNOWN')).toBe('UNKNOWN');
  });
});

