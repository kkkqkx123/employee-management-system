import { MantineProvider } from '@mantine/core';
import { QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { BrowserRouter } from 'react-router-dom';
import { Notifications } from '@mantine/notifications';
import { theme } from './theme';
import { AppShell } from './components/layout';
import { queryClient } from './services/queryClient';
import { AppRouter } from './router';
import { WebSocketProvider } from './components/providers/WebSocketProvider';
import '@mantine/core/styles.css';
import '@mantine/notifications/styles.css';

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <MantineProvider theme={theme}>
        <Notifications position="top-right" />
        <BrowserRouter>
          <WebSocketProvider showConnectionStatus={true} autoConnect={true}>
            <AppShell>
              <AppRouter />
            </AppShell>
          </WebSocketProvider>
        </BrowserRouter>
        <ReactQueryDevtools initialIsOpen={false} />
      </MantineProvider>
    </QueryClientProvider>
  );
}

export default App;
