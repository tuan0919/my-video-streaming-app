# Service Discovery Documentation

## Tổng quan

Service Discovery là một Eureka Server được sử dụng để quản lý service discovery trong hệ thống microservices. Nó cho phép các service đăng ký và tìm kiếm lẫn nhau một cách động.

## Chức năng chính

- **Service Registration**: Cho phép các service đăng ký với Eureka Server
- **Service Discovery**: Cho phép các service tìm kiếm và kết nối với nhau
- **Health Monitoring**: Theo dõi trạng thái sức khỏe của các service
- **Load Balancing**: Hỗ trợ load balancing cho các service instances

## Công nghệ sử dụng

- **Spring Boot** - Framework chính
- **Spring Cloud Netflix Eureka Server** - Service discovery server
- **Spring Web** - REST API endpoints

## Cấu trúc project

```
service-discovery/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/nlu/app/
│   │   │       ├── ServiceDiscoveryApplication.java
│   │   │       └── HomeController.java
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
│       └── java/
│           └── com/nlu/app/
│               └── ServiceDiscoveryApplicationTests.java
├── pom.xml
└── mvnw
```

## Cấu hình

### application.yaml

```yaml
server:
  port: 8761

spring:
  application:
    name: service-discovery

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
  server:
    wait-time-in-ms-when-sync-empty: 0
```

## API Endpoints

### 1. Eureka Dashboard

- **URL**: `http://localhost:8761`
- **Method**: GET
- **Description**: Giao diện web để xem các service đã đăng ký

### 2. Health Check

- **URL**: `http://localhost:8761/actuator/health`
- **Method**: GET
- **Description**: Kiểm tra trạng thái sức khỏe của Eureka Server

### 3. Greeting Endpoint

- **URL**: `http://localhost:8761/greeting`
- **Method**: GET
- **Parameters**:
  - `name` (optional): Tên để chào hỏi
- **Response**: `Hello {name}`

## Cách chạy

### Local Development

```bash
cd service-discovery
mvn spring-boot:run
```

### Docker

```bash
# Build image
docker build -t service-discovery .

# Run container
docker run -p 8761:8761 service-discovery
```

## Service Registration

Các service khác có thể đăng ký với Eureka Server bằng cách:

1. Thêm dependency trong `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

2. Thêm annotation trong main class:

```java
@EnableDiscoveryClient
```

3. Cấu hình trong `application.yaml`:

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## Monitoring

### Eureka Dashboard Features:

- **Instances**: Hiển thị tất cả service instances đã đăng ký
- **Status**: Trạng thái của từng service (UP/DOWN)
- **Metadata**: Thông tin chi tiết về mỗi service
- **Health**: Sức khỏe của service instances

### Metrics:

- Số lượng service đã đăng ký
- Số lượng service instances
- Response time của các service
- Error rates

## Troubleshooting

### Common Issues:

1. **Service không đăng ký được**:

   - Kiểm tra kết nối mạng
   - Đảm bảo Eureka Server đang chạy
   - Kiểm tra cấu hình service-url

2. **Service bị mất kết nối**:

   - Kiểm tra heartbeat interval
   - Kiểm tra lease renewal interval
   - Kiểm tra network connectivity

3. **Dashboard không load**:
   - Kiểm tra port 8761 có bị block không
   - Kiểm tra firewall settings

### Logs:

```bash
# Xem logs của service
tail -f logs/service-discovery.log

# Xem logs với debug level
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.com.netflix.eureka=DEBUG"
```

## Best Practices

1. **High Availability**: Triển khai nhiều Eureka Server instances
2. **Security**: Bảo vệ Eureka Server với authentication
3. **Monitoring**: Theo dõi metrics và logs thường xuyên
4. **Backup**: Backup cấu hình và data định kỳ

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
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Environment Variables

| Variable                             | Default   | Description                   |
| ------------------------------------ | --------- | ----------------------------- |
| `SERVER_PORT`                        | 8761      | Port của Eureka Server        |
| `EUREKA_INSTANCE_HOSTNAME`           | localhost | Hostname của Eureka Server    |
| `EUREKA_CLIENT_REGISTER_WITH_EUREKA` | false     | Có đăng ký với chính nó không |
| `EUREKA_CLIENT_FETCH_REGISTRY`       | false     | Có fetch registry không       |

## Security Considerations

1. **Authentication**: Implement authentication cho Eureka Server
2. **Authorization**: Restrict access to sensitive endpoints
3. **Network Security**: Use HTTPS in production
4. **Firewall**: Configure firewall rules appropriately
