# Employee Management System

A comprehensive employee management system built with Spring Boot, featuring user authentication, employee lifecycle management, department hierarchy, real-time communication, and payroll processing.

## 🚀 Features

### Core Functionality
- **User Authentication & Authorization**: JWT-based security with role-based access control
- **Employee Management**: Complete employee lifecycle with advanced search and filtering
- **Department Hierarchy**: Tree-structured department organization with path-based queries
- **Position Management**: Job titles, classifications, and position hierarchies
- **Real-time Communication**: WebSocket-powered chat system and notifications
- **Email System**: Template-based email communication with bulk sending capabilities
- **Payroll Processing**: Comprehensive payroll management and salary calculations
- **Import/Export**: Excel and CSV data import/export with validation
- **Audit Trail**: Complete audit logging for all operations

### Technical Features
- **Hybrid Database Architecture**: PostgreSQL for persistent data, Redis for caching and real-time features
- **RESTful APIs**: Comprehensive REST API with OpenAPI 3.0 documentation
- **Real-time Updates**: WebSocket integration for chat and notifications
- **Security**: JWT tokens, role-based permissions, and data encryption
- **Performance**: Redis caching, connection pooling, and optimized queries
- **Monitoring**: Spring Boot Actuator with health checks and metrics

## 🛠️ Technology Stack

### Backend
- **Java 24** - Programming language
- **Spring Boot 3.5.4** - Application framework
- **Maven** - Build system and dependency management
- **PostgreSQL 15.x** - Primary database for persistent data
- **Redis 7.x** - Cache layer and real-time features
- **JWT** - Authentication and authorization
- **WebSocket** - Real-time communication
- **Apache POI** - Excel import/export functionality
- **Flyway** - Database migration and versioning

### Development & Testing
- **Spring Boot DevTools** - Development-time features
- **JUnit 5** - Unit testing framework
- **Mockito** - Mocking framework
- **TestContainers** - Integration testing with real databases
- **Spring Security Test** - Security testing utilities

## 📋 Prerequisites

Before running the application, ensure you have the following installed:

- **Java 24** or higher
- **Maven 3.8+**
- **PostgreSQL 15.x**
- **Redis 7.x**
- **Git** (for cloning the repository)

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/your-org/employee-management-system.git
cd employee-management-system
```

### 2. Database Setup

#### PostgreSQL Setup
```bash
# Create database
createdb employee_management

# Create user (optional)
psql -c "CREATE USER employee_admin WITH PASSWORD 'your_password';"
psql -c "GRANT ALL PRIVILEGES ON DATABASE employee_management TO employee_admin;"
```

#### Redis Setup
```bash
# Start Redis server (varies by OS)
# Ubuntu/Debian
sudo systemctl start redis-server

# macOS with Homebrew
brew services start redis

# Windows
redis-server
```

### 3. Configuration

Copy the example configuration and update with your settings:

```bash
cp src/main/resources/application-dev.properties.example src/main/resources/application-dev.properties
```

Update the database connection settings in `application-dev.properties`:

```properties
# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/employee_management
spring.datasource.username=employee_admin
spring.datasource.password=your_password

# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379
```

#### Hybrid Environment Setup (WSL PostgreSQL + Docker Redis)

If you're running PostgreSQL in WSL and Redis in Docker, you can use the provided `application-hybrid.properties` configuration:

1. Get your WSL IP address:
   ```bash
   wsl hostname -I
   ```

2. Get your Docker Redis container IP:
   ```bash
   docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' redis-server
   ```

3. Update the `application-hybrid.properties` file with the correct IP addresses if needed.

4. Run the application with the hybrid profile:
   ```bash
   # Windows PowerShell
   $env:SPRING_PROFILES_ACTIVE="hybrid"; mvn spring-boot:run

   # Windows Command Prompt
   set SPRING_PROFILES_ACTIVE=hybrid && mvn spring-boot:run

   # Unix/Linux/macOS
   export SPRING_PROFILES_ACTIVE=hybrid && mvn spring-boot:run
   ```

### 4. Build and Run

```bash
# Build the application
mvn clean compile

