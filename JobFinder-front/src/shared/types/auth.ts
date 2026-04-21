export type UserRole = 'applicant' | 'employer';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface TokenPair {
  accessToken: string;
  refreshToken: string;
}

export interface ApplicantRegisterRequest {
  name: string;
  email: string;
  password: string;
  address: string;
  phoneNumber: string;
  locationId: number;
}

export interface EmployerRegisterRequest {
  name: string;
  email: string;
  password: string;
  description: string;
  address: string;
  websiteLink: string;
  locationId: number;
}

export interface SessionState {
  role: UserRole;
  userId: number;
  accessToken: string;
  refreshToken: string;
}

