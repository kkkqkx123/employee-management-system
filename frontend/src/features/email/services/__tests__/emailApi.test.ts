// Email API service tests

import { EmailApiService } from '../emailApi';
import { ApiService } from '../../../../services/api';
import { vi } from 'vitest';

// Mock the ApiService
vi.mock('../../../../services/api');

const mockApiService = ApiService as any;

describe('EmailApiService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getEmailTemplates', () => {
    it('should fetch email templates', async () => {
      const mockTemplates = [
        { id: 1, name: 'Welcome', subject: 'Welcome!', content: 'Hello!', category: 'WELCOME' },
        { id: 2, name: 'Reminder', subject: 'Reminder', content: 'Don\'t forget!', category: 'REMINDER' },
      ];

      mockApiService.get = vi.fn().mockResolvedValue({
        success: true,
        data: mockTemplates,
        message: 'Templates retrieved',
        timestamp: new Date().toISOString(),
      });

      const result = await EmailApiService.getEmailTemplates();

      expect(mockApiService.get).toHaveBeenCalledWith('/email/templates');
      expect(result.data).toEqual(mockTemplates);
    });
  });

  describe('previewEmail', () => {
    it('should generate email preview', async () => {
      const mockPreview = {
        subject: 'Welcome John!',
        content: 'Hello John Doe!',
        recipientCount: 1,
        estimatedDeliveryTime: '2 minutes',
      };

      const previewData = {
        templateId: 1,
        subject: 'Welcome {firstName}!',
        content: 'Hello {firstName} {lastName}!',
        variables: { firstName: 'John', lastName: 'Doe' },
        recipientIds: ['employee-1'],
        recipientTypes: ['employee'],
      };

      mockApiService.post = vi.fn().mockResolvedValue({
        success: true,
        data: mockPreview,
        message: 'Preview generated',
        timestamp: new Date().toISOString(),
      });

      const result = await EmailApiService.previewEmail(previewData);

      expect(mockApiService.post).toHaveBeenCalledWith('/email/preview', previewData);
      expect(result.data).toEqual(mockPreview);
    });
  });

  describe('sendBulkEmail', () => {
    it('should send bulk email', async () => {
      const mockResponse = { jobId: 'job-123' };

      const emailData = {
        templateId: 1,
        subject: 'Test Subject',
        content: 'Test Content',
        recipientIds: ['employee-1', 'department-1'],
        recipientTypes: ['employee', 'department'],
        variables: { company: 'Test Company' },
        priority: 'NORMAL' as const,
      };

      mockApiService.post = vi.fn().mockResolvedValue({
        success: true,
        data: mockResponse,
        message: 'Email sent',
        timestamp: new Date().toISOString(),
      });

      const result = await EmailApiService.sendBulkEmail(emailData);

      expect(mockApiService.post).toHaveBeenCalledWith('/email/send/bulk', emailData);
      expect(result.data).toEqual(mockResponse);
    });
  });
});