import { Navigate, Outlet } from 'react-router-dom';
import { hasRole } from '../../shared/session/sessionStore';
import { UserRole } from '../../shared/types/auth';

interface RequireRoleProps {
  role: UserRole;
}

export function RequireRole({ role }: RequireRoleProps) {
  if (!hasRole(role)) {
    return <Navigate to="/forbidden" replace />;
  }

  return <Outlet />;
}

