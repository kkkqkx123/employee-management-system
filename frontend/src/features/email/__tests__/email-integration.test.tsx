// Email integration tests

import { describe, it, expect, vi } from 'vitest';
import { EmailApiService } from '../services/emailApi';

// Mock the API service
vi.mock('../../../services/api', () => ({
  ApiService: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

describe('Email Integration', () => {
  it('should have EmailApiService with required methods', () => {
    expect(EmailApiService.getEmailTemplates).toBeDefined();
    expect(EmailApiService.previewEmail).toBeDefined();
    expect(EmailApiService.sendBulkEmail).toBeDefined();
    expect(EmailApiService.validateEmail).toBeDefined();
    expect(EmailApiService.getEmailSendProgress).toBeDefined();
  });

  it('should export email types', async () => {
    const { EmailComposer } = await import('../components/EmailComposer');
    const { useEmailComposition } = await import('../hooks/useEmailComposition');
    
    expect(EmailComposer).toBeDefined();
    expect(useEmailComposition).toBeDefined();
  });
});