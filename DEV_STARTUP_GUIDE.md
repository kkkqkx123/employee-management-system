# 开发环境启动指南

## 问题总结

当前项目启动失败主要有两个问题：

1. **日志配置错误**：Logback配置使用了不兼容的JSON编码器
2. **数据库连接问题**：PostgreSQL数据库用户认证失败

## 解决方案

### 1. 修复日志配置
✅ 已更新logback-spring.xml，移除了不兼容的JSON编码器配置
✅ 已更新logstash-logback-encoder版本到7.4

### 2. 设置数据库

#### 方案A：使用Docker快速启动（推荐）
```bash
# 启动PostgreSQL和Redis
docker-compose -f docker-compose.dev.yml up -d

# 检查服务状态
docker-compose -f docker-compose.dev.yml ps
```

#### 方案B：手动设置PostgreSQL
```bash
# 连接到PostgreSQL（假设已安装并运行）
psql -U postgres

# 运行初始化脚本
\i init-db.sql
```

### 3. 启动应用

#### 开发环境启动
```bash
# 跳过测试启动
mvn spring-boot:run -DskipTests

# 或者使用Maven包装器
./mvnw spring-boot:run -DskipTests
```

#### 指定环境启动
```bash
# 使用开发环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev -DskipTests

# 使用混合环境（需要设置环境变量）
mvn spring-boot:run -Dspring-boot.run.profiles=hybrid -DskipTests
```

### 4. 环境变量设置

如果使用混合环境(hybrid)，需要设置以下环境变量：

#### Windows (PowerShell)
```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="5432"
$env:DB_NAME="employee_management"
$env:DB_USERNAME="employee_admin"
$env:DB_PASSWORD="admin123"
$env:REDIS_HOST="localhost"
$env:REDIS_PORT="6379"
```

#### Linux/Mac
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=employee_management
export DB_USERNAME=employee_admin
export DB_PASSWORD=admin123
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

### 5. 验证启动

启动成功后，可以访问以下地址：

- **应用主页**: http://localhost:8080
- **Swagger文档**: http://localhost:8080/swagger-ui.html
- **Actuator健康检查**: http://localhost:8080/actuator/health

### 6. 常见问题

#### Q: 端口冲突
如果8080端口被占用，可以在application.properties中修改：
```properties
server.port=8081
```

#### Q: 数据库连接失败
检查PostgreSQL是否运行：
```bash
# Windows
netstat -an | findstr :5432

# Linux/Mac
netstat -an | grep :5432
```

#### Q: Redis连接失败
检查Redis是否运行：
```bash
# 使用docker-compose启动的Redis
docker-compose -f docker-compose.dev.yml logs redis
```

## 快速启动检查清单

- [ ] PostgreSQL已安装并运行
- [ ] Redis已安装并运行（或使用docker-compose）
- [ ] 数据库和用户已创建（运行init-db.sql）
- [ ] 应用配置文件已检查
- [ ] 端口未被占用

## 一键启动命令

```bash
# 完整的启动流程
cd d:\项目\Spring\employee-management-system

# 1. 启动依赖服务
docker-compose -f docker-compose.dev.yml up -d

# 2. 等待服务启动完成
sleep 10

# 3. 启动应用
mvn spring-boot:run -DskipTests
```