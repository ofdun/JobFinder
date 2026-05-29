import { describe, expect, it } from 'vitest';
import { resolveSafeReturnPath } from './resolveSafeReturnPath';

describe('resolveSafeReturnPath', () => {
  it('returns internal path', () => {
    expect(resolveSafeReturnPath('/vacancies/1/apply')).toBe('/vacancies/1/apply');
  });

  it('returns null for external URL', () => {
    expect(resolveSafeReturnPath('https://evil.example/steal')).toBeNull();
    expect(resolveSafeReturnPath('//evil.example/steal')).toBeNull();
  });

  it('returns null for blocked auth paths', () => {
    expect(resolveSafeReturnPath('/login')).toBeNull();
    expect(resolveSafeReturnPath('/register/applicant')).toBeNull();
  });

  it('returns null for empty value', () => {
    expect(resolveSafeReturnPath('   ')).toBeNull();
    expect(resolveSafeReturnPath(undefined)).toBeNull();
  });
});

