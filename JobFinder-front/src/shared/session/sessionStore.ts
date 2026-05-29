import { useSyncExternalStore } from 'react';
import { SessionState } from '../types/auth';

const SESSION_KEY = 'jobfinder.session';

let currentSession: SessionState | null = loadSession();
const listeners = new Set<() => void>();

function notifySessionChanged(): void {
  listeners.forEach((listener) => listener());
}

function decodeBase64Url(value: string): string | null {
  try {
    const base64 = value.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
    return atob(padded);
  } catch {
    return null;
  }
}

export function extractUserIdFromToken(token: string): number | null {
  const parts = token.split('.');
  if (parts.length !== 3) {
    return null;
  }

  const payloadText = decodeBase64Url(parts[1]);
  if (!payloadText) {
    return null;
  }

  try {
    const payload = JSON.parse(payloadText) as { sub?: string };
    const userId = Number(payload.sub);
    return Number.isInteger(userId) && userId > 0 ? userId : null;
  } catch {
    return null;
  }
}

function loadSession(): SessionState | null {
  const raw = sessionStorage.getItem(SESSION_KEY);
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as SessionState;
  } catch {
    return null;
  }
}

export function getSession(): SessionState | null {
  return currentSession;
}

export function setSession(session: SessionState): void {
  currentSession = session;
  sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
  notifySessionChanged();
}

export function clearSession(): void {
  currentSession = null;
  sessionStorage.removeItem(SESSION_KEY);
  notifySessionChanged();
}

export function subscribeSession(listener: () => void): () => void {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}

export function useSession(): SessionState | null {
  return useSyncExternalStore(subscribeSession, getSession, getSession);
}

export function hasRole(role: SessionState['role']): boolean {
  return currentSession?.role === role;
}

export function getCurrentUserId(): number | null {
  if (!currentSession) {
    return null;
  }

  if (Number.isInteger(currentSession.userId) && currentSession.userId > 0) {
    return currentSession.userId;
  }

  return extractUserIdFromToken(currentSession.accessToken);
}

