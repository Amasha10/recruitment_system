export function formatDate(iso, withTime = true) {
  if (!iso) return '—';
  const d = new Date(iso);
  return d.toLocaleString('en-GB', {
    day: '2-digit', month: 'short', year: 'numeric',
    ...(withTime ? { hour: '2-digit', minute: '2-digit' } : {}),
  });
}

export const ROLE_LABELS = {
  CANDIDATE: 'Candidate',
  RECRUITER: 'Recruiter',
  EMPLOYER: 'Employer / HR',
  ADMIN: 'Administrator',
};

export const humanize = (s) => s.toLowerCase().replace(/_/g, ' ').replace(/^\w/, (c) => c.toUpperCase());
