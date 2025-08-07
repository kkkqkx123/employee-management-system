# System Architecture

This document provides a comprehensive overview of the Employee Management System architecture, including system design, component interactions, data flow, and technical decisions.

## Table of Contents

1. [System Overview](#system-overview)
2. [Architecture Patterns](#architecture-patterns)
3. [Technology Stack](#technology-stack)
4. [System Components](#system-components)
5. [Data Architecture](#data-architecture)
6. [Security Architecture](#security-architecture)
7. [Communication Patterns](#communication-patterns)
8. [Deployment Architecture](#deployment-architecture)

## System Overview

The Employee Management System is a comprehensive enterprise application built using modern Spring Boot architecture with a hybrid database approach. The system provides complete employee lifecycle management, real-time communication, and administrative capabilities.

### Key Architectural Principles

- **Modular Design**: Feature-based package organization with clear boundaries
- **Separation of Concerns**: Distinct layers for presentation, business logic, and data access
- **Scalability**: Horizontal scaling support with stateless design
- **Security First**: JWT-based authentication with role-based authorization
- **Performance**: Hybrid database architecture with caching and optimization
- **Maintainability**: Clean code principles with comprehensive testing

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Frontend Layer                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   React Web     │  │   Mobile App    │  │  Admin Panel    │ │
│  │   Application   │  │   (Future)      │  │                 │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                │
                        ┌───────┴───────┐
                        │  Load Balancer │
                        │   (Optional)   │
                        └───────┬───────┘
                                │
┌─────────────────────────────────────────────────────────────────┐
│                      Application Layer                          │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                Spring Boot Application                      │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │ │
│  │  │ Controllers │  │  Services   │  │    Repositories     │ │ │
│  │  │   (REST)    │  │ (Business)  │  │   (Data Access)     │ │ │
│  │  └─────────────┘  └─────────────┘  └─────────────────────┘ │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │ │
│  │  │ WebSocket   │  │ Security    │  │    Configuration    │ │ │
│  │  │ Handlers    │  │ Filters     │  │      Classes        │ │ │
│  │  └─────────────┘  └─────────────┘  └─────────────────────┘ │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                │
                    ┌───────────┴───────────┐
                    │                       │
┌─────────────────────────────────┐  ┌─────────────────────────────────┐
│         Data Layer              │  │        Cache Layer              │
│  ┌─────────────────────────────┐ │  │  ┌─────────────────────────────┐ │
│  │       PostgreSQL            │ │  │  │          Redis              │ │
│  │   - Employee Data           │ │  │  │   - Session Storage         │ │
│  │   - Department Hierarchy    │ │  │  │   - JWT Token Blacklist     │ │
│  │   - User Management         │ │  │  │   - Chat Messages           │ │
│  │   - Audit Logs              │ │  │  │   - Notifications           │ │
│  │   - Payroll Data            │ │  │  │   - Cache Data              │ │
│  └─────────────────────────────┘ │  │  └─────────────────────────────┘ │
└─────────────────────────────────┘  └─────────────────────────────────┘
```

## Architecture Patterns

### 1. Layered Architecture

The application follows a traditional layered architecture with clear separation of concerns:

#### Presentation Layer (Controllers)
- **Responsibility**: Handle HTTP requests/responses, input validation, authentication
- **Components**: REST Controllers, WebSocket Handlers, Exception Handlers
- **Technologies**: Spring MVC, Spring WebSocket, OpenAPI/Swagger

#### Business Layer (Services)
- **Responsibility**: Business logic, transaction management, data transformation
- **Components**: Service classes, DTOs, Business validators
- **Technologies**: Spring Service, Spring Transaction, ModelMapper

#### Data Access Layer (Repositories)
- **Responsibility**: Data persistence, query execution, database interactions
- **Components**: JPA Repositories, Custom queries, Entity classes
- **Technologies**: Spring Data JPA, Hibernate, PostgreSQL

#### Cross-Cutting Concerns
- **Security**: JWT authentication, role-based authorization
- **Caching**: Redis-based caching for performance
- **Logging**: Structured logging with correlation IDs
- **Monitoring**: Health checks, metrics, and observability

### 2. Domain-Driven Design (DDD)

The application is organized around business domains:

```
src/main/java/com/example/demo/
├── security/           # User authentication and authorization
├── employee/           # Employee lifecycle management
├── department/         # Organizational structure
├── position/           # Job positions and titles
├── communication/      # Email, chat, notifications
├── payroll/           # Payroll processing
└── common/            # Shared utilities and components
```

### 3. Repository Pattern

Data access is abstracted through repository interfaces:

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);
    List<Employee> findByStatus(EmployeeStatus status);
    Optional<Employee> findByEmployeeNumber(String employeeNumber);
}
```

### 4. Service Layer Pattern

Business logic is encapsulated in service classes:

```java
@Service
@Transactional
public class EmployeeService {
    
    public EmployeeDto createEmployee(EmployeeCreateRequest request) {
        // Business logic implementation
    }
    
    @Cacheable("employees")
    public Page<EmployeeDto> getAllEmployees(Pageable pageable) {
        // Cached data retrieval
    }
}
```## 
Technology Stack

### Backend Technologies

#### Core Framework
- **Spring Boot 3.5.4**: Application framework and auto-configuration
- **Java 24**: Programming language with modern features
- **Maven**: Build automation and dependency management

#### Data Management
- **PostgreSQL 15.x**: Primary relational database for persistent data
- **Redis 7.x**: In-memory data store for caching and real-time features
- **Spring Data JPA**: Data access abstraction layer
- **Hibernate**: Object-relational mapping (ORM)
- **Flyway**: Database migration and versioning

#### Security
- **Spring Security**: Authentication and authorization framework
- **JWT (JSON Web Tokens)**: Stateless authentication mechanism
- **BCrypt**: Password hashing algorithm
- **HTTPS/TLS**: Secure communication protocol

#### Communication
- **Spring WebSocket**: Real-time bidirectional communication
- **STOMP**: Simple Text Oriented Messaging Protocol
- **Spring Mail**: Email sending capabilities
- **Apache POI**: Excel file processing

#### Monitoring & Operations
- **Spring Boot Actuator**: Production monitoring and management
- **Micrometer**: Application metrics collection
- **Logback**: Logging framework
- **OpenAPI 3.0**: API documentation

### Development & Testing
- **Spring Boot DevTools**: Development-time features
- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework for tests
- **TestContainers**: Integration testing with real databases
- **Spring Security Test**: Security testing utilities

## System Components

### 1. Security Module

#### Components
- **Authentication Service**: User login/logout, token management
- **Authorization Service**: Permission checking, role validation
- **JWT Token Provider**: Token generation, validation, refresh
- **Security Filters**: Request authentication, CORS handling
- **User Management**: User CRUD operations, role assignments

#### Key Features
- JWT-based stateless authentication
- Role-based access control (RBAC)
- Token blacklisting for secure logout
- Password policy enforcement
- Audit logging for security events

### 2. Employee Management Module

#### Components
- **Employee Service**: Core employee operations
- **Import/Export Service**: Bulk data operations
- **Search Service**: Advanced employee search
- **Validation Service**: Data integrity checks

#### Key Features
- Complete employee lifecycle management
- Advanced search and filtering
- Excel/CSV import/export
- Employee status management
- Audit trail for all changes

### 3. Department Management Module

#### Components
- **Department Service**: Hierarchical operations
- **Tree Service**: Department tree management
- **Statistics Service**: Department analytics

#### Key Features
- Hierarchical department structure
- Tree-based operations (move, reorganize)
- Department statistics and reporting
- Path-based queries for performance

### 4. Communication System

#### Email Subsystem
- **Email Service**: Template-based email sending
- **Template Engine**: Dynamic email content generation
- **Queue Management**: Asynchronous email processing
- **Delivery Tracking**: Email status monitoring

#### Chat Subsystem
- **Chat Service**: Real-time messaging
- **Room Management**: Chat room operations
- **Message History**: Persistent message storage
- **WebSocket Handler**: Real-time communication

#### Notification Subsystem
- **Notification Service**: System notifications
- **Real-time Delivery**: WebSocket-based notifications
- **Preference Management**: User notification settings
- **Batch Processing**: Bulk notification sending

### 5. Payroll Management Module

#### Components
- **Payroll Service**: Salary calculations
- **Ledger Management**: Financial record keeping
- **Calculation Engine**: Complex payroll algorithms
- **Audit Service**: Financial audit trails

#### Key Features
- Automated salary calculations
- Tax and deduction processing
- Payroll period management
- Financial reporting and analytics

## Data Architecture

### Hybrid Database Strategy

The system uses a hybrid approach combining PostgreSQL and Redis:

#### PostgreSQL (Primary Database)
**Purpose**: Persistent data storage with ACID compliance

**Data Types**:
- **Transactional Data**: Employee records, department hierarchy, user accounts
- **Relational Data**: Complex relationships between entities
- **Audit Data**: Complete audit trails and change history
- **Financial Data**: Payroll information with strict consistency requirements

**Schema Design**:
```sql
-- Core entities with proper relationships
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    parent_id BIGINT REFERENCES departments(id),
    dep_path VARCHAR(1000),
    level INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    employee_number VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    department_id BIGINT REFERENCES departments(id),
    position_id BIGINT REFERENCES positions(id),
    manager_id BIGINT REFERENCES employees(id),
    hire_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Redis (Cache & Real-time)
**Purpose**: High-performance caching and real-time data

**Data Types**:
- **Session Data**: User sessions and JWT token blacklist
- **Cache Data**: Frequently accessed data for performance
- **Real-time Data**: Chat messages, notifications, presence information
- **Temporary Data**: Import/export job status, temporary calculations

**Key Patterns**:
```
# Session management
session:{userId} -> {sessionData}

# JWT blacklist
jwt:blacklist:{tokenId} -> {expirationTime}

# Cache patterns
cache:employees:department:{deptId} -> {employeeList}
cache:departments:tree -> {departmentTree}

# Real-time data
chat:room:{roomId}:messages -> {messageList}
notifications:user:{userId} -> {notificationList}
```

### Data Flow Architecture

#### Read Operations
```
Client Request → Controller → Service → Cache Check → Database (if cache miss) → Response
```

#### Write Operations
```
Client Request → Controller → Service → Database → Cache Invalidation → Response
```

#### Real-time Operations
```
Client Action → WebSocket Handler → Service → Database → Redis Pub/Sub → WebSocket Broadcast
```

### Database Performance Optimization

#### Indexing Strategy
```sql
-- Performance indexes
CREATE INDEX idx_employees_department_status ON employees(department_id, status);
CREATE INDEX idx_employees_last_name ON employees(last_name);
CREATE INDEX idx_departments_parent_path ON departments(parent_id, dep_path);
CREATE INDEX idx_users_username_enabled ON users(username, enabled);
```

#### Connection Pooling
```properties
# HikariCP configuration for optimal performance
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

#### Query Optimization
- Use of JOIN FETCH to avoid N+1 queries
- Pagination for large datasets
- Specification API for dynamic queries
- Batch processing for bulk operations## Securi
ty Architecture

### Authentication Flow

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │    │ Auth Filter │    │Auth Service │    │  Database   │
└──────┬──────┘    └──────┬──────┘    └──────┬──────┘    └──────┬──────┘
       │                  │                  │                  │
       │ 1. Login Request │                  │                  │
       ├─────────────────→│                  │                  │
       │                  │ 2. Extract Creds │                  │
       │                  ├─────────────────→│                  │
       │                  │                  │ 3. Validate User │
       │                  │                  ├─────────────────→│
       │                  │                  │ 4. User Details  │
       │                  │                  │←─────────────────┤
       │                  │ 5. Generate JWT  │                  │
       │                  │←─────────────────┤                  │
       │ 6. JWT Response  │                  │                  │
       │←─────────────────┤                  │                  │
       │                  │                  │                  │
       │ 7. API Request   │                  │                  │
       │ (with JWT)       │                  │                  │
       ├─────────────────→│                  │                  │
       │                  │ 8. Validate JWT  │                  │
       │                  ├─────────────────→│                  │
       │                  │ 9. Set Security  │                  │
       │                  │   Context        │                  │
       │                  │←─────────────────┤                  │
       │ 10. API Response │                  │                  │
       │←─────────────────┤                  │                  │
```

### Authorization Model

#### Role-Based Access Control (RBAC)
```
Users ←→ Roles ←→ Resources
  │        │        │
  └────────┼────────┘
           │
      Permissions
```

#### Permission Structure
```java
public class Permission {
    private String resource;    // e.g., "EMPLOYEE"
    private String action;      // e.g., "READ", "WRITE", "DELETE"
    private String scope;       // e.g., "OWN", "DEPARTMENT", "ALL"
}

// Examples:
// EMPLOYEE_READ_ALL - Can read all employees
// EMPLOYEE_WRITE_DEPARTMENT - Can modify employees in same department
// EMPLOYEE_DELETE_OWN - Can only delete own employee record
```

#### Security Annotations
```java
@PreAuthorize("hasAuthority('EMPLOYEE_READ')")
public Page<EmployeeDto> getAllEmployees(Pageable pageable) { }

@PreAuthorize("hasAuthority('EMPLOYEE_WRITE') or @securityService.isOwner(#id, authentication.name)")
public EmployeeDto updateEmployee(Long id, EmployeeUpdateRequest request) { }
```

### Data Security

#### Encryption at Rest
```java
@Entity
public class Employee {
    @Convert(converter = EncryptedStringConverter.class)
    private String socialSecurityNumber;
    
    @Convert(converter = EncryptedStringConverter.class)
    private String bankAccountNumber;
}
```

#### Audit Trail
```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Employee {
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
    
    @CreatedBy
    private String createdBy;
    
    @LastModifiedBy
    private String lastModifiedBy;
}
```

## Communication Patterns

### Synchronous Communication

#### REST API Pattern
```
Client → HTTP Request → Controller → Service → Repository → Database
                                                     ↓
Client ← HTTP Response ← Controller ← Service ← Repository ← Database
```

#### Request/Response Flow
1. **Request Validation**: Input validation and sanitization
2. **Authentication**: JWT token validation
3. **Authorization**: Permission checking
4. **Business Logic**: Service layer processing
5. **Data Access**: Repository operations
6. **Response Formation**: DTO mapping and response creation

### Asynchronous Communication

#### WebSocket Pattern for Real-time Features
```
Client ←→ WebSocket Handler ←→ Message Broker ←→ Service Layer
                                     ↓
                              Redis Pub/Sub ←→ Database
```

#### Event-Driven Architecture
```java
@EventListener
public void handleEmployeeCreated(EmployeeCreatedEvent event) {
    // Send welcome email
    emailService.sendWelcomeEmail(event.getEmployee());
    
    // Create notification
    notificationService.createNotification(
        "New employee joined: " + event.getEmployee().getFullName()
    );
    
    // Update statistics
    statisticsService.updateEmployeeCount(event.getEmployee().getDepartmentId());
}
```

### Message Patterns

#### Email Communication
```
Service → Email Queue → Async Processor → SMTP Server → Recipient
                            ↓
                      Email Log ← Database
```

#### Real-time Notifications
```
Event Source → Notification Service → WebSocket Handler → Connected Clients
                        ↓
                   Database Storage
                        ↓
                   Redis Cache
```

## Deployment Architecture

### Single Instance Deployment

```
┌─────────────────────────────────────────────────────────────────┐
│                        Server Instance                          │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │              Spring Boot Application                        │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │ │
│  │  │   Tomcat    │  │   WebSocket │  │    Background       │ │ │
│  │  │  Container  │  │   Handler   │  │     Tasks           │ │ │
│  │  └─────────────┘  └─────────────┘  └─────────────────────┘ │ │
│  └─────────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    PostgreSQL                               │ │
│  └─────────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                      Redis                                  │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### Scalable Deployment

```
┌─────────────────┐    ┌─────────────────────────────────────────┐
│  Load Balancer  │    │              Application Tier           │
│   (Nginx/HAProxy)│    │  ┌─────────────┐  ┌─────────────────┐  │
└─────────┬───────┘    │  │Spring Boot  │  │  Spring Boot    │  │
          │            │  │Instance 1   │  │  Instance 2     │  │
          └───────────→│  └─────────────┘  └─────────────────┘  │
                       └─────────────────────────────────────────┘
                                         │
                       ┌─────────────────┴─────────────────────┐
                       │              Data Tier                │
                       │  ┌─────────────┐  ┌─────────────────┐ │
                       │  │PostgreSQL   │  │     Redis       │ │
                       │  │Primary/     │  │   Cluster       │ │
                       │  │Replica      │  │                 │ │
                       │  └─────────────┘  └─────────────────┘ │
                       └───────────────────────────────────────┘
```

### Container Deployment (Docker)

```dockerfile
# Multi-stage Docker build
FROM openjdk:24-jdk-slim as builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:24-jre-slim
WORKDIR /app
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: employee-management
spec:
  replicas: 3
  selector:
    matchLabels:
      app: employee-management
  template:
    metadata:
      labels:
        app: employee-management
    spec:
      containers:
      - name: app
        image: employee-management:latest
        ports:
        - containerPort: 8080
        env:
        - name: DB_HOST
          value: "postgresql-service"
        - name: REDIS_HOST
          value: "redis-service"
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
```

### Performance Considerations

#### Caching Strategy
- **L1 Cache**: Application-level caching with Spring Cache
- **L2 Cache**: Redis distributed cache
- **Database Cache**: PostgreSQL query result cache

#### Connection Pooling
- **Database**: HikariCP with optimized pool settings
- **Redis**: Jedis connection pool for Redis operations
- **HTTP**: Keep-alive connections for external services

#### Monitoring and Observability
- **Health Checks**: Spring Boot Actuator endpoints
- **Metrics**: Micrometer with Prometheus integration
- **Logging**: Structured logging with correlation IDs
- **Tracing**: Distributed tracing for request flows

This architecture provides a solid foundation for a scalable, maintainable, and secure employee management system that can grow with organizational needs while maintaining high performance and reliability.