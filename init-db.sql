-- 创建数据库
CREATE DATABASE employee_management;

-- 创建用户
CREATE USER employee_admin WITH PASSWORD 'admin123';

-- 授予权限
GRANT ALL PRIVILEGES ON DATABASE employee_management TO employee_admin;

-- 如果数据库已存在，可以直接运行以下命令：
-- ALTER USER employee_admin WITH PASSWORD 'admin123';