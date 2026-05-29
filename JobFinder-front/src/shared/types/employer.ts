export interface Employer {
  id: number;
  name: string;
  email: string;
  description: string;
  address: string;
  websiteLink: string;
  location?: {
    id: number;
    city: string;
    country: string;
  };
}

export interface EmployerUpdateRequest {
  name: string;
  email: string;
  password?: string;
  description: string;
  address: string;
  websiteLink: string;
  locationId: number;
}

