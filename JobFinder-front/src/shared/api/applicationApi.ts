import { Application, ApplicationRequest, ApplicationStatusUpdateRequest } from '../types/application';
import { request } from './httpClient';

export function createApplication(payload: ApplicationRequest): Promise<number> {
  return request<number>('/applications', {
    method: 'POST',
    auth: true,
    body: JSON.stringify(payload),
  });
}

export function getApplicationById(id: number): Promise<Application> {
  return request<Application>(`/applications/${id}`, { auth: true });
}

export function getVacancyApplications(vacancyId: number): Promise<Application[]> {
  return request<Application[]>(`/vacancies/${vacancyId}/applications`, { auth: true });
}

export function updateApplicationStatusById(id: number, payload: ApplicationStatusUpdateRequest): Promise<Application> {
  return request<Application>(`/applications/${id}/status`, {
    method: 'PATCH',
    auth: true,
    body: JSON.stringify(payload),
  });
}

export function deleteApplicationById(id: number): Promise<void> {
  return request<void>(`/applications/${id}`, {
    method: 'DELETE',
    auth: true,
  });
}

