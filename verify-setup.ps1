# verify-setup.ps1 - Verify hybrid development setup

Write-Host "=== Hybrid Development Setup Verification ===" -ForegroundColor Green
Write-Host ""

# Check Docker
Write-Host "1. Checking Docker containers..." -ForegroundColor Yellow
$containers = docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
Write-Host $containers
Write-Host ""

# Check PostgreSQL container specifically
$postgresContainer = docker ps --filter "name=postgres" --format "{{.Names}}" | Select-Object -First 1
if (-not $postgresContainer) {
    $postgresContainer = docker ps --format "{{.Names}}" | Select-String -Pattern "postgres" | Select-Object -First 1
    if ($postgresContainer) {
        $postgresContainer = $postgresContainer.ToString()
    }
}
if ($postgresContainer) {
    Write-Host "PostgreSQL container found: $postgresContainer" -ForegroundColor Green
    
    # Test database connection
    Write-Host "Testing database connection..." -ForegroundColor Cyan
    $testResult = docker exec $postgresContainer psql -U employee_admin -d employee_management -c "SELECT current_user, current_database();" 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Database connection successful!" -ForegroundColor Green
        Write-Host $testResult
    } else {
        Write-Host "Database connection failed:" -ForegroundColor Red
        Write-Host $testResult
        Write-Host ""
        Write-Host "Run this to fix: .\init-db-user.ps1" -ForegroundColor Yellow
    }
} else {
    Write-Host "PostgreSQL container not found!" -ForegroundColor Red
    Write-Host "Run: docker-compose up -d postgres" -ForegroundColor Yellow
}

Write-Host ""

# Check Redis container
$redisContainer = docker ps --filter "name=redis" --format "{{.Names}}" | Select-Object -First 1
if (-not $redisContainer) {
    $redisContainer = docker ps --format "{{.Names}}" | Select-String -Pattern "redis" | Select-Object -First 1
    if ($redisContainer) {
        $redisContainer = $redisContainer.ToString()
    }
}
if ($redisContainer) {
    Write-Host "Redis container found: $redisContainer" -ForegroundColor Green
    
    # Test Redis connection
    Write-Host "Testing Redis connection..." -ForegroundColor Cyan
    $redisTest = docker exec $redisContainer redis-cli ping 2>&1
    
    if ($redisTest -eq "PONG") {
        Write-Host "Redis connection successful!" -ForegroundColor Green
    } else {
        Write-Host "Redis connection failed: $redisTest" -ForegroundColor Red
    }
} else {
    Write-Host "Redis container not found!" -ForegroundColor Red
    Write-Host "Run: docker-compose up -d redis" -ForegroundColor Yellow
}

Write-Host ""

# Check environment variables
Write-Host "2. Checking environment variables..." -ForegroundColor Yellow
$env:DB_HOST="localhost"
$env:DB_PORT="5432"
$env:DB_NAME="employee_management"
$env:DB_USERNAME="employee_admin"
$env:DB_PASSWORD="admin123"
$env:REDIS_HOST="localhost"
$env:REDIS_PORT="6379"

Write-Host "Environment variables set:" -ForegroundColor Green
Write-Host "  DB_HOST=$env:DB_HOST" -ForegroundColor Green
Write-Host "  DB_USERNAME=$env:DB_USERNAME" -ForegroundColor Green
Write-Host "  DB_PASSWORD=$env:DB_PASSWORD" -ForegroundColor Green
Write-Host "  REDIS_HOST=$env:REDIS_HOST" -ForegroundColor Green

Write-Host ""

# Check application configuration
Write-Host "3. Checking application configuration..." -ForegroundColor Yellow
if (Test-Path "src/main/resources/application-hybrid.properties") {
    Write-Host "application-hybrid.properties found" -ForegroundColor Green
} else {
    Write-Host "application-hybrid.properties NOT found!" -ForegroundColor Red
}

if (Test-Path "pom.xml") {
    Write-Host "pom.xml found" -ForegroundColor Green
} else {
    Write-Host "pom.xml NOT found!" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== Verification Complete ===" -ForegroundColor Green
Write-Host ""
Write-Host "If all checks passed, run: .\start-hybrid.ps1" -ForegroundColor Cyan
Write-Host "If there are issues, run: .\init-db-user.ps1 first" -ForegroundColor Yellow