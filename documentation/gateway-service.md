# Gateway Service Documentation

## Tổng quan

Gateway Service là API Gateway của hệ thống, đóng vai trò là điểm vào duy nhất cho tất cả các request từ client. Nó xử lý routing, authentication, authorization, load balancing và các cross-cutting concerns khác.

## Chức năng chính

- **API Routing**: Định tuyến request đến các service phù hợp
- **Authentication & Authorization**: Xác thực và phân quyền người dùng
- **Load Balancing**: Cân bằng tải giữa các service instances
- **Rate Limiting**: Giới hạn số lượng request
- **Request/Response Transformation**: Chuyển đổi format request/response
- **CORS Handling**: Xử lý Cross-Origin Resource Sharing
- **Circuit Breaker**: Bảo vệ hệ thống khi service bị lỗi

## Công nghệ sử dụng

- **Spring Boot** - Framework chính
- **Spring Cloud Gateway** - API Gateway framework
- **Spring Security** - Authentication & Authorization
- **Spring Cloud Netflix Eureka Client** - Service discovery
- **Spring Cloud Config Client** - Configuration management

## Cấu trúc project

```
gateway-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/nlu/app/
│   │   │       ├── GatewayServiceApplication.java
│   │   │       ├── configuration/
│   │   │       │   ├── GatewayConfig.java
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   └── CorsConfig.java
│   │   │       ├── filter/
│   │   │       │   ├── AuthenticationFilter.java
│   │   │       │   └── AuthorizationFilter.java
│   │   │       ├── route/
│   │   │       │   └── RouteConfig.java
│   │   │       ├── service/
│   │   │       │   └── TokenService.java
│   │   │       ├── repository/
│   │   │       │   └── TokenRepository.java
│   │   │       ├── dto/
│   │   │       │   └── AuthRequest.java
│   │   │       └── exception/
│   │   │           ├── GatewayException.java
│   │   │           └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
│       └── java/
│           └── com/nlu/app/
│               └── GatewayServiceApplicationTests.java
├── pom.xml
└── mvnw
```

## Cấu hình

### application.yaml

```yaml
server:
  port: 8989

spring:
  application:
    name: gateway-service
  profiles:
    active: default
  config:
    import: optional:configserver:http://cloud-config-service
  cloud:
    config:
      discovery:
        enabled: true
        service-id: cloud-config-service

gateway-service:
  routes-authorization:
    - path: /identity/users/*
      method: ["PUT", "DELETE"]
      roles: ["ROLE_ADMIN"]
    - path: /video-streaming/videos
      method: ["POST"]
      roles: ["ROLE_USER", "ROLE_ADMIN"]
    # ... các route khác

eureka:
  client:
    server-url:
      defaultZone: http://localhost:8761/eureka/
    fetch-registry: true
    register-with-eureka: true
  instance:
    prefer-ip-address: true
```

## API Routes

### 1. Identity Service Routes

- **Base Path**: `/identity`
- **Endpoints**:
  - `GET /identity/users/*` - Lấy thông tin user
  - `POST /identity/auth/login` - Đăng nhập
  - `POST /identity/auth/register` - Đăng ký
  - `PUT /identity/users/*` - Cập nhật user (ADMIN only)
  - `DELETE /identity/users/*` - Xóa user (ADMIN only)

### 2. Video Streaming Service Routes

- **Base Path**: `/video-streaming`
- **Endpoints**:
  - `GET /video-streaming/videos` - Lấy danh sách video
  - `POST /video-streaming/videos` - Tạo video mới
  - `PUT /video-streaming/videos/upload` - Upload video
  - `POST /video-streaming/videos/up-vote` - Upvote video
  - `POST /video-streaming/videos/down-vote` - Downvote video
  - `POST /video-streaming/videos/progress` - Cập nhật progress
  - `GET /video-streaming/videos/link/*` - Lấy link video

### 3. Comment Service Routes

- **Base Path**: `/comment`
- **Endpoints**:
  - `GET /comment/users/video/*` - Lấy comments của video
  - `POST /comment/users/new` - Tạo comment mới
  - `POST /comment/users/comment/*/interact` - Tương tác với comment
  - `GET /comment/users/comment/*` - Lấy thông tin comment

### 4. Profile Service Routes

- **Base Path**: `/profile`
- **Endpoints**:
  - `GET /profile/users/*` - Lấy thông tin profile
  - `PUT /profile/users/follow` - Follow user
  - `PUT /profile/users/avatar` - Cập nhật avatar
  - `PUT /profile/users/info` - Cập nhật thông tin
  - `POST /profile/users/save/video` - Lưu video

