// Hooks index - exports all custom hooks

export * from './useApi';

// Re-export commonly used hooks from other libraries
export { useQuery, useMutation, useQueryClient, useInfiniteQuery } from '@tanstack/react-query';