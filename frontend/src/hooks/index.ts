// Hooks index - exports all custom hooks

export * from './useApi';
export * from './useEventBus';
export * from './useWebSocket';
export * from './useResponsive';

// Re-export commonly used hooks from other libraries
export { useQuery, useMutation, useQueryClient, useInfiniteQuery } from '@tanstack/react-query';