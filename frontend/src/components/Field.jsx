/** Label + input wrapper that shows the backend's per-field validation message. */
export default function Field({ label, error, hint, children, required }) {
  return (
    <label className={`field ${error ? 'has-error' : ''}`}>
      <span className="field-label">{label}{required && <span className="req"> *</span>}</span>
      {children}
      {error ? <span className="field-error">{error}</span> : hint ? <span className="field-hint">{hint}</span> : null}
    </label>
  );
}
