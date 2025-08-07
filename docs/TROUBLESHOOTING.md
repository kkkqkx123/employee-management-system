# Troubleshooting Guide

This guide helps you diagnose and resolve common issues with the Employee Management System.

## Table of Contents

1. [Common Startup Issues](#common-startup-issues)
2. [Database Connection Problems](#database-connection-problems)
3. [Authentication Issues](#authentication-issues)
4. [Performance Problems](#performance-problems)
5. [Email Configuration Issues](#email-configuration-issues)
6. [Redis Connection Issues](#redis-connection-issues)
7. [WebSocket Problems](#websocket-problems)
8. [Import/Export Issues](#importexport-issues)
9. [Logging and Debugging](#logging-and-debugging)
10. [Production Issues](#production-issues)

## Common Startup Issues

### Application Fails to Start

#### Symptom
```
Error starting ApplicationContext. To display the conditions report re-run your application with 'debug' enabled.
```

#### Diagnosis
1. Check the full stack trace in logs
2. Enable debug mode: `--debug` or `logging.level.org.springframework.boot.autoconfigure=DEBUG`
3. Verify all required dependencies are in classpath

#### Common Causes and Solutions

**Missing Database Driver**
```bash
# Error: Cannot load driver class: org.postgresql.Driver
# Solution: Ensure PostgreSQL driver is in pom.xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Port Already in Use**
```bash
# Error: Port 8080 was already in use
# Solution: Change port or kill process using the port
server.port=8081
# Or find and kill the process
lsof -ti:8080 | xargs kill -9
```

**Configuration Property Errors**
```bash
# Error: Binding to target failed
# Solution: Check property names and values in application.properties
# Enable configuration processor
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
</dependency>
```

### Bean Creation Failures

#### Symptom
```
Error creating bean with name 'dataSource': Bean instantiation via factory method failed
```

#### Solutions
1. **Check Database Configuration**
   ```properties
   # Verify these properties are correct
   spring.datasource.url=jdbc:postgresql://localhost:5432/employee_management
   spring.datasource.username=employee_admin
   spring.datasource.password=your_password
   ```

2. **Test Database Connection**
   ```bash
   # Test PostgreSQL connection
   psql -h localhost -p 5432 -U employee_admin -d employee_management
   ```

3. **Check Database Service**
   ```bash
   # Ubuntu/Debian
   sudo systemctl status postgresql
   sudo systemctl start postgresql
   
   # macOS
   brew services list | grep postgresql
   brew services start postgresql
   ```

## Database Connection Problems

### Connection Refused

#### Symptom
```
Connection to localhost:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
```

#### Solutions
1. **Verify PostgreSQL is Running**
   ```bash
   # Check if PostgreSQL is running
   sudo systemctl status postgresql
   
   # Start PostgreSQL if not running
   sudo systemctl start postgresql
   ```

2. **Check PostgreSQL Configuration**
   ```bash
   # Edit postgresql.conf
   sudo nano /etc/postgresql/15/main/postgresql.conf
   
   # Ensure these settings:
   listen_addresses = 'localhost'
   port = 5432
   ```

3. **Check pg_hba.conf**
   ```bash
   # Edit pg_hba.conf
   sudo nano /etc/postgresql/15/main/pg_hba.conf
   
   # Add or modify:
   local   all             all                                     md5
   host    all             all             127.0.0.1/32            md5
   ```

### Authentication Failed

#### Symptom
```
FATAL: password authentication failed for user "employee_admin"
```

#### Solutions
1. **Reset User Password**
   ```sql
   -- Connect as postgres user
   sudo -u postgres psql
   
   -- Reset password
   ALTER USER employee_admin PASSWORD 'new_password';
   ```

2. **Create User if Missing**
   ```sql
   -- Create user with proper permissions
   CREATE USER employee_admin WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE employee_management TO employee_admin;
   ```

### Database Does Not Exist

#### Symptom
```
FATAL: database "employee_management" does not exist
```

#### Solution
```bash
# Create database
sudo -u postgres createdb employee_management

# Or via SQL
sudo -u postgres psql
CREATE DATABASE employee_management;
GRANT ALL PRIVILEGES ON DATABASE employee_management TO employee_admin;
```

### Connection Pool Exhausted

#### Symptom
```
HikariPool-1 - Connection is not available, request timed out after 30000ms
```

#### Solutions
1. **Increase Pool Size**
   ```properties
   spring.datasource.hikari.maximum-pool-size=50
   spring.datasource.hikari.connection-timeout=60000
   ```

2. **Check for Connection Leaks**
   ```properties
   # Enable leak detection
   spring.datasource.hikari.leak-detection-threshold=60000
   ```

3. **Monitor Active Connections**
   ```sql
   -- Check active connections
   SELECT count(*) FROM pg_stat_activity WHERE state = 'active';
   
   -- Check connections by database
   SELECT datname, count(*) FROM pg_stat_activity GROUP BY datname;
   ```

## Authentication Issues

### JWT Token Problems

#### Invalid Token Error

**Symptom**
```json
{
  "success": false,
  "message": "Invalid JWT token"
}
```

**Solutions**
1. **Check Token Format**
   ```bash
   # Token should start with "Bearer "
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

2. **Verify Token Expiration**
   ```bash
   # Decode JWT token (use jwt.io or similar tool)
   # Check 'exp' claim
   ```

3. **Check JWT Secret**
   ```properties
   # Ensure JWT secret is consistent
   jwt.secret=${JWT_SECRET:your-secret-key}
   ```

#### Token Expired

**Symptom**
```json
{
  "success": false,
  "message": "JWT token has expired"
}
```

**Solutions**
1. **Refresh Token**
   ```bash
   curl -X POST http://localhost:8080/api/auth/refresh-token \
     -d "refreshToken=your-refresh-token"
   ```

2. **Adjust Token Expiration**
   ```properties
   # Increase token expiration (in milliseconds)
   jwt.expiration=86400000  # 24 hours
   ```

### Login Failures

#### Invalid Credentials

**Symptom**
```json
{
  "success": false,
  "message": "Invalid credentials"
}
```

**Solutions**
1. **Check User Exists**
   ```sql
   SELECT username, enabled FROM users WHERE username = 'your-username';
   ```

2. **Verify Password Encoding**
   ```java
   // Test password encoding
   BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
   boolean matches = encoder.matches("plainPassword", "encodedPassword");
   ```

3. **Check User Status**
   ```sql
   -- Ensure user is enabled
   UPDATE users SET enabled = true WHERE username = 'your-username';
   ```

## Performance Problems

### Slow Database Queries

#### Symptoms
- High response times
- Database connection timeouts
- High CPU usage on database server

#### Solutions
1. **Enable Query Logging**
   ```properties
   # Log slow queries
   spring.jpa.show-sql=true
   logging.level.org.hibernate.SQL=DEBUG
   logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
   ```

2. **Add Database Indexes**
   ```sql
   -- Common indexes for performance
   CREATE INDEX idx_employees_department_id ON employees(department_id);
   CREATE INDEX idx_employees_status ON employees(status);
   CREATE INDEX idx_employees_last_name ON employees(last_name);
   CREATE INDEX idx_users_username ON users(username);
   ```

3. **Optimize Queries**
   ```java
   // Use JOIN FETCH to avoid N+1 queries
   @Query("SELECT e FROM Employee e JOIN FETCH e.department WHERE e.status = :status")
   List<Employee> findByStatusWithDepartment(@Param("status") EmployeeStatus status);
   ```

### Memory Issues

#### High Memory Usage

**Symptoms**
- OutOfMemoryError
- Frequent garbage collection
- Application becomes unresponsive

**Solutions**
1. **Increase Heap Size**
   ```bash
   # Set JVM options
   export JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC"
   ```

2. **Monitor Memory Usage**
   ```bash
   # Check memory usage
   curl http://localhost:8080/actuator/metrics/jvm.memory.used
   ```

3. **Optimize Hibernate Settings**
   ```properties
   # Batch processing
   spring.jpa.properties.hibernate.jdbc.batch_size=50
   spring.jpa.properties.hibernate.order_inserts=true
   spring.jpa.properties.hibernate.order_updates=true
   ```

### Redis Performance Issues

#### Slow Redis Operations

**Solutions**
1. **Check Redis Memory Usage**
   ```bash
   redis-cli info memory
   ```

2. **Optimize Connection Pool**
   ```properties
   spring.redis.jedis.pool.max-active=20
   spring.redis.jedis.pool.max-idle=10
   spring.redis.jedis.pool.min-idle=5
   ```

3. **Monitor Redis Performance**
   ```bash
   # Monitor Redis commands
   redis-cli monitor
   
   # Check slow queries
   redis-cli slowlog get 10
   ```## E
mail Configuration Issues

### SMTP Connection Failed

#### Symptom
```
Mail server connection failed; nested exception is javax.mail.MessagingException: Could not connect to SMTP host
```

#### Solutions
1. **Verify SMTP Settings**
   ```properties
   # Check SMTP configuration
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-app-password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```

2. **Test SMTP Connection**
   ```bash
   # Test SMTP connection manually
   telnet smtp.gmail.com 587
   ```

3. **Check Firewall Settings**
   ```bash
   # Ensure SMTP ports are not blocked
   # Common SMTP ports: 25, 465, 587
   ```

### Authentication Failed

#### Symptom
```
AuthenticationFailedException: 535-5.7.8 Username and Password not accepted
```

#### Solutions
1. **Use App Passwords (Gmail)**
   - Enable 2-factor authentication
   - Generate app-specific password
   - Use app password instead of regular password

2. **Check Email Provider Settings**
   ```properties
   # Gmail
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   
   # Outlook
   spring.mail.host=smtp-mail.outlook.com
   spring.mail.port=587
   ```

### Email Not Sending

#### Symptom
- No error messages but emails not received
- Emails going to spam folder

#### Solutions
1. **Check Email Templates**
   ```bash
   # Verify template files exist
   ls -la src/main/resources/templates/email/
   ```

2. **Enable Email Logging**
   ```properties
   logging.level.org.springframework.mail=DEBUG
   ```

3. **Test Email Service**
   ```java
   @Autowired
   private EmailService emailService;
   
   // Test email sending
   emailService.sendSimpleEmail("test@example.com", "Test Subject", "Test Body");
   ```

## Redis Connection Issues

### Connection Refused

#### Symptom
```
Unable to connect to Redis; nested exception is io.lettuce.core.RedisConnectionException: Unable to connect to localhost:6379
```

#### Solutions
1. **Check Redis Service**
   ```bash
   # Check if Redis is running
   redis-cli ping
   
   # Start Redis if not running
   # Ubuntu/Debian
   sudo systemctl start redis-server
   
   # macOS
   brew services start redis
   
   # Windows
   redis-server
   ```

2. **Verify Redis Configuration**
   ```bash
   # Check Redis configuration
   redis-cli config get "*"
   
   # Check if Redis is listening on correct port
   netstat -tlnp | grep 6379
   ```

3. **Test Redis Connection**
   ```bash
   # Test Redis connection
   redis-cli -h localhost -p 6379 ping
   ```

### Redis Authentication Failed

#### Symptom
```
NOAUTH Authentication required
```

#### Solutions
1. **Set Redis Password**
   ```properties
   spring.redis.password=your-redis-password
   ```

2. **Configure Redis Auth**
   ```bash
   # In redis.conf
   requirepass your-redis-password
   
   # Restart Redis after configuration change
   sudo systemctl restart redis-server
   ```

### Redis Memory Issues

#### Symptom
```
OOM command not allowed when used memory > 'maxmemory'
```

#### Solutions
1. **Increase Redis Memory**
   ```bash
   # In redis.conf
   maxmemory 2gb
   maxmemory-policy allkeys-lru
   ```

2. **Monitor Redis Memory**
   ```bash
   redis-cli info memory
   ```

## WebSocket Problems

### WebSocket Connection Failed

#### Symptom
- WebSocket connections not establishing
- Real-time features not working
- Chat messages not being delivered

#### Solutions
1. **Check WebSocket Configuration**
   ```properties
   # Verify WebSocket settings
   websocket.allowed-origins=http://localhost:3000
   ```

2. **Test WebSocket Connection**
   ```javascript
   // Test WebSocket connection from browser console
   const socket = new WebSocket('ws://localhost:8080/ws');
   socket.onopen = () => console.log('Connected');
   socket.onerror = (error) => console.error('WebSocket error:', error);
   ```

3. **Check CORS Settings**
   ```java
   @Configuration
   @EnableWebSocketMessageBroker
   public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
       
       @Override
       public void registerStompEndpoints(StompEndpointRegistry registry) {
           registry.addEndpoint("/ws")
                   .setAllowedOrigins("http://localhost:3000")
                   .withSockJS();
       }
   }
   ```

### Message Broker Issues

#### Symptom
- Messages not being delivered to subscribers
- WebSocket connects but messages don't flow

#### Solutions
1. **Check Message Broker Configuration**
   ```properties
   # Verify broker settings
   spring.websocket.broker.application-destination-prefix=/app
   spring.websocket.broker.simple-broker=/topic,/queue
   ```

2. **Enable WebSocket Logging**
   ```properties
   logging.level.org.springframework.web.socket=DEBUG
   logging.level.org.springframework.messaging=DEBUG
   ```

## Import/Export Issues

### Excel Import Failures

#### Symptom
```
Failed to parse Excel file: Invalid header signature
```

#### Solutions
1. **Check File Format**
   - Ensure file is valid Excel format (.xlsx)
   - Verify file is not corrupted
   - Check file size limits

2. **Validate Excel Structure**
   ```java
   // Add validation for Excel structure
   if (!workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue().equals("Employee Number")) {
       throw new ImportException("Invalid Excel template");
   }
   ```

3. **Check File Upload Limits**
   ```properties
   # Increase file upload limits
   spring.servlet.multipart.max-file-size=10MB
   spring.servlet.multipart.max-request-size=10MB
   ```

### Data Validation Errors

#### Symptom
- Import succeeds but data is incorrect
- Validation errors during import

#### Solutions
1. **Enable Detailed Validation Logging**
   ```properties
   logging.level.com.example.demo.employee.service.EmployeeImportService=DEBUG
   ```

2. **Check Data Format**
   ```java
   // Validate date formats
   @DateTimeFormat(pattern = "yyyy-MM-dd")
   private LocalDate hireDate;
   
   // Validate email format
   @Email(message = "Invalid email format")
   private String email;
   ```

## Logging and Debugging

### Enable Debug Logging

#### Application Debug Mode
```bash
# Start with debug enabled
java -jar app.jar --debug

# Or set in application.properties
debug=true
```

#### Specific Package Logging
```properties
# Enable debug for specific packages
logging.level.com.example.demo=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

### Common Debug Scenarios

#### Database Query Debugging
```properties
# Show SQL queries
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Show parameter values
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

#### Security Debugging
```properties
# Debug security
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.web.filter.CommonsRequestLoggingFilter=DEBUG
```

#### WebSocket Debugging
```properties
# Debug WebSocket
logging.level.org.springframework.web.socket=DEBUG
logging.level.org.springframework.messaging=DEBUG
```

### Log Analysis

#### Common Log Patterns
```bash
# Find errors in logs
grep -i error logs/application.log

# Find specific exceptions
grep -i "NullPointerException" logs/application.log

# Monitor logs in real-time
tail -f logs/application.log

# Search for specific user actions
grep "user:john.doe" logs/application.log
```

## Production Issues

### High CPU Usage

#### Diagnosis
1. **Check JVM Metrics**
   ```bash
   curl http://localhost:8080/actuator/metrics/system.cpu.usage
   ```

2. **Analyze Thread Dumps**
   ```bash
   # Generate thread dump
   jstack <pid> > thread-dump.txt
   
   # Or use JVisualVM
   jvisualvm
   ```

3. **Monitor Database Queries**
   ```sql
   -- PostgreSQL: Check running queries
   SELECT pid, now() - pg_stat_activity.query_start AS duration, query 
   FROM pg_stat_activity 
   WHERE (now() - pg_stat_activity.query_start) > interval '5 minutes';
   ```

#### Solutions
1. **Optimize Database Queries**
2. **Add Caching**
3. **Scale Horizontally**

### Memory Leaks

#### Diagnosis
1. **Monitor Memory Usage**
   ```bash
   # Check heap usage
   curl http://localhost:8080/actuator/metrics/jvm.memory.used
   
   # Generate heap dump
   jmap -dump:format=b,file=heap-dump.hprof <pid>
   ```

2. **Analyze Heap Dump**
   - Use Eclipse MAT (Memory Analyzer Tool)
   - Look for objects with high retention

#### Solutions
1. **Fix Connection Leaks**
2. **Optimize Caching**
3. **Review Object Lifecycle**

### Database Connection Issues

#### Connection Pool Exhaustion
```properties
# Monitor connection pool
management.metrics.export.prometheus.enabled=true

# Check pool metrics
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
```

#### Solutions
1. **Increase Pool Size**
2. **Fix Connection Leaks**
3. **Optimize Query Performance**

### Getting Help

#### Collect Diagnostic Information
```bash
# System information
uname -a
java -version
mvn -version

# Application logs
tail -n 1000 logs/application.log

# JVM information
jinfo <pid>

# Thread dump
jstack <pid>

# Heap dump (if memory issues)
jmap -dump:format=b,file=heap.hprof <pid>
```

#### Health Check Endpoints
```bash
# Application health
curl http://localhost:8080/actuator/health

# Database health
curl http://localhost:8080/actuator/health/db

# Redis health
curl http://localhost:8080/actuator/health/redis
```

#### Contact Support
When contacting support, include:
1. Error messages and stack traces
2. Application logs
3. Configuration files (remove sensitive data)
4. Steps to reproduce the issue
5. Environment information

This troubleshooting guide covers the most common issues you might encounter. For issues not covered here, check the application logs and health endpoints for more specific error information.