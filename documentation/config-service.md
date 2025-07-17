# Config Service Documentation

## Tổng quan

Config Service quản lý cấu hình cho các service trong hệ thống. Nó cung cấp centralized configuration management và hỗ trợ dynamic configuration updates.

## Chức năng chính

- **Configuration Management**: Quản lý cấu hình tập trung
- **Dynamic Updates**: Cập nhật cấu hình động
- **Environment Management**: Quản lý cấu hình theo environment
- **Configuration Validation**: Kiểm tra tính hợp lệ của cấu hình
- **Configuration History**: Lịch sử thay đổi cấu hình
- **Configuration Backup**: Backup và restore cấu hình

## API Endpoints

### 1. Configuration Management

#### GET /config/{application}/{profile}

- **Description**: Lấy cấu hình cho application và profile
- **Path Parameters**:
  - `application` (string): Tên application
  - `profile` (string): Environment profile
- **Response**: Configuration properties

#### POST /config/{application}/{profile}

- **Description**: Cập nhật cấu hình
- **Path Parameters**:
  - `application` (string): Tên application
  - `profile` (string): Environment profile
- **Request Body**: Configuration properties

#### DELETE /config/{application}/{profile}

- **Description**: Xóa cấu hình
- **Path Parameters**:
  - `application` (string): Tên application
  - `profile` (string): Environment profile

### 2. Configuration History

#### GET /config/{application}/{profile}/history

- **Description**: Lấy lịch sử thay đổi cấu hình
- **Query Parameters**:
  - `page` (int): Page number
  - `size` (int): Page size

#### GET /config/{application}/{profile}/version/{version}

- **Description**: Lấy cấu hình theo version
- **Path Parameters**:
  - `version` (string): Version number

### 3. Configuration Validation

#### POST /config/validate

- **Description**: Validate cấu hình
- **Request Body**: Configuration to validate
- **Response**: Validation results

## Configuration Structure

### Application Configuration Files:

```
configuration/
├── application-default.yml
├── gateway-service-default.yml
├── identity-service-default.yml
├── video-streaming-service-default.yml
├── comment-service-default.yml
├── notification-service-default.yml
├── profile-service-default.yml
└── socket-service-default.yml
```

### Configuration Properties:

```yaml
# Database Configuration
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/database
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

# Kafka Configuration
spring:
  kafka:
    bootstrap-servers: localhost:19092,localhost:29092,localhost:39092
    consumer:
      group-id: ${spring.application.name}
      auto-offset-reset: earliest

# Redis Configuration
spring:
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms

# JWT Configuration
jwt:
  secret: your-secret-key
  expiration: 86400000

# Service Configuration
service:
  timeout: 5000
  retry:
    max-attempts: 3
    backoff: 1000
```

## Configuration Management

### 1. Configuration Sources:

- **Git Repository**: Configuration stored in Git
- **File System**: Local configuration files
- **Database**: Configuration stored in database
- **Environment Variables**: Override configuration

### 2. Configuration Refresh:

- **Manual Refresh**: Trigger refresh via API
- **Automatic Refresh**: Scheduled refresh
- **Event-driven Refresh**: Refresh on configuration change

### 3. Configuration Encryption:

- **Sensitive Data**: Encrypt sensitive configuration
- **Key Management**: Secure key management
- **Decryption**: Automatic decryption at runtime

## Cách chạy

```bash
cd config-service
mvn spring-boot:run
```

## Health Check

- **Health**: `http://localhost:8888/actuator/health`
- **Info**: `http://localhost:8888/actuator/info`

## Dependencies

- Spring Boot Web
- Spring Cloud Config Server
- Spring Boot Actuator
- Git (for configuration storage)
