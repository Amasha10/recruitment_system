import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Icon from './Icon';

const links = [
  { to: '/dashboard', label: 'Dashboard', icon: 'dashboard' },
  { to: '/users', label: 'Users & Roles', icon: 'users' },
  { to: '/settings', label: 'System Settings', icon: 'settings' },
  { to: '/audit', label: 'Audit Log', icon: 'audit' },
];

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark"><Icon name="briefcase" size={18} /></span>
          <div>
            <div className="brand-name">Recruitment System</div>
            <div className="brand-sub">Administrator Panel</div>
          </div>
        </div>
        <nav className="nav">
          {links.map((l) => (
            <NavLink key={l.to} to={l.to} className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}>
              <Icon name={l.icon} /> {l.label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-footer">
          <div className="me">
            <div className="avatar">{user?.fullName?.[0] ?? 'A'}</div>
            <div className="me-text">
              <div className="me-name">{user?.fullName}</div>
              <div className="me-email">{user?.email}</div>
            </div>
          </div>
          <button className="btn btn-ghost-light btn-block" onClick={handleLogout}>
            <Icon name="logout" size={16} /> Logout
          </button>
        </div>
      </aside>
      <main className="main">
        <Outlet />
      </main>
    </div>
  );
}
