// Coverage configuration and utilities

export const coverageThresholds = {
  global: {
    branches: 80,
    functions: 80,
    lines: 80,
    statements: 80,
  },
  // Per-file thresholds for critical components
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
};

// Files to exclude from coverage
export const coverageExcludes = [
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
];

// Test categories for organized reporting
export const testCategories = {
  unit: {
    pattern: '**/*.{test,spec}.{js,ts,jsx,tsx}',
    exclude: ['**/*.integration.{test,spec}.*', '**/*.e2e.{test,spec}.*'],
  },
  integration: {
    pattern: '**/*.integration.{test,spec}.{js,ts,jsx,tsx}',
  },
  accessibility: {
    pattern: '**/accessibility*.{test,spec}.{js,ts,jsx,tsx}',
  },
  performance: {
    pattern: '**/performance*.{test,spec}.{js,ts,jsx,tsx}',
  },
};

// Quality gates for CI/CD
export const qualityGates = {
  minCoverage: 80,
  maxFailedTests: 0,
  maxTestDuration: 300000, // 5 minutes
  maxBundleSize: 2000000, // 2MB
};