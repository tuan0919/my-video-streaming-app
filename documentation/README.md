# Video Streaming Application - Microservices Architecture

## Tổng quan

Ứng dụng Video Streaming là một hệ thống microservices được xây dựng bằng Spring Boot, cung cấp các tính năng streaming video, quản lý người dùng, bình luận, thông báo và nhiều tính năng khác.

## Kiến trúc hệ thống

### Các Service chính:

1. **Service Discovery** - Eureka Server cho service discovery
2. **Gateway Service** - API Gateway, routing và load balancing
3. **Identity Service** - Quản lý xác thực và phân quyền
4. **Video Streaming Service** - Quản lý video và streaming
5. **Comment Service** - Quản lý bình luận
6. **Notification Service** - Gửi thông báo
7. **Profile Service** - Quản lý thông tin người dùng
8. **File Service** - Quản lý file upload/download
9. **Socket Service** - WebSocket cho real-time communication
10. **Aggregator Service** - Tổng hợp dữ liệu từ nhiều service
11. **Config Service** - Quản lý cấu hình
12. **Cloud Config Service** - Centralized configuration management

### Infrastructure Services:

- **MySQL** - Database cho các service
- **Redis** - Caching
- **Kafka** - Message broker (3 brokers)
- **Zookeeper** - Coordination service cho Kafka
- **Debezium** - Change data capture
- **Ngrok** - Tunneling service

## Công nghệ sử dụng

### Backend:

- **Spring Boot** - Framework chính
- **Spring Cloud** - Microservices framework
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - Database access
- **Spring WebSocket** - Real-time communication
- **Eureka** - Service discovery
- **Gateway** - API Gateway

### Database & Caching:

- **MySQL 8.0** - Primary database
- **Redis** - Caching layer

### Message Broker:

- **Apache Kafka** - Event streaming
- **Debezium** - Change data capture

### DevOps & Monitoring:

- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **Ngrok** - Development tunneling

## Cấu trúc thư mục

```
my-video-streaming-app/
├── service-discovery/          # Eureka Server
├── gateway-service/            # API Gateway
├── identity-service/           # Authentication & Authorization
├── video-streaming-service/    # Video management
├── comment-service/            # Comment management
├── notification-service/       # Notification system
├── profile-service/            # User profile management
├── file-service/              # File management
├── socket-service/            # WebSocket service
├── aggregator-service/        # Data aggregation
├── config-service/            # Configuration management
├── cloud-config-service/      # Centralized config
├── common-dtos/              # Shared DTOs
├── configuration/            # Service configurations
├── dockerfiles/              # Docker configurations
├── env/                      # Environment files
├── documentation/            # Project documentation
└── docker-compose.yaml       # Infrastructure orchestration
```

## Cách chạy ứng dụng

### Yêu cầu hệ thống:

- Docker & Docker Compose
- Java 17+
- Maven 3.6+

### Bước 1: Khởi động infrastructure

```bash
docker-compose up -d
```

### Bước 2: Build và chạy các service

```bash
# Build tất cả services
mvn clean install -DskipTests

# Chạy từng service (theo thứ tự)
# 1. Service Discovery
cd service-discovery && mvn spring-boot:run

# 2. Config Service
cd config-service && mvn spring-boot:run

# 3. Gateway Service
cd gateway-service && mvn spring-boot:run

# 4. Identity Service
cd identity-service && mvn spring-boot:run

# 5. Các service khác...
```

## Ports mặc định

| Service                 | Port | Description        |
| ----------------------- | ---- | ------------------ |
| Service Discovery       | 8761 | Eureka Server      |
| Gateway Service         | 8080 | API Gateway        |
| Identity Service        | 8081 | Authentication     |
| Video Streaming Service | 8082 | Video management   |
| Comment Service         | 8083 | Comments           |
| Notification Service    | 8084 | Notifications      |
| Profile Service         | 8085 | User profiles      |
| File Service            | 8086 | File management    |
| Socket Service          | 8087 | WebSocket          |
| Aggregator Service      | 8088 | Data aggregation   |
| Config Service          | 8888 | Configuration      |
| Cloud Config Service    | 8889 | Centralized config |

## Database Ports

| Database            | Port  | Service         |
| ------------------- | ----- | --------------- |
| MySQL Video         | 3306  | Video Streaming |
| MySQL Identity      | 3307  | Identity        |
| MySQL Comments      | 33061 | Comments        |
| MySQL Notifications | 33062 | Notifications   |
| MySQL Profile       | 33063 | Profile         |

## Kafka & Infrastructure

| Service        | Port  | Description         |
| -------------- | ----- | ------------------- |
| Zookeeper      | 2181  | Kafka coordination  |
| Kafka Broker 1 | 19092 | Message broker      |
| Kafka Broker 2 | 29092 | Message broker      |
| Kafka Broker 3 | 39092 | Message broker      |
| Debezium       | 8083  | Change data capture |
| Redis          | 6379  | Caching             |
| Ngrok          | 18686 | Tunneling           |

## API Documentation

Mỗi service có API documentation riêng. Xem các file documentation chi tiết trong thư mục `documentation/`:

- [Service Discovery Documentation](service-discovery.md)
- [Gateway Service Documentation](gateway-service.md)
- [Identity Service Documentation](identity-service.md)
- [Video Streaming Service Documentation](video-streaming-service.md)
- [Comment Service Documentation](comment-service.md)
- [Notification Service Documentation](notification-service.md)
- [Profile Service Documentation](profile-service.md)
- [File Service Documentation](file-service.md)
- [Socket Service Documentation](socket-service.md)
- [Aggregator Service Documentation](aggregator-service.md)
- [Config Service Documentation](config-service.md)
- [Cloud Config Service Documentation](cloud-config-service.md)

## Development Workflow

1. **Local Development**: Sử dụng Docker Compose để chạy infrastructure
2. **Service Development**: Phát triển từng service độc lập
3. **Integration Testing**: Test integration giữa các service
4. **Deployment**: Deploy lên môi trường staging/production

## Monitoring & Logging

- **Eureka Dashboard**: http://localhost:8761
- **Gateway Routes**: http://localhost:8080/actuator/gateway/routes
- **Health Checks**: http://localhost:8080/actuator/health

## Troubleshooting

### Common Issues:

1. **Service không start**: Kiểm tra port conflicts
2. **Database connection**: Đảm bảo MySQL containers đã start
3. **Kafka connection**: Kiểm tra Zookeeper và Kafka brokers
4. **Service discovery**: Đảm bảo Eureka Server đã start trước

### Logs:

```bash
# Xem logs của container
docker-compose logs [service-name]

# Xem logs real-time
docker-compose logs -f [service-name]
```

## Contributing

1. Fork repository
2. Tạo feature branch
3. Commit changes
4. Push to branch
5. Tạo Pull Request

## License

MIT License - xem file LICENSE để biết thêm chi tiết.
