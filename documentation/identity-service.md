# Identity Service Documentation

## Tổng quan

Identity Service là service quản lý xác thực và phân quyền trong hệ thống. Nó xử lý việc đăng ký, đăng nhập, quản lý token JWT và phân quyền người dùng.

## Chức năng chính

- **User Registration**: Đăng ký tài khoản người dùng mới
- **User Authentication**: Xác thực người dùng thông qua login
- **JWT Token Management**: Tạo và quản lý JWT tokens
- **User Management**: CRUD operations cho user accounts
- **Role-based Authorization**: Phân quyền dựa trên roles
- **Password Management**: Reset password, change password
- **Session Management**: Quản lý phiên đăng nhập
- **Saga Pattern**: Xử lý distributed transactions

## Công nghệ sử dụng

- **Spring Boot** - Framework chính
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - Database access
- **Spring Cloud Netflix Eureka Client** - Service discovery
- **Spring Cloud Config Client** - Configuration management
- **Spring Kafka** - Event messaging
- **Redis** - Caching
- **MySQL** - Primary database
- **JWT** - Token-based authentication

## Cấu trúc project

```
identity-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/nlu/app/
│   │   │       ├── IdentityServiceApplication.java
│   │   │       ├── controller/
│   │   │       │   ├── AuthController.java
│   │   │       │   ├── UserController.java
│   │   │       │   └── AdminController.java
│   │   │       ├── service/
│   │   │       │   ├── AuthService.java
│   │   │       │   ├── UserService.java
│   │   │       │   ├── JwtService.java
│   │   │       │   └── EmailService.java
│   │   │       ├── entity/
│   │   │       │   ├── User.java
│   │   │       │   ├── Role.java
│   │   │       │   └── Token.java
│   │   │       ├── repository/
│   │   │       │   ├── UserRepository.java
│   │   │       │   ├── RoleRepository.java
│   │   │       │   └── TokenRepository.java
│   │   │       ├── dto/
│   │   │       │   ├── LoginRequest.java
│   │   │       │   ├── RegisterRequest.java
│   │   │       │   └── UserResponse.java
│   │   │       ├── mapper/
│   │   │       │   └── UserMapper.java
│   │   │       ├── configuration/
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   ├── JwtConfig.java
│   │   │       │   └── KafkaConfig.java
│   │   │       ├── saga/
│   │   │       │   └── UserRegistrationSaga.java
│   │   │       ├── constant/
│   │   │       │   └── SecurityConstants.java
│   │   │       └── exception/
│   │   │           ├── UserNotFoundException.java
│   │   │           └── AuthenticationException.java
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
│       └── java/
│           └── com/nlu/app/
│               └── IdentityServiceApplicationTests.java
├── env/
│   └── mysql.env
├── pom.xml
└── mvnw
```

## Cấu hình

### application.yaml

```yaml
server:
  port: 0

logging:
  level:
    springframework:
      security: DEBUG

spring:
  cache:
    type: redis
    redis:
      host: localhost
      port: 6379
      cache-null-values: true
      time-to-live:
        jwtToken: 30000000
  application:
    name: identity-service
  servlet:
    context-path: /identity
  profiles:
    active: default
  config:
    import: optional:configserver:http://cloud-config-service
  cloud:
    config:
      discovery:
        enabled: true
        service-id: cloud-config-service
  kafka:
    consumer:
      bootstrap-servers: localhost:19092,localhost:29092,localhost:39092
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      properties:
        spring:
          json:
            trusted:
              packages: "*"
      enable-auto-commit: false
    listener:
      ack-mode: manual
  jpa:
    hibernate:
      ddl-auto: update

eureka:
  client:
    server-url:
      defaultZone: http://localhost:8761/eureka/
    fetch-registry: true
    register-with-eureka: true
  instance:
    prefer-ip-address: true

profile-service:
  domain: lb://profile-service
notification-service:
  domain: lb://notification-service
aggregator-service:
  domain: lb://aggregator-service
```

## API Endpoints

### 1. Authentication Endpoints

#### POST /identity/auth/register

- **Description**: Đăng ký tài khoản mới
- **Request Body**:

```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "firstName": "string",
  "lastName": "string"
}
```

- **Response**:

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "userId": "string",
    "username": "string",
    "email": "string"
  }
}
```

#### POST /identity/auth/login

- **Description**: Đăng nhập
- **Request Body**:

```json
{
  "username": "string",
  "password": "string"
}
```

- **Response**:

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "string",
    "refreshToken": "string",
    "user": {
      "id": "string",
      "username": "string",
      "email": "string",
      "roles": ["string"]
    }
  }
}
```

#### POST /identity/auth/refresh

- **Description**: Refresh JWT token
- **Request Body**:

```json
{
  "refreshToken": "string"
}
```

- **Response**:

```json
{
  "success": true,
  "data": {
    "token": "string",
    "refreshToken": "string"
  }
}
```

#### POST /identity/auth/logout

- **Description**: Đăng xuất
- **Headers**: `Authorization: Bearer <token>`
- **Response**:

```json
{
  "success": true,
  "message": "Logout successful"
}
```

### 2. User Management Endpoints

#### GET /identity/users/{userId}

- **Description**: Lấy thông tin user
- **Headers**: `Authorization: Bearer <token>`
- **Response**:

