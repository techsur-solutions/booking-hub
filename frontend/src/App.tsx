import { BrowserRouter, useRoutes } from 'react-router-dom';
import { routes } from './routes';
import { AuthProvider } from './auth/AuthProvider';

function RouteRenderer() {
  return useRoutes(routes);
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <RouteRenderer />
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
