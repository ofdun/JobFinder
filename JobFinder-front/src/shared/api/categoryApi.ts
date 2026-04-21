import { Category } from '../types/category';
import { request } from './httpClient';

export function getCategories(): Promise<Category[]> {
  return request<Category[]>('/categories');
}