```json
{
  "success": true,
  "data": {
    "id": "string",
    "username": "string",
    "email": "string",
    "firstName": "string",
    "lastName": "string",
    "roles": ["string"],
    "enabled": true,
    "createdAt": "datetime"
  }
}
```

#### PUT /identity/users/{userId}

- **Description**: Cập nhật thông tin user (ADMIN only)
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:

```json
{
  "firstName": "string",
  "lastName": "string",
  "enabled": true,
  "roles": ["string"]
}
```

#### DELETE /identity/users/{userId}

- **Description**: Xóa user (ADMIN only)
- **Headers**: `Authorization: Bearer <token>`

#### GET /identity/users/search/id

- **Description**: Tìm kiếm user theo ID
- **Headers**: `Authorization: Bearer <token>`
- **Query Parameters**: `id=string`

### 3. Password Management Endpoints

#### POST /identity/auth/forgot-password

- **Description**: Gửi email reset password
- **Request Body**:

```json
{
  "email": "string"
}
```

#### POST /identity/auth/reset-password

- **Description**: Reset password với token
- **Request Body**:

```json
{
  "token": "string",
  "newPassword": "string"
}
```

#### POST /identity/auth/change-password

- **Description**: Đổi password
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:

```json
{
  "currentPassword": "string",
  "newPassword": "string"
}
```

## Database Schema

### User Entity

```sql
CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Role Entity

```sql
CREATE TABLE roles (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);
```

### User_Role Entity

```sql
CREATE TABLE user_roles (
    user_id VARCHAR(255),
    role_id VARCHAR(255),
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

### Token Entity

```sql
CREATE TABLE tokens (
    id VARCHAR(255) PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    token_type ENUM('ACCESS', 'REFRESH') NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    expired BOOLEAN DEFAULT FALSE,
    user_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## Authentication Flow

### 1. Registration Flow:

1. Client gửi registration request
2. Validate input data
3. Check username/email uniqueness
4. Hash password
5. Create user account
6. Send welcome email
7. Create profile (via Saga)
8. Return success response

### 2. Login Flow:

1. Client gửi login credentials
2. Validate username/password
3. Generate JWT access token
4. Generate refresh token
5. Store tokens in database
6. Return tokens to client

### 3. Token Validation Flow:

1. Extract token from Authorization header
2. Validate token signature
3. Check token expiration
4. Check token in database (revoked/expired)
5. Extract user information
6. Set security context

## Saga Pattern Implementation

### User Registration Saga:

1. **Start**: User registration request
2. **Create User**: Create user in Identity Service
3. **Create Profile**: Create profile in Profile Service
4. **Send Notification**: Send welcome notification
5. **Compensation**: Rollback if any step fails

## Security Features

### 1. Password Security:

- BCrypt hashing
- Password complexity validation
- Password history tracking
- Account lockout after failed attempts

### 2. Token Security:

- JWT with RSA signature
- Token expiration (access: 15min, refresh: 7 days)
- Token blacklisting
- Refresh token rotation

### 3. Rate Limiting:

- Login attempts per IP
- Registration attempts per IP
- Password reset requests

## Cách chạy

### Local Development

```bash
cd identity-service
mvn spring-boot:run
```

### Docker

```bash
# Build image
docker build -t identity-service .

# Run container
docker run -p 8081:8081 identity-service
```

## Health Check

### Endpoints:

- **Health**: `http://localhost:8081/identity/actuator/health`
- **Info**: `http://localhost:8081/identity/actuator/info`
- **Metrics**: `http://localhost:8081/identity/actuator/metrics`

## Monitoring

### Metrics:

- Login attempts (success/failure)
- Registration attempts
- Token generation/validation
- User creation/deletion
- Password reset requests

### Logs:

- Authentication events
- User management events
- Security events
- Error logs

## Troubleshooting

### Common Issues:

1. **Database connection failed**:

   - Kiểm tra MySQL service
   - Kiểm tra database credentials
   - Kiểm tra network connectivity

2. **Redis connection failed**:

   - Kiểm tra Redis service
   - Kiểm tra Redis configuration
   - Kiểm tra cache settings

3. **JWT token invalid**:

   - Kiểm tra JWT secret
   - Kiểm tra token expiration
   - Kiểm tra token signature

4. **Kafka connection failed**:
   - Kiểm tra Kafka brokers
   - Kiểm tra Zookeeper
   - Kiểm tra topic configuration

### Debug Mode:

```bash
# Enable debug logging
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.com.nlu.app=DEBUG"
```

## Dependencies

### Maven Dependencies:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-config</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
    </dependency>
</dependencies>
```

## Environment Variables

| Variable                     | Default                                         | Description               |
| ---------------------------- | ----------------------------------------------- | ------------------------- |
| `SERVER_PORT`                | 0                                               | Port của Identity Service |
| `SPRING_DATASOURCE_URL`      | jdbc:mysql://localhost:3307/identity            | Database URL              |
| `SPRING_DATASOURCE_USERNAME` | root                                            | Database username         |
| `SPRING_DATASOURCE_PASSWORD` | password                                        | Database password         |
| `JWT_SECRET`                 | -                                               | JWT secret key            |
| `JWT_EXPIRATION`             | 900000                                          | JWT expiration time (ms)  |
| `REDIS_HOST`                 | localhost                                       | Redis host                |
| `REDIS_PORT`                 | 6379                                            | Redis port                |
| `KAFKA_BOOTSTRAP_SERVERS`    | localhost:19092,localhost:29092,localhost:39092 | Kafka brokers             |
