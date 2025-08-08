# Security Implementation Guide

This document outlines the comprehensive security measures implemented in the React frontend application as part of Task 19.

## Overview

The security implementation includes multiple layers of protection against common web vulnerabilities and attacks:

- **XSS Prevention**: Input sanitization and output encoding
- **CSRF Protection**: Token-based protection for state-changing operations
- **Secure File Upload**: Validation and security checks for file uploads
- **Rate Limiting**: Client-side rate limiting to prevent abuse
- **Token Security**: Secure JWT token management and storage
- **Content Security Policy**: CSP headers to prevent code injection
- **Security Headers**: Additional security headers for protection

## Features Implemented

### 1. Input Sanitization and XSS Prevention

**Location**: `src/utils/security.ts`

- **HTML Sanitization**: Uses DOMPurify to sanitize HTML content
- **Text Sanitization**: Removes dangerous characters and patterns
- **URL Sanitization**: Validates and cleans URLs to prevent malicious redirects
- **XSS Detection**: Identifies potential XSS patterns in content

```typescript
import { SecurityUtils } from './utils/security';

// Sanitize HTML content
const safeHtml = SecurityUtils.sanitizeHtml(userInput);

// Sanitize text input
const safeText = SecurityUtils.sanitizeText(userInput);

// Check for XSS patterns
const containsXSS = SecurityUtils.containsXSS(content);
```

### 2. CSRF Protection

**Location**: `src/utils/csrfProtection.ts`

- **Token Generation**: Secure random token generation
- **Automatic Headers**: Adds CSRF tokens to state-changing requests
- **Origin Validation**: Validates request origins for additional security
- **React Hook**: Easy integration with React components

```typescript
import { useCSRFProtection } from './utils/csrfProtection';

const { getToken, getHeaders, validateToken } = useCSRFProtection();
```

### 3. Secure File Upload

**Location**: `src/components/ui/SecureFileUpload/`

- **File Type Validation**: Restricts allowed file types and extensions
- **Size Validation**: Enforces maximum file size limits
- **Security Scanning**: Detects suspicious file names and patterns
- **Rate Limiting**: Prevents upload abuse
- **Progress Tracking**: Secure upload with progress indication

```typescript
import { SecureFileUpload } from './components/ui/SecureFileUpload';

<SecureFileUpload
  onFileSelect={handleFileSelect}
  allowedTypes={['image/jpeg', 'image/png']}
  maxSize={10 * 1024 * 1024} // 10MB
  onUploadError={handleError}
/>
```

### 4. Rate Limiting

**Location**: `src/utils/security.ts` (RateLimiter class)

- **Request Limiting**: Limits number of requests per time window
- **Key-based Tracking**: Tracks limits per user/endpoint
- **Configurable Limits**: Adjustable limits for different operations
- **Automatic Cleanup**: Removes old request data

```typescript
import { RateLimiter } from './utils/security';

// Check if request is allowed
const isAllowed = RateLimiter.isAllowed('api_key', 100, 60000);

// Get remaining requests
const remaining = RateLimiter.getRemainingRequests('api_key', 100, 60000);
```

### 5. Token Security

**Location**: `src/utils/tokenSecurity.ts`

- **Secure Storage**: Multiple storage options (cookies, memory, encrypted localStorage)
- **JWT Validation**: Format and expiration validation
- **Automatic Refresh**: Token refresh logic
- **Environment-aware**: Different storage strategies for dev/prod

```typescript
import { TokenSecurity, createTokenStorage } from './utils/tokenSecurity';

const storage = createTokenStorage();
const isValid = TokenSecurity.isValidJWTFormat(token);
const needsRefresh = TokenSecurity.needsRefresh(token);
```

### 6. Content Security Policy

**Location**: `src/utils/contentSecurityPolicy.ts`

- **Dynamic CSP**: Environment-specific CSP policies
- **Violation Reporting**: Monitors and reports CSP violations
- **Nonce Support**: Generates nonces for inline scripts/styles
- **Automatic Application**: Applies CSP on app initialization

```typescript
import { ContentSecurityPolicy } from './utils/contentSecurityPolicy';

// Apply CSP policy
ContentSecurityPolicy.applyPolicy();

// Generate nonce for inline content
const nonce = ContentSecurityPolicy.generateNonce();
```

### 7. Security Headers

**Location**: `src/services/api.ts`

