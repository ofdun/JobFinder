import { Link } from 'react-router-dom';

export function ApplicantHomePage() {
  return (
    <main className="page">
      <h1>Кабинет соискателя</h1>
      <p>Переходите в профиль для управления личными данными.</p>
      <div className="toolbar">
        <Link to="/me/profile">Профиль</Link>
      </div>
    </main>
  );
}

