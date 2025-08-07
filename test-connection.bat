@echo off
echo 测试数据库连接...
echo 使用配置：
echo URL: jdbc:postgresql://localhost:5432/employee_management
echo 用户: employee_admin
echo 密码: dev_password123

echo.
echo 测试容器内部连接：
docker exec postgres-dev psql -U employee_admin -d employee_management -c "SELECT '连接成功' as status;"

echo.
echo 测试容器外部连接：
docker run --rm --network host postgres:15-alpine psql -h localhost -U employee_admin -d employee_management -c "SELECT '外部连接成功' as status;"

pause