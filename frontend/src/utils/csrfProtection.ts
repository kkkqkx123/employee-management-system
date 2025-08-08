/**
 * CSRF Protection utilities
 */

import { SecurityUtils } from './security';

export class CSRFProtection {
  private static readonly CSRF_TOKEN_KEY = 'csrf_token';
  private static readonly CSRF_HEADER_NAME = 'X-CSRF-Token';
  private static csrfToken: string | null = null;

  /**
   * Initialize CSRF protection by generating or retrieving token
   */
  static initialize(): void {
    // Try to get existing token from meta tag (set by server)
    const metaToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');
    
    if (metaToken) {
      this.csrfToken = metaToken;
    } else {
      // Generate client-side token if not provided by server
      this.csrfToken = SecurityUtils.generateSecureToken();
      
      // Store in sessionStorage for consistency across requests
      sessionStorage.setItem(this.CSRF_TOKEN_KEY, this.csrfToken);
    }
  }

  /**
   * Get current CSRF token
   */
  static getToken(): string | null {
    if (!this.csrfToken) {
      // Try to retrieve from sessionStorage
      this.csrfToken = sessionStorage.getItem(this.CSRF_TOKEN_KEY);
      
      if (!this.csrfToken) {
        this.initialize();
      }
    }
    
    return this.csrfToken;
  }

  /**
   * Get CSRF headers for API requests
   */
  static getHeaders(): Record<string, string> {
    const token = this.getToken();
    return token ? { [this.CSRF_HEADER_NAME]: token } : {};
  }

  /**
   * Validate CSRF token for state-changing operations
   */
  static validateToken(providedToken: string): boolean {
    const currentToken = this.getToken();
    return currentToken !== null && providedToken === currentToken;
  }

  /**
   * Generate new CSRF token
   */
  static regenerateToken(): string {
    this.csrfToken = SecurityUtils.generateSecureToken();
    sessionStorage.setItem(this.CSRF_TOKEN_KEY, this.csrfToken);
    return this.csrfToken;
  }

  /**
   * Clear CSRF token
   */
  static clearToken(): void {
    this.csrfToken = null;
    sessionStorage.removeItem(this.CSRF_TOKEN_KEY);
  }

  /**
   * Check if request method requires CSRF protection
   */
  static requiresProtection(method: string): boolean {
    const protectedMethods = ['POST', 'PUT', 'PATCH', 'DELETE'];
    return protectedMethods.includes(method.toUpperCase());
  }

  /**
   * Add CSRF protection to form data
   */
  static addToFormData(formData: FormData): FormData {
    const token = this.getToken();
    if (token) {
      formData.append('_token', token);
    }
    return formData;
  }

  /**
   * Create hidden CSRF input for forms
   */
  static createHiddenInput(): HTMLInputElement {
    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = '_token';
    input.value = this.getToken() || '';
    return input;
  }

  /**
   * Validate origin for additional CSRF protection
   */
  static validateOrigin(allowedOrigins: string[] = []): boolean {
    const currentOrigin = window.location.origin;
    const referrer = document.referrer;

    // If no referrer, assume same-origin (direct navigation)
    if (!referrer) {
      return true;
    }

    try {
      const referrerOrigin = new URL(referrer).origin;
      
      // Check if referrer matches current origin
      if (referrerOrigin === currentOrigin) {
        return true;
      }

      // Check against allowed origins
      return allowedOrigins.includes(referrerOrigin);
    } catch {
      // Invalid referrer URL
      return false;
    }
  }
}

/**
 * CSRF-protected fetch wrapper
 */
export const csrfFetch = async (url: string, options: RequestInit = {}): Promise<Response> => {
  const method = options.method || 'GET';
  
  // Add CSRF headers for protected methods
  if (CSRFProtection.requiresProtection(method)) {
    const csrfHeaders = CSRFProtection.getHeaders();
    options.headers = {
      ...options.headers,
      ...csrfHeaders,
    };

    // Validate origin for additional security
    if (!CSRFProtection.validateOrigin()) {
      throw new Error('Invalid request origin');
    }
  }

  return fetch(url, options);
};

/**
 * React hook for CSRF protection
 */
export const useCSRFProtection = () => {
  const getToken = () => CSRFProtection.getToken();
  const getHeaders = () => CSRFProtection.getHeaders();
  const validateToken = (token: string) => CSRFProtection.validateToken(token);
  const regenerateToken = () => CSRFProtection.regenerateToken();

  return {
    getToken,
    getHeaders,
    validateToken,
    regenerateToken,
  };
};

// Initialize CSRF protection when module loads
CSRFProtection.initialize();

export default CSRFProtection;