# Technology Stack

## Backend Technologies (Spring Boot)

### Core Technologies
- **Java 24** - Primary programming language
- **Spring Boot 3.5.4** - Application framework
- **Maven** - Build system and dependency management
- **PostgreSQL 15.x** - Primary database for persistent data
- **Redis 7.x** - Cache layer and real-time features
- **Tomcat** - Embedded servlet container (executable JAR deployment)

### Key Dependencies
- **Spring Boot Starter Web** - Web layer with Spring MVC
- **Spring Boot Starter WebSocket** - Real-time communication support
- **Spring Boot Starter Security** - JWT authentication and authorization
- **Spring Boot Starter Data JPA** - PostgreSQL database access layer
- **Spring Boot Starter Data Redis** - Redis caching and real-time features
- **PostgreSQL JDBC Driver** - Database connectivity
- **Flyway** - Database migration and versioning
- **Spring Boot Starter Mail** - Email functionality with templates
- **Spring Boot Starter Actuator** - Production monitoring and management
- **Spring Boot DevTools** - Development-time features (hot reload)
- **Lombok** - Code generation for boilerplate reduction
- **Spring Boot Configuration Processor** - Configuration metadata generation
- **Apache POI** - Excel import/export functionality
- **JWT Libraries** - JSON Web Token support for stateless authentication

### Backend Build Commands

#### Development
```bash
# Run the application in development mode
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Build & Test
```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package as executable JAR
mvn clean package

# Skip tests during build
mvn clean package -DskipTests
```

#### Useful Development Commands
```bash
# Generate sources (Lombok processing)
mvn generate-sources

# Check for dependency updates
mvn versions:display-dependency-updates
```

## Frontend Technologies (React)

### Core Technologies
- **React 18+** - Frontend framework with modern features
- **TypeScript** - Type-safe JavaScript development
- **Vite** - Fast build tool and development server
- **Node.js** - JavaScript runtime for development tools

### State Management
- **Zustand** - Lightweight global state management
- **TanStack Query (React Query)** - Server state management and caching
- **React Hook Form** - Form state management and validation

### UI and Styling
- **Mantine** - Comprehensive React component library
- **CSS Modules** - Scoped styling with Mantine theming
- **React Router v6** - Client-side routing

### Real-time and Communication
- **Socket.IO Client** - WebSocket client for real-time features
- **Axios** - HTTP client for API communication

### Development and Testing
- **Vitest** - Fast unit test runner
- **React Testing Library** - Component testing utilities
- **MSW (Mock Service Worker)** - API mocking for tests
- **Playwright** - End-to-end testing framework
- **ESLint + Prettier** - Code quality and formatting
- **TypeScript Strict Mode** - Enhanced type checking

### Validation and Forms
- **Zod** - Runtime type validation and schema definition
- **React Hook Form** - Performant form management

### Frontend Build Commands

#### Development
```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Start development server with specific port
npm run dev -- --port 3000
```

#### Build & Test
```bash
# Build for production
npm run build

# Preview production build
npm run preview

# Run unit tests
npm run test

# Run tests in watch mode
npm run test:watch

# Run E2E tests
npm run test:e2e

# Type checking
npm run type-check

# Linting
npm run lint

# Format code
npm run format
```

#### Quality Assurance
```bash
# Run all tests with coverage
npm run test:coverage

# Bundle analysis
npm run analyze

# Accessibility audit
npm run a11y
```

## Database Architecture

### Hybrid Data Storage Strategy
The system uses a hybrid approach combining PostgreSQL and Redis:

**PostgreSQL (Primary Database):**
- Core business entities (Employee, Department, Position, User, Role)
- Transactional data (Payroll, Audit logs)
- Relational integrity enforcement
- Complex queries and reporting
- ACID compliance for critical operations

**Redis (Cache & Real-time):**
- Session management and JWT token blacklisting
- Real-time chat messages and notifications
- Search result caching
- Frequently accessed data caching
- WebSocket connection management

### Database Setup Requirements
```bash
# PostgreSQL setup (required)
# Install PostgreSQL 15.x
# Create database: employee_management
# Create user with appropriate permissions

# Redis setup (required)
# Install Redis 7.x
# Configure for persistence and caching
```

## Development Workflow

### Full-Stack Development
1. **Database Setup**: Ensure PostgreSQL and Redis are running
2. **Backend First**: Start Spring Boot application for API development
3. **Database Migration**: Run Flyway migrations on startup
4. **Frontend Development**: Use Vite dev server with proxy to backend
5. **Real-time Features**: Ensure WebSocket endpoints and Redis are running
6. **Testing**: Run both backend and frontend tests
7. **Integration**: Test full-stack features end-to-end

### Recommended Development Setup
```bash
# Terminal 1: Database Services
# Start PostgreSQL service
# Start Redis service

# Terminal 2: Backend
cd demo
mvn spring-boot:run

# Terminal 3: Frontend
cd frontend
npm run dev

