export type ApplicationStatus = 'NEW' | 'INVITATION' | 'REJECTION';

export interface ApplicationRequest {
  vacancyId: number;
  resumeId: number;
  applicationStatus: ApplicationStatus;
}

export interface Application {
  id: number;
  vacancyId: number;
  resumeId: number;
  applicationDate: string;
  applicationStatus: ApplicationStatus;
}

export interface ApplicationStatusUpdateRequest {
  applicationStatus: ApplicationStatus;
}

