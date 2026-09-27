export type EducationDegree = 'BACHELOR' | 'MASTER' | 'SPECIALIST' | 'PHD';

export interface Education {
    id?: number;
    resumeId?: number;
    educationDegree: EducationDegree;
    institutionName: string;
    faculty: string;
    department: string;
    yearOfGraduation: number;
}

export interface JobExperience {
    id?: number;
    resumeId?: number;
    position: string;
    companyName: string;
    description: string;
    startDate: string;
    endDate?: string;
}

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
    educations?: Education[];
    jobExperiences?: JobExperience[];
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
    educations: Omit<Education, 'id' | 'resumeId'>[];
    jobExperiences: Omit<JobExperience, 'id' | 'resumeId'>[];
}

export interface ResumeUpdateRequest {
    applicantId?: number;
    categoryId?: number;
    description?: string;
    skillIds?: number[];
    languageIds?: number[];
    educations?: Omit<Education, 'id' | 'resumeId'>[];
    jobExperiences?: Omit<JobExperience, 'id' | 'resumeId'>[];
}
