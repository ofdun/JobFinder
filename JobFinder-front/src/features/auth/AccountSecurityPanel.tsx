import { FormEvent, useEffect, useRef, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { ApiError, request } from '../../shared/api/httpClient';
import { clearSession, getSession } from '../../shared/session/sessionStore';
import { queryClient } from '../../shared/lib/queryClient';
import { PasswordInput } from '../../shared/ui/PasswordInput';

interface SecurityStatus {
  enabled: boolean;
  recoveryCodesRemaining: number;
  passwordExpiresAt: number;
}

type SecurityAction = 'setup' | 'enable' | 'disable' | 'password';

export function AccountSecurityPanel() {
  const navigate = useNavigate();
  const [password, setPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [code, setCode] = useState('');
  const [setup, setSetup] = useState<{ secret: string; qrCode: string } | null>(null);
  const [backup, setBackup] = useState<string[]>([]);
  const [error, setError] = useState('');
  const [activeAction, setActiveAction] = useState<SecurityAction | null>(null);
  const [locked, setLocked] = useState(false);
  const busy = activeAction !== null || locked;
  const errorElement = useRef<HTMLParagraphElement>(null);
  useEffect(() => {
    if (error) {
      errorElement.current?.focus();
      errorElement.current?.scrollIntoView({ block: 'nearest' });
    }
  }, [error]);
  const session = getSession();
  const status = useQuery({
    queryKey: ['account-security', session?.role, session?.userId],
    queryFn: () => request<SecurityStatus>('/auth/security', { auth: true }),
    refetchOnWindowFocus: false,
    retry: false,
    enabled: backup.length === 0,
  });

  const signInAgain = () => {
    setSetup(null);
    setBackup([]);
    setPassword('');
    setCode('');
    setNewPassword('');
    queryClient.clear();
    clearSession();
    navigate('/login');
  };

  const act = async (action: SecurityAction) => {
    if (busy) return;
    setActiveAction(action);
    setError('');
    try {
      const options = { method: 'POST', auth: true, manageAuthErrors: false };
      if (action === 'setup') {
        setSetup(await request('/auth/security/totp/setup', { ...options, body: JSON.stringify({ password }) }));
        setPassword('');
      } else if (action === 'enable') {
        const result = await request<{ recoveryCodes: string[] }>('/auth/security/totp/enable', {
          ...options, body: JSON.stringify({ code }),
        });
        setBackup(result.recoveryCodes);
        setSetup(null);
        setCode('');
      } else {
        await request(action === 'disable' ? '/auth/security/totp/disable' : '/auth/security/password', {
          ...options,
          body: JSON.stringify(action === 'disable' ? { password, code } : { currentPassword: password, newPassword, code }),
        });
        signInAgain();
      }
    } catch (cause) {
      if (cause instanceof ApiError && cause.status === 423) setLocked(true);
      setError(cause instanceof Error ? cause.message : 'Не удалось выполнить действие');
    } finally {
      setActiveAction(null);
    }
  };

  if (backup.length) return <section className="card account-security">
    <h2>Резервные коды</h2>
    <p>Сохраните коды в безопасном месте. Каждый код можно использовать один раз для восстановления доступа вместе с паролем.</p>
    <pre style={{ whiteSpace: 'pre-wrap', overflowWrap: 'anywhere' }}>{backup.join('\n')}</pre>
    <button type="button" onClick={signInAgain}>Коды сохранены — перейти ко входу</button>
  </section>;

  return <section className="card account-security">
    <h2>Безопасность аккаунта</h2>
    {status.isPending ? <p>Загрузка...</p> : null}
    {status.isError ? <p className="error">{status.error.message}</p> : null}
    {status.data ? <>
      <p>Двухфакторная аутентификация: {status.data.enabled ? 'подключена' : 'не подключена'}</p>
      <p>Сменить пароль до: {new Date(status.data.passwordExpiresAt).toLocaleDateString('ru-RU')}</p>
      {status.data.enabled ? <p>Резервных кодов осталось: {status.data.recoveryCodesRemaining}</p> : null}
      {setup ? <form className="authenticator-setup" onSubmit={(event: FormEvent) => { event.preventDefault(); void act('enable'); }}>
        <img src={setup.qrCode} alt="QR-код подключения аутентификатора" width={240} height={240} />
        <p>Ключ для ручного ввода: <code>{setup.secret}</code></p>
        <label>Код из приложения<input value={code} onChange={event => setCode(event.target.value)} inputMode="numeric"
          autoComplete="one-time-code" pattern="[0-9]{6}" required /></label>
        <div className="toolbar">
          <button disabled={busy}>Подтвердить подключение</button>
          <button type="button" disabled={busy} onClick={() => { setSetup(null); setCode(''); }}>Отмена</button>
        </div>
      </form> : <>
        <PasswordInput label="Текущий пароль" autoComplete="current-password" value={password}
          onChange={event => setPassword(event.target.value)} />
        <p>Введите текущий пароль от аккаунта. Код из приложения вводится отдельно после сканирования QR-кода.</p>
        {status.data.enabled ? <label>Код из приложения<input value={code} onChange={event => setCode(event.target.value)}
          inputMode="numeric" autoComplete="one-time-code" /></label> : null}
        {!status.data.enabled ? <button type="button" disabled={busy || !password} onClick={() => void act('setup')}>
          {activeAction === 'setup' ? 'Создаём QR-код…' : 'Подключить приложение-аутентификатор'}
        </button> : <button type="button" disabled={busy || !password || !code} onClick={() => void act('disable')}>
          Отключить двухфакторную аутентификацию
        </button>}
        <PasswordInput label="Новый пароль" autoComplete="new-password" minLength={12} maxLength={72}
          value={newPassword} onChange={event => setNewPassword(event.target.value)} />
        <button type="button" disabled={busy || !password || newPassword.length < 12 || (status.data.enabled && !code)}
          onClick={() => void act('password')}>Сменить пароль</button>
      </>}
    </> : null}
    {error ? <p className="error" role="alert" tabIndex={-1} ref={errorElement}>{error}</p> : null}
    {locked ? <button type="button" onClick={signInAgain}>Вернуться ко входу</button> : null}
  </section>;
}
