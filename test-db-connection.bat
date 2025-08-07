@echo off
echo Testing database connection...
echo.
echo Environment Variables:
echo DB_HOST=%DB_HOST%
echo DB_PORT=%DB_PORT%
echo DB_NAME=%DB_NAME%
echo DB_USERNAME=%DB_USERNAME%
echo DB_PASSWORD=%DB_PASSWORD%
echo.
echo Testing PostgreSQL connection...
docker exec postgres-dev psql -U employee_admin -d employee_management -c "SELECT 'Connection successful' as status;"
echo.
echo Testing from application.properties...
echo spring.datasource.url=jdbc:postgresql://localhost:5432/employee_management
echo spring.datasource.username=employee_admin
echo spring.datasource.password=dev_password123
echo.
pause