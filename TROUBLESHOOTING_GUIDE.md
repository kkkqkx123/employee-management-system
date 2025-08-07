# 员工管理系统 - 启动故障排除指南

## 问题分析总结

基于错误日志分析，应用启动失败主要有两个根本原因：

### 1. 数据库连接认证失败
- **错误信息**: `FATAL: password authentication failed for user "employee_admin"`
- **原因**: 环境变量未正确配置，数据库用户密码不匹配

### 2. 日志配置问题（已修复）
- **错误信息**: `ClassNotFoundException: net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder`
- **状态**: ✅ 已修复 - 移除了复杂的JSON编码器配置

## 解决方案步骤

### 步骤1: 安装和启动PostgreSQL

#### Windows系统：
1. **下载PostgreSQL**:
   - 访问: https://www.postgresql.org/download/windows/
   - 下载并安装PostgreSQL 15+版本

2. **启动PostgreSQL服务**:
   ```powershell
   # 检查PostgreSQL服务状态
   Get-Service postgresql*
   
   # 启动服务（如果未运行）
   Start-Service postgresql-x64-15
   ```

3. **验证连接**:
   ```powershell
   # 使用psql连接（默认密码是安装时设置的）
   psql -U postgres -h localhost
   ```

#### 使用Docker（推荐）：
```powershell
# 启动PostgreSQL容器
docker run -d \
  --name postgres-dev \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=employee_management \
  -e POSTGRES_USER=employee_admin \
  -e POSTGRES_PASSWORD=dev_password123 \
  -p 5432:5432 \
  postgres:15-alpine
```

### 步骤2: 初始化数据库

#### 方法A: 使用批处理脚本（Windows）
```powershell
# 运行批处理脚本
.\setup-dev-db.bat
```

#### 方法B: 使用SQL脚本
```powershell
# 使用psql执行SQL脚本
psql -U postgres -h localhost -f setup-dev-db.sql
```

#### 方法C: 手动执行SQL命令
```sql
-- 连接到PostgreSQL (作为postgres用户)
CREATE DATABASE employee_management;
CREATE USER employee_admin WITH PASSWORD 'dev_password123';
GRANT ALL PRIVILEGES ON DATABASE employee_management TO employee_admin;
```

### 步骤3: 验证环境配置

确保以下文件存在且配置正确：

#### ✅ `.env.hybrid` 文件（已创建）
```properties
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=employee_management
DB_USERNAME=employee_admin
DB_PASSWORD=dev_password123

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DB=1
```

### 步骤4: 启动应用

#### 使用Maven（推荐）：
```powershell
# 跳过测试启动（开发环境）
mvn spring-boot:run -DskipTests

# 指定配置文件启动
mvn spring-boot:run -Dspring-boot.run.profiles=hybrid
```

#### 使用IDE:
1. 在IDE中设置环境变量：
   - `SPRING_PROFILES_ACTIVE=hybrid`
2. 直接运行 `DemoApplication.java`

### 步骤5: 验证启动

#### 检查应用状态：
```powershell
# 检查健康端点
curl http://localhost:8080/api/actuator/health

# 检查应用信息
curl http://localhost:8080/api/actuator/info
```

#### 查看日志：
```powershell
# 实时查看日志
tail -f logs/employee-management.log

# 查看错误日志
type logs/error.log
```

## 常见问题解决

### 问题1: PostgreSQL连接被拒绝
```powershell
# 检查PostgreSQL监听地址
# 编辑 postgresql.conf (通常在 C:\Program Files\PostgreSQL\15\data\)
# 确保：
listen_addresses = '*'

# 编辑 pg_hba.conf
# 添加：
host    all             all             127.0.0.1/32            md5
host    all             all             ::1/128                 md5
```

### 问题2: Redis连接失败
```powershell
# 启动Redis（如果使用Docker）
docker run -d --name redis-dev -p 6379:6379 redis:7-alpine

# 或启动本地Redis
redis-server
```

### 问题3: 端口冲突
```powershell
# 检查端口占用
netstat -ano | findstr :8080
netstat -ano | findstr :5432
netstat -ano | findstr :6379

# 杀掉占用进程
taskkill /PID <PID> /F
```

### 问题4: 权限问题
```powershell
# 确保logs目录可写
mkdir logs
icacls logs /grant Everyone:F
```

## 开发环境快速启动

### 一键启动（推荐）：
```powershell
# 1. 启动PostgreSQL和Redis
# 2. 初始化数据库
# 3. 启动应用

# 启动数据库（Docker方式）
docker-compose -f docker-compose.dev.yml up -d

# 等待数据库启动
Start-Sleep -Seconds 10

# 启动应用
mvn spring-boot:run -DskipTests
```

### 验证测试：
```powershell
# 测试数据库连接
psql -h localhost -U employee_admin -d employee_management -c "SELECT 1"

# 测试Redis连接
redis-cli ping

# 测试应用端点
curl http://localhost:8080/api/actuator/health
```

## 技术支持

如果遇到其他问题：

1. **检查错误日志**: `logs/error.log`
2. **查看完整日志**: `logs/employee-management.log`
3. **验证配置**: 确保`.env.hybrid`文件存在且正确
4. **数据库状态**: 确认PostgreSQL运行且用户存在

## 成功启动标志

应用成功启动后，你应该看到：
```
Tomcat started on port(s): 8080 (http)
Started DemoApplication in X.XXX seconds
```

访问 http://localhost:8080/api/actuator/health 应该返回：
```json
{"status":"UP"}
```