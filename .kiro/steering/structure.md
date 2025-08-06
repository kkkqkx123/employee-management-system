# Project Structure

## Full-Stack Architecture
This project follows a full-stack architecture with separate backend (Spring Boot) and frontend (React) applications.

## Backend Structure (Spring Boot)

### Root Level
- `pom.xml` - Maven configuration and dependencies
- `mvnw`, `mvnw.cmd` - Maven wrapper scripts
- `HELP.md` - Getting started documentation

### Source Organization
```
demo/src/
├── main/
│   ├── java/
│   │   └── com/example/demo/
│   │       ├── DemoApplication.java     # Main Spring Boot application
│   │       ├── ServletInitializer.java  # WAR deployment configuration
│   │       ├── security/                # Security and Permission Management
│   │       │   ├── config/
│   │       │   │   ├── SecurityConfig.java
│   │       │   │   ├── JwtConfig.java
│   │       │   │   └── RedisConfig.java
│   │       │   ├── entity/
│   │       │   │   ├── User.java
│   │       │   │   ├── Role.java
│   │       │   │   └── Resource.java
│   │       │   ├── repository/
│   │       │   │   ├── UserRepository.java
│   │       │   │   ├── RoleRepository.java
│   │       │   │   └── ResourceRepository.java
│   │       │   ├── service/
│   │       │   │   ├── UserService.java
│   │       │   │   ├── RoleService.java
│   │       │   │   ├── AuthenticationService.java
│   │       │   │   └── PermissionService.java
│   │       │   ├── controller/
│   │       │   │   ├── AuthController.java
│   │       │   │   ├── UserController.java
│   │       │   │   └── RoleController.java
│   │       │   ├── dto/
│   │       │   │   ├── LoginRequest.java
│   │       │   │   ├── LoginResponse.java
│   │       │   │   ├── UserDto.java
│   │       │   │   ├── UserCreateRequest.java
│   │       │   │   ├── UserUpdateRequest.java
│   │       │   │   ├── RoleDto.java
│   │       │   │   └── ResourceDto.java
│   │       │   ├── security/
│   │       │   │   ├── JwtAuthenticationFilter.java
│   │       │   │   ├── JwtTokenProvider.java
│   │       │   │   ├── CustomUserDetailsService.java
│   │       │   │   ├── CustomUserPrincipal.java
│   │       │   │   ├── SecurityUtils.java
│   │       │   │   ├── AESUtil.java
│   │       │   │   └── EncryptedStringConverter.java
│   │       │   └── exception/
│   │       │       ├── AuthenticationException.java
│   │       │       ├── AuthorizationException.java
│   │       │       ├── UserNotFoundException.java
│   │       │       ├── RoleNotFoundException.java
│   │       │       ├── ResourceNotFoundException.java
│   │       │       ├── RoleAlreadyExistsException.java
│   │       │       ├── UserAlreadyExistsException.java
│   │       │       ├── RoleInUseException.java
│   │       │       └── InvalidPasswordException.java
│   │       ├── department/              # Department Management
│   │       │   ├── entity/
│   │       │   │   └── Department.java
│   │       │   ├── repository/
│   │       │   │   └── DepartmentRepository.java
│   │       │   ├── service/
│   │       │   │   ├── DepartmentService.java
│   │       │   │   └── impl/
│   │       │   │       └── DepartmentServiceImpl.java
│   │       │   ├── controller/
│   │       │   │   └── DepartmentController.java
│   │       │   ├── dto/
│   │       │   │   ├── DepartmentDto.java
│   │       │   │   ├── DepartmentTreeDto.java
│   │       │   │   ├── DepartmentCreateRequest.java
│   │       │   │   ├── DepartmentUpdateRequest.java
│   │       │   │   └── DepartmentStatisticsDto.java
│   │       │   └── exception/
│   │       │       ├── DepartmentNotFoundException.java
│   │       │       ├── DepartmentHierarchyException.java
│   │       │       └── DepartmentInUseException.java
│   │       ├── employee/                # Employee Management
│   │       │   ├── entity/
│   │       │   │   ├── Employee.java
│   │       │   │   ├── EmployeeStatus.java
│   │       │   │   ├── EmploymentType.java
│   │       │   │   ├── Gender.java
│   │       │   │   ├── MaritalStatus.java
│   │       │   │   └── PayType.java
│   │       │   ├── repository/
│   │       │   │   └── EmployeeRepository.java
│   │       │   ├── service/
│   │       │   │   ├── EmployeeService.java
│   │       │   │   ├── EmployeeImportService.java
│   │       │   │   ├── EmployeeExportService.java
│   │       │   │   └── impl/
│   │       │   │       ├── EmployeeServiceImpl.java
│   │       │   │       ├── EmployeeImportServiceImpl.java
│   │       │   │       └── EmployeeExportServiceImpl.java
│   │       │   ├── controller/
│   │       │   │   └── EmployeeController.java
│   │       │   ├── dto/
│   │       │   │   ├── EmployeeDto.java
│   │       │   │   ├── EmployeeCreateRequest.java
│   │       │   │   ├── EmployeeUpdateRequest.java
│   │       │   │   ├── EmployeeSearchCriteria.java
│   │       │   │   ├── EmployeeImportResult.java
│   │       │   │   └── EmployeeExportRequest.java
│   │       │   ├── util/
│   │       │   │   ├── EmployeeExcelUtil.java
│   │       │   │   └── EmployeeValidationUtil.java
│   │       │   └── exception/
│   │       │       ├── EmployeeNotFoundException.java
│   │       │       ├── EmployeeAlreadyExistsException.java
│   │       │       ├── EmployeeImportException.java
│   │       │       └── EmployeeExportException.java
│   │       ├── position/                # Position and Title Management
│   │       │   ├── entity/
│   │       │   │   └── Position.java
│   │       │   ├── enums/
│   │       │   │   ├── PositionCategory.java
│   │       │   │   ├── PositionLevel.java
│   │       │   │   └── EmploymentType.java
│   │       │   ├── repository/
│   │       │   │   └── PositionRepository.java
│   │       │   ├── service/
│   │       │   │   ├── PositionService.java
│   │       │   │   └── impl/
│   │       │   │       └── PositionServiceImpl.java
│   │       │   ├── controller/
│   │       │   │   └── PositionController.java
│   │       │   ├── dto/
│   │       │   │   ├── PositionDto.java
│   │       │   │   ├── PositionCreateRequest.java
│   │       │   │   ├── PositionUpdateRequest.java
│   │       │   │   └── PositionSearchCriteria.java
│   │       │   └── exception/
│   │       │       ├── PositionNotFoundException.java
│   │       │       ├── PositionAlreadyExistsException.java
│   │       │       └── PositionInUseException.java
│   │       ├── communication/           # Communication System
│   │       │   ├── email/
│   │       │   │   ├── entity/
│   │       │   │   │   ├── EmailTemplate.java
│   │       │   │   │   ├── EmailLog.java
│   │       │   │   │   ├── EmailPriority.java
│   │       │   │   │   ├── EmailStatus.java
│   │       │   │   │   ├── TemplateCategory.java
│   │       │   │   │   └── TemplateType.java
│   │       │   │   ├── repository/
│   │       │   │   │   ├── EmailLogRepository.java
│   │       │   │   │   └── EmailTemplateRepository.java
│   │       │   │   ├── service/
│   │       │   │   │   ├── EmailService.java
│   │       │   │   │   ├── EmailTemplateService.java
│   │       │   │   │   └── impl/
│   │       │   │   │       ├── EmailServiceImpl.java
│   │       │   │   │       └── EmailTemplateServiceImpl.java
│   │       │   │   ├── dto/
│   │       │   │   │   ├── EmailRequest.java
│   │       │   │   │   ├── BulkEmailRequest.java
│   │       │   │   │   ├── EmailTemplateDto.java
│   │       │   │   │   ├── EmailLogDto.java
│   │       │   │   │   └── EmailStatisticsDto.java
│   │       │   │   └── util/
│   │       │   │       └── EmailTemplateProcessor.java
│   │       │   ├── chat/
│   │       │   │   ├── entity/
│   │       │   │   │   ├── ChatMessage.java
│   │       │   │   │   ├── ChatRoom.java
│   │       │   │   │   ├── ChatParticipant.java
│   │       │   │   │   ├── ChatMessageType.java
│   │       │   │   │   ├── ChatRoomType.java
│   │       │   │   │   └── ChatParticipantRole.java
│   │       │   │   ├── repository/
│   │       │   │   │   ├── ChatMessageRepository.java
│   │       │   │   │   ├── ChatRoomRepository.java
│   │       │   │   │   └── ChatParticipantRepository.java
│   │       │   │   ├── service/
│   │       │   │   │   ├── ChatService.java
│   │       │   │   │   └── impl/
│   │       │   │   │       └── ChatServiceImpl.java
│   │       │   │   ├── controller/
│   │       │   │   │   └── ChatController.java
│   │       │   │   ├── dto/
│   │       │   │   │   ├── ChatMessageDto.java
│   │       │   │   │   ├── ChatRoomDto.java
│   │       │   │   │   ├── ChatParticipantDto.java
│   │       │   │   │   ├── CreateChatRoomRequest.java
│   │       │   │   │   └── SendMessageRequest.java
│   │       │   │   └── websocket/
│   │       │   │       ├── ChatWebSocketHandler.java
│   │       │   │       └── WebSocketConfig.java
│   │       │   ├── notification/
│   │       │   │   ├── entity/
│   │       │   │   │   ├── Notification.java
│   │       │   │   │   ├── NotificationType.java
│   │       │   │   │   └── NotificationPriority.java
│   │       │   │   ├── repository/
│   │       │   │   │   └── NotificationRepository.java
│   │       │   │   ├── service/
│   │       │   │   │   ├── NotificationService.java
│   │       │   │   │   └── impl/
│   │       │   │   │       └── NotificationServiceImpl.java
│   │       │   │   ├── controller/
│   │       │   │   │   └── NotificationController.java
│   │       │   │   ├── dto/
│   │       │   │   │   ├── NotificationDto.java
│   │       │   │   │   ├── NotificationCreateRequest.java
│   │       │   │   │   └── NotificationMarkReadRequest.java
│   │       │   │   └── websocket/
│   │       │   │       └── NotificationWebSocketHandler.java
│   │       │   ├── announcement/
│   │       │   │   ├── entity/
│   │       │   │   │   ├── Announcement.java
│   │       │   │   │   └── AnnouncementTarget.java
│   │       │   │   ├── repository/
│   │       │   │   │   └── AnnouncementRepository.java
│   │       │   │   ├── service/
│   │       │   │   │   ├── AnnouncementService.java
│   │       │   │   │   ├── AnnouncementValidationService.java
│   │       │   │   │   ├── AnnouncementScheduledService.java
│   │       │   │   │   └── impl/
│   │       │   │   │       └── AnnouncementServiceImpl.java
│   │       │   │   ├── controller/
│   │       │   │   │   └── AnnouncementController.java
│   │       │   │   └── dto/
│   │       │   │       ├── AnnouncementDto.java
│   │       │   │       ├── AnnouncementCreateRequest.java
│   │       │   │       ├── AnnouncementUpdateRequest.java
│   │       │   │       └── AnnouncementStatisticsDto.java
│   │       │   └── exception/
│   │       │       ├── EmailSendingException.java
│   │       │       ├── TemplateNotFoundException.java
│   │       │       ├── EmailLogNotFoundException.java
│   │       │       ├── ChatRoomNotFoundException.java
│   │       │       ├── ChatMessageNotFoundException.java
│   │       │       ├── NotificationException.java
│   │       │       └── AnnouncementNotFoundException.java
│   │       ├── payroll/                 # Payroll Management
│   │       │   ├── entity/
│   │       │   │   ├── PayrollLedger.java
│   │       │   │   ├── PayrollLedgerComponent.java
│   │       │   │   ├── PayrollPeriod.java
│   │       │   │   ├── SalaryComponent.java
│   │       │   │   ├── PayrollAudit.java
│   │       │   │   ├── PaymentMethod.java
│   │       │   │   ├── PayrollLedgerStatus.java
│   │       │   │   ├── PayrollPeriodStatus.java
│   │       │   │   └── PayrollPeriodType.java
│   │       │   ├── repository/
│   │       │   │   ├── PayrollLedgerRepository.java
│   │       │   │   ├── PayrollPeriodRepository.java
│   │       │   │   ├── SalaryComponentRepository.java
│   │       │   │   └── PayrollAuditRepository.java
│   │       │   ├── service/
│   │       │   │   ├── PayrollService.java
│   │       │   │   ├── PayrollCalculationService.java
│   │       │   │   └── impl/
│   │       │   │       ├── PayrollServiceImpl.java
│   │       │   │       └── PayrollCalculationServiceImpl.java
│   │       │   ├── controller/
│   │       │   │   └── PayrollController.java
│   │       │   ├── dto/
│   │       │   │   ├── PayrollLedgerDto.java
│   │       │   │   ├── PayrollLedgerComponentDto.java
│   │       │   │   ├── PayrollPeriodDto.java
│   │       │   │   ├── SalaryComponentDto.java
│   │       │   │   ├── PayrollCalculationRequest.java
│   │       │   │   ├── PayrollReportRequest.java
│   │       │   │   └── PayrollSummaryDto.java
│   │       │   └── exception/
│   │       │       ├── PayrollNotFoundException.java
│   │       │       ├── PayrollCalculationException.java
│   │       │       ├── PayrollPeriodException.java
│   │       │       └── PayrollValidationException.java
│   │       ├── config/                  # Application configuration
│   │       │   ├── RedisConfig.java
│   │       │   ├── WebSocketConfig.java
│   │       │   ├── AsyncConfig.java
│   │       │   ├── CorsConfig.java
│   │       │   ├── CsrfConfig.java
│   │       │   ├── FlywayConfig.java
│   │       │   ├── JpaConfig.java
│   │       │   ├── RequestResponseLoggingConfig.java
│   │       │   └── SecurityHeadersConfig.java
│   │       └── common/                  # Common utilities and shared components
│   │           ├── dto/
│   │           │   ├── ApiResponse.java
│   │           │   ├── ErrorResponse.java
│   │           │   └── PageResponse.java
│   │           ├── exception/
│   │           │   ├── BusinessException.java
│   │           │   ├── GlobalExceptionHandler.java
│   │           │   └── ValidationException.java
│   │           └── util/
│   │               ├── CacheUtil.java
│   │               ├── DateUtil.java
│   │               ├── FileUtil.java
│   │               ├── StringUtil.java
│   │               └── ValidationUtil.java
│   └── resources/
│       ├── application.properties       # Application configuration
│       ├── application-dev.properties   # Development configuration
│       ├── application-prod.properties  # Production configuration
│       ├── static/                      # Static web assets
│       ├── templates/                   # Email templates
│       └── db/
│           └── migration/               # Database migration scripts (Flyway)
│               ├── V1__Create_security_tables.sql
│               ├── V2__Create_departments_table.sql
│               └── ... (other migration scripts)
└── test/
    └── java/
        └── com/example/demo/            # Test classes mirror main structure
            ├── security/
            ├── department/
            ├── employee/
            ├── position/
            ├── communication/
            ├── payroll/
            └── integration/             # Integration tests
```

