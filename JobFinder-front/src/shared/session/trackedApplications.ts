import { getCurrentUserId } from './sessionStore';

function getStorageKey(): string | null {
  const userId = getCurrentUserId();
  if (!userId) {
    return null;
  }

  return `jobfinder.applications.${userId}`;
}

export function getTrackedApplicationIds(): number[] {
  const storageKey = getStorageKey();
  if (!storageKey) {
    return [];
  }

  const raw = sessionStorage.getItem(storageKey);
  if (!raw) {
    return [];
  }

  try {
    const parsed = JSON.parse(raw) as number[];
    return parsed.filter((id) => Number.isInteger(id) && id > 0);
  } catch {
    return [];
  }
}

export function trackApplicationId(applicationId: number): void {
  if (!Number.isInteger(applicationId) || applicationId <= 0) {
    return;
  }

  const storageKey = getStorageKey();
  if (!storageKey) {
    return;
  }

  const current = getTrackedApplicationIds();
  if (current.includes(applicationId)) {
    return;
  }

  sessionStorage.setItem(storageKey, JSON.stringify([applicationId, ...current]));
}

export function untrackApplicationId(applicationId: number): void {
  const storageKey = getStorageKey();
  if (!storageKey) {
    return;
  }

  const next = getTrackedApplicationIds().filter((id) => id !== applicationId);
  sessionStorage.setItem(storageKey, JSON.stringify(next));
}

