// Email feature types

import type { EmailTemplate, EmailRequest, EmailPriority, EmailTemplateCategory } from '../../../types/entities';

export interface EmailRecipient {
  id: string;
  type: 'employee' | 'department' | 'group';
  name: string;
  email?: string;
  employeeCount?: number;
}

export interface EmailComposition {
  templateId?: number;
  subject: string;
  content: string;
  recipients: EmailRecipient[];
  variables: Record<string, string>;
  priority: EmailPriority;
  scheduledAt?: string;
}

export interface EmailPreview {
  subject: string;
  content: string;
  recipientCount: number;
  estimatedDeliveryTime: string;
}

export interface EmailSendProgress {
  total: number;
  sent: number;
  failed: number;
  inProgress: boolean;
  errors: EmailSendError[];
}

export interface EmailSendError {
  recipient: string;
  error: string;
}

export interface EmailValidationResult {
  isValid: boolean;
  errors: EmailValidationError[];
}

export interface EmailValidationError {
  field: string;
  message: string;
}

export interface BulkEmailRequest {
  templateId?: number;
  subject: string;
  content: string;
  recipientIds: string[];
  recipientTypes: ('employee' | 'department')[];
  variables: Record<string, string>;
  priority: EmailPriority;
  scheduledAt?: string;
}

export interface EmailTemplateVariable {
  name: string;
  description: string;
  required: boolean;
  defaultValue?: string;
}

export interface EmailTemplateWithVariables extends EmailTemplate {
  variableDefinitions: EmailTemplateVariable[];
}

// Re-export types from entities for convenience
export type { EmailTemplate, EmailRequest, EmailPriority, EmailTemplateCategory };