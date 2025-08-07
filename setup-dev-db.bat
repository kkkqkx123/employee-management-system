@echo off
echo Setting up development database for Employee Management System...
echo.

REM Check if PostgreSQL is running
psql -U postgres -c "SELECT 1" >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: PostgreSQL is not running or not accessible!
    echo Please start PostgreSQL service first.
    pause
    exit /b 1
)

echo Creating database...
psql -U postgres -c "CREATE DATABASE employee_management;"

if %errorlevel% neq 0 (
    echo ERROR: Failed to create database!
    pause
    exit /b 1
)

echo Creating user...
psql -U postgres -c "CREATE USER employee_admin WITH PASSWORD 'dev_password123';"

if %errorlevel% neq 0 (
    echo User might already exist, altering password...
    psql -U postgres -c "ALTER USER employee_admin WITH PASSWORD 'dev_password123';"
)

echo Granting privileges...
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE employee_management TO employee_admin;"
psql -U postgres -c "GRANT ALL PRIVILEGES ON SCHEMA public TO employee_admin;"
psql -U postgres -c "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO employee_admin;"

echo.
echo Database setup completed successfully!
echo Database: employee_management
echo User: employee_admin
echo Password: dev_password123
echo.
echo You can now run: mvn spring-boot:run
echo.
pause