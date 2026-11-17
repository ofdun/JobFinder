import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

describe('httpClient refresh flow', () => {
  afterEach(() => vi.unstubAllEnvs());

  beforeEach(() => {
    vi.stubEnv('BASE_URL', '/');
    vi.resetModules();

    const storage = new Map<string, string>();
    Object.defineProperty(globalThis, 'sessionStorage', {
      value: {
        getItem: (key: string) => storage.get(key) ?? null,
        setItem: (key: string, value: string) => storage.set(key, value),
        removeItem: (key: string) => storage.delete(key),
      },
      configurable: true,
    });

    Object.defineProperty(globalThis, 'window', {
      value: {
        location: {
          pathname: '/',
          search: '',
          hash: '',
          assign: vi.fn(),
        },
      },
      configurable: true,
    });
  });

  it('refreshes token and retries protected request after 401', async () => {
    const sessionStore = await import('../session/sessionStore');
    const httpClient = await import('./httpClient');

    sessionStore.setSession({
      role: 'applicant',
      userId: 11,
      accessToken: 'old-access',
      refreshToken: 'old-refresh',
    });

    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ description: 'Unauthorized' }), {
          status: 401,
          headers: { 'Content-Type': 'application/json' },
        }),
      )
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ accessToken: 'new-access', refreshToken: 'new-refresh' }), {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
        }),
      )
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ ok: true }), {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
        }),
      );

    Object.defineProperty(globalThis, 'fetch', {
      value: fetchMock,
      configurable: true,
    });

    const payload = await httpClient.request<{ ok: boolean }>('/secure', { auth: true });

    expect(payload.ok).toBe(true);
    expect(sessionStore.getSession()?.accessToken).toBe('new-access');
    expect(fetchMock).toHaveBeenCalledTimes(3);
  });

  it('does not repeat a rejected second-factor request or clear its session', async () => {
    const sessionStore = await import('../session/sessionStore');
    const httpClient = await import('./httpClient');
    sessionStore.setSession({
      role: 'applicant', userId: 11, accessToken: 'access', refreshToken: 'refresh',
    });
    const fetchMock = vi.fn().mockResolvedValue(new Response('{}', {
      status: 401, headers: { 'Content-Type': 'application/json' },
    }));
    Object.defineProperty(globalThis, 'fetch', { value: fetchMock, configurable: true });

    await expect(httpClient.request('/auth/security/totp/enable', {
      method: 'POST', auth: true, manageAuthErrors: false,
      body: JSON.stringify({ code: 'invalid' }),
    })).rejects.toBeInstanceOf(httpClient.ApiError);

    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(sessionStore.getSession()?.accessToken).toBe('access');
    expect(window.location.assign).not.toHaveBeenCalled();
  });

  it('throws ApiError when refresh request fails', async () => {
    const sessionStore = await import('../session/sessionStore');
    const queryClientModule = await import('../lib/queryClient');
    const httpClient = await import('./httpClient');

    const clearSpy = vi.spyOn(queryClientModule.queryClient, 'clear').mockImplementation(() => undefined);

    sessionStore.setSession({
      role: 'employer',
      userId: 22,
      accessToken: 'old-access',
      refreshToken: 'bad-refresh',
    });

    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ description: 'Unauthorized' }), {
          status: 401,
          headers: { 'Content-Type': 'application/json' },
        }),
      )
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ description: 'Refresh denied' }), {
          status: 401,
          headers: { 'Content-Type': 'application/json' },
        }),
      );

    Object.defineProperty(globalThis, 'fetch', {
      value: fetchMock,
      configurable: true,
    });

    await expect(httpClient.request('/secure', { auth: true })).rejects.toBeInstanceOf(httpClient.ApiError);
    expect(clearSpy).toHaveBeenCalledTimes(1);
    expect(sessionStore.getSession()).toBeNull();
    expect(globalThis.window.location.assign).toHaveBeenCalledTimes(1);
  });
  it('keeps session-expiry redirects and return paths inside the configured SPA base', async () => {
    vi.stubEnv('BASE_URL', '/legacy/');
    Object.assign(window.location, { pathname: '/legacy/me/applicant', search: '?tab=resumes', hash: '' });
    const httpClient = await import('./httpClient');
    Object.defineProperty(globalThis, 'fetch', {
      value: vi.fn().mockResolvedValue(new Response('{}', {
        status: 401,
        headers: { 'Content-Type': 'application/json' },
      })),
      configurable: true,
    });
    await expect(httpClient.request('/secure', { auth: true })).rejects.toBeInstanceOf(httpClient.ApiError);
    expect(window.location.assign).toHaveBeenCalledWith(
      '/legacy/login?from=%2Fme%2Fapplicant%3Ftab%3Dresumes',
    );
  });

  it('keeps permission-denied redirects inside the configured SPA base', async () => {
    vi.stubEnv('BASE_URL', '/legacy/');
    const httpClient = await import('./httpClient');
    Object.defineProperty(globalThis, 'fetch', {
      value: vi.fn().mockResolvedValue(new Response('{}', {
        status: 403,
        headers: { 'Content-Type': 'application/json' },
      })),
      configurable: true,
    });
    await expect(httpClient.request('/secure', { auth: true })).rejects.toBeInstanceOf(httpClient.ApiError);
    expect(window.location.assign).toHaveBeenCalledWith('/legacy/forbidden');
  });

});

