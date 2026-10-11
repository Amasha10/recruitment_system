import { useEffect, useState } from 'react';
import { adminApi } from '../api/adminApi';
import Spinner from '../components/Spinner';
import Pagination from '../components/Pagination';
import { formatDate, humanize } from '../utils';

export default function AuditLog() {
  const [actions, setActions] = useState([]);
  const [action, setAction] = useState('');
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => { adminApi.auditActions().then(setActions).catch(() => {}); }, []);

  useEffect(() => {
    setData(null);
    adminApi.audit({ action, page }).then(setData).catch((e) => setError(e.message));
  }, [action, page]);

  return (
    <>
      <header className="page-head">
        <div>
          <h1>Audit Log</h1>
          <p className="muted">Every administrative action, recorded automatically by the AuditLogListener (Observer pattern).</p>
        </div>
      </header>

      <div className="card filters">
        <select value={action} onChange={(e) => { setAction(e.target.value); setPage(0); }} aria-label="Action">
          <option value="">All actions</option>
          {actions.map((a) => <option key={a} value={a}>{humanize(a)}</option>)}
        </select>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="card no-pad">
        {!data ? <Spinner /> : (
          <>
            <div className="table-scroll">
              <table className="table">
                <thead><tr><th>Time</th><th>Admin</th><th>Action</th><th>Target</th><th>Details</th></tr></thead>
                <tbody>
                  {data.content.length === 0 && <tr><td colSpan={5} className="empty-row">No entries yet.</td></tr>}
                  {data.content.map((l) => (
                    <tr key={l.id}>
                      <td className="small nowrap">{formatDate(l.timestamp)}</td>
                      <td className="small">{l.performedBy}</td>
                      <td><span className={`badge action-${l.action}`}>{humanize(l.action)}</span></td>
                      <td className="small">{l.target}</td>
                      <td className="muted small">{l.details}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination data={data} noun="entries" onPage={setPage} />
          </>
        )}
      </div>
    </>
  );
}
