# Hybrid Development Environment Startup Guide

This guide provides step-by-step instructions for starting the Employee Management System in hybrid development mode (PostgreSQL and Redis in Docker, Spring Boot locally).

## Prerequisites

1. **Docker Desktop** - Running with PostgreSQL and Redis containers
2. **Java 24** - Installed and configured
3. **Maven** - For building and running the Spring Boot application
4. **PowerShell** - For running environment setup scripts (Windows)

## Database Setup Verification

First, ensure your Docker containers are running:

```bash
# Check if containers are running
docker ps

# You should see containers for:
# - PostgreSQL (port 5432)
# - Redis (port 6379)
```

If containers are not running, start them:

```bash
# Start the database services
docker-compose up -d postgres redis
```

## Environment Variables Setup

The application requires specific environment variables to connect to the databases. Use the provided PowerShell script:

### Step 1: Set Environment Variables

```powershell
# Run the setup script to set environment variables
.\setup-dev-db.ps1
```

This script sets the following variables:
- `DB_HOST=localhost`
- `DB_PORT=5432`
- `DB_NAME=employee_management`
- `DB_USERNAME=employee_admin`
- `DB_PASSWORD=dev_password123`
- `REDIS_HOST=localhost`
- `REDIS_PORT=6379`
- `REDIS_DB=1`
- `JWT_SECRET=dev-jwt-secret-key-for-testing-only-change-in-production`
- `ENCRYPTION_KEY=dev-encryption-key-for-testing-only-change-in-production`

### Step 2: Verify Environment Variables

```powershell
# Verify the variables are set correctly
echo $env:DB_HOST
echo $env:DB_USERNAME
echo $env:DB_PASSWORD
```

## Database User Setup

Ensure the PostgreSQL user exists with the correct password:

```bash
# Connect to PostgreSQL container
docker exec -it <postgres-container-name> psql -U postgres

# Create user and database (if not exists)
CREATE USER employee_admin WITH PASSWORD 'dev_password123';
CREATE DATABASE employee_management OWNER employee_admin;
GRANT ALL PRIVILEGES ON DATABASE employee_management TO employee_admin;
\q
```

## Starting the Application

### Method 1: Using Maven (Recommended)

```bash
# Clean and compile
mvn clean compile

# Run with hybrid profile (environment variables must be set first)
mvn spring-boot:run -Dspring-boot.run.profiles=hybrid
```

### Method 2: Using JAR file

```bash
# Build the JAR
mvn clean package -DskipTests

# Run the JAR with hybrid profile
java -Dspring.profiles.active=hybrid -jar target/demo-0.0.1-SNAPSHOT.jar
```

## Complete Startup Sequence

Here's the complete sequence to start the application:

```powershell
# 1. Ensure Docker containers are running
docker-compose up -d postgres redis

# 2. Set environment variables
.\setup-dev-db.ps1

# 3. Verify database connection (optional)
.\test-db-connection.bat

# 4. Start the Spring Boot application
mvn spring-boot:run -Dspring-boot.run.profiles=hybrid
```

## Verification

Once the application starts successfully, you should see:

```
2024-XX-XX XX:XX:XX - Started DemoApplication in X.XXX seconds
```

### Test the API

```bash
# Test health endpoint
curl http://localhost:8080/api/actuator/health

# Expected response:
# {"status":"UP","components":{"db":{"status":"UP"},"redis":{"status":"UP"}}}
```

## Troubleshooting

### Common Issues and Solutions

#### 1. Password Authentication Failed
**Error:** `FATAL: password authentication failed for user "employee_admin"`

**Solution:**
- Ensure you ran `.\setup-dev-db.ps1` before starting the application
- Verify the database user exists with the correct password
- Check that environment variables are set correctly

#### 2. Connection Refused
**Error:** `Connection refused` or `Connection timeout`

**Solution:**
- Ensure Docker containers are running: `docker ps`
- Check if ports 5432 (PostgreSQL) and 6379 (Redis) are available
- Verify container network connectivity

#### 3. Environment Variables Not Set
**Error:** Application uses default values instead of environment variables

**Solution:**
- Run `.\setup-dev-db.ps1` in the same PowerShell session where you start the application
- Verify variables are set: `echo $env:DB_PASSWORD`
- Consider using an IDE that preserves environment variables

#### 4. Flyway Migration Issues
**Error:** Flyway validation or migration errors

**Solution:**
- Ensure the database is empty or has compatible schema
- Check migration scripts in `src/main/resources/db/migration/`
- Use `spring.jpa.hibernate.ddl-auto=update` for development

## Development Workflow

For daily development, use this simplified workflow:

```powershell
# Daily startup (assuming Docker containers are already running)
.\setup-dev-db.ps1
mvn spring-boot:run -Dspring-boot.run.profiles=hybrid
```

## Configuration Files

The hybrid profile uses these configuration files:
- `application.properties` - Base configuration
- `application-hybrid.properties` - Hybrid-specific overrides
- `.env.hybrid` - Environment variable reference (not loaded by Java)

## Next Steps

After successful startup:
1. Access the API at `http://localhost:8080/api`
2. Check health endpoints at `http://localhost:8080/api/actuator/health`
3. Start the frontend development server (if needed)
4. Begin development with hot reload enabled

## Notes

- Environment variables are session-specific in PowerShell
- The application will use fallback values if environment variables are not set
- For production deployment, use proper environment variable management
- Consider using an IDE plugin for environment variable management