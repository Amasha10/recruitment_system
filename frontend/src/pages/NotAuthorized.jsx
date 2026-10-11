import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Icon from '../components/Icon';
import { ROLE_LABELS } from '../utils';

export default function NotAuthorized() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  return (
    <div className="login-page">
      <div className="login-card center">
        <span className="denied-icon"><Icon name="shield" size={40} /></span>
        <h1>Access denied</h1>
        <p className="muted">
          You are signed in as <strong>{user?.email}</strong> ({ROLE_LABELS[user?.role]}).
          Only System Administrators can use this panel.
        </p>
        <button className="btn btn-primary" onClick={async () => { await logout(); navigate('/login'); }}>
          Sign in with another account
        </button>
      </div>
    </div>
  );
}
