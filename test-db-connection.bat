@echo off
echo Testing database connection for Hybrid Environment...
echo.
echo Environment Variables:
echo DB_HOST=%DB_HOST%
echo DB_PORT=%DB_PORT%
echo DB_NAME=%DB_NAME%
echo DB_USERNAME=%DB_USERNAME%
echo DB_PASSWORD=%DB_PASSWORD%
echo.
echo Expected Values:
echo DB_HOST=localhost
echo DB_PORT=5432
echo DB_NAME=employee_management
echo DB_USERNAME=employee_admin
echo DB_PASSWORD=dev_password123
echo.
echo Testing PostgreSQL connection...
echo Note: Make sure PostgreSQL container is running with: docker-compose up -d postgres
echo.
docker exec -it postgres-dev psql -U employee_admin -d employee_management -c "SELECT 'Connection successful' as status, current_database(), current_user;"
echo.
echo If connection fails, ensure:
echo 1. Docker container is running: docker ps
echo 2. Environment variables are set: .\setup-dev-db.ps1
echo 3. Database user exists with correct password
echo.
pause