## Frontend Structure (React)

### Recommended React Project Structure
```
frontend/
├── public/                              # Static assets
├── src/
│   ├── components/                      # Reusable UI components
│   │   ├── ui/                         # Basic UI components (DataTable, FormField, etc.)
│   │   ├── forms/                      # Form components
│   │   └── layout/                     # Layout components (AppShell, Navigation, Header)
│   ├── features/                       # Feature-based modules
│   │   ├── auth/                       # Authentication feature
│   │   │   ├── components/             # Auth-specific components
│   │   │   ├── hooks/                  # Auth-specific hooks
│   │   │   ├── services/               # Auth API services
│   │   │   └── types/                  # Auth type definitions
│   │   ├── employees/                  # Employee management
│   │   ├── departments/                # Department management
│   │   ├── chat/                       # Chat functionality
│   │   ├── email/                      # Email management
│   │   ├── notifications/              # Notification system
│   │   └── permissions/                # Permission management
│   ├── hooks/                          # Global custom React hooks
│   ├── services/                       # API and external services
│   │   ├── api.ts                      # Base API client
│   │   ├── websocket.ts                # WebSocket client
│   │   └── auth.ts                     # Authentication service
│   ├── stores/                         # Zustand stores
│   │   ├── authStore.ts                # Authentication state
│   │   ├── uiStore.ts                  # UI state (theme, navigation)
│   │   └── notificationStore.ts        # Notification state
│   ├── types/                          # TypeScript type definitions
│   │   ├── api.ts                      # API response types
│   │   ├── auth.ts                     # Authentication types
│   │   └── entities.ts                 # Entity types
│   ├── utils/                          # Utility functions
│   ├── constants/                      # Application constants
│   └── assets/                         # Static assets (images, icons)
├── package.json                        # Node.js dependencies
├── vite.config.ts                      # Vite configuration
├── tsconfig.json                       # TypeScript configuration
└── vitest.config.ts                    # Testing configuration
```

