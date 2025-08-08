// Email API service

import { ApiService } from '../../../services/api';
import type { ApiResponse, PaginatedResponse } from '../../../types/api';
import type { Employee, Department } from '../../../types/entities';
import type {
  EmailTemplate,
  EmailTemplateWithVariables,
  BulkEmailRequest,
  EmailSendProgress,
  EmailPreview,
  EmailValidationResult,
} from '../types';

export class EmailApiService {
  // Email Templates
  static async getEmailTemplates(): Promise<ApiResponse<EmailTemplate[]>> {
    return ApiService.get<EmailTemplate[]>('/email/templates');
  }

  static async getEmailTemplate(id: number): Promise<ApiResponse<EmailTemplateWithVariables>> {
    return ApiService.get<EmailTemplateWithVariables>(`/email/templates/${id}`);
  }

  static async getEmailTemplatesByCategory(category: string): Promise<ApiResponse<EmailTemplate[]>> {
    return ApiService.get<EmailTemplate[]>(`/email/templates/category/${category}`);
  }

  // Recipients
  static async getEmployees(searchTerm?: string): Promise<ApiResponse<PaginatedResponse<Employee>>> {
    const params = searchTerm ? { search: searchTerm } : {};
    return ApiService.get<PaginatedResponse<Employee>>('/employees', { params });
  }

  static async getDepartments(): Promise<ApiResponse<Department[]>> {
    return ApiService.get<Department[]>('/departments');
  }

  static async getDepartmentEmployees(departmentId: number): Promise<ApiResponse<Employee[]>> {
    return ApiService.get<Employee[]>(`/departments/${departmentId}/employees`);
  }

  // Email Preview
  static async previewEmail(data: {
    templateId?: number;
    subject: string;
    content: string;
    variables: Record<string, string>;
    recipientIds: string[];
    recipientTypes: ('employee' | 'department')[];
  }): Promise<ApiResponse<EmailPreview>> {
    return ApiService.post<EmailPreview>('/email/preview', data);
  }

  // Email Validation
  static async validateEmail(data: {
    subject: string;
    content: string;
    recipientIds: string[];
    variables: Record<string, string>;
  }): Promise<ApiResponse<EmailValidationResult>> {
    return ApiService.post<EmailValidationResult>('/email/validate', data);
  }

  // Send Email
  static async sendBulkEmail(data: BulkEmailRequest): Promise<ApiResponse<{ jobId: string }>> {
    return ApiService.post<{ jobId: string }>('/email/send/bulk', data);
  }

  static async sendSingleEmail(data: {
    templateId?: number;
    subject: string;
    content: string;
    recipientEmail: string;
    variables: Record<string, string>;
  }): Promise<ApiResponse<{ messageId: string }>> {
    return ApiService.post<{ messageId: string }>('/email/send', data);
  }

  // Email Progress Tracking
  static async getEmailSendProgress(jobId: string): Promise<ApiResponse<EmailSendProgress>> {
    return ApiService.get<EmailSendProgress>(`/email/jobs/${jobId}/progress`);
  }

  // Email History
  static async getEmailHistory(params?: {
    page?: number;
    size?: number;
    startDate?: string;
    endDate?: string;
  }): Promise<ApiResponse<PaginatedResponse<any>>> {
    return ApiService.get<PaginatedResponse<any>>('/email/history', { params });
  }
}