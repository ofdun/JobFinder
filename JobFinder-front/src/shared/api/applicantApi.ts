import { Applicant, ApplicantUpdateRequest } from '../types/applicant';
import { request } from './httpClient';

export function getApplicantById(id: number): Promise<Applicant> {
  return request<Applicant>(`/applicants/${id}`, { auth: true });
}

export function updateApplicantById(id: number, payload: ApplicantUpdateRequest): Promise<Applicant> {
  return request<Applicant>(`/applicants/${id}`, {
    method: 'PUT',
    auth: true,
    body: JSON.stringify(payload),
  });
}

export function deleteApplicantById(id: number): Promise<void> {
  return request<void>(`/applicants/${id}`, {
    method: 'DELETE',
    auth: true,
  });
}

