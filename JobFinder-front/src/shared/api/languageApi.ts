import { Language } from '../types/language';
import { request } from './httpClient';

export function getLanguages(): Promise<Language[]> {
  return request<Language[]>('/languages');
}

