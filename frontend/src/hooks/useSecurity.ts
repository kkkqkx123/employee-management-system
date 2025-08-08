/**
 * React hooks for security features
 */

import { useCallback, useEffect, useState } from 'react';
import { SecurityUtils, RateLimiter } from '../utils/security';
import { CSRFProtection } from '../utils/csrfProtection';
import { TokenSecurity } from '../utils/tokenSecurity';

/**
 * Hook for input sanitization
 */
export const useSanitization = () => {
  const sanitizeHtml = useCallback((html: string) => {
    return SecurityUtils.sanitizeHtml(html);
  }, []);

  const sanitizeText = useCallback((text: string) => {
    return SecurityUtils.sanitizeText(text);
  }, []);

  const sanitizeUrl = useCallback((url: string) => {
    return SecurityUtils.sanitizeUrl(url);
  }, []);

  const sanitizeFormData = useCallback((data: Record<string, any>) => {
    return SecurityUtils.sanitizeFormData(data);
  }, []);

  const validateEmail = useCallback((email: string) => {
    return SecurityUtils.validateEmail(email);
  }, []);

  const containsXSS = useCallback((content: string) => {
    return SecurityUtils.containsXSS(content);
  }, []);

  return {
    sanitizeHtml,
    sanitizeText,
    sanitizeUrl,
    sanitizeFormData,
    validateEmail,
    containsXSS,
  };
};

/**
 * Hook for CSRF protection
 */
export const useCSRFProtection = () => {
  const [token, setToken] = useState<string | null>(null);

  useEffect(() => {
    setToken(CSRFProtection.getToken());
  }, []);

  const getToken = useCallback(() => {
    return CSRFProtection.getToken();
  }, []);

  const getHeaders = useCallback(() => {
    return CSRFProtection.getHeaders();
  }, []);

  const validateToken = useCallback((providedToken: string) => {
    return CSRFProtection.validateToken(providedToken);
  }, []);

  const regenerateToken = useCallback(() => {
    const newToken = CSRFProtection.regenerateToken();
    setToken(newToken);
    return newToken;
  }, []);

  return {
    token,
    getToken,
    getHeaders,
    validateToken,
    regenerateToken,
  };
};

/**
 * Hook for rate limiting
 */
export const useRateLimit = (key: string, maxRequests: number = 10, windowMs: number = 60000) => {
  const [isAllowed, setIsAllowed] = useState(true);
  const [remainingRequests, setRemainingRequests] = useState(maxRequests);

  const checkLimit = useCallback(() => {
    const allowed = RateLimiter.isAllowed(key, maxRequests, windowMs);
    const remaining = RateLimiter.getRemainingRequests(key, maxRequests, windowMs);
    
    setIsAllowed(allowed);
    setRemainingRequests(remaining);
    
    return allowed;
  }, [key, maxRequests, windowMs]);

  const resetLimit = useCallback(() => {
    RateLimiter.clearKey(key);
    setIsAllowed(true);
    setRemainingRequests(maxRequests);
  }, [key, maxRequests]);

  return {
    isAllowed,
    remainingRequests,
    checkLimit,
    resetLimit,
  };
};

/**
 * Hook for secure file validation
 */
export const useFileValidation = (options: {
  allowedTypes?: string[];
  maxSize?: number;
  allowedExtensions?: string[];
} = {}) => {
  const validateFile = useCallback((file: File) => {
    return SecurityUtils.validateFile(file, options);
  }, [options]);

  const validateFiles = useCallback((files: File[]) => {
    const results = files.map(file => ({
      file,
      ...validateFile(file),
    }));

    const validFiles = results.filter(result => result.isValid).map(result => result.file);
    const errors = results.filter(result => !result.isValid).map(result => result.error);

    return {
      validFiles,
      errors,
      isValid: errors.length === 0,
    };
  }, [validateFile]);

  return {
    validateFile,
    validateFiles,
  };
};

/**
 * Hook for token security
 */
export const useTokenSecurity = () => {
  const [isTokenValid, setIsTokenValid] = useState(false);
  const [tokenExpiration, setTokenExpiration] = useState<Date | null>(null);
  const [needsRefresh, setNeedsRefresh] = useState(false);

  const checkToken = useCallback((token: string) => {
    const isValid = TokenSecurity.isValidJWTFormat(token) && !TokenSecurity.isTokenExpired(token);
    const expiration = TokenSecurity.getTokenExpiration(token);
    const needsRefreshCheck = TokenSecurity.needsRefresh(token);

    setIsTokenValid(isValid);
    setTokenExpiration(expiration);
    setNeedsRefresh(needsRefreshCheck);

    return {
      isValid,
      expiration,
      needsRefresh: needsRefreshCheck,
    };
  }, []);

  return {
    isTokenValid,
    tokenExpiration,
    needsRefresh,
    checkToken,
    isValidJWTFormat: TokenSecurity.isValidJWTFormat,
    isTokenExpired: TokenSecurity.isTokenExpired,
    getTokenExpiration: TokenSecurity.getTokenExpiration,
    getTimeUntilExpiration: TokenSecurity.getTimeUntilExpiration,
  };
};

/**
 * Hook for security monitoring
 */
export const useSecurityMonitoring = () => {
  const [violations, setViolations] = useState<any[]>([]);
  const [suspiciousActivity, setSuspiciousActivity] = useState<any[]>([]);

  useEffect(() => {
    const handleCSPViolation = (event: SecurityPolicyViolationEvent) => {
      const violation = {
        id: Date.now(),
        type: 'csp_violation',
        blockedURI: event.blockedURI,
        violatedDirective: event.violatedDirective,
        timestamp: new Date().toISOString(),
      };

      setViolations(prev => [...prev, violation]);
    };

    document.addEventListener('securitypolicyviolation', handleCSPViolation);

    return () => {
      document.removeEventListener('securitypolicyviolation', handleCSPViolation);
    };
  }, []);

  const reportSuspiciousActivity = useCallback((activity: {
    type: string;
    description: string;
    metadata?: any;
  }) => {
    const report = {
      id: Date.now(),
      ...activity,
      timestamp: new Date().toISOString(),
    };

    setSuspiciousActivity(prev => [...prev, report]);

    // In production, send to monitoring service
    if (import.meta.env.PROD) {
      console.warn('🚨 Suspicious Activity:', report);
    }
  }, []);

  const clearViolations = useCallback(() => {
    setViolations([]);
  }, []);

  const clearSuspiciousActivity = useCallback(() => {
    setSuspiciousActivity([]);
  }, []);

  return {
    violations,
    suspiciousActivity,
    reportSuspiciousActivity,
    clearViolations,
    clearSuspiciousActivity,
  };
};

export default {
  useSanitization,
  useCSRFProtection,
  useRateLimit,
  useFileValidation,
  useTokenSecurity,
  useSecurityMonitoring,
};