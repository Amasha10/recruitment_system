import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { adminApi } from '../api/adminApi';
import Spinner from '../components/Spinner';
import { RoleBadge } from '../components/Badges';
import { formatDate, humanize, ROLE_LABELS } from '../utils';

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    adminApi.dashboard().then(setData).catch((e) => setError(e.message));
  }, []);

  if (error) return <div className="alert alert-error">{error}</div>;
  if (!data) return <Spinner />;

  const stats = [
    { label: 'Total users', value: data.totalUsers, tone: 'blue' },
    { label: 'Active accounts', value: data.activeUsers, tone: 'green' },
    { label: 'Disabled accounts', value: data.disabledUsers, tone: 'gray' },
    { label: 'Recruiters', value: data.usersByRole.RECRUITER ?? 0, tone: 'amber' },
  ];

  return (
    <>
      <header className="page-head">
        <div>
          <h1>Dashboard</h1>
          <p className="muted">Overview of user accounts and recent administrative activity.</p>
        </div>
      </header>

      <section className="stat-grid">
        {stats.map((s) => (
          <div key={s.label} className={`card stat stat-${s.tone}`}>
            <div className="stat-value">{s.value}</div>
            <div className="stat-label">{s.label}</div>
          </div>
        ))}
      </section>

      <section className="grid-2">
        <div className="card">
          <h2 className="card-title">Users by role</h2>
          {Object.entries(data.usersByRole).map(([role, count]) => {
            const pct = data.totalUsers ? Math.round((count * 100) / data.totalUsers) : 0;
            return (
              <div key={role} className="bar-row">
                <div className="bar-head"><span>{ROLE_LABELS[role]}</span><strong>{count}</strong></div>
                <div className="bar"><div className={`bar-fill fill-${role}`} style={{ width: `${pct}%` }} /></div>
              </div>
            );
          })}
        </div>

        <div className="card">
          <div className="card-title-row">
            <h2 className="card-title">Newest accounts</h2>
            <Link to="/users" className="link small">View all</Link>
          </div>
          <table className="table compact">
            <tbody>
              {data.recentUsers.map((u) => (
                <tr key={u.id}>
                  <td>
                    <Link to={`/users/${u.id}`} className="link strong">{u.fullName}</Link>
                    <div className="muted small">{u.email}</div>
                  </td>
                  <td><RoleBadge role={u.role} /></td>
                  <td className="muted small nowrap">{formatDate(u.createdAt, false)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section className="card">
        <div className="card-title-row">
          <h2 className="card-title">Recent admin activity</h2>
          <Link to="/audit" className="link small">Full audit log</Link>
        </div>
        {data.recentActivity.length === 0 ? (
          <p className="muted small">No admin actions recorded yet. Try editing a user.</p>
        ) : (
          <table className="table compact">
            <tbody>
              {data.recentActivity.map((a) => (
                <tr key={a.id}>
                  <td className="muted small nowrap">{formatDate(a.timestamp)}</td>
                  <td><span className="badge badge-neutral">{humanize(a.action)}</span></td>
                  <td className="small">{a.target}</td>
                  <td className="muted small">{a.details}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </>
  );
}
