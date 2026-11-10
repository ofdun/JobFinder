import {
  ApplicantRegisterRequest,
  EmployerRegisterRequest,
  LoginRequest,
  LoginResult,
  UserRole,
} from '../types/auth';
import { request } from './httpClient';

export function login(role: UserRole, payload: LoginRequest): Promise<LoginResult> {
  return request<LoginResult>(`/auth/${role}/login`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function verifyTotp(challengeToken: string, code: string): Promise<LoginResult> {
  return request<LoginResult>('/auth/security/totp/verify', {
    method: 'POST', body: JSON.stringify({ challengeToken, code }),
  });
}

export function renewPassword(challengeToken: string, newPassword: string): Promise<void> {
  return request<void>('/auth/security/password/renew', {
    method: 'POST', body: JSON.stringify({ challengeToken, newPassword }),
  });
}

export function recoverAccount(role: UserRole, email: string, password: string, recoveryCode: string, newPassword: string, resetAuthenticator = false): Promise<void> {
  return request<void>('/auth/security/recover', {
    method: 'POST', body: JSON.stringify({ userType: role.toUpperCase(), email, password, recoveryCode, newPassword, resetAuthenticator }),
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

