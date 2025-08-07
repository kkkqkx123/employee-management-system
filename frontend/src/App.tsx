import { MantineProvider } from '@mantine/core';
import { QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { BrowserRouter } from 'react-router-dom';
import { Notifications } from '@mantine/notifications';
import { theme } from './theme';
import { AppShell } from './components/layout';
import { queryClient } from './services/queryClient';
import { ApiDemo } from './demo/ApiDemo';
import '@mantine/core/styles.css';
import '@mantine/notifications/styles.css';

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <MantineProvider theme={theme}>
        <Notifications position="top-right" />
        <BrowserRouter>
          <AppShell>
            <div>
              <h1>Employee Management System</h1>
              <p>Frontend foundation setup complete!</p>
              <p>Build system and development tools configured!</p>
              <p>Core UI component library implemented!</p>
              <p>State management and API integration configured!</p>
              <ApiDemo />
            </div>
          </AppShell>
        </BrowserRouter>
        <ReactQueryDevtools initialIsOpen={false} />
      </MantineProvider>
    </QueryClientProvider>
  );
}

export default App;