# Run database migrations
mvn flyway:migrate

# Start the application
mvn spring-boot:run

# Or run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run with hybrid profile (WSL PostgreSQL + Docker Redis)
mvn spring-boot:run -Dspring-boot.run.profiles=hybrid
```

The application will start on `http://localhost:8080`

### 5. Access the Application

- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **API Base URL**: http://localhost:8080/api

## 📚 API Documentation

### Interactive Documentation
Visit http://localhost:8080/swagger-ui.html for interactive API documentation with:
- Complete endpoint documentation
- Request/response examples
- Authentication testing
- Schema definitions

### Authentication
Most endpoints require authentication. To get started:

1. **Login** to get a JWT token:
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "admin", "password": "admin123"}'
   ```

2. **Use the token** in subsequent requests:
   ```bash
   curl -X GET http://localhost:8080/api/employees \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

### Key Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/auth/login` | POST | User authentication |
| `/api/users` | GET | List users with pagination |
| `/api/employees` | GET | List employees with search |
| `/api/departments/tree` | GET | Department hierarchy |
| `/api/positions` | GET | Available positions |
| `/api/notifications` | GET | User notifications |

## 🔧 Development

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=EmployeeServiceTest

# Run integration tests
mvn test -Dtest=*IntegrationTest

# Generate test coverage report
mvn jacoco:report
```

### Database Migrations

```bash
# Run migrations
mvn flyway:migrate

# Check migration status
mvn flyway:info

# Clean database (development only)
mvn flyway:clean
```

### Development Profile

For development, use the `dev` profile which includes:
- Detailed logging
- H2 console access (if configured)
- Development-specific configurations

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/example/demo/
│   │   ├── common/              # Common utilities and DTOs
│   │   ├── config/              # Configuration classes
│   │   ├── security/            # Authentication and authorization
│   │   ├── department/          # Department management
│   │   ├── employee/            # Employee management
│   │   ├── position/            # Position management
│   │   ├── communication/       # Email, chat, notifications
│   │   ├── payroll/             # Payroll management
│   │   └── DemoApplication.java # Main application class
│   └── resources/
│       ├── db/migration/        # Flyway migration scripts
│       ├── templates/           # Email templates
│       └── application*.properties # Configuration files
└── test/
    └── java/com/example/demo/   # Test classes
```

## 🔒 Security

### Authentication
- JWT-based authentication with configurable expiration
- Secure password hashing with BCrypt
- Token blacklisting for secure logout

### Authorization
- Role-based access control (RBAC)
- Method-level security with @PreAuthorize
- Resource-based permissions

### Data Protection
- Sensitive data encryption at rest
- Input validation and sanitization
- SQL injection prevention with JPA

## 📊 Monitoring

### Health Checks
- Database connectivity
- Redis connectivity
- Application health status

### Metrics
- Custom business metrics
- JVM metrics
- HTTP request metrics

Access monitoring endpoints at:
- Health: `/actuator/health`
- Metrics: `/actuator/metrics`
- Info: `/actuator/info`

## 🚀 Deployment

### Production Build

```bash
# Build production JAR
mvn clean package -Pprod

# The JAR file will be in target/demo-0.0.1-SNAPSHOT.jar
```

### Environment Variables

Set the following environment variables for production:

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=your-db-host
export DB_PORT=5432
export DB_NAME=employee_management
export DB_USERNAME=your-username
export DB_PASSWORD=your-password
export REDIS_HOST=your-redis-host
export REDIS_PORT=6379
export JWT_SECRET=your-jwt-secret-key
```

### Docker Deployment (Optional)

```bash
# Build Docker image
docker build -t employee-management-system .

# Run with Docker Compose
docker-compose up -d
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Spring Boot conventions
- Write comprehensive tests
- Update documentation
- Follow security best practices

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support and questions:
- **Email**: support@employee-management.com
- **Documentation**: [API Documentation](http://localhost:8080/swagger-ui.html)
- **Issues**: [GitHub Issues](https://github.com/your-org/employee-management-system/issues)

## 🔄 Changelog

See [CHANGELOG.md](CHANGELOG.md) for a detailed list of changes and version history.