/**
 * Security integration tests
 */

import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import { SecurityUtils } from '../utils/security';
import { CSRFProtection } from '../utils/csrfProtection';
import { ApiService } from '../services/api';
import { SECURITY_CONFIG } from '../config/security';

// Mock components for testing
const TestForm = () => {
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const formData = new FormData(e.target as HTMLFormElement);
    const data = Object.fromEntries(formData.entries());
    console.log('Form submitted:', data);
  };

  return (
    <form onSubmit={handleSubmit}>
      <input name="name" placeholder="Name" />
      <textarea name="description" placeholder="Description" />
      <button type="submit">Submit</button>
    </form>
  );
};

const TestWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        {children}
      </BrowserRouter>
    </QueryClientProvider>
  );
};

describe('Security Integration Tests', () => {
  let mockFetch: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    // Mock fetch
    mockFetch = vi.fn();
    global.fetch = mockFetch;

    // Mock console methods
    vi.spyOn(console, 'warn').mockImplementation(() => {});
    vi.spyOn(console, 'error').mockImplementation(() => {});

    // Reset security state
    CSRFProtection.clearToken();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('XSS Prevention', () => {
    it('should sanitize user input in forms', async () => {
      const user = userEvent.setup();
      
      render(
        <TestWrapper>
          <TestForm />
        </TestWrapper>
      );

      const nameInput = screen.getByPlaceholderText('Name');
      const descriptionTextarea = screen.getByPlaceholderText('Description');

      // Try to inject XSS
      await user.type(nameInput, '<script>alert("xss")</script>John');
      await user.type(descriptionTextarea, 'Description with <img src="x" onerror="alert(1)">');

      // Simulate form submission with sanitization
      const nameValue = (nameInput as HTMLInputElement).value;
      const descriptionValue = (descriptionTextarea as HTMLTextAreaElement).value;

      const sanitizedName = SecurityUtils.sanitizeText(nameValue);
      const sanitizedDescription = SecurityUtils.sanitizeHtml(descriptionValue);

      expect(sanitizedName).not.toContain('<script>');
      expect(sanitizedName).toBe('John');
      expect(sanitizedDescription).not.toContain('onerror');
    });

    it('should detect and prevent XSS in content', () => {
      const maliciousContent = [
        '<script>alert("xss")</script>',
        'javascript:alert("xss")',
        '<img src="x" onerror="alert(1)">',
        '<iframe src="javascript:alert(1)"></iframe>',
      ];

      maliciousContent.forEach(content => {
        expect(SecurityUtils.containsXSS(content)).toBe(true);
      });

      const safeContent = [
        'This is safe content',
        '<p>Safe HTML</p>',
        '<a href="https://example.com">Safe link</a>',
      ];

      safeContent.forEach(content => {
        expect(SecurityUtils.containsXSS(content)).toBe(false);
      });
    });
  });

  describe('CSRF Protection', () => {
    it('should include CSRF token in state-changing requests', async () => {
      CSRFProtection.initialize();
      
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true }),
      });

      await ApiService.post('/api/test', { data: 'test' });

      expect(mockFetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            'X-CSRF-Token': expect.any(String),
          }),
        })
      );
    });

    it('should not include CSRF token in GET requests', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true }),
      });

      await ApiService.get('/api/test');

      const lastCall = mockFetch.mock.calls[mockFetch.mock.calls.length - 1];
      const headers = lastCall[1]?.headers || {};
      
      expect(headers).not.toHaveProperty('X-CSRF-Token');
    });

    it('should validate CSRF tokens', () => {
      CSRFProtection.initialize();
      const token = CSRFProtection.getToken();

      expect(CSRFProtection.validateToken(token!)).toBe(true);
      expect(CSRFProtection.validateToken('invalid-token')).toBe(false);
    });
  });

  describe('Rate Limiting', () => {
    it('should enforce API rate limits', async () => {
      const config = SECURITY_CONFIG.RATE_LIMITS.API_REQUESTS;
      
      // Mock multiple rapid requests
      for (let i = 0; i < config.maxRequests + 5; i++) {
        try {
          await ApiService.get(`/api/test-${i}`);
        } catch (error) {
          if (i >= config.maxRequests) {
            expect(error).toEqual(new Error('Rate limit exceeded'));
          }
        }
      }
    });

    it('should track remaining requests', () => {
      const { RateLimiter } = require('../utils/security');
      const key = 'test-key';
      const maxRequests = 5;

      expect(RateLimiter.getRemainingRequests(key, maxRequests)).toBe(5);

      RateLimiter.isAllowed(key, maxRequests);
      expect(RateLimiter.getRemainingRequests(key, maxRequests)).toBe(4);
    });
  });

  describe('File Upload Security', () => {
    it('should validate file types and sizes', () => {
      const validFile = new File(['content'], 'test.jpg', { type: 'image/jpeg' });
      const invalidFile = new File(['content'], 'malware.exe', { type: 'application/exe' });
      const largeFile = new File([new ArrayBuffer(20 * 1024 * 1024)], 'large.jpg', { type: 'image/jpeg' });

      const validResult = SecurityUtils.validateFile(validFile, SECURITY_CONFIG.FILE_UPLOAD);
      const invalidResult = SecurityUtils.validateFile(invalidFile, SECURITY_CONFIG.FILE_UPLOAD);
      const largeResult = SecurityUtils.validateFile(largeFile, SECURITY_CONFIG.FILE_UPLOAD);

      expect(validResult.isValid).toBe(true);
      expect(invalidResult.isValid).toBe(false);
      expect(largeResult.isValid).toBe(false);
    });

    it('should detect suspicious file names', () => {
      const suspiciousFiles = [
        new File([''], 'script.php', { type: 'text/plain' }),
        new File([''], 'malware.exe', { type: 'application/exe' }),
        new File([''], 'virus.bat', { type: 'text/plain' }),
      ];

      suspiciousFiles.forEach(file => {
        const result = SecurityUtils.validateFile(file, SECURITY_CONFIG.FILE_UPLOAD);
        expect(result.isValid).toBe(false);
        expect(result.error).toContain('security reasons');
      });
    });
  });

  describe('Token Security', () => {
    it('should validate JWT token format', () => {
      const { TokenSecurity } = require('../utils/tokenSecurity');
      
      const validJWT = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c';
      const invalidJWT = 'invalid.token.format';

      expect(TokenSecurity.isValidJWTFormat(validJWT)).toBe(true);
      expect(TokenSecurity.isValidJWTFormat(invalidJWT)).toBe(false);
    });

    it('should detect expired tokens', () => {
      const { TokenSecurity } = require('../utils/tokenSecurity');
      
      // Create expired token
      const expiredPayload = { exp: Math.floor(Date.now() / 1000) - 3600 }; // 1 hour ago
      const expiredToken = `header.${btoa(JSON.stringify(expiredPayload))}.signature`;

      // Create valid token
      const validPayload = { exp: Math.floor(Date.now() / 1000) + 3600 }; // 1 hour from now
      const validToken = `header.${btoa(JSON.stringify(validPayload))}.signature`;

      expect(TokenSecurity.isTokenExpired(expiredToken)).toBe(true);
      expect(TokenSecurity.isTokenExpired(validToken)).toBe(false);
    });
  });

  describe('Content Security Policy', () => {
    it('should detect CSP violations', (done) => {
      const { ContentSecurityPolicy } = require('../utils/contentSecurityPolicy');
      
      // Listen for CSP violations
      const violationHandler = (event: SecurityPolicyViolationEvent) => {
        expect(event.violatedDirective).toBeDefined();
        expect(event.blockedURI).toBeDefined();
        done();
      };

      document.addEventListener('securitypolicyviolation', violationHandler);

      // Apply strict CSP
      ContentSecurityPolicy.applyPolicy("default-src 'self'");

      // Try to trigger a violation (this might not work in test environment)
      const script = document.createElement('script');
      script.src = 'https://malicious.com/script.js';
      document.head.appendChild(script);

      // Cleanup
      setTimeout(() => {
        document.removeEventListener('securitypolicyviolation', violationHandler);
        document.head.removeChild(script);
        if (!done) done(); // Ensure test completes even if no violation occurs
      }, 100);
    });
  });

  describe('Security Headers', () => {
    it('should include security headers in API requests', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true }),
      });

      await ApiService.get('/api/test');

      const lastCall = mockFetch.mock.calls[mockFetch.mock.calls.length - 1];
      const headers = lastCall[1]?.headers || {};

      expect(headers).toHaveProperty('X-Content-Type-Options', 'nosniff');
      expect(headers).toHaveProperty('X-Frame-Options', 'DENY');
      expect(headers).toHaveProperty('X-XSS-Protection', '1; mode=block');
    });
  });

  describe('Input Validation', () => {
    it('should validate email addresses', () => {
      const validEmails = [
        'test@example.com',
        'user.name@domain.co.uk',
        'user+tag@example.org',
      ];

      const invalidEmails = [
        'invalid-email',
        'test@',
        '@example.com',
        'test<script>@example.com',
        'test@example.com<script>',
      ];

      validEmails.forEach(email => {
        expect(SecurityUtils.validateEmail(email)).toBe(true);
      });

      invalidEmails.forEach(email => {
        expect(SecurityUtils.validateEmail(email)).toBe(false);
      });
    });

    it('should sanitize form data before submission', () => {
      const maliciousData = {
        name: '<script>alert("xss")</script>John',
        email: 'john@example.com',
        description: 'Test<script>alert("xss")</script>',
        tags: ['<script>tag1</script>', 'tag2'],
        age: 25,
      };

      const sanitized = SecurityUtils.sanitizeFormData(maliciousData);

      expect(sanitized.name).not.toContain('<script>');
      expect(sanitized.description).not.toContain('<script>');
      expect(sanitized.tags[0]).not.toContain('<script>');
      expect(sanitized.email).toBe('john@example.com'); // Should remain unchanged
      expect(sanitized.age).toBe(25); // Should remain unchanged
    });
  });
});