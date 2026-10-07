import { BrowserRouter, useRoutes } from 'react-router-dom';
import { routes } from './routes';

function RouteRenderer() {
  return useRoutes(routes);
}

function App() {
  return (
    <BrowserRouter>
      <RouteRenderer />
    </BrowserRouter>
  );
}

export default App;
