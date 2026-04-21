import { FormEvent, useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { useLocation, useNavigate } from 'react-router-dom';
import { login } from '../../shared/api/authApi';
import { resolveSafeReturnPath } from '../../shared/lib/resolveSafeReturnPath';
import { extractUserIdFromToken, setSession } from '../../shared/session/sessionStore';
import { UserRole } from '../../shared/types/auth';

interface LoginFormProps {
  role: UserRole;
}

export function LoginForm({ role }: LoginFormProps) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();
  const location = useLocation();

  const mutation = useMutation({
    mutationFn: async () => {
      const tokenPair = await login(role, { email, password });
      const userId = extractUserIdFromToken(tokenPair.accessToken);
      if (!userId) {
        throw new Error('Invalid access token payload: missing sub');
      }

      return { tokenPair, userId };
    },
    onSuccess: ({ tokenPair, userId }) => {
      setSession({
        role,
        userId,
        accessToken: tokenPair.accessToken,
        refreshToken: tokenPair.refreshToken,
      });

      const destinationFromState = (location.state as { from?: string } | null)?.from;
      const destinationFromQuery = new URLSearchParams(location.search).get('from') ?? undefined;
      const destination = resolveSafeReturnPath(destinationFromState) ?? resolveSafeReturnPath(destinationFromQuery);
      navigate(destination || (role === 'applicant' ? '/me/applicant' : '/me/employer'));
    },
  });

  const onSubmit = (event: FormEvent) => {
    event.preventDefault();
    mutation.mutate();
  };

  return (
    <form className="card" onSubmit={onSubmit}>
      <h2>Вход ({role === 'applicant' ? 'соискатель' : 'работодатель'})</h2>
      <label>
        Email
        <input value={email} onChange={(event) => setEmail(event.target.value)} type="email" required />
      </label>
      <label>
        Пароль
        <input value={password} onChange={(event) => setPassword(event.target.value)} type="password" required />
      </label>
      {mutation.isError ? (
        <p className="error">{mutation.error.message || 'Не удалось войти. Проверьте данные.'}</p>
      ) : null}
      <button type="submit" disabled={mutation.isPending}>
        {mutation.isPending ? 'Входим...' : 'Войти'}
      </button>
    </form>
  );
}

