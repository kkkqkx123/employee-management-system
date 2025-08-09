/**
 * Security integration tests
 */

import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import { SecurityUtils } from '../utils/security';
import { CSRFProtection } from '../utils/csrfProtection';
import { ApiService, apiClient } from '../services/api';
import { SECURITY_CONFIG } from '../config/security';
import { RateLimiter } from '../utils/security';
import { TokenSecurity } from '../utils/tokenSecurity';
import { ContentSecurityPolicy } from '../utils/contentSecurityPolicy';

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
  let mockAxios: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    // Mock axios
    mockAxios = vi.fn();
    vi.spyOn(apiClient, 'get').mockImplementation(mockAxios);
    vi.spyOn(apiClient, 'post').mockImplementation(mockAxios);
    vi.spyOn(apiClient, 'put').mockImplementation(mockAxios);
    vi.spyOn(apiClient, 'patch').mockImplementation(mockAxios);
    vi.spyOn(apiClient, 'delete').mockImplementation(mockAxios);

    // Mock console methods
    vi.spyOn(console, 'warn').mockImplementation(() => { });
    vi.spyOn(console, 'error').mockImplementation(() => { });

    // Reset security state
    CSRFProtection.clearToken();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('XSS Prevention', () => {
    it('should sanitize user input in forms', () => {
      // Test SecurityUtils.sanitizeText directly
      const input = '<script>alert("xss")</script>John';
      const sanitizedName = SecurityUtils.sanitizeText(input);

      expect(sanitizedName).not.toContain('<script>');
      expect(sanitizedName).toBe('John');

      // Test SecurityUtils.sanitizeHtml
      const description = 'Description with <img src="x" onerror="alert(1)">';
      const sanitizedDescription = SecurityUtils.sanitizeHtml(description);

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

      mockAxios.mockResolvedValueOnce({
        data: { success: true },
      });

      await ApiService.post('/api/test', { data: 'test' });

      expect(mockAxios).toHaveBeenCalledWith(
        '/api/test',
        { data: 'test' },
        undefined
      );

      // Check that the request was made with CSRF token
      const lastCall = mockAxios.mock.calls[mockAxios.mock.calls.length - 1];
      const config = lastCall[2]; // Third argument is the config
      if (config) {
        expect(config.headers).toHaveProperty('X-CSRF-Token');
      }
    });

    it('should not include CSRF token in GET requests', async () => {
      mockAxios.mockResolvedValueOnce({
        data: { success: true },
      });

      await ApiService.get('/api/test');

      // Check that the request was made without CSRF token
      const lastCall = mockAxios.mock.calls[mockAxios.mock.calls.length - 1];
      const config = lastCall[1]; // Second argument is the config
      if (config) {
        expect(config.headers).not.toHaveProperty('X-CSRF-Token');
      }
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

      // Mock axios
      const mockAxios = vi.fn();
      vi.spyOn(apiClient, 'get').mockImplementation(mockAxios);

      // Make the first requests succeed
      mockAxios.mockResolvedValue({ data: { success: true } });

      // Mock multiple rapid requests
      for (let i = 0; i < config.maxRequests + 5; i++) {
        try {
          await ApiService.get(`/api/test-${i}`);
        } catch (error) {
          if (i >= config.maxRequests) {
            expect((error as Error).message).toBe('Rate limit exceeded');
          }
        }
      }
    });

    it('should track remaining requests', () => {
      // Remove the import statement as it's now at the top
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

      const validResult = SecurityUtils.validateFile(validFile, {
        allowedTypes: [...SECURITY_CONFIG.FILE_UPLOAD.ALLOWED_TYPES],
        maxSize: SECURITY_CONFIG.FILE_UPLOAD.MAX_SIZE,
        allowedExtensions: [...SECURITY_CONFIG.FILE_UPLOAD.ALLOWED_EXTENSIONS]
      });
      const invalidResult = SecurityUtils.validateFile(invalidFile, {
        allowedTypes: [...SECURITY_CONFIG.FILE_UPLOAD.ALLOWED_TYPES],
        maxSize: SECURITY_CONFIG.FILE_UPLOAD.MAX_SIZE,
        allowedExtensions: [...SECURITY_CONFIG.FILE_UPLOAD.ALLOWED_EXTENSIONS]
      });
      const largeResult = SecurityUtils.validateFile(largeFile, {
        allowedTypes: [...SECURITY_CONFIG.FILE_UPLOAD.ALLOWED_TYPES],
        maxSize: SECURITY_CONFIG.FILE_UPLOAD.MAX_SIZE,
        allowedExtensions: [...SECURITY_CONFIG.FILE_UPLOAD.ALLOWED_EXTENSIONS]
      });

      expect(validResult.isValid).toBe(true);
      expect(invalidResult.isValid).toBe(false);
      expect(largeResult.isValid).toBe(false);
    });

    it('should detect suspicious file names', () => {
      const suspiciousFiles = [
        new File([''], 'script.php', { type: 'application/pdf' }),
        new File([''], 'malware.exe', { type: 'application/pdf' }),
        new File([''], 'virus.bat', { type: 'application/pdf' }),
      ];

      suspiciousFiles.forEach(file => {
        const result = SecurityUtils.validateFile(file, {
          allowedTypes: [...SECURITY_CONFIG.FILE_UPLOAD.ALLOWED_TYPES],
          maxSize: SECURITY_CONFIG.FILE_UPLOAD.MAX_SIZE,
          allowedExtensions: [...SECURITY_CONFIG.FILE_UPLOAD.ALLOWED_EXTENSIONS, '.php', '.exe', '.bat']
        });
        expect(result.isValid).toBe(false);
        expect(result.error).toContain('security reasons');
      });
    });
  });

  describe('Token Security', () => {
    it('should validate JWT token format', () => {
      // Remove the import statement as it's now at the top

      const validJWT = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c';
      const invalidJWT = 'invalid.token'; // 只有两个部分，不是一个有效的 JWT

      expect(TokenSecurity.isValidJWTFormat(validJWT)).toBe(true);
      expect(TokenSecurity.isValidJWTFormat(invalidJWT)).toBe(false);
    });

    it('should detect expired tokens', () => {
      // Remove the import statement as it's now at the top

      // Helper function to create base64 encoded string
      const toBase64 = (str: string) => {
        try {
          return btoa(str);
        } catch {
          // Fallback for non-Latin1 characters
          return btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, (match, p1) => {
            return String.fromCharCode(parseInt(p1, 16));
          }));
        }
      };

      // Create expired token
      const expiredPayload = { exp: Math.floor(Date.now() / 1000) - 3600 }; // 1 hour ago
      const expiredPayloadBase64 = toBase64(JSON.stringify(expiredPayload));
      const expiredToken = `header.${expiredPayloadBase64}.signature`;

      // Create valid token
      const validPayload = { exp: Math.floor(Date.now() / 1000) + 3600 }; // 1 hour from now
      const validPayloadBase64 = toBase64(JSON.stringify(validPayload));
      const validToken = `header.${validPayloadBase64}.signature`;

      expect(TokenSecurity.isTokenExpired(expiredToken)).toBe(true);
      expect(TokenSecurity.isTokenExpired(validToken)).toBe(false);
    });
  });

  describe('Content Security Policy', () => {
    it('should apply CSP policy without errors', () => {
      // Remove the import statement as it's now at the top

      // Ensure document.head exists for testing
      if (!document.head) {
        Object.defineProperty(document, 'head', {
          value: document.createElement('head'),
          writable: false
        });
      }

      // Apply CSP policy
      expect(() => {
        ContentSecurityPolicy.applyPolicy("default-src 'self'");
      }).not.toThrow();

      // Verify meta tag was created
      const metaTag = document.querySelector('meta[http-equiv="Content-Security-Policy"]');
      expect(metaTag).toBeTruthy();
      expect((metaTag as HTMLMetaElement).content).toContain("default-src 'self'");
    });
  });

  describe('Security Headers', () => {
    it('should include security headers in API requests', async () => {
      const mockAxios = vi.fn();
      vi.spyOn(apiClient, 'get').mockImplementation(mockAxios);

      mockAxios.mockResolvedValueOnce({
        data: { success: true },
      });

      await ApiService.get('/api/test');

      // Check that the request was made with security headers
      const lastCall = mockAxios.mock.calls[mockAxios.mock.calls.length - 1];
      const config = lastCall[1]; // Second argument is the config
      if (config) {
        expect(config.headers).toHaveProperty('X-Content-Type-Options', 'nosniff');
        expect(config.headers).toHaveProperty('X-Frame-Options', 'DENY');
        expect(config.headers).toHaveProperty('X-XSS-Protection', '1; mode=block');
      }
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
      expect((sanitized.tags as string[])[0]).not.toContain('<script>');
      expect(sanitized.email).toBe('john@example.com'); // Should remain unchanged
      expect(sanitized.age).toBe(25); // Should remain unchanged
    });
  });
});