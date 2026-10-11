import { useCallback, useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { adminApi } from '../api/adminApi';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';
import Spinner from '../components/Spinner';
import Pagination from '../components/Pagination';
import Icon from '../components/Icon';
import { ConfirmModal } from '../components/Modal';
import { RoleBadge, StatusBadge } from '../components/Badges';
import { formatDate, ROLE_LABELS } from '../utils';

export default function Users() {
  const toast = useToast();
  const { user: me } = useAuth();
  const [params, setParams] = useSearchParams();
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [keywordInput, setKeywordInput] = useState(params.get('keyword') ?? '');
  const [confirm, setConfirm] = useState(null); // { type: 'delete' | 'toggle', user }
  const [busy, setBusy] = useState(false);

  const filters = {
    keyword: params.get('keyword') ?? '',
    role: params.get('role') ?? '',
    status: params.get('status') ?? '',
    page: Number(params.get('page') ?? 0),
  };

  const load = useCallback(() => {
    setError('');
    adminApi.listUsers(filters).then(setData).catch((e) => setError(e.message));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params]);

  useEffect(load, [load]);

  const setFilter = (changes) => {
    const next = { ...filters, page: 0, ...changes };
    const clean = Object.fromEntries(Object.entries(next).filter(([, v]) => v !== '' && v !== 0));
    setParams(clean);
  };

  const runConfirmed = async () => {
    const { type, user } = confirm;
    setBusy(true);
    try {
      if (type === 'delete') {
        await adminApi.deleteUser(user.id);
        toast(`${user.fullName} was deleted`);
      } else {
        const updated = await adminApi.toggleStatus(user.id);
        toast(`${updated.fullName} is now ${updated.enabled ? 'active' : 'disabled'}`);
      }
      load();
    } catch (e) {
      toast(e.message, 'error');
    } finally {
      setBusy(false);
      setConfirm(null);
    }
  };

  return (
    <>
      <header className="page-head">
        <div>
          <h1>Users &amp; Roles</h1>
          <p className="muted">Manage accounts, roles and access permissions.</p>
        </div>
        <Link to="/users/new" className="btn btn-primary"><Icon name="plus" size={16} /> Add user</Link>
      </header>

      <form className="card filters" onSubmit={(e) => { e.preventDefault(); setFilter({ keyword: keywordInput.trim() }); }}>
        <div className="search-input">
          <Icon name="search" size={16} />
          <input placeholder="Search by name or email" value={keywordInput} onChange={(e) => setKeywordInput(e.target.value)} />
        </div>
        <select value={filters.role} onChange={(e) => setFilter({ role: e.target.value })} aria-label="Role">
          <option value="">All roles</option>
          {Object.entries(ROLE_LABELS).map(([v, l]) => <option key={v} value={v}>{l}</option>)}
        </select>
        <select value={filters.status} onChange={(e) => setFilter({ status: e.target.value })} aria-label="Status">
          <option value="">Any status</option>
          <option value="active">Active</option>
          <option value="disabled">Disabled</option>
        </select>
        <button className="btn btn-dark">Search</button>
        <button type="button" className="btn btn-outline" onClick={() => { setKeywordInput(''); setParams({}); }}>Clear</button>
      </form>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="card no-pad">
        {!data ? <Spinner /> : (
          <>
            <div className="table-scroll">
              <table className="table">
                <thead>
                  <tr><th>Name</th><th>Role</th><th>Status</th><th>Phone</th><th>Last login</th><th className="right">Actions</th></tr>
                </thead>
                <tbody>
                  {data.content.length === 0 && (
                    <tr><td colSpan={6} className="empty-row">No users match these filters.</td></tr>
                  )}
                  {data.content.map((u) => {
                    const isMe = u.email === me?.email;
                    return (
                      <tr key={u.id}>
                        <td>
                          <Link to={`/users/${u.id}`} className="link strong">{u.fullName}</Link>
                          {isMe && <span className="you-tag">you</span>}
                          <div className="muted small">{u.email}</div>
                        </td>
                        <td><RoleBadge role={u.role} /></td>
                        <td><StatusBadge enabled={u.enabled} /></td>
                        <td className="small">{u.phone ?? '—'}</td>
                        <td className="muted small nowrap">{u.lastLoginAt ? formatDate(u.lastLoginAt) : 'Never'}</td>
                        <td className="right nowrap">
                          <Link to={`/users/${u.id}/edit`} className="icon-btn" title="Edit"><Icon name="edit" size={16} /></Link>
                          <button className={`icon-btn ${u.enabled ? 'warn' : 'ok'}`} title={u.enabled ? 'Disable' : 'Enable'}
                                  disabled={isMe} onClick={() => setConfirm({ type: 'toggle', user: u })}>
                            <Icon name="power" size={16} />
                          </button>
                          <button className="icon-btn danger" title="Delete" disabled={isMe}
                                  onClick={() => setConfirm({ type: 'delete', user: u })}>
                            <Icon name="trash" size={16} />
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
            <Pagination data={data} noun="users" onPage={(p) => setParams({ ...Object.fromEntries(params), page: p })} />
          </>
        )}
      </div>

      {confirm && (
        <ConfirmModal
          title={confirm.type === 'delete' ? 'Delete user' : confirm.user.enabled ? 'Disable account' : 'Enable account'}
          message={confirm.type === 'delete'
            ? `Permanently delete ${confirm.user.fullName} (${confirm.user.email})? This cannot be undone.`
            : confirm.user.enabled
              ? `${confirm.user.fullName} will no longer be able to log in.`
              : `${confirm.user.fullName} will be able to log in again.`}
          confirmLabel={confirm.type === 'delete' ? 'Delete' : confirm.user.enabled ? 'Disable' : 'Enable'}
          danger={confirm.type === 'delete' || confirm.user.enabled}
          busy={busy}
          onConfirm={runConfirmed}
          onClose={() => setConfirm(null)}
        />
      )}
    </>
  );
}
