# start-hybrid.ps1 - Complete Hybrid Environment Startup Script
# This script starts the Employee Management System in hybrid development mode

Write-Host "=== Employee Management System - Hybrid Startup ===" -ForegroundColor Green
Write-Host ""

# Step 1: Check Docker
Write-Host "Step 1: Checking Docker containers..." -ForegroundColor Yellow
$containers = docker ps --format "table {{.Names}}\t{{.Status}}" | Select-String -Pattern "(postgres|redis)"

if ($containers.Count -eq 0) {
    Write-Host "No PostgreSQL or Redis containers found. Starting them..." -ForegroundColor Red
    Write-Host "Running: docker-compose up -d postgres redis" -ForegroundColor Cyan
    docker-compose up -d postgres redis
    
    Write-Host "Waiting for containers to start..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
} else {
    Write-Host "Found running containers:" -ForegroundColor Green
    $containers | ForEach-Object { Write-Host "  $_" -ForegroundColor Green }
}

Write-Host ""

# Step 2: Set Environment Variables
Write-Host "Step 2: Setting environment variables..." -ForegroundColor Yellow
$env:DB_HOST="localhost"
$env:DB_PORT="5432"
$env:DB_NAME="employee_management"
$env:DB_USERNAME="employee_admin_new"
$env:DB_PASSWORD="admin123"
$env:REDIS_HOST="localhost"
$env:REDIS_PORT="6379"
$env:REDIS_DB="1"
$env:JWT_SECRET="dev-jwt-secret-key-for-testing-only-change-in-production"
$env:ENCRYPTION_KEY="dev-encryption-key-for-testing-only-change-in-production"

Write-Host "Environment variables set:" -ForegroundColor Green
Write-Host "  DB_HOST=$env:DB_HOST" -ForegroundColor Green
Write-Host "  DB_USERNAME=$env:DB_USERNAME" -ForegroundColor Green
Write-Host "  DB_PASSWORD=$env:DB_PASSWORD" -ForegroundColor Green
Write-Host "  REDIS_HOST=$env:REDIS_HOST" -ForegroundColor Green

Write-Host ""

# Step 3: Initialize Database User
Write-Host "Step 3: Checking database user..." -ForegroundColor Yellow

# Get the actual container name - try multiple methods
$containerName = docker ps --filter "name=postgres" --format "{{.Names}}" | Select-Object -First 1
if (-not $containerName) {
    $containerName = docker ps --format "{{.Names}}" | Select-String -Pattern "postgres" | Select-Object -First 1
    if ($containerName) {
        $containerName = $containerName.ToString()
    }
}

Write-Host "Looking for PostgreSQL container..." -ForegroundColor Cyan
Write-Host "Found container: $containerName" -ForegroundColor Green

if ($containerName) {
    Write-Host "Testing database connection..." -ForegroundColor Cyan
    $testResult = docker exec $containerName psql -U employee_admin -d employee_management -c "SELECT 'Connection test successful' as status;" 2>&1
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Database user not found or password incorrect. Initializing..." -ForegroundColor Yellow
        
        # Create/update user and database
        $sqlCommands = @"
DO `$`$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = 'employee_admin') THEN
        CREATE USER employee_admin WITH PASSWORD 'admin123';
    ELSE
        ALTER USER employee_admin WITH PASSWORD 'admin123';
    END IF;
END
`$`$;

SELECT 'CREATE DATABASE employee_management OWNER employee_admin'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'employee_management')\gexec

GRANT ALL PRIVILEGES ON DATABASE employee_management TO employee_admin;
GRANT CREATE ON SCHEMA public TO employee_admin;
"@
        
        $sqlCommands | docker exec -i $containerName psql -U postgres
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Database user initialized successfully!" -ForegroundColor Green
        } else {
            Write-Host "Failed to initialize database user. Please run: .\init-db-user.ps1" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "Database connection successful!" -ForegroundColor Green
    }
} else {
    Write-Host "PostgreSQL container not found. Please check your Docker setup." -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 4: Start Spring Boot Application
Write-Host "Step 4: Starting Spring Boot application..." -ForegroundColor Yellow
Write-Host "Running: mvn spring-boot:run -Dspring-boot.run.profiles=hybrid" -ForegroundColor Cyan
Write-Host ""
Write-Host "The application will start with the following configuration:" -ForegroundColor Cyan
Write-Host "  - Profile: hybrid" -ForegroundColor Cyan
Write-Host "  - Database: PostgreSQL (Docker)" -ForegroundColor Cyan
Write-Host "  - Cache: Redis (Docker)" -ForegroundColor Cyan
Write-Host "  - Port: 8080" -ForegroundColor Cyan
Write-Host "  - Context Path: /api" -ForegroundColor Cyan
Write-Host ""
Write-Host "Once started, you can access:" -ForegroundColor Cyan
Write-Host "  - Health Check: http://localhost:8080/api/actuator/health" -ForegroundColor Cyan
Write-Host "  - API Base: http://localhost:8080/api" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press Ctrl+C to stop the application when needed." -ForegroundColor Yellow
Write-Host ""

# Start the application
mvn spring-boot:run "-Dspring-boot.run.profiles=hybrid"