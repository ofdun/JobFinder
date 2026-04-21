import { Resume, ResumePageResponse, ResumeRequest, ResumeUpdateRequest } from '../types/resume';
import { request } from './httpClient';

interface ResumeSearchParams {
  q?: string;
  applicantId?: number;
  categoryId?: number;
  creationDateFrom?: string;
  creationDateTo?: string;
  skillIds?: number[];
  languageIds?: number[];
  limit?: number;
  offset?: number;
  sortBy?: string;
  sortDesc?: boolean;
}

function toSearchParams(params: ResumeSearchParams): string {
  const searchParams = new URLSearchParams();

  if (params.applicantId !== undefined) {
    searchParams.set('applicantId', String(params.applicantId));
  }

  if (params.q) {
    searchParams.set('q', params.q);
  }

  if (params.categoryId !== undefined) {
    searchParams.set('categoryId', String(params.categoryId));
  }

  if (params.creationDateFrom) {
    searchParams.set('creationDateFrom', params.creationDateFrom);
  }

  if (params.creationDateTo) {
    searchParams.set('creationDateTo', params.creationDateTo);
  }

  params.skillIds?.forEach((skillId) => {
    searchParams.append('skillIds', String(skillId));
  });

  params.languageIds?.forEach((languageId) => {
    searchParams.append('languageIds', String(languageId));
  });

  if (params.limit !== undefined) {
    searchParams.set('limit', String(params.limit));
  }

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

export function searchResumes(params: ResumeSearchParams): Promise<ResumePageResponse> {
  const query = toSearchParams(params);
  const suffix = query ? `?${query}` : '';

  return request<ResumePageResponse>(`/resumes/search${suffix}`, { auth: true });
}

export function getResumeById(id: number): Promise<Resume> {
  return request<Resume>(`/resumes/${id}`, { auth: true });
}

export function createResume(payload: ResumeRequest): Promise<number> {
  return request<number>('/resumes', {
    method: 'POST',
    auth: true,
    body: JSON.stringify(payload),
  });
}

export function updateResumeById(id: number, payload: ResumeUpdateRequest): Promise<Resume> {
  return request<Resume>(`/resumes/${id}`, {
    method: 'PUT',
    auth: true,
    body: JSON.stringify(payload),
  });
}

export function deleteResumeById(id: number): Promise<void> {
  return request<void>(`/resumes/${id}`, {
    method: 'DELETE',
    auth: true,
  });
}

