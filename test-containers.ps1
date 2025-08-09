# test-containers.ps1 - Test container detection

Write-Host "=== Container Detection Test ===" -ForegroundColor Green
Write-Host ""

Write-Host "All running containers:" -ForegroundColor Yellow
docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Status}}"
Write-Host ""

# Test PostgreSQL container detection
Write-Host "Testing PostgreSQL container detection..." -ForegroundColor Yellow

$postgresContainer = docker ps --filter "name=postgres" --format "{{.Names}}" | Select-Object -First 1
Write-Host "Method 1 (filter name=postgres): $postgresContainer" -ForegroundColor Cyan

if (-not $postgresContainer) {
    $postgresContainer = docker ps --format "{{.Names}}" | Select-String -Pattern "postgres" | Select-Object -First 1
    if ($postgresContainer) {
        $postgresContainer = $postgresContainer.ToString()
        Write-Host "Method 2 (pattern match): $postgresContainer" -ForegroundColor Cyan
    }
}

if ($postgresContainer) {
    Write-Host "PostgreSQL container found: $postgresContainer" -ForegroundColor Green
    
    # Test connection
    Write-Host "Testing connection to PostgreSQL..." -ForegroundColor Cyan
    $testResult = docker exec $postgresContainer psql -U postgres -c "SELECT version();" 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "PostgreSQL is accessible!" -ForegroundColor Green
    } else {
        Write-Host "PostgreSQL connection failed: $testResult" -ForegroundColor Red
    }
} else {
    Write-Host "PostgreSQL container NOT found!" -ForegroundColor Red
}

Write-Host ""

# Test Redis container detection
Write-Host "Testing Redis container detection..." -ForegroundColor Yellow

$redisContainer = docker ps --filter "name=redis" --format "{{.Names}}" | Select-Object -First 1
Write-Host "Method 1 (filter name=redis): $redisContainer" -ForegroundColor Cyan

if (-not $redisContainer) {
    $redisContainer = docker ps --format "{{.Names}}" | Select-String -Pattern "redis" | Select-Object -First 1
    if ($redisContainer) {
        $redisContainer = $redisContainer.ToString()
        Write-Host "Method 2 (pattern match): $redisContainer" -ForegroundColor Cyan
    }
}

if ($redisContainer) {
    Write-Host "Redis container found: $redisContainer" -ForegroundColor Green
    
    # Test connection
    Write-Host "Testing connection to Redis..." -ForegroundColor Cyan
    $redisTest = docker exec $redisContainer redis-cli ping 2>&1
    
    if ($redisTest -eq "PONG") {
        Write-Host "Redis is accessible!" -ForegroundColor Green
    } else {
        Write-Host "Redis connection failed: $redisTest" -ForegroundColor Red
    }
} else {
    Write-Host "Redis container NOT found!" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== Test Complete ===" -ForegroundColor Green