import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <main className="page">
      <h1>404</h1>
      <p>Страница не найдена.</p>
      <Link to="/vacancies/search">К поиску вакансий</Link>
    </main>
  );
}

