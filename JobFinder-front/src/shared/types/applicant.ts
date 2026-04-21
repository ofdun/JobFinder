export interface Applicant {
  id: number;
  name: string;
  email: string;
  address: string;
  phoneNumber: string;
  location?: {
    id: number;
    city: string;
    country: string;
  };
}

export interface ApplicantUpdateRequest {
  name: string;
  email: string;
  password?: string;
  address: string;
  phoneNumber: string;
  locationId: number;
}

