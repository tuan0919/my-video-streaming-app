# Aggregator Service Documentation

## Tổng quan

Aggregator Service tổng hợp dữ liệu từ nhiều service khác nhau để cung cấp các API tổng hợp cho client. Nó giúp giảm số lượng request từ client và cung cấp dữ liệu đã được tổng hợp.

## Chức năng chính

- **Data Aggregation**: Tổng hợp dữ liệu từ nhiều service
- **Search Aggregation**: Tìm kiếm tổng hợp across services
- **User Dashboard**: Dữ liệu dashboard cho user
- **Video Analytics**: Thống kê tổng hợp cho video
- **Recommendation Engine**: Gợi ý video và user
- **Caching**: Cache dữ liệu tổng hợp

## API Endpoints

### 1. Video Aggregation

#### GET /aggregator/query/video/{videoId}

- **Description**: Lấy thông tin tổng hợp của video
- **Response**:

```json
{
  "video": {
    "id": "string",
    "title": "string",
    "description": "string",
    "uploader": {
      "id": "string",
      "username": "string",
      "avatarUrl": "string"
    },
    "stats": {
      "viewCount": 0,
      "likeCount": 0,
      "commentCount": 0
    },
    "comments": [],
    "relatedVideos": []
  }
}
```

#### GET /aggregator/query/video/{videoId}/comments

- **Description**: Lấy comments của video với user info
- **Query Parameters**:
  - `page` (int): Page number
  - `size` (int): Page size

### 2. Search Aggregation

#### GET /aggregator/query/search/video

- **Description**: Tìm kiếm video tổng hợp
- **Query Parameters**:
  - `q` (string): Search query
  - `category` (string): Filter by category
  - `sort` (string): Sort by relevance/date/views

#### GET /aggregator/query/search/user

- **Description**: Tìm kiếm user tổng hợp
- **Query Parameters**:
  - `q` (string): Search query
  - `location` (string): Filter by location

### 3. User Aggregation

#### GET /aggregator/query/user/saved

- **Description**: Lấy saved videos của user
- **Headers**: `Authorization: Bearer <token>`
- **Query Parameters**:
  - `page` (int): Page number
  - `size` (int): Page size

#### GET /aggregator/query/user/posted

- **Description**: Lấy videos đã upload của user
- **Headers**: `Authorization: Bearer <token>`
- **Query Parameters**:
  - `page` (int): Page number
  - `size` (int): Page size

#### GET /aggregator/query/user/page/{userId}

- **Description**: Lấy trang profile tổng hợp của user
- **Response**:

```json
{
  "profile": {
    "id": "string",
    "username": "string",
    "avatarUrl": "string",
    "bio": "string",
    "stats": {
      "followerCount": 0,
      "followingCount": 0,
      "videoCount": 0,
      "totalViews": 0
    }
  },
  "videos": [],
  "playlists": []
}
```

### 4. Recommendation

#### GET /aggregator/query/suggest/user

- **Description**: Gợi ý user để follow
- **Headers**: `Authorization: Bearer <token>`
- **Query Parameters**:
  - `page` (int): Page number
  - `size` (int): Page size

#### GET /aggregator/query/suggest/video

- **Description**: Gợi ý video để xem
- **Headers**: `Authorization: Bearer <token>`
- **Query Parameters**:
  - `page` (int): Page number
  - `size` (int): Page size

### 5. Notification Aggregation

#### GET /aggregator/query/notifications

- **Description**: Lấy notifications tổng hợp
- **Headers**: `Authorization: Bearer <token>`
- **Query Parameters**:
  - `page` (int): Page number
  - `size` (int): Page size

## Data Aggregation Patterns

### 1. Parallel Aggregation:

- Gọi nhiều service song song
- Tổng hợp kết quả
- Trả về response tổng hợp

### 2. Sequential Aggregation:

- Gọi service theo thứ tự
- Sử dụng kết quả của service trước
- Tối ưu cho dependencies

### 3. Cached Aggregation:

- Cache kết quả tổng hợp
- Invalidate cache khi data thay đổi
- Giảm load cho các service

## Circuit Breaker Pattern

### Implementation:

- Sử dụng Hystrix hoặc Resilience4j
- Fallback methods cho service failures
- Timeout configuration
- Error handling

### Example:

```java
@HystrixCommand(fallbackMethod = "getVideoFallback")
public VideoAggregatedResponse getVideo(String videoId) {
    // Call multiple services
    return aggregatedResponse;
}

public VideoAggregatedResponse getVideoFallback(String videoId) {
    // Return cached data or default response
    return fallbackResponse;
}
```

## Caching Strategy

### 1. Cache Levels:

- **L1 Cache**: In-memory cache (Redis)
- **L2 Cache**: Distributed cache
- **L3 Cache**: Database cache

### 2. Cache Keys:

- `video:{videoId}:aggregated`
- `user:{userId}:profile`
- `search:{query}:results`

### 3. Cache Invalidation:

- Event-driven invalidation
- Time-based expiration
- Manual invalidation

## Cách chạy

```bash
cd aggregator-service
mvn spring-boot:run
```

## Health Check

- **Health**: `http://localhost:8088/aggregator/actuator/health`
- **Circuit Breaker**: `http://localhost:8088/aggregator/actuator/hystrix.stream`

## Dependencies

- Spring Boot Web
- Spring Cloud Eureka Client
- Spring Cloud Config Client
- Spring Cloud OpenFeign
- Spring Boot Starter Cache
- Hystrix/Resilience4j
- Redis
