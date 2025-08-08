import { MantineProvider } from '@mantine/core';
import { QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { BrowserRouter } from 'react-router-dom';
import { Notifications } from '@mantine/notifications';
import { theme } from './theme';
import { AppShell } from './components/layout';
import { SkipLinks } from './components/ui/SkipLinks/SkipLinks';
import { queryClient } from './services/queryClient';
import { AppRouter } from './router';
import { WebSocketProvider } from './components/providers/WebSocketProvider';
import { initializePerformanceOptimizations } from './utils/performance';
import '@mantine/core/styles.css';
import '@mantine/notifications/styles.css';
import './styles/accessibility.css';

// Initialize performance optimizations
if (typeof window !== 'undefined') {
  initializePerformanceOptimizations();
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <MantineProvider theme={theme}>
        <SkipLinks />
        <Notifications 
          position="top-right"
          role="region"
          aria-label="Notifications"
        />
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
