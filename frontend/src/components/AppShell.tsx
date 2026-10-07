import { ReactNode } from 'react';
import { Nav } from './Nav';

interface AppShellProps {
  children: ReactNode;
}

export function AppShell({ children }: AppShellProps) {
  return (
    <div>
      <Nav />
      <main style={{ padding: '20px' }}>
        {children}
      </main>
    </div>
  );
}
