import { MatchResult } from '../types/matching';
import { request } from './httpClient';

export function getVacancyMatchingCandidates(vacancyId: number, maxAmount = 20): Promise<MatchResult[]> {
  const params = new URLSearchParams();
  params.set('maxAmount', String(maxAmount));

  return request<MatchResult[]>(`/matching/vacancies/${vacancyId}/candidates?${params.toString()}`, {
    auth: true,
  });
}

