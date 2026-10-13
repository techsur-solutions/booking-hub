/**
 * Remember Me cookie utilities.
 * Stores user email for 360 days as a convenience (not for authentication).
 * Phase 1 F0 confirmed: legacy remember-me is a 360-day email-prefill cookie.
 * Cookie is SameSite=Strict to prevent CSRF. Stores email only, not credentials.
 */

export function saveRememberMe(email: string): void {
  document.cookie = `remember_me_email=${encodeURIComponent(email)}; max-age=${360 * 24 * 60 * 60}; path=/; SameSite=Strict`;
}

export function getRememberMeEmail(): string | null {
  const match = document.cookie.match(/remember_me_email=([^;]+)/);
  return match ? decodeURIComponent(match[1]) : null;
}

export function clearRememberMe(): void {
  document.cookie = 'remember_me_email=; max-age=0; path=/';
}
