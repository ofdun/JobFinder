import { FormEvent, useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { useLocation, useNavigate } from "react-router-dom";
import { login, verifyTotp, renewPassword, recoverAccount } from "../../shared/api/authApi";
import { resolveSafeReturnPath } from "../../shared/lib/resolveSafeReturnPath";
import {
  extractUserIdFromToken,
  setSession,
} from "../../shared/session/sessionStore";
import { UserRole, AuthenticationChallenge } from "../../shared/types/auth";
import { PasswordInput } from "../../shared/ui/PasswordInput";
import { ApiError } from "../../shared/api/httpClient";

interface LoginFormProps {
  role: UserRole;
}

export function LoginForm({ role }: LoginFormProps) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [challenge, setChallenge] = useState<AuthenticationChallenge | null>(null);
  const [code, setCode] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [recovery, setRecovery] = useState(false);
  const [resetAuthenticator, setResetAuthenticator] = useState(false);
  const [notice, setNotice] = useState("");
  const navigate = useNavigate();
  const location = useLocation();

  const mutation = useMutation({
    mutationFn: async () => {
      if (recovery) {
        await recoverAccount(role, email, password, code, newPassword, resetAuthenticator);
        return null;
      }
      if (challenge?.nextStep === 'PASSWORD_CHANGE') {
        await renewPassword(challenge.challengeToken, newPassword);
        return null;
      }
      const tokenPair = challenge
        ? await verifyTotp(challenge.challengeToken, code)
        : await login(role, { email, password });
      if ('challengeToken' in tokenPair) return tokenPair;
      const userId = extractUserIdFromToken(tokenPair.accessToken);
      if (!userId) {
        throw new Error("Invalid access token payload: missing sub");
      }

      return { tokenPair, userId };
    },
    onSuccess: (result) => {
      setCode("");
      if (!result) {
        setChallenge(null);
        setRecovery(false);
        setPassword("");
        setNewPassword("");
        setNotice("Пароль изменён. Войдите с новым паролем.");
        return;
      }
      if ('challengeToken' in result) {
        setChallenge(result);
        setPassword("");
        return;
      }
      const { tokenPair, userId } = result;
      setSession({
        role,
        userId,
        accessToken: tokenPair.accessToken,
        refreshToken: tokenPair.refreshToken,
      });

      const destinationFromState = (location.state as { from?: string } | null)
        ?.from;
      const destinationFromQuery =
        new URLSearchParams(location.search).get("from") ?? undefined;
      const destination =
        resolveSafeReturnPath(destinationFromState) ??
        resolveSafeReturnPath(destinationFromQuery);
      navigate(destination || (role === 'applicant' ? '/me/applicant' : '/me/employer'));
    },
  });

  const accountLocked = !recovery && mutation.error instanceof ApiError && mutation.error.status === 423;

  const onSubmit = (event: FormEvent) => {
    event.preventDefault();
    if (mutation.isPending || accountLocked) return;
    mutation.mutate();
  };

  return (
    <form className="card" onSubmit={onSubmit}>
      <h2>Вход ({role === "applicant" ? "соискатель" : "работодатель"})</h2>
      {notice ? <p role="status">{notice}</p> : null}
      {!challenge ? <>
      <label>
        Email
        <input
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          type="email"
          autoComplete="email"
          required
        />
      </label>
      </> : null}
      {challenge?.nextStep === 'TOTP' || recovery ? <label>
        {recovery ? 'Резервный код' : 'Код из приложения-аутентификатора'}
        <input value={code} onChange={event => setCode(event.target.value)} autoComplete="one-time-code"
          inputMode={recovery ? 'text' : 'numeric'} required />
      </label> : null}
      {challenge?.nextStep === 'PASSWORD_CHANGE' || recovery ?
        <PasswordInput label="Новый пароль" value={newPassword} onChange={event => setNewPassword(event.target.value)}
          autoComplete="new-password" minLength={12} maxLength={72} required />
      : null}
      {!challenge ?
        <PasswordInput
          label="Пароль"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          autoComplete="current-password"
          required
        />
      : null}
      {mutation.isError ? (
        <p className="error" role="alert">
          {mutation.error.message || "Не удалось войти. Проверьте данные."}
        </p>
      ) : null}
      {recovery ? <label className="inline-checkbox">
        <input type="checkbox" checked={resetAuthenticator} onChange={event => setResetAuthenticator(event.target.checked)} />
        Телефон потерян — сбросить привязку аутентификатора
      </label> : null}
      <button type="submit" disabled={mutation.isPending || accountLocked}>
        {mutation.isPending ? "Проверяем..." : recovery || challenge?.nextStep === 'PASSWORD_CHANGE' ? "Изменить пароль" : challenge ? "Подтвердить" : "Войти"}
      </button>
      {accountLocked ? <button type="button" onClick={() => {
        setChallenge(null); setPassword(""); setCode(""); setNewPassword(""); setNotice(""); mutation.reset();
      }}>Вернуться ко входу</button> : null}
      <button type="button" disabled={mutation.isPending} onClick={() => {
        setRecovery(!recovery); setChallenge(null); setCode(""); setNotice(""); mutation.reset();
      }}>{recovery ? 'Вернуться ко входу' : 'Восстановить доступ резервным кодом'}</button>
    </form>
  );
}
