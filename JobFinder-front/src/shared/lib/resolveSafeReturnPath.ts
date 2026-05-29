const BLOCKED_PATHS = new Set(['/login', '/register/applicant', '/register/employer']);

export function resolveSafeReturnPath(rawPath?: string | null): string | null {
  if (!rawPath) {
    return null;
  }

  const value = rawPath.trim();
  if (!value) {
    return null;
  }

  // Only allow internal absolute paths and block open redirects.
  if (!value.startsWith('/') || value.startsWith('//') || value.includes('://')) {
    return null;
  }

  const [pathOnly] = value.split('?');
  if (BLOCKED_PATHS.has(pathOnly)) {
    return null;
  }

  return value;
}

