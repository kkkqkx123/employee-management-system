/**
 * Content Security Policy utilities and configuration
 */

export interface CSPDirectives {
  'default-src'?: string[];
  'script-src'?: string[];
  'style-src'?: string[];
  'img-src'?: string[];
  'font-src'?: string[];
  'connect-src'?: string[];
  'media-src'?: string[];
  'object-src'?: string[];
  'frame-src'?: string[];
  'worker-src'?: string[];
  'child-src'?: string[];
  'form-action'?: string[];
  'frame-ancestors'?: string[];
  'base-uri'?: string[];
  'manifest-src'?: string[];
}

export class ContentSecurityPolicy {
  private static readonly DEFAULT_POLICY: CSPDirectives = {
    'default-src': ["'self'"],
    'script-src': [
      "'self'",
      "'unsafe-inline'", // Required for Vite in development
      "'unsafe-eval'", // Required for development tools
      'https://cdn.jsdelivr.net', // For external libraries
    ],
    'style-src': [
      "'self'",
      "'unsafe-inline'", // Required for CSS-in-JS and dynamic styles
      'https://fonts.googleapis.com',
    ],
    'img-src': [
      "'self'",
      'data:', // For base64 images
      'blob:', // For generated images
      'https:', // Allow HTTPS images
    ],
    'font-src': [
      "'self'",
      'https://fonts.gstatic.com',
      'data:', // For base64 fonts
    ],
    'connect-src': [
      "'self'",
      'ws:', // WebSocket connections
      'wss:', // Secure WebSocket connections
      'https://api.example.com', // Your API endpoints
    ],
    'media-src': ["'self'", 'blob:', 'data:'],
    'object-src': ["'none'"], // Disable plugins
    'frame-src': ["'none'"], // Disable frames by default
    'worker-src': ["'self'", 'blob:'],
    'child-src': ["'self'"],
    'form-action': ["'self'"],
    'frame-ancestors': ["'none'"], // Prevent clickjacking
    'base-uri': ["'self'"],
    'manifest-src': ["'self'"],
  };

  /**
   * Generate CSP header value from directives
   */
  static generatePolicy(directives: CSPDirectives = this.DEFAULT_POLICY): string {
    return Object.entries(directives)
      .map(([directive, sources]) => `${directive} ${sources.join(' ')}`)
      .join('; ');
  }

  /**
   * Get production-ready CSP policy
   */
  static getProductionPolicy(): string {
    const productionDirectives: CSPDirectives = {
      ...this.DEFAULT_POLICY,
      'script-src': [
        "'self'",
        // Remove unsafe-inline and unsafe-eval for production
        'https://cdn.jsdelivr.net',
      ],
      'style-src': [
        "'self'",
        "'unsafe-inline'", // Still needed for CSS-in-JS
        'https://fonts.googleapis.com',
      ],
    };

    return this.generatePolicy(productionDirectives);
  }

  /**
   * Get development CSP policy (more permissive)
   */
  static getDevelopmentPolicy(): string {
    const devDirectives: CSPDirectives = {
      ...this.DEFAULT_POLICY,
      'script-src': [
        "'self'",
        "'unsafe-inline'",
        "'unsafe-eval'",
        'localhost:*',
        '127.0.0.1:*',
        'https://cdn.jsdelivr.net',
      ],
      'connect-src': [
        "'self'",
        'ws:',
        'wss:',
        'localhost:*',
        '127.0.0.1:*',
        'https://api.example.com',
      ],
    };

    return this.generatePolicy(devDirectives);
  }

  /**
   * Apply CSP policy to the document
   */
  static applyPolicy(policy?: string): void {
    const cspPolicy = policy || (import.meta.env.PROD 
      ? this.getProductionPolicy() 
      : this.getDevelopmentPolicy());

    // Create or update CSP meta tag
    let metaTag = document.querySelector('meta[http-equiv="Content-Security-Policy"]') as HTMLMetaElement;
    
    if (!metaTag) {
      metaTag = document.createElement('meta');
      metaTag.httpEquiv = 'Content-Security-Policy';
      document.head.appendChild(metaTag);
    }

    metaTag.content = cspPolicy;

    // Log CSP policy in development
    if (import.meta.env.DEV) {
      console.log('🔒 CSP Policy Applied:', cspPolicy);
    }
  }

  /**
   * Report CSP violations (for monitoring)
   */
  static setupViolationReporting(): void {
    document.addEventListener('securitypolicyviolation', (event) => {
      const violation = {
        blockedURI: event.blockedURI,
        violatedDirective: event.violatedDirective,
        originalPolicy: event.originalPolicy,
        sourceFile: event.sourceFile,
        lineNumber: event.lineNumber,
        columnNumber: event.columnNumber,
        timestamp: new Date().toISOString(),
      };

      // Log violation in development
      if (import.meta.env.DEV) {
        console.warn('🚨 CSP Violation:', violation);
      }

      // In production, you might want to send this to your monitoring service
      if (import.meta.env.PROD) {
        // Example: Send to monitoring service
        // fetch('/api/csp-violations', {
        //   method: 'POST',
        //   headers: { 'Content-Type': 'application/json' },
        //   body: JSON.stringify(violation),
        // });
      }
    });
  }

  /**
   * Check if current environment supports CSP
   */
  static isSupported(): boolean {
    return 'SecurityPolicyViolationEvent' in window;
  }

  /**
   * Validate that a URL is allowed by CSP
   */
  static isUrlAllowed(url: string, directive: keyof CSPDirectives): boolean {
    try {
      const urlObj = new URL(url);
      const policy = import.meta.env.PROD 
        ? this.getProductionPolicy() 
        : this.getDevelopmentPolicy();

      // This is a simplified check - in practice, you'd need a full CSP parser
      return policy.includes(urlObj.origin) || 
             policy.includes("'self'") || 
             policy.includes('https:');
    } catch {
      return false;
    }
  }

  /**
   * Generate nonce for inline scripts/styles
   */
  static generateNonce(): string {
    const array = new Uint8Array(16);
    crypto.getRandomValues(array);
    return btoa(String.fromCharCode(...array));
  }

  /**
   * Add nonce to script/style elements
   */
  static addNonceToElement(element: HTMLScriptElement | HTMLStyleElement, nonce: string): void {
    element.nonce = nonce;
  }
}

// Initialize CSP when module loads
if (typeof window !== 'undefined') {
  // Apply CSP policy
  ContentSecurityPolicy.applyPolicy();
  
  // Setup violation reporting
  ContentSecurityPolicy.setupViolationReporting();
  
  // Log CSP support status
  if (import.meta.env.DEV) {
    console.log('🔒 CSP Support:', ContentSecurityPolicy.isSupported() ? 'Enabled' : 'Not supported');
  }
}

export default ContentSecurityPolicy;