import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { adminApi } from '../api/adminApi';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';
import Spinner from '../components/Spinner';
import Icon from '../components/Icon';
import Field from '../components/Field';
import Modal, { ConfirmModal } from '../components/Modal';
import { RoleBadge, StatusBadge } from '../components/Badges';
import { formatDate } from '../utils';

function ResetPasswordModal({ user, onClose }) {
  const toast = useToast();
  const [pw, setPw] = useState('');
  const [confirm, setConfirm] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    if (pw !== confirm) { setError('Passwords do not match'); return; }
    setBusy(true);
    try {
      await adminApi.resetPassword(user.id, pw, confirm);
      toast(`Password reset for ${user.fullName}`);
      onClose();
    } catch (err) {
      setError(err.fieldErrors?.newPassword ?? err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal title={`Reset password · ${user.fullName}`} onClose={onClose}>
      <form onSubmit={submit} className="modal-form">
        {error && <div className="alert alert-error">{error}</div>}
        <Field label="New password" hint="Uppercase, lowercase and a number">
          <input type="password" value={pw} onChange={(e) => setPw(e.target.value)} autoFocus required autoComplete="new-password" />
        </Field>
        <Field label="Confirm password">
          <input type="password" value={confirm} onChange={(e) => setConfirm(e.target.value)} required autoComplete="new-password" />
        </Field>
        <div className="modal-actions">
          <button type="button" className="btn btn-outline" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : 'Reset password'}</button>
        </div>
      </form>
    </Modal>
  );
}

export default function UserDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useToast();
  const { user: me } = useAuth();
  const [user, setUser] = useState(null);
  const [error, setError] = useState('');
  const [modal, setModal] = useState(null); // 'reset' | 'toggle' | 'delete'
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    adminApi.getUser(id).then(setUser).catch((e) => setError(e.message));
  }, [id]);

  if (error) return <div className="alert alert-error">{error} <Link to="/users" className="link">Back to users</Link></div>;
  if (!user) return <Spinner />;

  const isMe = user.email === me?.email;

  const toggle = async () => {
    setBusy(true);
    try {
      const updated = await adminApi.toggleStatus(user.id);
      setUser(updated);
      toast(`Account ${updated.enabled ? 'enabled' : 'disabled'}`);
    } catch (e) { toast(e.message, 'error'); }
    finally { setBusy(false); setModal(null); }
  };

  const remove = async () => {
    setBusy(true);
    try {
      await adminApi.deleteUser(user.id);
      toast(`${user.fullName} was deleted`);
      navigate('/users');
    } catch (e) { toast(e.message, 'error'); setBusy(false); setModal(null); }
  };

  const rows = [
    ['Email', user.email],
    ['Phone', user.phone ?? '—'],
    ['Role', <RoleBadge role={user.role} />],
    ['Status', <StatusBadge enabled={user.enabled} />],
    ['Created', formatDate(user.createdAt)],
    ['Last updated', formatDate(user.updatedAt)],
    ['Last login', user.lastLoginAt ? formatDate(user.lastLoginAt) : 'Never'],
  ];

  return (
    <>
      <Link to="/users" className="back-link"><Icon name="back" size={16} /> Back to users</Link>
      <header className="page-head">
        <div className="profile-head">
          <div className="avatar avatar-lg">{user.fullName[0]}</div>
          <div>
            <h1>{user.fullName}</h1>
            <p className="muted">{user.roleLabel}{isMe && ' · this is you'}</p>
          </div>
        </div>
        <div className="btn-row">
          <Link to={`/users/${user.id}/edit`} className="btn btn-outline"><Icon name="edit" size={16} /> Edit</Link>
          <button className="btn btn-outline" onClick={() => setModal('reset')}><Icon name="key" size={16} /> Reset password</button>
          <button className={`btn ${user.enabled ? 'btn-warn' : 'btn-success'}`} disabled={isMe} onClick={() => setModal('toggle')}>
            <Icon name="power" size={16} /> {user.enabled ? 'Disable' : 'Enable'}
          </button>
          <button className="btn btn-danger" disabled={isMe} onClick={() => setModal('delete')}><Icon name="trash" size={16} /> Delete</button>
        </div>
      </header>

      <div className="card form-card">
        <dl className="details">
          {rows.map(([k, v]) => (<div key={k} className="detail-row"><dt>{k}</dt><dd>{v}</dd></div>))}
        </dl>
      </div>

      {modal === 'reset' && <ResetPasswordModal user={user} onClose={() => setModal(null)} />}
      {modal === 'toggle' && (
        <ConfirmModal title={user.enabled ? 'Disable account' : 'Enable account'}
          message={user.enabled ? `${user.fullName} will no longer be able to log in.` : `${user.fullName} will be able to log in again.`}
          confirmLabel={user.enabled ? 'Disable' : 'Enable'} danger={user.enabled} busy={busy}
          onConfirm={toggle} onClose={() => setModal(null)} />
      )}
      {modal === 'delete' && (
        <ConfirmModal title="Delete user" danger busy={busy} confirmLabel="Delete"
          message={`Permanently delete ${user.fullName} (${user.email})? This cannot be undone.`}
          onConfirm={remove} onClose={() => setModal(null)} />
      )}
    </>
  );
}
