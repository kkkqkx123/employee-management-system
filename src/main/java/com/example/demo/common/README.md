# Common Infrastructure and Utilities

This package contains the foundational infrastructure components and utility classes that are used throughout the Employee Management System. These components provide standardized responses, error handling, validation, and common operations.

## Package Structure

```
common/
├── dto/                    # Data Transfer Objects for standardized responses
│   ├── ApiResponse.java    # Generic API response wrapper
│   ├── ErrorResponse.java  # Standardized error response structure
│   └── PageResponse.java   # Paginated response wrapper
├── exception/              # Exception handling infrastructure
│   ├── BusinessException.java        # Business logic exceptions
│   ├── GlobalExceptionHandler.java   # Centralized exception handling
│   └── ValidationException.java      # Validation error exceptions
└── util/                   # Utility classes for common operations
    ├── CacheUtil.java      # Redis cache operations
    ├── DateUtil.java       # Date/time utilities
    ├── FileUtil.java       # File operations
    ├── StringUtil.java     # String manipulation utilities
    └── ValidationUtil.java # Business validation utilities
```

## Core Components

### 1. Response DTOs

#### ApiResponse<T>
A generic wrapper for all API responses that provides:
- Consistent response structure across all endpoints
- Success/failure indication
- Error details when applicable
- Timestamp and path information for debugging

**Usage:**
```java
// Success response
return ResponseEntity.ok(ApiResponse.success(data));

// Success with custom message
return ResponseEntity.ok(ApiResponse.success(data, "Operation completed"));

// Error response
return ResponseEntity.badRequest().body(ApiResponse.error("Invalid input"));
```

#### ErrorResponse
Detailed error information structure that includes:
- Application-specific error codes
- HTTP status codes
- User-friendly messages
- Developer details
- Validation errors
- Context information

#### PageResponse<T>
Standardized pagination wrapper that provides:
- Page content
- Pagination metadata (page, size, total elements, etc.)
- Navigation flags (first, last, hasNext, hasPrevious)
- Easy conversion from Spring Data Page objects

### 2. Exception Handling

#### GlobalExceptionHandler
Centralized exception handling using `@ControllerAdvice` that:
- Handles all application exceptions consistently
- Maps exceptions to appropriate HTTP status codes
- Provides standardized error responses
- Logs exceptions for monitoring
- Handles validation errors with field-specific messages

#### Custom Exceptions
- **BusinessException**: For business logic violations
- **ValidationException**: For data validation errors
- Feature-specific exceptions for each module

### 3. Utility Classes

#### CacheUtil
Redis cache operations utility providing:
- Generic cache operations (get, set, delete)
- Cache key management with prefixes
- TTL (Time To Live) management
- Bulk operations
- Cache statistics and monitoring
- Pattern-based key operations

#### DateUtil
Date and time operations utility providing:
- Date formatting and parsing
- Business day calculations
- Age calculations
- Date range validations
- Timezone conversions
- Period calculations

#### StringUtil
String manipulation utility providing:
- Null-safe string operations
- Validation helpers
- Text formatting
- Case conversions
- Sanitization methods
- Pattern matching utilities

#### ValidationUtil
Business-specific validation utility providing:
- Employee number format validation
- Department and position code validation
- Date of birth and hire date validation
- Salary range validation
- Password strength validation
- Name and address validation

#### FileUtil
File operations utility providing:
- File upload/download operations
- File type validation
- Size validation
- Path sanitization
- Temporary file management
- File content operations

## Configuration Integration

The common infrastructure integrates with the application configuration:

### WebSocket Configuration
- Secure WebSocket connections with authentication
- Message-level security validation
- Connection monitoring and logging
- Heartbeat configuration

### CSRF Protection
- Cookie-based CSRF token management
- Configurable exclusion patterns
- Environment-specific configurations
- XOR encoding for security

### Caching Configuration
- Redis-based caching with TTL management
- Cache key naming strategies
- Performance monitoring

## Best Practices

### 1. Error Handling
```java
// Always use standardized responses
@ExceptionHandler(BusinessException.class)
public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
    ErrorResponse error = ErrorResponse.of("BUSINESS_ERROR", 400, ex.getMessage());
    return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage(), error));
}
```

### 2. Validation
```java
// Use ValidationUtil for business validations
if (!ValidationUtil.isValidEmployeeNumber(employeeNumber)) {
    throw new ValidationException("Invalid employee number format");
}
```

### 3. Caching
```java
// Use CacheUtil for cache operations
@Service
public class EmployeeService {
    
    public Employee getEmployee(Long id) {
        return CacheUtil.get("employee:" + id, Employee.class)
            .orElseGet(() -> {
                Employee emp = repository.findById(id);
                CacheUtil.set("employee:" + id, emp, Duration.ofMinutes(30));
                return emp;
            });
    }
}
```

### 4. Date Operations
```java
// Use DateUtil for date operations
LocalDate hireDate = DateUtil.parseDate(hireDateString);
if (!ValidationUtil.isValidHireDate(hireDate)) {
    throw new ValidationException("Invalid hire date");
}
```

## Testing

Comprehensive test coverage is provided for all utility classes and exception handling:

- Unit tests for all utility methods
- Integration tests for exception handling
- Cache operation tests with TestContainers
- WebSocket security tests
- CSRF protection tests

## Security Considerations

1. **Input Validation**: All utilities include input validation and sanitization
2. **XSS Prevention**: String utilities include XSS protection methods
3. **Path Traversal**: File utilities prevent path traversal attacks
4. **Cache Security**: Cache keys are properly namespaced and validated
5. **Error Information**: Error responses don't expose sensitive information

## Performance Considerations

1. **Caching**: Efficient Redis operations with connection pooling
2. **Lazy Loading**: Utilities use lazy initialization where appropriate
3. **Memory Management**: Proper resource cleanup in file operations
4. **Batch Operations**: Support for bulk cache operations
5. **Connection Management**: Proper WebSocket connection handling

## Monitoring and Logging

All components include comprehensive logging:
- Operation logs for debugging
- Performance metrics for monitoring
- Error logs with context information
- Cache hit/miss statistics
- WebSocket connection monitoring

## Dependencies

The common infrastructure relies on:
- Spring Boot 3.5.4
- Spring Security
- Spring Data Redis
- Jackson for JSON processing
- Lombok for code generation
- SLF4J for logging

## Future Enhancements

Planned improvements include:
- Distributed caching with Redis Cluster
- Advanced validation with custom annotations
- Metrics collection with Micrometer
- Enhanced WebSocket security with JWT validation
- Audit logging for all operations