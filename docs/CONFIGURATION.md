# Configuration Guide

This document provides comprehensive information about configuring the Employee Management System for different environments and use cases.

## Table of Contents

1. [Configuration Files](#configuration-files)
2. [Database Configuration](#database-configuration)
3. [Security Configuration](#security-configuration)
4. [Email Configuration](#email-configuration)
5. [Redis Configuration](#redis-configuration)
6. [WebSocket Configuration](#websocket-configuration)
7. [Logging Configuration](#logging-configuration)
8. [Environment-Specific Configuration](#environment-specific-configuration)
9. [Performance Tuning](#performance-tuning)
10. [Monitoring Configuration](#monitoring-configuration)

## Configuration Files

The application uses Spring Boot's configuration system with multiple property files:

### Primary Configuration Files
- `application.properties` - Base configuration
- `application-dev.properties` - Development environment
- `application-test.properties` - Testing environment
- `application-prod.properties` - Production environment

### Configuration Hierarchy
```
application.properties (base)
├── application-dev.properties (development)
├── application-test.properties (testing)
└── application-prod.properties (production)
```

## Database Configuration

### PostgreSQL Configuration

#### Basic Settings
```properties
# Database Connection
spring.datasource.url=jdbc:postgresql://localhost:5432/employee_management
spring.datasource.username=employee_admin
spring.datasource.password=your_secure_password
spring.datasource.driver-class-name=org.postgresql.Driver

# Connection Pool Settings (HikariCP)
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.leak-detection-threshold=60000

# JPA/Hibernate Settings
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true
```

#### SSL Configuration
```properties
# SSL Database Connection (Production)
spring.datasource.url=jdbc:postgresql://db-host:5432/employee_management?sslmode=require
spring.datasource.hikari.data-source-properties.ssl=true
spring.datasource.hikari.data-source-properties.sslmode=require
```

#### Environment Variables
```bash
# Database configuration via environment variables
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=employee_management
export DB_USERNAME=employee_admin
export DB_PASSWORD=your_secure_password

# Use in application.properties
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:employee_management}
spring.datasource.username=${DB_USERNAME:employee_admin}
spring.datasource.password=${DB_PASSWORD:password}
```

### Flyway Migration Configuration

```properties
# Flyway Settings
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.validate-on-migrate=true
spring.flyway.out-of-order=false
spring.flyway.table=flyway_schema_history
spring.flyway.sql-migration-prefix=V
spring.flyway.sql-migration-separator=__
spring.flyway.sql-migration-suffixes=.sql
```

## Security Configuration

### JWT Configuration

```properties
# JWT Settings
jwt.secret=${JWT_SECRET:mySecretKey}
jwt.expiration=86400000
jwt.refresh-expiration=604800000
jwt.issuer=employee-management-system
jwt.audience=employee-management-users

# Security Headers
security.headers.frame-options=DENY
security.headers.content-type-options=nosniff
security.headers.xss-protection=1; mode=block
security.headers.referrer-policy=strict-origin-when-cross-origin
```

### Password Policy

```properties
# Password Requirements
security.password.min-length=8
security.password.require-uppercase=true
security.password.require-lowercase=true
security.password.require-numbers=true
security.password.require-special-chars=true
security.password.max-age-days=90
security.password.history-count=5
```

### Session Configuration

```properties
# Session Management
server.servlet.session.timeout=30m
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=strict
```

### CORS Configuration

```properties
# CORS Settings
cors.allowed-origins=http://localhost:3000,https://your-frontend-domain.com
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed-headers=*
cors.allow-credentials=true
cors.max-age=3600
```

## Email Configuration

### SMTP Configuration

```properties
# Email Settings
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com

# Email Templates
email.templates.path=classpath:templates/email/
email.from.address=noreply@company.com
email.from.name=Employee Management System
```

### Email Provider Examples

#### Gmail
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

#### Outlook/Hotmail
```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=your-email@outlook.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

#### SendGrid
```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=your-sendgrid-api-key
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## Redis Configuration

### Basic Redis Settings

```properties
# Redis Connection
spring.redis.host=${REDIS_HOST:localhost}
spring.redis.port=${REDIS_PORT:6379}
spring.redis.database=0
spring.redis.password=${REDIS_PASSWORD:}
spring.redis.timeout=2000ms

# Connection Pool (Jedis)
spring.redis.jedis.pool.max-active=8
spring.redis.jedis.pool.max-idle=8
spring.redis.jedis.pool.min-idle=0
spring.redis.jedis.pool.max-wait=-1ms

# Cache Configuration
spring.cache.type=redis
spring.cache.redis.time-to-live=600000
spring.cache.redis.cache-null-values=false
spring.cache.redis.key-prefix=emp_mgmt:
```

### Redis Cluster Configuration

```properties
# Redis Cluster
spring.redis.cluster.nodes=redis-node1:6379,redis-node2:6379,redis-node3:6379
spring.redis.cluster.max-redirects=3
spring.redis.lettuce.pool.max-active=8
spring.redis.lettuce.pool.max-idle=8
```

### Redis Sentinel Configuration

```properties
# Redis Sentinel
spring.redis.sentinel.master=mymaster
spring.redis.sentinel.nodes=sentinel1:26379,sentinel2:26379,sentinel3:26379
spring.redis.sentinel.password=sentinel-password
```## W
ebSocket Configuration

### Basic WebSocket Settings

```properties
# WebSocket Configuration
websocket.allowed-origins=http://localhost:3000,https://your-frontend-domain.com
websocket.message-size-limit=64000
websocket.send-buffer-size=512000
websocket.send-time-limit=20000

# STOMP Configuration
stomp.relay.host=localhost
stomp.relay.port=61613
stomp.relay.client-login=guest
stomp.relay.client-passcode=guest
stomp.relay.system-login=guest
stomp.relay.system-passcode=guest
```

### Message Broker Settings

```properties
# In-Memory Message Broker
spring.websocket.broker.application-destination-prefix=/app
spring.websocket.broker.user-destination-prefix=/user
spring.websocket.broker.simple-broker=/topic,/queue

# External Message Broker (RabbitMQ)
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
spring.websocket.broker.relay.host=localhost
spring.websocket.broker.relay.port=61613
```

## Logging Configuration

### Logback Configuration (logback-spring.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProfile name="dev">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
        <logger name="com.example.demo" level="DEBUG"/>
        <logger name="org.springframework.security" level="DEBUG"/>
    </springProfile>

    <springProfile name="prod">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>logs/employee-management.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>logs/employee-management.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
                <maxFileSize>100MB</maxFileSize>
                <maxHistory>30</maxHistory>
                <totalSizeCap>3GB</totalSizeCap>
            </rollingPolicy>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
</configuration>
```

### Application Properties Logging

```properties
# Logging Levels
logging.level.com.example.demo=INFO
logging.level.org.springframework.security=WARN
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Log File Configuration
logging.file.name=logs/employee-management.log
logging.file.max-size=100MB
logging.file.max-history=30
logging.file.total-size-cap=3GB

# Log Pattern
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

## Environment-Specific Configuration

### Development Environment (application-dev.properties)

```properties
# Development Profile
spring.profiles.active=dev

# Database - H2 for quick development
spring.datasource.url=jdbc:h2:mem:devdb
spring.datasource.driver-class-name=org.h2.Driver
spring.h2.console.enabled=true

# Or PostgreSQL for development
spring.datasource.url=jdbc:postgresql://localhost:5432/employee_management_dev
spring.datasource.username=dev_user
spring.datasource.password=dev_password

# JPA Settings
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Security - Relaxed for development
jwt.secret=dev-secret-key
security.password.min-length=4

# Email - Console output for development
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.username=
spring.mail.password=

# Redis - Local instance
spring.redis.host=localhost
spring.redis.port=6379

# Logging
logging.level.com.example.demo=DEBUG
logging.level.org.springframework.web=DEBUG
```

### Testing Environment (application-test.properties)

```properties
# Test Profile
spring.profiles.active=test

# In-memory database for tests
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop

# Disable external services
spring.mail.host=localhost
spring.mail.port=3025

# Test Redis
spring.redis.host=localhost
spring.redis.port=6370

# Disable security for tests
security.enabled=false

# Fast password encoding for tests
security.password.encoder=plain

# Logging
logging.level.com.example.demo=WARN
logging.level.org.springframework=WARN
```

### Production Environment (application-prod.properties)

```properties
# Production Profile
spring.profiles.active=prod

# Database - Production PostgreSQL
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?sslmode=require
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Connection Pool - Production settings
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# JPA - Production settings
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Security - Production settings
jwt.secret=${JWT_SECRET}
security.password.min-length=12

# Email - Production SMTP
spring.mail.host=${SMTP_HOST}
spring.mail.port=${SMTP_PORT}
spring.mail.username=${SMTP_USERNAME}
spring.mail.password=${SMTP_PASSWORD}

# Redis - Production cluster
spring.redis.cluster.nodes=${REDIS_CLUSTER_NODES}
spring.redis.password=${REDIS_PASSWORD}

# Server settings
server.port=${PORT:8080}
server.compression.enabled=true
server.compression.mime-types=text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json
server.compression.min-response-size=1024

# SSL Configuration
server.ssl.enabled=true
server.ssl.key-store=${SSL_KEYSTORE_PATH}
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12

# Logging
logging.level.com.example.demo=INFO
logging.level.org.springframework=WARN
logging.file.name=/var/log/employee-management/application.log
```

## Performance Tuning

### JVM Settings

```bash
# Production JVM settings
JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication"

# Development JVM settings
JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC"
```

### Database Performance

```properties
# Connection Pool Tuning
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.leak-detection-threshold=60000

# JPA Performance
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory
```

### Redis Performance

```properties
# Redis Connection Pool
spring.redis.jedis.pool.max-active=20
spring.redis.jedis.pool.max-idle=10
spring.redis.jedis.pool.min-idle=5
spring.redis.jedis.pool.max-wait=1000ms

# Cache Settings
spring.cache.redis.time-to-live=3600000
spring.cache.redis.cache-null-values=false
```

## Monitoring Configuration

### Actuator Configuration

```properties
# Actuator Endpoints
management.endpoints.web.exposure.include=health,info,metrics,prometheus,loggers
management.endpoint.health.show-details=when-authorized
management.endpoint.health.show-components=always

# Health Indicators
management.health.db.enabled=true
management.health.redis.enabled=true
management.health.mail.enabled=true
management.health.diskspace.enabled=true

# Metrics
management.metrics.export.prometheus.enabled=true
management.metrics.distribution.percentiles-histogram.http.server.requests=true
management.metrics.distribution.percentiles.http.server.requests=0.5,0.95,0.99
```

### Custom Health Indicators

```properties
# Custom Health Checks
health.check.database.timeout=5000
health.check.redis.timeout=3000
health.check.external-api.timeout=10000
```

## Environment Variables Reference

### Required Environment Variables

```bash
# Database
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=employee_management
export DB_USERNAME=employee_admin
export DB_PASSWORD=secure_password

# Redis
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=redis_password

# Security
export JWT_SECRET=your-256-bit-secret-key
export ENCRYPTION_KEY=your-encryption-key

# Email
export SMTP_HOST=smtp.gmail.com
export SMTP_PORT=587
export SMTP_USERNAME=your-email@gmail.com
export SMTP_PASSWORD=your-app-password

# SSL (Production)
export SSL_KEYSTORE_PATH=/path/to/keystore.p12
export SSL_KEYSTORE_PASSWORD=keystore_password
```

### Optional Environment Variables

```bash
# Application
export SERVER_PORT=8080
export SPRING_PROFILES_ACTIVE=prod

# Monitoring
export MANAGEMENT_PORT=8081

# Logging
export LOG_LEVEL=INFO
export LOG_FILE=/var/log/employee-management/app.log

# Performance
export JVM_OPTS="-Xms2g -Xmx4g"
```

## Configuration Validation

### Startup Validation

The application validates critical configuration on startup:

```properties
# Validation Settings
validation.startup.enabled=true
validation.startup.fail-fast=true
validation.database.connection-test=true
validation.redis.connection-test=true
validation.email.smtp-test=false
```

### Configuration Properties Validation

```java
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {
    
    @NotBlank
    @Size(min = 32)
    private String jwtSecret;
    
    @Min(1)
    @Max(86400)
    private int jwtExpiration = 3600;
    
    @Email
    private String fromEmail;
    
    // Getters and setters
}
```

This configuration guide provides comprehensive coverage of all configuration options available in the Employee Management System. Adjust these settings based on your specific environment and requirements.