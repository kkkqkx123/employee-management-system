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
│   │       │   │   ├── Resource.java
│   │       │   │   ├── UserRole.java
│   │       │   │   └── RoleResource.java
│   │       │   ├── repository/
│   │       │   │   ├── UserRepository.java
│   │       │   │   ├── RoleRepository.java
│   │       │   │   ├── ResourceRepository.java
│   │       │   │   ├── UserRoleRepository.java
│   │       │   │   └── RoleResourceRepository.java
│   │       │   ├── service/
│   │       │   │   ├── UserService.java
│   │       │   │   ├── RoleService.java
│   │       │   │   ├── ResourceService.java
│   │       │   │   ├── AuthenticationService.java
│   │       │   │   └── PermissionService.java
│   │       │   ├── controller/
│   │       │   │   ├── AuthController.java
│   │       │   │   ├── UserController.java
│   │       │   │   ├── RoleController.java
│   │       │   │   └── ResourceController.java
│   │       │   ├── dto/
│   │       │   │   ├── LoginRequest.java
│   │       │   │   ├── LoginResponse.java
│   │       │   │   ├── UserDto.java
│   │       │   │   ├── RoleDto.java
│   │       │   │   └── ResourceDto.java
│   │       │   ├── security/
│   │       │   │   ├── JwtAuthenticationFilter.java
│   │       │   │   ├── JwtTokenProvider.java
│   │       │   │   ├── CustomUserDetailsService.java
│   │       │   │   └── SecurityUtils.java
│   │       │   └── exception/
│   │       │       ├── AuthenticationException.java
│   │       │       ├── AuthorizationException.java
│   │       │       └── UserNotFoundException.java
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
│   │       │   │   └── EmployeeStatus.java
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
│   │       │   │   │   └── EmailLog.java
│   │       │   │   ├── service/
│   │       │   │   │   ├── EmailService.java
│   │       │   │   │   ├── EmailTemplateService.java
│   │       │   │   │   └── impl/
│   │       │   │   │       ├── EmailServiceImpl.java
│   │       │   │   │       └── EmailTemplateServiceImpl.java
│   │       │   │   ├── controller/
│   │       │   │   │   └── EmailController.java
│   │       │   │   ├── dto/
│   │       │   │   │   ├── EmailRequest.java
│   │       │   │   │   ├── BulkEmailRequest.java
│   │       │   │   │   ├── EmailTemplateDto.java
│   │       │   │   │   └── EmailLogDto.java
│   │       │   │   └── util/
│   │       │   │       └── EmailTemplateProcessor.java
│   │       │   ├── chat/
│   │       │   │   ├── entity/
│   │       │   │   │   ├── ChatMessage.java
│   │       │   │   │   ├── ChatRoom.java
│   │       │   │   │   └── ChatParticipant.java
│   │       │   │   ├── service/
│   │       │   │   │   ├── ChatService.java
│   │       │   │   │   └── impl/
│   │       │   │   │       └── ChatServiceImpl.java
│   │       │   │   ├── controller/
│   │       │   │   │   └── ChatController.java
│   │       │   │   ├── dto/
│   │       │   │   │   ├── ChatMessageDto.java
│   │       │   │   │   ├── ChatRoomDto.java
│   │       │   │   │   └── ChatParticipantDto.java
│   │       │   │   └── websocket/
│   │       │   │       ├── ChatWebSocketHandler.java
│   │       │   │       └── WebSocketConfig.java
│   │       │   ├── notification/
│   │       │   │   ├── entity/
│   │       │   │   │   ├── MessageContent.java
│   │       │   │   │   └── SystemMessage.java
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
│   │       │   └── exception/
│   │       │       ├── EmailSendingException.java
│   │       │       ├── TemplateNotFoundException.java
│   │       │       ├── ChatRoomNotFoundException.java
│   │       │       └── NotificationException.java
│   │       ├── payroll/                 # Payroll Management
│   │       │   ├── entity/
│   │       │   │   ├── PayrollLedger.java
│   │       │   │   ├── PayrollPeriod.java
│   │       │   │   ├── SalaryComponent.java
│   │       │   │   └── PayrollAudit.java
│   │       │   ├── repository/
│   │       │   │   ├── PayrollLedgerRepository.java
│   │       │   │   ├── PayrollPeriodRepository.java
│   │       │   │   ├── SalaryComponentRepository.java
│   │       │   │   └── PayrollAuditRepository.java
│   │       │   ├── service/
│   │       │   │   ├── PayrollService.java
│   │       │   │   ├── PayrollCalculationService.java
│   │       │   │   ├── PayrollReportService.java
│   │       │   │   └── impl/
│   │       │   │       ├── PayrollServiceImpl.java
│   │       │   │       ├── PayrollCalculationServiceImpl.java
│   │       │   │       └── PayrollReportServiceImpl.java
│   │       │   ├── controller/
│   │       │   │   └── PayrollController.java
│   │       │   ├── dto/
│   │       │   │   ├── PayrollLedgerDto.java
│   │       │   │   ├── PayrollPeriodDto.java
│   │       │   │   ├── SalaryComponentDto.java
│   │       │   │   ├── PayrollCalculationRequest.java
│   │       │   │   ├── PayrollReportRequest.java
│   │       │   │   └── PayrollSummaryDto.java
│   │       │   ├── util/
│   │       │   │   ├── PayrollCalculationUtil.java
│   │       │   │   └── PayrollValidationUtil.java
│   │       │   └── exception/
│   │       │       ├── PayrollNotFoundException.java
│   │       │       ├── PayrollCalculationException.java
│   │       │       ├── PayrollPeriodException.java
│   │       │       └── PayrollValidationException.java
│   │       ├── config/                  # Application configuration
│   │       │   ├── RedisConfig.java
│   │       │   ├── WebSocketConfig.java
│   │       │   ├── AsyncConfig.java
│   │       │   └── CorsConfig.java
│   │       └── common/                  # Common utilities and shared components
│   │           ├── dto/
│   │           │   ├── ApiResponse.java
│   │           │   ├── ErrorResponse.java
│   │           │   └── PageResponse.java
│   │           ├── exception/
│   │           │   ├── GlobalExceptionHandler.java
│   │           │   ├── BusinessException.java
│   │           │   └── ValidationException.java
│   │           └── util/
│   │               ├── DateUtil.java
│   │               ├── StringUtil.java
│   │               └── ValidationUtil.java
│   └── resources/
│       ├── application.properties       # Application configuration
│       ├── application-dev.properties   # Development configuration
│       ├── application-prod.properties  # Production configuration
│       ├── static/                      # Static web assets (for React build)
│       └── templates/                   # Email templates (Freemarker .ftl files)
│           ├── welcome-email.ftl
│           ├── notification-email.ftl
│           └── payroll-summary.ftl
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
- **Communication module**: Subdivided into email, chat, and notification submodules
- **Common package**: Shared utilities, DTOs, and exception handling across all features
- **Configuration**: Application config in `config` package with feature-specific configurations
- **Resources**: Application properties, email templates, and static assets

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

### Frontend
- Use feature-based organization for scalability
- Implement component-driven development with Storybook
- Follow TypeScript strict mode for type safety
- Use functional components with hooks
- Implement proper error boundaries and loading states
- Ensure accessibility compliance (WCAG 2.1)
- Write comprehensive tests for all components and features