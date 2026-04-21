import { clearSession, getSession, setSession } from '../session/sessionStore';
import { queryClient } from '../lib/queryClient';
import { TokenPair } from '../types/auth';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1';

interface RequestOptions extends RequestInit {
  auth?: boolean;
  retryAfterRefresh?: boolean;
}

let refreshInFlight: Promise<string | null> | null = null;

export class ApiError extends Error {
  readonly status: number;
  readonly requestId: string | null;
  readonly details: unknown;

  constructor(status: number, message: string, requestId: string | null = null, details: unknown = null) {
    super(message);
    this.status = status;
    this.requestId = requestId;
    this.details = details;
  }
}

function readErrorMessage(body: unknown): string | null {
  if (!body || typeof body !== 'object') {
    return null;
  }

  const payload = body as Record<string, unknown>;
  const candidates = [payload.description, payload.exceptionMessage, payload.message];
  const candidate = candidates.find((value) => typeof value === 'string' && value.trim().length > 0);
  return typeof candidate === 'string' ? candidate : null;
}

function resolveFallbackMessage(status: number): string {
  if (status === 401) {
    return 'Требуется авторизация.';
  }
  if (status === 403) {
    return 'Недостаточно прав для выполнения запроса.';
  }
  if (status === 404) {
    return 'Запрашиваемый ресурс не найден.';
  }

  return 'Не удалось выполнить запрос к API.';
}

function redirectTo(path: string): void {
  if (typeof window === 'undefined') {
    return;
  }

  if (window.location.pathname === path) {
    return;
  }

  window.location.assign(path);
}

function handleAuthError(status: number, authRequired: boolean): void {
  if (!authRequired) {
    return;
  }

  if (status === 401) {
    const from =
      typeof window === 'undefined'
        ? '/'
        : `${window.location.pathname}${window.location.search}${window.location.hash}`;
    queryClient.clear();
    clearSession();
    redirectTo(`/login?from=${encodeURIComponent(from)}`);
    return;
  }

  if (status === 403) {
    redirectTo('/forbidden');
  }
}

async function refreshAccessToken(): Promise<string | null> {
  const session = getSession();
  if (!session) {
    return null;
  }

  if (refreshInFlight) {
    return refreshInFlight;
  }

  refreshInFlight = (async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/${session.role}/refresh`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ refreshToken: session.refreshToken }),
      });

      if (!response.ok) {
        return null;
      }

      const tokenPair = (await response.json()) as TokenPair;
      if (!tokenPair.accessToken || !tokenPair.refreshToken) {
        return null;
      }

      setSession({
        ...session,
        accessToken: tokenPair.accessToken,
        refreshToken: tokenPair.refreshToken,
      });

      return tokenPair.accessToken;
    } catch {
      return null;
    } finally {
      refreshInFlight = null;
    }
  })();

  return refreshInFlight;
}

async function parseErrorResponse(response: Response): Promise<{ message: string; details: unknown }> {
  const contentType = response.headers.get('content-type')?.toLowerCase() ?? '';

  if (contentType.includes('application/json')) {
    try {
      const data = (await response.json()) as unknown;
      return {
        message: readErrorMessage(data) ?? resolveFallbackMessage(response.status),
        details: data,
      };
    } catch {
      return { message: resolveFallbackMessage(response.status), details: null };
    }
  }

  const text = await response.text();
  return { message: text || resolveFallbackMessage(response.status), details: text || null };
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers = new Headers(options.headers);

  headers.set('Content-Type', 'application/json');

  if (options.auth) {
    const token = getSession()?.accessToken;
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    if (response.status === 401 && options.auth && !options.retryAfterRefresh) {
      const refreshedToken = await refreshAccessToken();
      if (refreshedToken) {
        return request<T>(path, {
          ...options,
          retryAfterRefresh: true,
        });
      }
    }

    const requestId = response.headers.get('X-Request-Id');
    const error = await parseErrorResponse(response);

    handleAuthError(response.status, Boolean(options.auth));

    throw new ApiError(response.status, error.message, requestId, error.details);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

