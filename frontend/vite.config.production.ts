import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import path from 'path';

// Production-optimized Vite configuration
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false, // Disable sourcemaps in production for security
    minify: 'terser',
    target: 'esnext',
    chunkSizeWarningLimit: 1000,
    rollupOptions: {
      output: {
        manualChunks: {
          // Core React libraries
          'react-vendor': ['react', 'react-dom'],
          
          // UI Framework
          'ui-framework': [
            '@mantine/core', 
            '@mantine/hooks', 
            '@mantine/notifications',
            '@mantine/modals',
            '@mantine/dates'
          ],
          
          // Data fetching and state management
          'data-layer': [
            '@tanstack/react-query',
            'zustand',
            'axios'
          ],
          
          // Routing
          'router': ['react-router-dom'],
          
          // Forms and validation
          'forms': [
            'react-hook-form',
            '@hookform/resolvers',
            'zod'
          ],
          
          // Icons and assets
          'icons': ['@tabler/icons-react'],
          
          // WebSocket and real-time
          'realtime': ['socket.io-client'],
          
          // Utilities
          'utils': ['date-fns', 'dayjs'],
        },
        // Optimize chunk naming for better caching
        chunkFileNames: (chunkInfo) => {
          const facadeModuleId = chunkInfo.facadeModuleId
            ? chunkInfo.facadeModuleId.split('/').pop()?.replace('.tsx', '').replace('.ts', '')
            : 'chunk';
          return `js/[name]-[hash].js`;
        },
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: (assetInfo) => {
          const info = assetInfo.name?.split('.') || [];
          const ext = info[info.length - 1];
          if (/png|jpe?g|svg|gif|tiff|bmp|ico/i.test(ext || '')) {
            return `images/[name]-[hash][extname]`;
          }
          if (/css/i.test(ext || '')) {
            return `css/[name]-[hash][extname]`;
          }
          return `assets/[name]-[hash][extname]`;
        },
      },
    },
    terserOptions: {
      compress: {
        drop_console: true, // Remove console.log in production
        drop_debugger: true, // Remove debugger statements
        pure_funcs: ['console.log', 'console.info', 'console.debug'], // Remove specific console methods
      },
      mangle: {
        safari10: true, // Fix Safari 10 issues
      },
      format: {
        comments: false, // Remove comments
      },
    },
    reportCompressedSize: false, // Disable for faster builds
    cssCodeSplit: true, // Enable CSS code splitting
  },
  css: {
    modules: {
      localsConvention: 'camelCaseOnly',
    },
    postcss: {
      plugins: [
        // Add autoprefixer for better browser compatibility
      ],
    },
  },
  define: {
    // Define global constants for production
    __DEV__: false,
    __PROD__: true,
    'process.env.NODE_ENV': '"production"',
  },
  esbuild: {
    // Remove console and debugger in production
    drop: ['console', 'debugger'],
  },
  // Optimize dependencies
  optimizeDeps: {
    include: [
      'react',
      'react-dom',
      '@mantine/core',
      '@mantine/hooks',
      '@tanstack/react-query',
      'react-router-dom',
    ],
  },
});