### 5. Notification Service Routes

- **Base Path**: `/notification`
- **Endpoints**:
  - `GET /notification/users` - Lấy notifications
  - `POST /notification/users/count` - Đếm notifications
  - `PUT /notification/users/read` - Đánh dấu đã đọc

### 6. Aggregator Service Routes

- **Base Path**: `/aggregator`
- **Endpoints**:
  - `GET /aggregator/query/video/*` - Query video data
  - `GET /aggregator/query/search/video` - Tìm kiếm video
  - `GET /aggregator/query/search/user` - Tìm kiếm user
  - `GET /aggregator/query/user/saved` - Lấy saved videos
  - `GET /aggregator/query/video/*/comments` - Lấy comments của video

### 7. Socket Service Routes

- **Base Path**: `/socket`
- **Endpoints**:
  - `GET /socket/websocket` - WebSocket connection

## Authentication & Authorization

### JWT Token Flow:

1. Client gửi credentials đến `/identity/auth/login`
2. Identity Service xác thực và trả về JWT token
3. Client sử dụng token trong header `Authorization: Bearer <token>`
4. Gateway validate token và extract user information
5. Gateway kiểm tra authorization dựa trên roles

### Role-based Authorization:

- **ROLE_USER**: Người dùng thông thường
- **ROLE_ADMIN**: Quản trị viên
- **ROLE_ANONYMOUS**: Người dùng chưa đăng nhập

## Filters

### 1. Authentication Filter

- Validate JWT token
- Extract user information
- Set security context

### 2. Authorization Filter

- Kiểm tra quyền truy cập
- Validate roles
- Block unauthorized requests

### 3. CORS Filter

- Handle Cross-Origin requests
- Configure allowed origins
- Handle preflight requests

## Cách chạy

### Local Development

```bash
cd gateway-service
mvn spring-boot:run
```

### Docker

```bash
# Build image
docker build -t gateway-service .

# Run container
docker run -p 8989:8989 gateway-service
```

## Health Check

### Endpoints:

- **Health**: `http://localhost:8989/actuator/health`
- **Info**: `http://localhost:8989/actuator/info`
- **Metrics**: `http://localhost:8989/actuator/metrics`
- **Routes**: `http://localhost:8989/actuator/gateway/routes`

## Monitoring

### Metrics:

- Request count per route
- Response time
- Error rate
- Active connections
- Circuit breaker status

### Logs:

- Access logs
- Error logs
- Authentication logs
- Authorization logs

## Troubleshooting

### Common Issues:

1. **Service không tìm thấy**:

   - Kiểm tra Eureka Server
   - Kiểm tra service registration
   - Kiểm tra service discovery

2. **Authentication failed**:

   - Kiểm tra JWT token
   - Kiểm tra token expiration
   - Kiểm tra token signature

3. **Authorization failed**:

   - Kiểm tra user roles
   - Kiểm tra route configuration
   - Kiểm tra permission mapping

4. **CORS issues**:
   - Kiểm tra CORS configuration
   - Kiểm tra allowed origins
   - Kiểm tra preflight requests

### Debug Mode:

```bash
# Enable debug logging
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.org.springframework.cloud.gateway=DEBUG"
```

## Security Considerations

1. **HTTPS**: Sử dụng HTTPS trong production
2. **Token Security**: Implement token refresh mechanism
3. **Rate Limiting**: Implement rate limiting per user/IP
4. **Input Validation**: Validate all input parameters
5. **Logging**: Log security events
6. **Monitoring**: Monitor for suspicious activities

## Performance Optimization

1. **Caching**: Cache frequently accessed data
2. **Connection Pooling**: Optimize connection pools
3. **Circuit Breaker**: Implement circuit breaker pattern
4. **Load Balancing**: Use load balancing strategies
5. **Compression**: Enable response compression

## Dependencies

### Maven Dependencies:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
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
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt</artifactId>
    </dependency>
</dependencies>
```

## Environment Variables

| Variable                                | Default                       | Description              |
| --------------------------------------- | ----------------------------- | ------------------------ |
| `SERVER_PORT`                           | 8989                          | Port của Gateway Service |
| `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE` | http://localhost:8761/eureka/ | Eureka Server URL        |
| `SPRING_PROFILES_ACTIVE`                | default                       | Active profile           |
| `JWT_SECRET`                            | -                             | JWT secret key           |
| `JWT_EXPIRATION`                        | 86400000                      | JWT expiration time (ms) |
