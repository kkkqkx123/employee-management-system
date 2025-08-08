/**
 * Security module exports
 */

import { SecurityUtils, RateLimiter } from '../utils/security';
import { CSRFProtection, csrfFetch, useCSRFProtection } from '../utils/csrfProtection';
import {
  TokenSecurity,
  SecureCookieTokenStorage,
  MemoryTokenStorage,
  EncryptedLocalStorageTokenStorage,
  createTokenStorage
} from '../utils/tokenSecurity';
import { ContentSecurityPolicy } from '../utils/contentSecurityPolicy';
import { SECURITY_CONFIG, SECURITY_FEATURES, getSecurityConfig } from '../config/security';

// Core security utilities
export { SecurityUtils, RateLimiter };
export { CSRFProtection, csrfFetch, useCSRFProtection };
export {
  TokenSecurity,
  SecureCookieTokenStorage,
  MemoryTokenStorage,
  EncryptedLocalStorageTokenStorage,
  createTokenStorage
};
export { ContentSecurityPolicy };

// Security configuration
export { SECURITY_CONFIG, SECURITY_FEATURES, getSecurityConfig };

// Security hooks
export {
  useSanitization,
  useCSRFProtection as useCSRF,
  useRateLimit,
  useFileValidation,
  useTokenSecurity,
  useSecurityMonitoring,
} from '../hooks/useSecurity';

// Security components
export { SecureFileUpload } from '../components/ui/SecureFileUpload';

// Types
export type { TokenStorage } from '../utils/tokenSecurity';
export type { CSPDirectives } from '../utils/contentSecurityPolicy';
export type { SecureFileUploadProps } from '../components/ui/SecureFileUpload';

// Security initialization
export const initializeSecurity = () => {
  // Initialize CSRF protection
  CSRFProtection.initialize();
  
  // Apply Content Security Policy
  ContentSecurityPolicy.applyPolicy();
  
  // Setup violation reporting
  ContentSecurityPolicy.setupViolationReporting();
  
  console.log('🔒 Security features initialized');
};

// Security validation
export const validateSecuritySetup = () => {
  const checks = {
    csrfToken: !!CSRFProtection.getToken(),
    cspSupport: ContentSecurityPolicy.isSupported(),
    secureStorage: typeof crypto !== 'undefined' && typeof crypto.getRandomValues === 'function',
  };

  const allPassed = Object.values(checks).every(Boolean);
  
  if (allPassed) {
    console.log('✅ Security validation passed', checks);
  } else {
    console.warn('⚠️ Security validation issues detected', checks);
  }

  return { allPassed, checks };
};

export default {
  SecurityUtils,
  RateLimiter,
  CSRFProtection,
  TokenSecurity,
  ContentSecurityPolicy,
  SECURITY_CONFIG,
  initializeSecurity,
  validateSecuritySetup,
};