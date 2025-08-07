@echo off
echo 测试数据库连接...
echo 正在检查PostgreSQL容器状态...
docker ps -f name=postgres-dev

echo.
echo 测试PostgreSQL连接...
docker exec postgres-dev psql -U employee_admin -d employee_management -c "SELECT '连接成功' as status, version() as db_version;"

echo.
echo 测试端口映射...
docker port postgres-dev

echo.
echo 尝试使用psql从主机连接...
docker run --rm --network host postgres:15-alpine psql -h localhost -U employee_admin -d employee_management -c "SELECT '主机连接成功' as status;"

echo.
echo 检查应用配置...
echo 数据库URL: jdbc:postgresql://localhost:5432/employee_management
echo 用户名: employee_admin
echo 密码: dev_password123

pause