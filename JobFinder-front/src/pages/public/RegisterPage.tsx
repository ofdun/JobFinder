import { useState } from 'react';
import { RegisterApplicantForm } from '../../features/auth/RegisterApplicantForm';
import { RegisterEmployerForm } from '../../features/auth/RegisterEmployerForm';

export function RegisterPage() {
  const [role, setRole] = useState<'applicant' | 'employer'>('applicant');

  return (
    <main className="page">
      <h1>Регистрация</h1>
      <div className="toolbar">
        <button type="button" onClick={() => setRole('applicant')} disabled={role === 'applicant'}>
          Соискатель
        </button>
        <button type="button" onClick={() => setRole('employer')} disabled={role === 'employer'}>
          Работодатель
        </button>
      </div>
      {role === 'applicant' ? <RegisterApplicantForm /> : <RegisterEmployerForm />}
    </main>
  );
}

