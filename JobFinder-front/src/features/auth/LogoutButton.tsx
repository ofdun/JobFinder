import { useNavigate } from 'react-router-dom';
import { queryClient } from '../../shared/lib/queryClient';
import { clearSession } from '../../shared/session/sessionStore';

interface LogoutButtonProps {
  className?: string;
}

export function LogoutButton({ className }: LogoutButtonProps) {
  const navigate = useNavigate();

  const onLogout = () => {
    queryClient.clear();
    clearSession();
    navigate('/login');
  };

  return (
    <button type="button" className={className} onClick={onLogout}>
      Выйти
    </button>
  );
}

