# init-db-user.ps1 - Initialize database user for hybrid development

Write-Host "=== Database User Initialization ===" -ForegroundColor Green
Write-Host ""

# Check if PostgreSQL container is running
Write-Host "Checking PostgreSQL container..." -ForegroundColor Yellow
$postgresContainer = docker ps --filter "name=postgres" --format "{{.Names}}" | Select-Object -First 1
if (-not $postgresContainer) {
    $postgresContainer = docker ps --format "{{.Names}}" | Select-String -Pattern "postgres" | Select-Object -First 1
    if ($postgresContainer) {
        $postgresContainer = $postgresContainer.ToString()
    }
}

if (-not $postgresContainer) {
    Write-Host "PostgreSQL container not found. Starting it..." -ForegroundColor Red
    docker-compose up -d postgres
    Write-Host "Waiting for PostgreSQL to start..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
}

# Get the actual container name
$containerName = docker ps --filter "name=postgres" --format "{{.Names}}" | Select-Object -First 1
if (-not $containerName) {
    $containerName = docker ps --format "{{.Names}}" | Select-String -Pattern "postgres" | Select-Object -First 1
    if ($containerName) {
        $containerName = $containerName.ToString()
    }
}

if (-not $containerName) {
    Write-Host "Could not find PostgreSQL container. Please check your docker-compose setup." -ForegroundColor Red
    exit 1
}

Write-Host "Found PostgreSQL container: $containerName" -ForegroundColor Green
Write-Host ""

# Create database and user
Write-Host "Creating database and user..." -ForegroundColor Yellow
$sqlCommands = @"
-- Create user if not exists
DO `$`$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = 'employee_admin') THEN
        CREATE USER employee_admin WITH PASSWORD 'admin123';
    ELSE
        ALTER USER employee_admin WITH PASSWORD 'admin123';
    END IF;
END
`$`$;

-- Create database if not exists
SELECT 'CREATE DATABASE employee_management OWNER employee_admin'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'employee_management')\gexec

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE employee_management TO employee_admin;
GRANT CREATE ON SCHEMA public TO employee_admin;

-- Show result
SELECT 'Database setup completed successfully' as status;
"@

# Execute SQL commands
Write-Host "Executing SQL commands..." -ForegroundColor Cyan
$sqlCommands | docker exec -i $containerName psql -U postgres

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "Database user setup completed successfully!" -ForegroundColor Green
    Write-Host "User: employee_admin" -ForegroundColor Green
    Write-Host "Password: admin123" -ForegroundColor Green
    Write-Host "Database: employee_management" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "Database setup failed. Please check the error messages above." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Testing connection..." -ForegroundColor Yellow
$testResult = docker exec $containerName psql -U employee_admin -d employee_management -c "SELECT 'Connection test successful' as status;" 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "Connection test passed!" -ForegroundColor Green
    Write-Host $testResult
} else {
    Write-Host "Connection test failed:" -ForegroundColor Red
    Write-Host $testResult
}

Write-Host ""
Write-Host "You can now run: .\start-hybrid.ps1" -ForegroundColor Cyan