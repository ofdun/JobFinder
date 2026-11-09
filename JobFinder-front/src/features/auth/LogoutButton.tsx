import { useNavigate } from "react-router-dom";
import { queryClient } from "../../shared/lib/queryClient";
import { clearSession, getSession } from "../../shared/session/sessionStore";
import { request } from "../../shared/api/httpClient";

interface LogoutButtonProps {
  className?: string;
}

export function LogoutButton({ className }: LogoutButtonProps) {
  const navigate = useNavigate();

  const onLogout = async () => {
    const refreshToken = getSession()?.refreshToken;
    try {
      if (refreshToken) {
        await request('/auth/security/logout', {
          method: 'POST', body: JSON.stringify({ refreshToken }),
        });
      }
    } catch {
      return;
    } finally {
      queryClient.clear();
      clearSession();
      navigate('/login');
    }
  };

  return (
    <button type="button" className={className} onClick={onLogout}>
      Выйти
    </button>
  );
}