# Terminal 4: Testing (as needed)
npm run test:watch
```

## Code Style and Best Practices

### Backend (Spring Boot)
- Use Lombok annotations to reduce boilerplate code
- Follow Spring Boot conventions for package structure
- Leverage Spring Boot's auto-configuration capabilities
- Use Maven commands directly (not wrapper scripts)
- Implement proper exception handling and validation
- Use DTOs for API responses to avoid exposing entities
- Implement comprehensive logging with SLF4J
- **Database Best Practices:**
  - Use JPA entities with proper relationships and constraints
  - Implement Flyway migrations for all schema changes
  - Use @Transactional for data consistency
  - Apply field-level encryption for sensitive PII data
  - Implement audit trails with @CreatedDate and @LastModifiedDate
- **Security Best Practices:**
  - Use JWT tokens for stateless authentication
  - Implement role-based access control with @PreAuthorize
  - Apply permission-based data filtering in repositories
  - Encrypt sensitive fields (dateOfBirth, bankAccount, taxId)
  - Maintain comprehensive audit logs
- **Caching Strategy:**
  - Use Redis for session management and frequently accessed data
  - Implement @Cacheable annotations for expensive operations
  - Cache department trees and user permissions
  - Use Redis for real-time chat and notification features

### Frontend (React)
- Use TypeScript strict mode for enhanced type safety
- Follow functional component patterns with hooks
- Implement proper error boundaries for error handling
- Use feature-based folder organization
- Write comprehensive tests for components and hooks
- Ensure accessibility compliance (WCAG 2.1)
- Implement proper loading states and user feedback
- Use semantic HTML and proper ARIA labels
- Follow React best practices for performance optimization

### Integration
- Use consistent API response formats between backend and frontend
- Implement proper error handling across the full stack
- Ensure real-time features work reliably with WebSocket connections
- Maintain type safety between backend DTOs and frontend interfaces
- Use environment variables for configuration management
- **Database Integration:**
  - Ensure PostgreSQL and Redis are properly configured
  - Use connection pooling for optimal performance
  - Implement proper transaction boundaries
  - Handle database migrations gracefully
- **Security Integration:**
  - Implement JWT token validation on frontend
  - Handle token refresh and expiration
  - Ensure encrypted data is properly handled
  - Maintain audit trail consistency
- **Real-time Integration:**
  - Use WebSocket for chat and notifications
  - Implement proper connection management
  - Handle reconnection and error scenarios
  - Ensure message delivery reliability

## Environment Configuration

### Development Environment Setup

#### PostgreSQL Configuration
```properties
# application-dev.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/employee_management
spring.datasource.username=employee_admin
spring.datasource.password=dev_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

#### Redis Configuration
```properties
# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.database=0
spring.redis.timeout=2000ms
spring.redis.jedis.pool.max-active=8
spring.redis.jedis.pool.max-idle=8

# Cache Configuration
spring.cache.type=redis
spring.cache.redis.time-to-live=600000
```

#### Security Configuration
```properties
# JWT Configuration
jwt.secret=${JWT_SECRET:mySecretKey}
jwt.expiration=86400000
jwt.refresh-expiration=604800000

# Encryption Configuration
encryption.key=${ENCRYPTION_KEY:myEncryptionKey}
```

### Production Environment Setup

#### Database Connection Pooling
```properties
# application-prod.properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.max-lifetime=1200000
```

#### Security Hardening
```properties
# Production Security
server.error.include-message=never
server.error.include-binding-errors=never
server.error.include-stacktrace=never
server.error.include-exception=false

# SSL Configuration (if applicable)
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_PASSWORD}
server.ssl.key-store-type=PKCS12
```

### Docker Configuration (Optional)

#### Docker Compose for Development
```yaml
# docker-compose.yml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: employee_management
      POSTGRES_USER: employee_admin
      POSTGRES_PASSWORD: dev_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

volumes:
  postgres_data:
  redis_data:
```

### Testing Configuration

#### Test Database Setup
```properties
# application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop

# Disable Redis for unit tests
spring.cache.type=none
spring.redis.host=localhost
spring.redis.port=6370
```

#### Integration Test Configuration
```properties
# Use TestContainers for integration tests
spring.test.database.replace=none
testcontainers.postgresql.image=postgres:15
testcontainers.redis.image=redis:7-alpine
```

## Troubleshooting

### Common Database Issues
- **Connection refused**: Ensure PostgreSQL service is running
- **Authentication failed**: Check username/password in configuration
- **Migration failed**: Verify Flyway scripts and database permissions
- **Redis connection timeout**: Check Redis service status and configuration

### Performance Optimization
- **Slow queries**: Add appropriate database indexes
- **High memory usage**: Tune JPA batch sizes and connection pool
- **Cache misses**: Review Redis cache configuration and TTL settings
- **WebSocket issues**: Check Redis pub/sub configuration for real-time features

### Security Considerations
- **JWT token security**: Use strong secret keys and proper expiration
- **Database encryption**: Ensure sensitive fields are properly encrypted
- **Audit trail**: Verify all user actions are logged with proper context
- **Permission checks**: Test role-based access control thoroughly