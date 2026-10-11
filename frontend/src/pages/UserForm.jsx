import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { adminApi } from '../api/adminApi';
import { useToast } from '../context/ToastContext';
import Field from '../components/Field';
import Spinner from '../components/Spinner';
import Icon from '../components/Icon';

const EMPTY = { fullName: '', email: '', phone: '', role: '', enabled: true, password: '' };

/** Client-side checks mirror the backend rules so users get instant feedback. */
function validate(form, isNew) {
  const e = {};
  if (form.fullName.trim().length < 3) e.fullName = 'Name must be 3-100 characters';
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) e.email = 'Enter a valid email address';
  if (form.phone && !/^(\+94|0)\d{9}$/.test(form.phone.trim())) e.phone = 'Phone must be like 0771234567 or +94771234567';
  if (!form.role) e.role = 'Select a role';
  if (isNew) {
    if (!form.password) e.password = 'Password is required for a new user';
    else if (!/[A-Z]/.test(form.password) || !/[a-z]/.test(form.password) || !/\d/.test(form.password))
      e.password = 'Must contain uppercase, lowercase and a number';
  }
  return e;
}

export default function UserForm() {
  const { id } = useParams();
  const isNew = !id;
  const navigate = useNavigate();
  const toast = useToast();

  const [form, setForm] = useState(EMPTY);
  const [roles, setRoles] = useState([]);
  const [errors, setErrors] = useState({});
  const [formError, setFormError] = useState('');
  const [loading, setLoading] = useState(!isNew);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    adminApi.roles().then(setRoles).catch(() => {});
    if (!isNew) {
      adminApi.getUser(id)
        .then((u) => setForm({ fullName: u.fullName, email: u.email, phone: u.phone ?? '', role: u.role, enabled: u.enabled, password: '' }))
        .catch((e) => setFormError(e.message))
        .finally(() => setLoading(false));
    }
  }, [id, isNew]);

  const set = (key) => (e) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
    setForm((f) => ({ ...f, [key]: value }));
    setErrors((er) => ({ ...er, [key]: undefined }));
  };

  const submit = async (e) => {
    e.preventDefault();
    setFormError('');
    const clientErrors = validate(form, isNew);
    setErrors(clientErrors);
    if (Object.keys(clientErrors).length) return;

    const payload = { ...form, fullName: form.fullName.trim(), email: form.email.trim(), phone: form.phone.trim() };
    if (!isNew) delete payload.password;

    setSaving(true);
    try {
      const saved = isNew ? await adminApi.createUser(payload) : await adminApi.updateUser(id, payload);
      toast(isNew ? `User ${saved.email} created` : 'User updated');
      navigate(`/users/${saved.id}`);
    } catch (err) {
      setErrors(err.fieldErrors ?? {});
      setFormError(err.message);
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <Spinner />;

  return (
    <>
      <Link to={isNew ? '/users' : `/users/${id}`} className="back-link"><Icon name="back" size={16} /> Back</Link>
      <header className="page-head"><h1>{isNew ? 'Add user' : 'Edit user'}</h1></header>

      <form className="card form-card" onSubmit={submit} noValidate>
        {formError && <div className="alert alert-error">{formError}</div>}
        <div className="form-grid">
          <Field label="Full name" required error={errors.fullName}>
            <input value={form.fullName} onChange={set('fullName')} />
          </Field>
          <Field label="Email" required error={errors.email}>
            <input type="email" value={form.email} onChange={set('email')} />
          </Field>
          <Field label="Phone" error={errors.phone} hint="Optional · 0771234567">
            <input value={form.phone} onChange={set('phone')} />
          </Field>
          <Field label="Role" required error={errors.role}>
            <select value={form.role} onChange={set('role')}>
              <option value="">Select a role</option>
              {roles.map((r) => <option key={r.value} value={r.value}>{r.label}</option>)}
            </select>
          </Field>
          {isNew && (
            <Field label="Initial password" required error={errors.password} hint="Uppercase, lowercase and a number">
              <input type="password" value={form.password} onChange={set('password')} autoComplete="new-password" />
            </Field>
          )}
        </div>
        <label className="switch">
          <input type="checkbox" checked={form.enabled} onChange={set('enabled')} />
          <span>Account is active (user can log in)</span>
        </label>
        <div className="form-actions">
          <button className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save'}</button>
          <Link to={isNew ? '/users' : `/users/${id}`} className="btn btn-outline">Cancel</Link>
        </div>
      </form>
    </>
  );
}
