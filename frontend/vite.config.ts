import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import path from 'path';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom'],
          ui: ['@mantine/core', '@mantine/hooks'],
          utils: ['axios', 'socket.io-client'],
        },
      },
    },
    sourcemap: true,
    minify: 'terser',
    target: 'esnext',
  },
  css: {
    modules: {
      localsConvention: 'camelCaseOnly',
    },
  },
});
