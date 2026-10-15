import { BrowserRouter, useRoutes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { routes } from './routes';
import { AuthProvider } from './auth/AuthProvider';
import { ToastProvider } from './components/Toast';

// Global QueryClient — shared across the app
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // Retry once on failure (reduces noise for network errors in E2E)
      retry: 1,
      // Stale time: 0 — refetch on mount by default
      staleTime: 0,
    },
  },
});

function RouteRenderer() {
  return useRoutes(routes);
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <ToastProvider>
            {/* Skip link — keyboard users can bypass navigation (WCAG 2.4.1) */}
            <a href="#main-content" className="skip-link">
              Skip to main content
            </a>
            <RouteRenderer />
          </ToastProvider>
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;
