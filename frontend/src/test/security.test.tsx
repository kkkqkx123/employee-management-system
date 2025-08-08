/**
 * Security tests for XSS prevention, CSRF protection, and secure operations
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { SecurityUtils, RateLimiter } from '../utils/security';
import { CSRFProtection } from '../utils/csrfProtection';
import { TokenSecurity, SecureCookieTokenStorage, MemoryTokenStorage } from '../utils/tokenSecurity';
import { SecureFileUpload } from '../components/ui/SecureFileUpload';

describe('Security Utils', () => {
  describe('XSS Prevention', () => {
    it('should sanitize HTML content', () => {
      const maliciousHtml = '<script>alert("xss")</script><p>Safe content</p>';
      const sanitized = SecurityUtils.sanitizeHtml(maliciousHtml);
      
      expect(sanitized).not.toContain('<script>');
      expect(sanitized).toContain('<p>Safe content</p>');
    });

    it('should sanitize text input', () => {
      const maliciousText = '<script>alert("xss")</script>Normal text';
      const sanitized = SecurityUtils.sanitizeText(maliciousText);
      
      expect(sanitized).not.toContain('<script>');
      expect(sanitized).toBe('Normal text');
    });

    it('should detect XSS patterns', () => {
      const xssContent = '<script>alert("xss")</script>';
      const safeContent = 'This is safe content';
      
      expect(SecurityUtils.containsXSS(xssContent)).toBe(true);
      expect(SecurityUtils.containsXSS(safeContent)).toBe(false);
    });

    it('should sanitize URLs', () => {
      const maliciousUrl = 'javascript:alert("xss")';
      const safeUrl = 'https://example.com';
      const relativeUrl = '/safe/path';
      
      expect(SecurityUtils.sanitizeUrl(maliciousUrl)).toBe('#');
      expect(SecurityUtils.sanitizeUrl(safeUrl)).toContain('https://example.com');
      expect(SecurityUtils.sanitizeUrl(relativeUrl)).toBe('/safe/path');
    });

    it('should escape HTML entities', () => {
      const htmlContent = '<div>Test & "quotes"</div>';
      const escaped = SecurityUtils.escapeHtml(htmlContent);
      
      expect(escaped).toContain('&lt;div&gt;');
      expect(escaped).toContain('&amp;');
      expect(escaped).toContain('&quot;');
    });
  });

  describe('Form Data Sanitization', () => {
    it('should sanitize form data', () => {
      const formData = {
        name: '<script>alert("xss")</script>John',
        email: 'john@example.com',
        description: 'Normal description<script>alert("xss")</script>',
        tags: ['<script>tag1</script>', 'tag2'],
        age: 25
      };

      const sanitized = SecurityUtils.sanitizeFormData(formData);

      expect(sanitized.name).not.toContain('<script>');
      expect(sanitized.name).toBe('John');
      expect(sanitized.email).toBe('john@example.com');
      expect(sanitized.description).not.toContain('<script>');
      expect(sanitized.tags[0]).not.toContain('<script>');
      expect(sanitized.age).toBe(25);
    });
  });

  describe('Email Validation', () => {
    it('should validate email format', () => {
      expect(SecurityUtils.validateEmail('test@example.com')).toBe(true);
      expect(SecurityUtils.validateEmail('invalid-email')).toBe(false);
      expect(SecurityUtils.validateEmail('test<script>@example.com')).toBe(false);
      expect(SecurityUtils.validateEmail('test@example.com<script>')).toBe(false);
    });
  });

  describe('File Validation', () => {
    it('should validate file types and sizes', () => {
      const validFile = new File(['content'], 'test.jpg', { type: 'image/jpeg' });
      const invalidFile = new File(['content'], 'test.exe', { type: 'application/exe' });
      const largeFile = new File([new ArrayBuffer(20 * 1024 * 1024)], 'large.jpg', { type: 'image/jpeg' });

      const validResult = SecurityUtils.validateFile(validFile);
      const invalidResult = SecurityUtils.validateFile(invalidFile);
      const largeResult = SecurityUtils.validateFile(largeFile);

      expect(validResult.isValid).toBe(true);
      expect(invalidResult.isValid).toBe(false);
      expect(largeResult.isValid).toBe(false);
      expect(largeResult.error).toContain('size exceeds');
    });

    it('should detect suspicious file names', () => {
      const suspiciousFile = new File(['content'], 'malware.php', { type: 'text/plain' });
      const result = SecurityUtils.validateFile(suspiciousFile);

      expect(result.isValid).toBe(false);
      expect(result.error).toContain('security reasons');
    });
  });
});

describe('CSRF Protection', () => {
  beforeEach(() => {
    CSRFProtection.clearToken();
    // Mock sessionStorage
    Object.defineProperty(window, 'sessionStorage', {
      value: {
        getItem: vi.fn(),
        setItem: vi.fn(),
        removeItem: vi.fn(),
      },
      writable: true,
    });
  });

  it('should generate and manage CSRF tokens', () => {
    CSRFProtection.initialize();
    const token = CSRFProtection.getToken();

    expect(token).toBeTruthy();
    expect(typeof token).toBe('string');
    expect(token.length).toBeGreaterThan(0);
  });

  it('should provide CSRF headers', () => {
    CSRFProtection.initialize();
    const headers = CSRFProtection.getHeaders();

    expect(headers).toHaveProperty('X-CSRF-Token');
    expect(headers['X-CSRF-Token']).toBeTruthy();
  });

  it('should validate CSRF tokens', () => {
    CSRFProtection.initialize();
    const token = CSRFProtection.getToken();

    expect(CSRFProtection.validateToken(token!)).toBe(true);
    expect(CSRFProtection.validateToken('invalid-token')).toBe(false);
  });

  it('should identify methods requiring protection', () => {
    expect(CSRFProtection.requiresProtection('POST')).toBe(true);
    expect(CSRFProtection.requiresProtection('PUT')).toBe(true);
    expect(CSRFProtection.requiresProtection('DELETE')).toBe(true);
    expect(CSRFProtection.requiresProtection('GET')).toBe(false);
  });

  it('should validate request origins', () => {
    // Mock window.location
    Object.defineProperty(window, 'location', {
      value: { origin: 'https://example.com' },
      writable: true,
    });

    // Mock document.referrer
    Object.defineProperty(document, 'referrer', {
      value: 'https://example.com/page',
      writable: true,
    });

    expect(CSRFProtection.validateOrigin()).toBe(true);

    // Test with different origin
    Object.defineProperty(document, 'referrer', {
      value: 'https://malicious.com/page',
      writable: true,
    });

    expect(CSRFProtection.validateOrigin()).toBe(false);
  });
});

describe('Rate Limiting', () => {
  beforeEach(() => {
    RateLimiter.clearAll();
  });

  it('should allow requests within limit', () => {
    const key = 'test-key';
    
    // Should allow first request
    expect(RateLimiter.isAllowed(key, 5, 60000)).toBe(true);
    
    // Should allow subsequent requests within limit
    for (let i = 0; i < 4; i++) {
      expect(RateLimiter.isAllowed(key, 5, 60000)).toBe(true);
    }
  });

  it('should block requests exceeding limit', () => {
    const key = 'test-key';
    
    // Use up the limit
    for (let i = 0; i < 5; i++) {
      RateLimiter.isAllowed(key, 5, 60000);
    }
    
    // Next request should be blocked
    expect(RateLimiter.isAllowed(key, 5, 60000)).toBe(false);
  });

  it('should track remaining requests', () => {
    const key = 'test-key';
    
    expect(RateLimiter.getRemainingRequests(key, 5, 60000)).toBe(5);
    
    RateLimiter.isAllowed(key, 5, 60000);
    expect(RateLimiter.getRemainingRequests(key, 5, 60000)).toBe(4);
  });

  it('should reset after time window', async () => {
    const key = 'test-key';
    const windowMs = 100; // 100ms window for testing
    
    // Use up the limit
    for (let i = 0; i < 5; i++) {
      RateLimiter.isAllowed(key, 5, windowMs);
    }
    
    expect(RateLimiter.isAllowed(key, 5, windowMs)).toBe(false);
    
    // Wait for window to expire
    await new Promise(resolve => setTimeout(resolve, windowMs + 10));
    
    // Should allow requests again
    expect(RateLimiter.isAllowed(key, 5, windowMs)).toBe(true);
  });
});

describe('Token Security', () => {
  describe('JWT Validation', () => {
    it('should validate JWT format', () => {
      const validJWT = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c';
      const invalidJWT = 'invalid.jwt.token';
      
      expect(TokenSecurity.isValidJWTFormat(validJWT)).toBe(true);
      expect(TokenSecurity.isValidJWTFormat(invalidJWT)).toBe(false);
    });

    it('should check token expiration', () => {
      // Create a token that expires in the future
      const futureExp = Math.floor(Date.now() / 1000) + 3600; // 1 hour from now
      const futureToken = `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.${btoa(JSON.stringify({ exp: futureExp }))}.signature`;
      
      // Create a token that's already expired
      const pastExp = Math.floor(Date.now() / 1000) - 3600; // 1 hour ago
      const pastToken = `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.${btoa(JSON.stringify({ exp: pastExp }))}.signature`;
      
      expect(TokenSecurity.isTokenExpired(futureToken)).toBe(false);
      expect(TokenSecurity.isTokenExpired(pastToken)).toBe(true);
    });

    it('should determine if token needs refresh', () => {
      // Token expires in 2 minutes
      const soonExp = Math.floor(Date.now() / 1000) + 120;
      const soonToken = `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.${btoa(JSON.stringify({ exp: soonExp }))}.signature`;
      
      // Token expires in 10 minutes
      const laterExp = Math.floor(Date.now() / 1000) + 600;
      const laterToken = `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.${btoa(JSON.stringify({ exp: laterExp }))}.signature`;
      
      expect(TokenSecurity.needsRefresh(soonToken, 5)).toBe(true);
      expect(TokenSecurity.needsRefresh(laterToken, 5)).toBe(false);
    });
  });

  describe('Token Storage', () => {
    it('should store and retrieve tokens in memory', () => {
      const storage = new MemoryTokenStorage();
      const token = 'test-token';
      
      storage.setToken(token);
      expect(storage.getToken()).toBe(token);
      
      storage.removeToken();
      expect(storage.getToken()).toBeNull();
    });
  });
});

describe('Secure File Upload', () => {
  const mockOnFileSelect = vi.fn();
  const mockOnUploadError = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render file upload component', () => {
    render(
      <SecureFileUpload
        onFileSelect={mockOnFileSelect}
        onUploadError={mockOnUploadError}
      />
    );

    expect(screen.getByText(/click to select files/i)).toBeInTheDocument();
  });

  it('should validate file types', async () => {
    render(
      <SecureFileUpload
        onFileSelect={mockOnFileSelect}
        onUploadError={mockOnUploadError}
        allowedTypes={['image/jpeg']}
      />
    );

    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    
    // Create invalid file
    const invalidFile = new File(['content'], 'test.txt', { type: 'text/plain' });
    
    Object.defineProperty(fileInput, 'files', {
      value: [invalidFile],
      writable: false,
    });

    fireEvent.change(fileInput);

    await waitFor(() => {
      expect(mockOnUploadError).toHaveBeenCalledWith(
        expect.stringContaining('File type text/plain is not allowed')
      );
    });
  });

  it('should validate file size', async () => {
    render(
      <SecureFileUpload
        onFileSelect={mockOnFileSelect}
        onUploadError={mockOnUploadError}
        maxSize={1024} // 1KB limit
      />
    );

    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    
    // Create large file
    const largeFile = new File([new ArrayBuffer(2048)], 'large.jpg', { type: 'image/jpeg' });
    
    Object.defineProperty(fileInput, 'files', {
      value: [largeFile],
      writable: false,
    });

    fireEvent.change(fileInput);

    await waitFor(() => {
      expect(mockOnUploadError).toHaveBeenCalledWith(
        expect.stringContaining('File size exceeds maximum')
      );
    });
  });

  it('should handle drag and drop', () => {
    render(
      <SecureFileUpload
        onFileSelect={mockOnFileSelect}
        onUploadError={mockOnUploadError}
      />
    );

    const dropZone = screen.getByRole('button');
    
    fireEvent.dragOver(dropZone);
    expect(dropZone).toHaveClass('dragging');
    
    fireEvent.dragLeave(dropZone);
    expect(dropZone).not.toHaveClass('dragging');
  });
});