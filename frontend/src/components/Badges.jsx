import { ROLE_LABELS } from '../utils';

export function RoleBadge({ role }) {
  return <span className={`badge role-${role}`}>{ROLE_LABELS[role] ?? role}</span>;
}

export function StatusBadge({ enabled }) {
  return enabled
    ? <span className="badge status-active">Active</span>
    : <span className="badge status-disabled">Disabled</span>;
}
