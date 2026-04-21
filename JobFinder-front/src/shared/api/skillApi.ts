import { Skill } from '../types/skill';
import { request } from './httpClient';

export function getSkills(): Promise<Skill[]> {
  return request<Skill[]>('/skills');
}

