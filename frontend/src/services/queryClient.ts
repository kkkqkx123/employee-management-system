// TanStack Query client configuration

import { QueryClient, QueryCache, MutationCache } from '@tanstack/react-query';
import { useUIStore } from '../stores/uiStore';
import type { ApiError } from '../types/api';
import type { EmployeeSearchCriteria } from '../types/entities';

// Create query client with custom configuration
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // Global query defaults
      staleTime: 5 * 60 * 1000, // 5 minutes
      gcTime: 10 * 60 * 1000, // 10 minutes (formerly cacheTime)
      retry: (failureCount, error) => {
        // Don't retry on 4xx errors (client errors)
        if ((error as unknown as ApiError)?.message?.includes('4')) {
          return false;
        }
        // Retry up to 3 times for other errors
        return failureCount < 3;
      },
      retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
      refetchOnWindowFocus: false,
      refetchOnReconnect: true,
    },
    mutations: {
      // Global mutation defaults
      retry: false,
    },
  },
  queryCache: new QueryCache({
    onError: (error, query) => {
      // Global error handling for queries
      console.error('Query error:', error, query);
      
      const apiError = error as unknown as ApiError;
      if (apiError?.message) {
        // Show error notification
        useUIStore.getState().showNotification({
          type: 'error',
          title: 'Query Error',
          message: apiError.message,
          autoClose: true,
          duration: 5000,
        });
      }
    },
  }),
  mutationCache: new MutationCache({
    onError: (error, variables, context, mutation) => {
      // Global error handling for mutations
      console.error('Mutation error:', error, { variables, context, mutation });
      
      const apiError = error as unknown as ApiError;
      if (apiError?.message) {
        // Show error notification
        useUIStore.getState().showNotification({
          type: 'error',
          title: 'Operation Failed',
          message: apiError.message,
          autoClose: true,
          duration: 5000,
        });
      }
    },
    onSuccess: (data, variables, context, mutation) => {
      // Global success handling for mutations
      if (import.meta.env.DEV) {
        console.log('Mutation success:', { data, variables, context, mutation });
      }
    },
  }),
});

// Query keys factory for consistent key management
export const queryKeys = {
  // Authentication
  auth: {
    user: ['auth', 'user'] as const,
    validate: ['auth', 'validate'] as const,
  },
  
  // Users
  users: {
    all: ['users'] as const,
    lists: () => [...queryKeys.users.all, 'list'] as const,
    list: (params: Record<string, unknown>) => [...queryKeys.users.lists(), params] as const,
    details: () => [...queryKeys.users.all, 'detail'] as const,
    detail: (id: number) => [...queryKeys.users.details(), id] as const,
    search: (query: string) => [...queryKeys.users.all, 'search', query] as const,
  },
  
  // Employees
  employees: {
    all: ['employees'] as const,
    lists: () => [...queryKeys.employees.all, 'list'] as const,
    list: (params: Record<string, unknown>) => [...queryKeys.employees.lists(), params] as const,
    details: () => [...queryKeys.employees.all, 'detail'] as const,
    detail: (id: number) => [...queryKeys.employees.details(), id] as const,
    search: (query: string) => [...queryKeys.employees.all, 'search', query] as const,
    advancedSearch: (criteria: EmployeeSearchCriteria) => [...queryKeys.employees.all, 'advanced-search', criteria] as const,
  },
  
  // Departments
  departments: {
    all: ['departments'] as const,
    lists: () => [...queryKeys.departments.all, 'list'] as const,
    list: (params?: Record<string, unknown>) => [...queryKeys.departments.lists(), params || {}] as const,
    tree: ['departments', 'tree'] as const,
    details: () => [...queryKeys.departments.all, 'detail'] as const,
    detail: (id: number) => [...queryKeys.departments.details(), id] as const,
  },
  
  // Positions
  positions: {
    all: ['positions'] as const,
    lists: () => [...queryKeys.positions.all, 'list'] as const,
    list: (params?: Record<string, unknown>) => [...queryKeys.positions.lists(), params || {}] as const,
    details: () => [...queryKeys.positions.all, 'detail'] as const,
    detail: (id: number) => [...queryKeys.positions.details(), id] as const,
  },
  
  // Chat
  chat: {
    all: ['chat'] as const,
    conversations: ['chat', 'conversations'] as const,
    messages: (conversationId: string) => ['chat', 'messages', conversationId] as const,
  },
  
  // Email
  email: {
    all: ['email'] as const,
    templates: ['email', 'templates'] as const,
    template: (id: number) => ['email', 'template', id] as const,
  },
  
  // Notifications
  notifications: {
    all: ['notifications'] as const,
    lists: () => [...queryKeys.notifications.all, 'list'] as const,
    list: (params?: Record<string, unknown>) => [...queryKeys.notifications.lists(), params || {}] as const,
    unread: ['notifications', 'unread'] as const,
  },
} as const;

// Utility functions for cache management
export const invalidateQueries = {
  // Invalidate all user-related queries
  users: () => queryClient.invalidateQueries({ queryKey: queryKeys.users.all }),
  
  // Invalidate all employee-related queries
  employees: () => queryClient.invalidateQueries({ queryKey: queryKeys.employees.all }),
  
  // Invalidate all department-related queries
  departments: () => queryClient.invalidateQueries({ queryKey: queryKeys.departments.all }),
  
  // Invalidate all position-related queries
  positions: () => queryClient.invalidateQueries({ queryKey: queryKeys.positions.all }),
  
  // Invalidate all chat-related queries
  chat: () => queryClient.invalidateQueries({ queryKey: queryKeys.chat.all }),
  
  // Invalidate all notification-related queries
  notifications: () => queryClient.invalidateQueries({ queryKey: queryKeys.notifications.all }),
};

// Prefetch utilities
export const prefetchQueries = {
  // Prefetch employee list
  employees: (params?: Record<string, unknown>) =>
    queryClient.prefetchQuery({
      queryKey: queryKeys.employees.list(params || {}),
      staleTime: 2 * 60 * 1000, // 2 minutes
    }),
  
  // Prefetch department tree
  departmentTree: () =>
    queryClient.prefetchQuery({
      queryKey: queryKeys.departments.tree,
      staleTime: 5 * 60 * 1000, // 5 minutes
    }),
  
  // Prefetch notifications
  notifications: () =>
    queryClient.prefetchQuery({
      queryKey: queryKeys.notifications.list(),
      staleTime: 1 * 60 * 1000, // 1 minute
    }),
};

// Cache utilities
export const cacheUtils = {
  // Get cached data
  getQueryData: <T>(queryKey: readonly unknown[]) => 
    queryClient.getQueryData<T>(queryKey),
  
  // Set cached data
  setQueryData: <T>(queryKey: readonly unknown[], data: T) =>
    queryClient.setQueryData(queryKey, data),
  
  // Remove cached data
  removeQueries: (queryKey: readonly unknown[]) =>
    queryClient.removeQueries({ queryKey }),
  
  // Clear all cache
  clear: () => queryClient.clear(),
};

export default queryClient;