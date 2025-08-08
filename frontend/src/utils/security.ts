/**
 * Security utilities for XSS prevention, input sanitization, and secure operations
 */

import DOMPurify from 'dompurify';

// XSS Prevention and Input Sanitization
export class SecurityUtils {
  /**
   * Sanitize HTML content to prevent XSS attacks
   */
  static sanitizeHtml(dirty: string): string {
    return DOMPurify.sanitize(dirty, {
      ALLOWED_TAGS: ['b', 'i', 'em', 'strong', 'a', 'p', 'br', 'ul', 'ol', 'li'],
      ALLOWED_ATTR: ['href', 'target'],
      ALLOW_DATA_ATTR: false,
    });
  }

  /**
   * Sanitize text input to prevent script injection
   */
  static sanitizeText(input: string): string {
    return input
      .replace(/[<>]/g, '') // Remove angle brackets
      .replace(/javascript:/gi, '') // Remove javascript: protocol
      .replace(/on\w+=/gi, '') // Remove event handlers
      .trim();
  }

  /**
   * Validate and sanitize URL to prevent malicious redirects
   */
  static sanitizeUrl(url: string): string {
    // Allow only http, https, and relative URLs
    const allowedProtocols = /^(https?:\/\/|\/)/;
    
    if (!allowedProtocols.test(url)) {
      return '#';
    }

    // Remove javascript: and data: protocols
    const cleanUrl = url.replace(/javascript:|data:/gi, '');
    
    return encodeURI(cleanUrl);
  }

  /**
   * Escape HTML entities in user input
   */
  static escapeHtml(text: string): string {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }

  /**
   * Validate file type and size for secure uploads
   */
  static validateFile(file: File, options: {
    allowedTypes?: string[];
    maxSize?: number; // in bytes
    allowedExtensions?: string[];
  } = {}): { isValid: boolean; error?: string } {
    const {
      allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'application/pdf', 'text/csv', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'],
      maxSize = 10 * 1024 * 1024, // 10MB default
      allowedExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.pdf', '.csv', '.xlsx']
    } = options;

    // Check file size
    if (file.size > maxSize) {
      return {
        isValid: false,
        error: `File size exceeds maximum allowed size of ${Math.round(maxSize / (1024 * 1024))}MB`
      };
    }

    // Check file type
    if (!allowedTypes.includes(file.type)) {
      return {
        isValid: false,
        error: `File type ${file.type} is not allowed`
      };
    }

    // Check file extension
    const fileExtension = '.' + file.name.split('.').pop()?.toLowerCase();
    if (!allowedExtensions.includes(fileExtension)) {
      return {
        isValid: false,
        error: `File extension ${fileExtension} is not allowed`
      };
    }

    // Check for suspicious file names
    const suspiciousPatterns = [
      /\.php$/i,
      /\.jsp$/i,
      /\.asp$/i,
      /\.exe$/i,
      /\.bat$/i,
      /\.cmd$/i,
      /\.scr$/i,
      /\.vbs$/i,
      /\.js$/i,
      /\.html$/i,
      /\.htm$/i
    ];

    if (suspiciousPatterns.some(pattern => pattern.test(file.name))) {
      return {
        isValid: false,
        error: 'File type is not allowed for security reasons'
      };
    }

    return { isValid: true };
  }

  /**
   * Generate a secure random string for CSRF tokens
   */
  static generateSecureToken(length: number = 32): string {
    const array = new Uint8Array(length);
    crypto.getRandomValues(array);
    return Array.from(array, byte => byte.toString(16).padStart(2, '0')).join('');
  }

  /**
   * Validate email format to prevent injection
   */
  static validateEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email) && !email.includes('<') && !email.includes('>');
  }

  /**
   * Sanitize form data before submission
   */
  static sanitizeFormData(data: Record<string, unknown>): Record<string, unknown> {
    const sanitized: Record<string, unknown> = {};

    for (const [key, value] of Object.entries(data)) {
      if (typeof value === 'string') {
        sanitized[key] = this.sanitizeText(value);
      } else if (Array.isArray(value)) {
        sanitized[key] = value.map(item =>
          typeof item === 'string' ? this.sanitizeText(item) : item
        );
      } else {
        sanitized[key] = value;
      }
    }

    return sanitized;
  }

  /**
   * Check if content contains potential XSS patterns
   */
  static containsXSS(content: string): boolean {
    const xssPatterns = [
      /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi,
      /javascript:/gi,
      /on\w+\s*=/gi,
      /<iframe\b[^<]*(?:(?!<\/iframe>)<[^<]*)*<\/iframe>/gi,
      /<object\b[^<]*(?:(?!<\/object>)<[^<]*)*<\/object>/gi,
      /<embed\b[^<]*(?:(?!<\/embed>)<[^<]*)*<\/embed>/gi,
      /expression\s*\(/gi,
      /vbscript:/gi,
      /data:text\/html/gi
    ];

    return xssPatterns.some(pattern => pattern.test(content));
  }
}

// Rate Limiting Utility
export class RateLimiter {
  private static requests: Map<string, number[]> = new Map();

  /**
   * Check if request is within rate limit
   */
  static isAllowed(key: string, maxRequests: number = 10, windowMs: number = 60000): boolean {
    const now = Date.now();
    const windowStart = now - windowMs;

    // Get existing requests for this key
    const requests = this.requests.get(key) || [];

    // Filter out old requests
    const recentRequests = requests.filter(timestamp => timestamp > windowStart);

    // Check if within limit
    if (recentRequests.length >= maxRequests) {
      return false;
    }

    // Add current request
    recentRequests.push(now);
    this.requests.set(key, recentRequests);

    return true;
  }

  /**
   * Get remaining requests for a key
   */
  static getRemainingRequests(key: string, maxRequests: number = 10, windowMs: number = 60000): number {
    const now = Date.now();
    const windowStart = now - windowMs;

    const requests = this.requests.get(key) || [];
    const recentRequests = requests.filter(timestamp => timestamp > windowStart);

    return Math.max(0, maxRequests - recentRequests.length);
  }

  /**
   * Clear rate limit data for a key
   */
  static clearKey(key: string): void {
    this.requests.delete(key);
  }

  /**
   * Clear all rate limit data
   */
  static clearAll(): void {
    this.requests.clear();
  }
}

export default SecurityUtils;