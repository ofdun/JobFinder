import {
  DraftVacancy,
  Vacancy,
  VacancyPageResponse,
  VacancyRequest,
  VacancySearchParams,
} from '../types/vacancy';
import { request } from './httpClient';

function toSearchParams(params: VacancySearchParams): string {
  const searchParams = new URLSearchParams();

  if (params.q) {
    searchParams.set('q', params.q);
  }

  if (params.limit !== undefined) {
    searchParams.set('limit', String(params.limit));
  }

  if (params.employerId !== undefined) {
    searchParams.set('employerId', String(params.employerId));
  }

  if (params.locationId !== undefined) {
    searchParams.set('locationId', String(params.locationId));
  }

  if (params.salaryMin !== undefined) {
    searchParams.set('salaryMin', String(params.salaryMin));
  }

  if (params.salaryMax !== undefined) {
    searchParams.set('salaryMax', String(params.salaryMax));
  }

  if (params.paymentFrequency) {
    searchParams.set('paymentFrequency', params.paymentFrequency);
  }

  if (params.employmentType) {
    searchParams.set('employmentType', params.employmentType);
  }

  if (params.workFormat) {
    searchParams.set('workFormat', params.workFormat);
  }

  if (params.status) {
    searchParams.set('status', params.status);
  }

  if (params.publicationDateFrom) {
    searchParams.set('publicationDateFrom', params.publicationDateFrom);
  }

  if (params.publicationDateTo) {
    searchParams.set('publicationDateTo', params.publicationDateTo);
  }

  params.skillIds?.forEach((skillId) => {
    searchParams.append('skillIds', String(skillId));
  });

  params.languageIds?.forEach((languageId) => {
    searchParams.append('languageIds', String(languageId));
  });

  if (params.offset !== undefined) {
    searchParams.set('offset', String(params.offset));
  }

  if (params.sortBy) {
    searchParams.set('sortBy', params.sortBy);
  }

  if (params.sortDesc !== undefined) {
    searchParams.set('sortDesc', String(params.sortDesc));
  }

  return searchParams.toString();
}

export function searchVacancies(params: VacancySearchParams): Promise<VacancyPageResponse> {
  const query = toSearchParams(params);
  const suffix = query ? `?${query}` : '';

  return request<VacancyPageResponse>(`/vacancies/search${suffix}`);
}

export function getVacancyById(id: number): Promise<Vacancy> {
  return request<Vacancy>(`/vacancies/${id}`);
}

export function createVacancy(payload: VacancyRequest): Promise<number> {
  return request<number>('/vacancies', {
    method: 'POST',
    auth: true,
    body: JSON.stringify(payload),
  });
}

export function updateVacancyById(id: number, payload: VacancyRequest): Promise<Vacancy> {
  return request<Vacancy>(`/vacancies/${id}`, {
    method: 'PUT',
    auth: true,
    body: JSON.stringify(payload),
  });
}

export function deleteVacancyById(id: number): Promise<void> {
  return request<void>(`/vacancies/${id}`, {
    method: 'DELETE',
    auth: true,
  });
}

export function getVacancyDrafts(vacancyId: number): Promise<DraftVacancy[]> {
  return request<DraftVacancy[]>(`/vacancies/${vacancyId}/drafts`, { auth: true });
}

export function createVacancyDraft(vacancyId: number): Promise<DraftVacancy> {
  return request<DraftVacancy>(`/vacancies/${vacancyId}/drafts`, {
    method: 'POST',
    auth: true,
  });
}

export function applyVacancyDraft(vacancyId: number, draftId: number): Promise<Vacancy> {
  return request<Vacancy>(`/vacancies/${vacancyId}/drafts/${draftId}/apply`, {
    method: 'POST',
    auth: true,
  });
}

export function deleteVacancyDraft(vacancyId: number, draftId: number): Promise<void> {
  return request<void>(`/vacancies/${vacancyId}/drafts/${draftId}`, {
    method: 'DELETE',
    auth: true,
  });
}
