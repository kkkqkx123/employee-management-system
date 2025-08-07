// Entity types for the application

export interface Employee {
  id: number;
  employeeNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  department: Department;
  position: Position;
  hireDate: string;
  salary?: number;
  status: EmployeeStatus;
  profilePicture?: string;
  dateOfBirth?: string;
  address?: string;
  emergencyContact?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Department {
  id: number;
  name: string;
  description?: string;
  parentId?: number;
  parent?: Department;
  children?: Department[];
  employeeCount: number;
  managerId?: number;
  manager?: Employee;
  createdAt: string;
  updatedAt: string;
}

export interface Position {
  id: number;
  title: string;
  description?: string;
  department: Department;
  level: PositionLevel;
  category: PositionCategory;
  minSalary?: number;
  maxSalary?: number;
  requirements?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ChatMessage {
  id: number;
  content: string;
  senderId: number;
  senderName: string;
  recipientId: number;
  recipientName: string;
  conversationId?: string;
  messageType: ChatMessageType;
  createdAt: string;
  read: boolean;
  edited?: boolean;
  editedAt?: string;
}

export interface ChatConversation {
  id: string;
  participants: User[];
  lastMessage?: ChatMessage;
  unreadCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface Notification {
  id: number;
  title: string;
  message: string;
  type: NotificationType;
  priority: NotificationPriority;
  userId: number;
  read: boolean;
  actionUrl?: string;
  metadata?: Record<string, any>;
  createdAt: string;
  readAt?: string;
}

export interface EmailTemplate {
  id: number;
  name: string;
  subject: string;
  content: string;
  variables: string[];
  category: EmailTemplateCategory;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface EmailRequest {
  templateId?: number;
  subject: string;
  content: string;
  recipients: string[];
  variables?: Record<string, string>;
  priority: EmailPriority;
  scheduledAt?: string;
}

// Const objects instead of enums for better TypeScript compatibility
export const EmployeeStatus = {
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
  TERMINATED: 'TERMINATED',
  ON_LEAVE: 'ON_LEAVE',
} as const;

export const PositionLevel = {
  ENTRY: 'ENTRY',
  JUNIOR: 'JUNIOR',
  SENIOR: 'SENIOR',
  LEAD: 'LEAD',
  MANAGER: 'MANAGER',
  DIRECTOR: 'DIRECTOR',
  EXECUTIVE: 'EXECUTIVE',
} as const;

export const PositionCategory = {
  ENGINEERING: 'ENGINEERING',
  MARKETING: 'MARKETING',
  SALES: 'SALES',
  HR: 'HR',
  FINANCE: 'FINANCE',
  OPERATIONS: 'OPERATIONS',
  SUPPORT: 'SUPPORT',
} as const;

export const ChatMessageType = {
  TEXT: 'TEXT',
  FILE: 'FILE',
  IMAGE: 'IMAGE',
  SYSTEM: 'SYSTEM',
} as const;

export const NotificationType = {
  INFO: 'INFO',
  SUCCESS: 'SUCCESS',
  WARNING: 'WARNING',
  ERROR: 'ERROR',
  SYSTEM: 'SYSTEM',
} as const;

export const NotificationPriority = {
  LOW: 'LOW',
  NORMAL: 'NORMAL',
  HIGH: 'HIGH',
  URGENT: 'URGENT',
} as const;

export const EmailPriority = {
  LOW: 'LOW',
  NORMAL: 'NORMAL',
  HIGH: 'HIGH',
} as const;

export const EmailTemplateCategory = {
  WELCOME: 'WELCOME',
  ANNOUNCEMENT: 'ANNOUNCEMENT',
  REMINDER: 'REMINDER',
  NOTIFICATION: 'NOTIFICATION',
  SYSTEM: 'SYSTEM',
} as const;

// Type definitions for the const objects
export type EmployeeStatus = typeof EmployeeStatus[keyof typeof EmployeeStatus];
export type PositionLevel = typeof PositionLevel[keyof typeof PositionLevel];
export type PositionCategory = typeof PositionCategory[keyof typeof PositionCategory];
export type ChatMessageType = typeof ChatMessageType[keyof typeof ChatMessageType];
export type NotificationType = typeof NotificationType[keyof typeof NotificationType];
export type NotificationPriority = typeof NotificationPriority[keyof typeof NotificationPriority];
export type EmailPriority = typeof EmailPriority[keyof typeof EmailPriority];
export type EmailTemplateCategory = typeof EmailTemplateCategory[keyof typeof EmailTemplateCategory];

// Form types
export interface EmployeeCreateRequest {
  employeeNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  departmentId: number;
  positionId: number;
  hireDate: string;
  salary?: number;
  status: EmployeeStatus;
  dateOfBirth?: string;
  address?: string;
  emergencyContact?: string;
}

export interface EmployeeUpdateRequest extends Partial<EmployeeCreateRequest> {
  id: number;
}

export interface DepartmentCreateRequest {
  name: string;
  description?: string;
  parentId?: number;
  managerId?: number;
}

export interface DepartmentUpdateRequest extends Partial<DepartmentCreateRequest> {
  id: number;
}

export interface EmployeeSearchCriteria {
  firstName?: string;
  lastName?: string;
  email?: string;
  departmentId?: number;
  positionId?: number;
  status?: EmployeeStatus;
  hireDateFrom?: string;
  hireDateTo?: string;
}

// Import from auth types
import type { User } from './auth';