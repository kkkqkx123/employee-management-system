@echo off
echo URL:jdbc:postgresql://localhost:5432/employee_management
echo user:employee_admin
echo password:dev_password123

echo.
docker exec postgres-dev psql -U employee_admin -d employee_management -c "SELECT '连接成功' as status;"

echo.
echo 测试容器外部连接：
docker run --rm --network host postgres:15-alpine psql -h localhost -U employee_admin -d employee_management -c "SELECT '外部连接成功' as status;"

pause