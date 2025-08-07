// Application constants
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';
export const WS_URL = import.meta.env.VITE_WS_URL || 'ws://localhost:8080';

export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  DASHBOARD: '/dashboard',
  EMPLOYEES: '/employees',
  DEPARTMENTS: '/departments',
  CHAT: '/chat',
  EMAIL: '/email',
  NOTIFICATIONS: '/notifications',
  PERMISSIONS: '/permissions',
} as const;

export const PERMISSIONS = {
  USER_READ: 'USER_READ',
  USER_CREATE: 'USER_CREATE',
  USER_UPDATE: 'USER_UPDATE',
  USER_DELETE: 'USER_DELETE',
  EMPLOYEE_READ: 'EMPLOYEE_READ',
  EMPLOYEE_CREATE: 'EMPLOYEE_CREATE',
  EMPLOYEE_UPDATE: 'EMPLOYEE_UPDATE',
  EMPLOYEE_DELETE: 'EMPLOYEE_DELETE',
  DEPARTMENT_READ: 'DEPARTMENT_READ',
  DEPARTMENT_CREATE: 'DEPARTMENT_CREATE',
  DEPARTMENT_UPDATE: 'DEPARTMENT_UPDATE',
  DEPARTMENT_DELETE: 'DEPARTMENT_DELETE',
} as const;