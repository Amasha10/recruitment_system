import { useEffect, useState } from 'react';
import { adminApi } from '../api/adminApi';
import { useToast } from '../context/ToastContext';
import Spinner from '../components/Spinner';

const LABELS = {
  'company.name': 'Company name',
  'company.supportEmail': 'Support email',
  'security.passwordMinLength': 'Minimum password length',
  'applications.maxActivePerCandidate': 'Max active applications per candidate',
  'registration.candidatesOpen': 'Candidate self-registration',
};

export default function Settings() {
  const toast = useToast();
  const [settings, setSettings] = useState(null);
  const [values, setValues] = useState({});
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  const apply = (list) => {
    setSettings(list);
    setValues(Object.fromEntries(list.map((s) => [s.key, s.value])));
  };

  useEffect(() => { adminApi.settings().then(apply).catch((e) => setError(e.message)); }, []);

  const dirty = settings?.some((s) => values[s.key] !== s.value);

  const save = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      const res = await adminApi.saveSettings(values);
      apply(res.settings);
      toast(res.changed ? `${res.changed} setting(s) updated` : 'No changes to save');
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  if (!settings) return error ? <div className="alert alert-error">{error}</div> : <Spinner />;

  return (
    <>
      <header className="page-head">
        <div>
          <h1>System Settings</h1>
          <p className="muted">System-level information used across the recruitment system.</p>
        </div>
      </header>

      <form className="card form-card" onSubmit={save}>
        {error && <div className="alert alert-error">{error}</div>}
        {settings.map((s) => {
          const isBool = s.value === 'true' || s.value === 'false';
          const isNum = /^\d+$/.test(s.value);
          return (
            <label key={s.key} className="field setting">
              <span className="field-label">{LABELS[s.key] ?? s.key}</span>
              {isBool ? (
                <select value={values[s.key]} onChange={(e) => setValues((v) => ({ ...v, [s.key]: e.target.value }))}>
                  <option value="true">Enabled</option>
                  <option value="false">Disabled</option>
                </select>
              ) : (
                <input type={isNum ? 'number' : 'text'} value={values[s.key]}
                       onChange={(e) => setValues((v) => ({ ...v, [s.key]: e.target.value }))} />
              )}
              <span className="field-hint">{s.description} · <code>{s.key}</code></span>
            </label>
          );
        })}
        <div className="form-actions">
          <button className="btn btn-primary" disabled={saving || !dirty}>{saving ? 'Saving…' : 'Save settings'}</button>
          {dirty && <button type="button" className="btn btn-outline" onClick={() => apply(settings)}>Discard changes</button>}
        </div>
      </form>
    </>
  );
}
