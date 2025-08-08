/// <reference types="vitest" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    css: true,
    include: ['src/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}'],
    exclude: ['node_modules', 'dist', '.idea', '.git', '.cache'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html', 'lcov'],
      exclude: [
        'node_modules/',
        'src/test/',
        '**/*.d.ts',
        '**/*.config.*',
        '**/coverage/**',
        'src/main.tsx',
        'src/vite-env.d.ts',
        '**/*.stories.*',
        '**/mocks/**',
        '**/factories/**',
      ],
      thresholds: {
        global: {
          branches: 80,
          functions: 80,
          lines: 80,
          statements: 80,
        },
        './src/components/ui/': {
          branches: 90,
          functions: 90,
          lines: 90,
          statements: 90,
        },
        './src/features/auth/': {
          branches: 85,
          functions: 85,
          lines: 85,
          statements: 85,
        },
        './src/hooks/': {
          branches: 85,
          functions: 85,
          lines: 85,
          statements: 85,
        },
      },
      reportsDirectory: './coverage',
      all: true,
      include: ['src/**/*.{js,ts,jsx,tsx}'],
    },
  },
});