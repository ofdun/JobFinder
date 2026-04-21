export type PaymentFrequency = 'HOURLY' | 'WEEKLY' | 'MONTHLY' | 'PROJECT';
export type EmploymentType = 'FULL_TIME' | 'PART_TIME' | 'FREELANCE';
export type JobFormat = 'REMOTE' | 'OFFICE' | 'HYBRID';

export interface Location {
  id: number;
  city: string;
  country: string;
}

export interface Skill {
  id: number;
  name: string;
}

export interface Language {
  id: number;
  name: string;
  proficiencyLevel: string;
}

export interface Vacancy {
  id: number;
  employerId: number;
  location: Location;
  salary: number;
  skills: Skill[];
  languages: Language[];
  paymentFrequency: PaymentFrequency;
  experience: string;
  jobFormat: JobFormat;
  employmentType: EmploymentType;
  description: string;
  publicationDate: string;
  address: string;
}

export interface VacancyPageResponse {
  items: Vacancy[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface VacancySearchParams {
  q?: string;
  employerId?: number;
  locationId?: number;
  salaryMin?: number;
  salaryMax?: number;
  paymentFrequency?: PaymentFrequency;
  employmentType?: EmploymentType;
  workFormat?: JobFormat;
  publicationDateFrom?: string;
  publicationDateTo?: string;
  skillIds?: number[];
  languageIds?: number[];
  limit?: number;
  offset?: number;
  sortBy?: string;
  sortDesc?: boolean;
}

export interface VacancyRequest {
  employerId: number;
  locationId: number;
  salary: number;
  skillIds: number[];
  languageIds: number[];
  paymentFrequency: PaymentFrequency;
  experience: string;
  jobFormat: JobFormat;
  employmentType: EmploymentType;
  description: string;
  address: string;
}

