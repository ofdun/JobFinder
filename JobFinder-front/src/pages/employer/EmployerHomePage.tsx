import { Link } from 'react-router-dom';

export function EmployerHomePage() {
  return (
    <main className="page">
      <h1>Кабинет работодателя</h1>
      <p>Переходите в профиль компании для управления данными.</p>
      <div className="toolbar">
        <Link to="/me/employer/profile">Профиль компании</Link>
      </div>
    </main>
  );
}

