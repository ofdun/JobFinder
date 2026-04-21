import { Location } from '../types/location';
import { request } from './httpClient';

export function getLocationById(id: number): Promise<Location> {
  return request<Location>(`/locations/${id}`);
}

export function searchLocations(q: string, limit = 20): Promise<Location[]> {
  const params = new URLSearchParams({ q, limit: String(limit) });
  return request<Location[]>(`/locations/search?${params.toString()}`);
}