Automatically adds security headers to all API requests:

- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `X-Requested-With: XMLHttpRequest`

### 8. Security Configuration

**Location**: `src/config/security.ts`

Centralized security configuration with environment-specific settings:

```typescript
import { SECURITY_CONFIG, getSecurityConfig } from './config/security';

const config = getSecurityConfig();
const maxFileSize = config.FILE_UPLOAD.MAX_SIZE;
const rateLimits = config.RATE_LIMITS;
```

## Security Hooks

**Location**: `src/hooks/useSecurity.ts`

React hooks for easy integration of security features:

```typescript
import { 
  useSanitization, 
  useCSRFProtection, 
  useRateLimit,
  useFileValidation,
  useTokenSecurity 
} from './hooks/useSecurity';

// In your component
const { sanitizeHtml, sanitizeText } = useSanitization();
const { getToken, getHeaders } = useCSRFProtection();
const { validateFile } = useFileValidation();
```

## Testing

**Location**: `src/test/security.test.tsx`, `src/test/security-integration.test.tsx`

Comprehensive test suite covering:

- XSS prevention and input sanitization
- CSRF token generation and validation
- Rate limiting functionality
- File upload security
- Token validation and management
- Integration tests for security workflows

## Usage Examples

### Secure Form Submission

```typescript
import { useSanitization, useCSRFProtection } from './hooks/useSecurity';

const MyForm = () => {
  const { sanitizeFormData } = useSanitization();
  const { getHeaders } = useCSRFProtection();

  const handleSubmit = async (formData) => {
    // Sanitize form data
    const sanitizedData = sanitizeFormData(formData);
    
    // Submit with CSRF protection
    await fetch('/api/submit', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...getHeaders(),
      },
      body: JSON.stringify(sanitizedData),
    });
  };
};
```

### Secure File Upload

```typescript
import { SecureFileUpload } from './components/ui/SecureFileUpload';

const FileUploadPage = () => {
  const handleFileSelect = (file) => {
    console.log('Secure file selected:', file.name);
  };

  const handleUploadError = (error) => {
    console.error('Upload security error:', error);
  };

  return (
    <SecureFileUpload
      onFileSelect={handleFileSelect}
      onUploadError={handleUploadError}
      allowedTypes={['image/jpeg', 'image/png', 'application/pdf']}
      maxSize={10 * 1024 * 1024}
      uploadUrl="/api/upload"
    />
  );
};
```

## Security Best Practices

1. **Always sanitize user input** before displaying or processing
2. **Use CSRF protection** for all state-changing operations
3. **Validate file uploads** thoroughly before processing
4. **Implement rate limiting** to prevent abuse
5. **Use secure token storage** appropriate for your environment
6. **Apply Content Security Policy** to prevent code injection
7. **Monitor security violations** and respond appropriately
8. **Keep security dependencies updated**

## Configuration

Security features can be configured in `src/config/security.ts`:

```typescript
export const SECURITY_CONFIG = {
  RATE_LIMITS: {
    API_REQUESTS: { maxRequests: 100, windowMs: 60000 },
    FILE_UPLOADS: { maxRequests: 5, windowMs: 60000 },
  },
  FILE_UPLOAD: {
    MAX_SIZE: 10 * 1024 * 1024,
    ALLOWED_TYPES: ['image/jpeg', 'image/png'],
  },
  // ... more configuration
};
```

## Environment Considerations

- **Development**: More permissive settings for easier development
- **Production**: Strict security policies and monitoring
- **Testing**: Isolated security testing with mocked dependencies

## Monitoring and Logging

The security implementation includes:

- **CSP violation reporting**
- **Rate limit monitoring**
- **Security event logging**
- **Failed attempt tracking**

## Dependencies

The security implementation uses these additional dependencies:

- `dompurify`: HTML sanitization
- `js-cookie`: Secure cookie management
- `@types/dompurify`: TypeScript types
- `@types/js-cookie`: TypeScript types

## Task 19 Completion

✅ **Input sanitization to prevent XSS attacks**
✅ **Proper JWT token storage and management**
✅ **CSRF protection for state-changing operations**
✅ **Secure file upload with validation**
✅ **Rate limiting on the client side**
✅ **Content security policy headers**
✅ **Security tests and vulnerability assessments**

All security requirements from Task 19 have been successfully implemented with comprehensive testing and documentation.