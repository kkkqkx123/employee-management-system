/**
 * Security configuration and constants
 */

export const SECURITY_CONFIG = {
  // Rate limiting configuration
  RATE_LIMITS: {
    API_REQUESTS: {
      maxRequests: 100,
      windowMs: 60000, // 1 minute
    },
    FILE_UPLOADS: {
      maxRequests: 5,
      windowMs: 60000, // 1 minute
    },
    LOGIN_ATTEMPTS: {
      maxRequests: 5,
      windowMs: 300000, // 5 minutes
    },
    PASSWORD_RESET: {
      maxRequests: 3,
      windowMs: 600000, // 10 minutes
    },
  },

  // File upload security
  FILE_UPLOAD: {
    MAX_SIZE: 10 * 1024 * 1024, // 10MB
    ALLOWED_TYPES: [
      'image/jpeg',
      'image/png',
      'image/gif',
      'image/webp',
      'application/pdf',
      'text/csv',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      'application/vnd.ms-excel',
    ],
    ALLOWED_EXTENSIONS: [
      '.jpg',
      '.jpeg',
      '.png',
      '.gif',
      '.webp',
      '.pdf',
      '.csv',
      '.xlsx',
      '.xls',
    ],
    BLOCKED_EXTENSIONS: [
      '.exe',
      '.bat',
      '.cmd',
      '.com',
      '.pif',
      '.scr',
      '.vbs',
      '.js',
      '.jar',
      '.php',
      '.asp',
      '.jsp',
      '.html',
      '.htm',
    ],
  },

  // Token security
  TOKEN: {
    REFRESH_THRESHOLD_MINUTES: 5,
    MAX_AGE_HOURS: 24,
    STORAGE_KEY: 'auth_token',
    REFRESH_STORAGE_KEY: 'refresh_token',
  },

  // CSRF protection
  CSRF: {
    TOKEN_LENGTH: 32,
    HEADER_NAME: 'X-CSRF-Token',
    STORAGE_KEY: 'csrf_token',
    PROTECTED_METHODS: ['POST', 'PUT', 'PATCH', 'DELETE'],
  },

  // Content Security Policy
  CSP: {
    REPORT_VIOLATIONS: true,
    STRICT_MODE: import.meta.env.PROD,
    ALLOWED_DOMAINS: [
      'https://fonts.googleapis.com',
      'https://fonts.gstatic.com',
      'https://cdn.jsdelivr.net',
    ],
  },

  // Input validation
  VALIDATION: {
    MAX_INPUT_LENGTH: 1000,
    MAX_TEXTAREA_LENGTH: 5000,
    MAX_FILENAME_LENGTH: 255,
    EMAIL_REGEX: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
    PHONE_REGEX: /^\+?[\d\s\-\(\)]+$/,
    XSS_PATTERNS: [
      /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi,
      /javascript:/gi,
      /on\w+\s*=/gi,
      /<iframe\b[^<]*(?:(?!<\/iframe>)<[^<]*)*<\/iframe>/gi,
      /<object\b[^<]*(?:(?!<\/object>)<[^<]*)*<\/object>/gi,
      /<embed\b[^<]*(?:(?!<\/embed>)<[^<]*)*<\/embed>/gi,
      /expression\s*\(/gi,
      /vbscript:/gi,
      /data:text\/html/gi,
    ],
  },

  // Security headers
  HEADERS: {
    CONTENT_TYPE_OPTIONS: 'nosniff',
    FRAME_OPTIONS: 'DENY',
    XSS_PROTECTION: '1; mode=block',
    REFERRER_POLICY: 'strict-origin-when-cross-origin',
    PERMISSIONS_POLICY: 'geolocation=(), microphone=(), camera=()',
  },

  // Monitoring and logging
  MONITORING: {
    LOG_SECURITY_EVENTS: true,
    TRACK_FAILED_ATTEMPTS: true,
    ALERT_THRESHOLD: 10, // Number of violations before alerting
    RETENTION_DAYS: 30,
  },
} as const;

/**
 * Security feature flags
 */
export const SECURITY_FEATURES = {
  ENABLE_CSRF_PROTECTION: true,
  ENABLE_RATE_LIMITING: true,
  ENABLE_INPUT_SANITIZATION: true,
  ENABLE_FILE_VALIDATION: true,
  ENABLE_CSP: true,
  ENABLE_SECURITY_HEADERS: true,
  ENABLE_TOKEN_VALIDATION: true,
  ENABLE_VIOLATION_REPORTING: true,
} as const;

/**
 * Environment-specific security settings
 */
export const getSecurityConfig = () => {
  const isDevelopment = import.meta.env.DEV;
  const isProduction = import.meta.env.PROD;

  return {
    ...SECURITY_CONFIG,
    
    // Adjust settings based on environment
    RATE_LIMITS: {
      ...SECURITY_CONFIG.RATE_LIMITS,
      // More lenient in development
      API_REQUESTS: {
        ...SECURITY_CONFIG.RATE_LIMITS.API_REQUESTS,
        maxRequests: isDevelopment ? 1000 : SECURITY_CONFIG.RATE_LIMITS.API_REQUESTS.maxRequests,
      },
    },

    CSP: {
      ...SECURITY_CONFIG.CSP,
      STRICT_MODE: isProduction,
      REPORT_VIOLATIONS: isProduction,
    },

    MONITORING: {
      ...SECURITY_CONFIG.MONITORING,
      LOG_SECURITY_EVENTS: true, // Always log in all environments
      TRACK_FAILED_ATTEMPTS: isProduction,
    },
  };
};

/**
 * Validate security configuration
 */
export const validateSecurityConfig = () => {
  const config = getSecurityConfig();
  const errors: string[] = [];

  // Validate rate limits
  Object.entries(config.RATE_LIMITS).forEach(([key, limit]) => {
    if (limit.maxRequests <= 0) {
      errors.push(`Invalid rate limit for ${key}: maxRequests must be positive`);
    }
    if (limit.windowMs <= 0) {
      errors.push(`Invalid rate limit for ${key}: windowMs must be positive`);
    }
  });

  // Validate file upload settings
  if (config.FILE_UPLOAD.MAX_SIZE <= 0) {
    errors.push('Invalid file upload max size: must be positive');
  }

  if (config.FILE_UPLOAD.ALLOWED_TYPES.length === 0) {
    errors.push('No allowed file types specified');
  }

  // Validate token settings
  if (config.TOKEN.REFRESH_THRESHOLD_MINUTES <= 0) {
    errors.push('Invalid token refresh threshold: must be positive');
  }

  if (config.TOKEN.MAX_AGE_HOURS <= 0) {
    errors.push('Invalid token max age: must be positive');
  }

  // Validate CSRF settings
  if (config.CSRF.TOKEN_LENGTH < 16) {
    errors.push('CSRF token length should be at least 16 characters');
  }

  if (errors.length > 0) {
    console.error('Security configuration validation failed:', errors);
    throw new Error(`Security configuration validation failed: ${errors.join(', ')}`);
  }

  return true;
};

// Validate configuration on module load
try {
  validateSecurityConfig();
  console.log('✅ Security configuration validated successfully');
} catch (error) {
  console.error('❌ Security configuration validation failed:', error);
}

export default SECURITY_CONFIG;