import { beforeAll, describe, expect, it } from 'vitest';

function buildToken(payload: object): string {
  const header = btoa(JSON.stringify({ alg: 'none', typ: 'JWT' }));
  const body = btoa(JSON.stringify(payload));
  return `${header}.${body}.signature`;
}

describe('extractUserIdFromToken', () => {
  let extractUserIdFromToken: (token: string) => number | null;

  beforeAll(async () => {
    Object.defineProperty(globalThis, 'sessionStorage', {
      value: {
        getItem: () => null,
        setItem: () => undefined,
        removeItem: () => undefined,
      },
      configurable: true,
    });

    ({ extractUserIdFromToken } = await import('./sessionStore'));
  });

  it('returns user id from sub claim', () => {
    const token = buildToken({ sub: '123' });
    expect(extractUserIdFromToken(token)).toBe(123);
  });

  it('returns null for invalid token', () => {
    expect(extractUserIdFromToken('invalid.token')).toBeNull();
  });
});

