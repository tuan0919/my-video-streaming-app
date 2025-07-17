# Cloud Config Service Documentation

## Tổng quan

Cloud Config Service là phiên bản nâng cao của Config Service, cung cấp centralized configuration management với các tính năng bổ sung như service discovery integration, load balancing và high availability.

## Chức năng chính

- **Centralized Configuration**: Quản lý cấu hình tập trung
- **Service Discovery Integration**: Tích hợp với Eureka
- **Load Balancing**: Cân bằng tải cho config requests
- **High Availability**: Đảm bảo tính sẵn sàng cao
- **Configuration Encryption**: Mã hóa cấu hình nhạy cảm
- **Configuration Monitoring**: Giám sát thay đổi cấu hình
- **Multi-Environment Support**: Hỗ trợ nhiều môi trường

## API Endpoints

### 1. Configuration Endpoints

#### GET /{application}/{profile}

- **Description**: Lấy cấu hình cho application và profile
- **Path Parameters**:
  - `application` (string): Tên application
  - `profile` (string): Environment profile
- **Response**: Configuration properties

#### GET /{application}/{profile}/{label}

- **Description**: Lấy cấu hình với label cụ thể
- **Path Parameters**:
  - `application` (string): Tên application
  - `profile` (string): Environment profile
  - `label` (string): Git branch/label

### 2. Service Discovery Endpoints

#### GET /service-instances/{serviceId}

- **Description**: Lấy danh sách instances của service
- **Path Parameters**:
  - `serviceId` (string): Service ID

#### GET /service-instances/{serviceId}/{instanceId}

- **Description**: Lấy thông tin instance cụ thể
- **Path Parameters**:
  - `serviceId` (string): Service ID
  - `instanceId` (string): Instance ID

### 3. Health Check Endpoints

#### GET /actuator/health

- **Description**: Kiểm tra sức khỏe service

#### GET /actuator/info

- **Description**: Thông tin service

## Configuration Structure

### Repository Structure:

```
config-repo/
├── application.yml
├── application-dev.yml
├── application-prod.yml
├── gateway-service.yml
├── gateway-service-dev.yml
├── gateway-service-prod.yml
├── identity-service.yml
├── identity-service-dev.yml
├── identity-service-prod.yml
└── ...
```

### Configuration Examples:

#### application.yml (Global Configuration)

```yaml
spring:
  profiles:
    active: default
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-repo/config-repo
          default-label: main
          search-paths: "{application}"

management:
  endpoints:
    web:
      exposure:
        include: health,info,configprops
```

#### gateway-service.yml

```yaml
server:
  port: 8989

spring:
  application:
    name: gateway-service
  cloud:
    gateway:
      routes:
        - id: identity-service
          uri: lb://identity-service
          predicates:
            - Path=/identity/**
        - id: video-streaming-service
          uri: lb://video-streaming-service
          predicates:
            - Path=/video-streaming/**
```

#### identity-service.yml

```yaml
server:
  port: 0

spring:
  application:
    name: identity-service
  datasource:
    url: jdbc:mysql://localhost:3307/identity
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
  jpa:
    hibernate:
      ddl-auto: update

jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: ${JWT_EXPIRATION:86400000}
```

## Service Discovery Integration

### 1. Eureka Integration:

- Config Service đăng ký với Eureka
- Các service khác tìm config service qua Eureka
- Load balancing tự động

### 2. Configuration Client Setup:

```yaml
spring:
  cloud:
    config:
      discovery:
        enabled: true
        service-id: cloud-config-service
      fail-fast: true
      retry:
        initial-interval: 1000
        max-interval: 2000
        max-attempts: 6
```

## Security Features

### 1. Configuration Encryption:

```yaml
# Encrypt sensitive data
spring:
  datasource:
    password: "{cipher}AQA..."
```

### 2. Access Control:

- Authentication cho config access
- Role-based authorization
- API key management

### 3. Audit Logging:

- Log tất cả config access
- Track configuration changes
- Monitor suspicious activities

## High Availability

### 1. Multiple Instances:

- Deploy nhiều config service instances
- Load balancing giữa các instances
- Automatic failover

### 2. Backup Strategy:

- Regular configuration backups
- Git repository backup
- Disaster recovery plan

### 3. Monitoring:

- Health checks
- Performance metrics
- Error tracking

## Configuration Refresh

### 1. Manual Refresh:

```bash
# Refresh configuration
curl -X POST http://localhost:8889/actuator/refresh
```

### 2. Automatic Refresh:

- Webhook integration với Git
- Scheduled refresh jobs
- Event-driven refresh

### 3. Conditional Refresh:

- Refresh based on conditions
- Selective refresh
- Rollback capabilities

## Cách chạy

```bash
cd cloud-config-service
mvn spring-boot:run
```

## Health Check

- **Health**: `http://localhost:8889/actuator/health`
- **Info**: `http://localhost:8889/actuator/info`
- **Config Props**: `http://localhost:8889/actuator/configprops`

## Dependencies

- Spring Boot Web
- Spring Cloud Config Server
- Spring Cloud Netflix Eureka Client
- Spring Boot Actuator
- Spring Security
- Git (for configuration storage)

## Environment Variables

| Variable                                       | Default | Description                       |
| ---------------------------------------------- | ------- | --------------------------------- |
| `SERVER_PORT`                                  | 8889    | Port của Cloud Config Service     |
| `SPRING_CLOUD_CONFIG_SERVER_GIT_URI`           | -       | Git repository URI                |
| `SPRING_CLOUD_CONFIG_SERVER_GIT_DEFAULT_LABEL` | main    | Default Git branch                |
| `ENCRYPT_KEY`                                  | -       | Encryption key for sensitive data |
