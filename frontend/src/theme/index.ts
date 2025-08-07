import { createTheme, type MantineColorsTuple } from '@mantine/core';

// Custom color palette
const primaryColor: MantineColorsTuple = [
  '#f0f9ff',
  '#e0f2fe',
  '#bae6fd',
  '#7dd3fc',
  '#38bdf8',
  '#0ea5e9',
  '#0284c7',
  '#0369a1',
  '#075985',
  '#0c4a6e',
];

const successColor: MantineColorsTuple = [
  '#f0fdf4',
  '#dcfce7',
  '#bbf7d0',
  '#86efac',
  '#4ade80',
  '#22c55e',
  '#16a34a',
  '#15803d',
  '#166534',
  '#14532d',
];

const warningColor: MantineColorsTuple = [
  '#fffbeb',
  '#fef3c7',
  '#fde68a',
  '#fcd34d',
  '#fbbf24',
  '#f59e0b',
  '#d97706',
  '#b45309',
  '#92400e',
  '#78350f',
];

const errorColor: MantineColorsTuple = [
  '#fef2f2',
  '#fecaca',
  '#fca5a5',
  '#f87171',
  '#ef4444',
  '#dc2626',
  '#b91c1c',
  '#991b1b',
  '#7f1d1d',
  '#6b1d1d',
];

export const theme = createTheme({
  primaryColor: 'blue',
  colors: {
    blue: primaryColor,
    green: successColor,
    yellow: warningColor,
    red: errorColor,
  },
  fontFamily: 'Inter, system-ui, Avenir, Helvetica, Arial, sans-serif',
  headings: {
    fontFamily: 'Inter, system-ui, Avenir, Helvetica, Arial, sans-serif',
    sizes: {
      h1: { fontSize: '2.5rem', fontWeight: '700', lineHeight: '1.2' },
      h2: { fontSize: '2rem', fontWeight: '600', lineHeight: '1.3' },
      h3: { fontSize: '1.5rem', fontWeight: '600', lineHeight: '1.4' },
      h4: { fontSize: '1.25rem', fontWeight: '500', lineHeight: '1.4' },
      h5: { fontSize: '1.125rem', fontWeight: '500', lineHeight: '1.5' },
      h6: { fontSize: '1rem', fontWeight: '500', lineHeight: '1.5' },
    },
  },
  spacing: {
    xs: '0.25rem',
    sm: '0.5rem',
    md: '1rem',
    lg: '1.5rem',
    xl: '2rem',
  },
  radius: {
    xs: '0.125rem',
    sm: '0.25rem',
    md: '0.375rem',
    lg: '0.5rem',
    xl: '0.75rem',
  },
  shadows: {
    xs: '0 1px 2px 0 rgb(0 0 0 / 0.05)',
    sm: '0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1)',
    md: '0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)',
    lg: '0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1)',
    xl: '0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1)',
  },
  components: {
    Button: {
      defaultProps: {
        size: 'md',
        radius: 'md',
      },
    },
    TextInput: {
      defaultProps: {
        size: 'md',
        radius: 'md',
      },
    },
    Select: {
      defaultProps: {
        size: 'md',
        radius: 'md',
      },
    },
    Modal: {
      defaultProps: {
        centered: true,
        overlayProps: { backgroundOpacity: 0.55, blur: 3 },
      },
    },
  },
});