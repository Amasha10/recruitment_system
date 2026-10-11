import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Spinner from './Spinner';
import NotAuthorized from '../pages/NotAuthorized';

/** Must be logged in AND have the ADMIN role (the backend enforces this too). */
export default function ProtectedRoute() {
  const { user, loading, isAdmin } = useAuth();
  if (loading) return <div className="center-screen"><Spinner /></div>;
  if (!user) return <Navigate to="/login" replace />;
  if (!isAdmin) return <NotAuthorized />;
  return <Outlet />;
}
