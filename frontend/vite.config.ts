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
          ui: ['@mantine/core', '@mantine/hooks', '@mantine/notifications'],
          query: ['@tanstack/react-query'],
          router: ['react-router-dom'],
          utils: ['axios', 'socket.io-client'],
          forms: ['react-hook-form', '@hookform/resolvers', 'zod'],
          icons: ['@tabler/icons-react'],
        },
      },
    },
    sourcemap: process.env.NODE_ENV === 'development',
    minify: 'terser',
    target: 'esnext',
    chunkSizeWarningLimit: 1000,
    terserOptions: {
      compress: {
        drop_console: process.env.NODE_ENV === 'production',
        drop_debugger: process.env.NODE_ENV === 'production',
      },
    },
    reportCompressedSize: false, // Disable for faster builds
  },
  css: {
    modules: {
      localsConvention: 'camelCaseOnly',
    },
  },
});
