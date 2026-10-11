// Small fetch wrapper: sends the session cookie, parses JSON, and turns errors into ApiError objects.

export class ApiError extends Error {
  constructor(status, message, fieldErrors = {}) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

const BASE = import.meta.env.VITE_API_URL ?? '';

export async function request(method, path, body) {
  let res;
  try {
    res = await fetch(BASE + path, {
      method,
      credentials: 'include',
      headers: body !== undefined ? { 'Content-Type': 'application/json' } : {},
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError(0, 'Cannot reach the server. Is the Spring Boot backend running on port 8080?');
  }

  if (res.status === 204) return null;

  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { /* non-JSON response */ }

  if (!res.ok) {
    if (res.status === 401 && !path.startsWith('/api/auth/')) {
      window.dispatchEvent(new Event('session-expired'));
    }
    throw new ApiError(res.status, data?.message ?? `Request failed (${res.status})`, data?.fieldErrors ?? {});
  }
  return data;
}

export const api = {
  get: (path) => request('GET', path),
  post: (path, body) => request('POST', path, body ?? {}),
  put: (path, body) => request('PUT', path, body),
  patch: (path, body) => request('PATCH', path, body),
  del: (path) => request('DELETE', path),
};

/** Builds "?a=1&b=2", skipping empty values. */
export function query(params) {
  const q = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') q.set(k, v);
  });
  const s = q.toString();
  return s ? `?${s}` : '';
}
