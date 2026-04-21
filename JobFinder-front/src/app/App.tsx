import { AppProviders } from './providers';
import { AppShell } from './AppShell';
import { AppRouter } from './router';

export function App() {
  return (
    <AppProviders>
      <AppShell />
      <AppRouter />
    </AppProviders>
  );
}

