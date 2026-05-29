import { Employer, EmployerUpdateRequest } from '../types/employer';
import { request } from './httpClient';

export function getEmployerById(id: number): Promise<Employer> {
  return request<Employer>(`/employers/${id}`, { auth: true });
}

export function updateEmployerById(id: number, payload: EmployerUpdateRequest): Promise<Employer> {
  return request<Employer>(`/employers/${id}`, {
    method: 'PUT',
    auth: true,
    body: JSON.stringify(payload),
  });
}

export function deleteEmployerById(id: number): Promise<void> {
  return request<void>(`/employers/${id}`, {
    method: 'DELETE',
    auth: true,
  });
}

