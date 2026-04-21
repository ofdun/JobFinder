import { useState } from 'react';
import { LoginForm } from '../../features/auth/LoginForm';

export function LoginPage() {
  const [role, setRole] = useState<'applicant' | 'employer'>('applicant');

  return (
    <main className="page">
      <h1>Вход</h1>
      <div className="toolbar">
        <button type="button" onClick={() => setRole('applicant')} disabled={role === 'applicant'}>
          Соискатель
        </button>
        <button type="button" onClick={() => setRole('employer')} disabled={role === 'employer'}>
          Работодатель
        </button>
      </div>
      <LoginForm role={role} />
    </main>
  );
}