## Package Conventions

### Backend (Spring Boot)
- **Base package**: `com.example.demo`
- **Feature-based organization**: Each major feature (security, department, employee, position, communication, payroll) has its own package
- **Layered architecture within features**: Each feature contains entity, repository, service, controller, dto, and exception packages
- **Security module**: Complete authentication and authorization system with JWT, roles, and permissions
- **Communication module**: Subdivided into email, chat, notification, and announcement submodules
- **Common package**: Contains foundational infrastructure components and utility classes. It provides standardized responses (`dto`), centralized exception handling (`exception`), and common operations (`util`) shared across all features.
- **Configuration**: Application config in `config` package with feature-specific configurations
- **Resources**: Application properties, email templates, and Flyway database migration scripts

### Frontend (React)
- **Feature-based organization**: Group related components, hooks, and services by feature
- **Component hierarchy**: UI components → Feature components → Layout components
- **Type definitions**: Centralized in `types/` with feature-specific types in feature folders
- **Service layer**: API clients and external service integrations
- **State management**: Separate stores for different concerns (auth, UI, notifications)

## Configuration Files

### Backend
- `application.properties` - Main configuration
- `application-{profile}.properties` - Environment-specific configs
- `db/migration/*.sql` - Flyway database migration files

### Frontend
- `package.json` - Dependencies and scripts
- `vite.config.ts` - Build tool configuration
- `tsconfig.json` - TypeScript compiler options
- `.env` files - Environment variables

