import {
  ApplicantRegisterRequest,
  EmployerRegisterRequest,
  LoginRequest,
  TokenPair,
  UserRole,
} from '../types/auth';
import { request } from './httpClient';

export function login(role: UserRole, payload: LoginRequest): Promise<TokenPair> {
  return request<TokenPair>(`/auth/${role}/login`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function registerApplicant(payload: ApplicantRegisterRequest): Promise<number> {
  return request<number>('/applicants', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function registerEmployer(payload: EmployerRegisterRequest): Promise<number> {
  return request<number>('/employers', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

