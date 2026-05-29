export interface Resume {
  id: number;
  applicantId: number;
  category?: {
    id: number;
    name: string;
  };
  description: string;
  skills?: Array<{
    id: number;
    name: string;
  }>;
  languages?: Array<{
    id: number;
    name: string;
    proficiencyLevel: string;
  }>;
}

export interface ResumePageResponse {
  items: Resume[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ResumeRequest {
  applicantId: number;
  categoryId: number;
  description: string;
  skillIds: number[];
  languageIds: number[];
}

export interface ResumeUpdateRequest {
  categoryId?: number;
  description?: string;
  skillIds?: number[];
  languageIds?: number[];
}