## Build Artifacts
- **Backend**: `target/` - Maven build output, WAR file for deployment
- **Frontend**: `dist/` - Vite build output, optimized static files

## Development Guidelines

### Backend
- Keep the main application class minimal - only for bootstrapping
- Use `@SpringBootApplication` annotation on the main class
- Organize code by feature/domain with layered architecture within each feature
- Each feature module should be self-contained with its own entities, repositories, services, controllers, DTOs, and exceptions
- Use Maven commands: `mvn` (not `./mvnw` wrapper)
- Follow the established package structure with feature-based organization
- Implement proper separation of concerns with clear boundaries between modules
- Use Redis for data persistence with proper entity annotations (@RedisHash, @Indexed)
- Implement comprehensive exception handling with feature-specific exceptions
- Use Lombok annotations to reduce boilerplate code
- Follow consistent naming conventions across all modules
- Utilize `common` package utilities for consistency:
    - Use standardized `ApiResponse` for all controller methods.
    - Use `ValidationUtil` for business-specific validations.
    - Use `CacheUtil` for Redis cache operations.
    - Use `DateUtil` for all date and time manipulations.

### Frontend
- Use feature-based organization for scalability
- Implement component-driven development with Storybook
- Follow TypeScript strict mode for type safety
- Use functional components with hooks
- Implement proper error boundaries and loading states
- Ensure accessibility compliance (WCAG 2.1)
- Write comprehensive tests for all components and features