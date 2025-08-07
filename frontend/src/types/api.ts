// API response types and interfaces

export interface ApiResponse<T = any> {
  success: boolean;
  message?: string;
  data: T;
  timestamp?: string;
}

export interface ApiError {
  success: false;
  message: string;
  errors?: Array<{
    field: string;
    message: string;
  }>;
  timestamp: string;
  path?: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
}

export interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
}

export interface SearchParams extends PaginationParams {
  q?: string;
}

// HTTP method types
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';

// Request configuration
export interface RequestConfig {
  method?: HttpMethod;
  headers?: Record<string, string>;
  params?: Record<string, any>;
  data?: any;
  timeout?: number;
}

// API endpoints
export const API_ENDPOINTS = {
  // Authentication
  AUTH: {
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh-token',
    VALIDATE: '/auth/validate-token',
  },
  // Users
  USERS: {
    BASE: '/users',
    BY_ID: (id: number) => `/users/${id}`,
    SEARCH: '/users/search',
  },
  // Employees
  EMPLOYEES: {
    BASE: '/employees',
    BY_ID: (id: number) => `/employees/${id}`,
    SEARCH: '/employees/search',
    ADVANCED_SEARCH: '/employees/search/advanced',
    IMPORT: '/employees/import',
    EXPORT: '/employees/export',
  },
  // Departments
  DEPARTMENTS: {
    BASE: '/departments',
    BY_ID: (id: number) => `/departments/${id}`,
    TREE: '/departments/tree',
  },
  // Positions
  POSITIONS: {
    BASE: '/positions',
    BY_ID: (id: number) => `/positions/${id}`,
  },
  // Chat
  CHAT: {
    MESSAGES: '/chat/messages',
    CONVERSATIONS: '/chat/conversations',
    SEND: '/chat/send',
  },
  // Email
  EMAIL: {
    SEND: '/email/send',
    TEMPLATES: '/email/templates',
    BULK_SEND: '/email/bulk-send',
  },
  // Notifications
  NOTIFICATIONS: {
    BASE: '/notifications',
    BY_ID: (id: number) => `/notifications/${id}`,
    MARK_READ: '/notifications/mark-read',
  },
} as const;