# Comment Service Documentation

## Tổng quan

Comment Service quản lý hệ thống bình luận cho video. Nó xử lý việc tạo, đọc, cập nhật, xóa bình luận và tương tác với bình luận.

## Chức năng chính

- **Comment Management**: CRUD operations cho comments
- **Comment Threading**: Hỗ trợ reply comments
- **Comment Moderation**: Kiểm duyệt bình luận
- **Comment Analytics**: Thống kê tương tác
- **Comment Search**: Tìm kiếm bình luận
- **Comment Notifications**: Thông báo khi có comment mới

## API Endpoints

### 1. Comment Management

#### GET /comment/users/video/{videoId}

- **Description**: Lấy comments của video
- **Query Parameters**:
  - `page` (int): Page number
  - `size` (int): Page size
  - `sort` (string): Sort by date/likes
- **Response**: Danh sách comments với pagination

#### POST /comment/users/new

- **Description**: Tạo comment mới
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:

```json
{
  "videoId": "string",
  "content": "string",
  "parentCommentId": "string" // optional for replies
}
```

#### PUT /comment/users/comment/{commentId}

- **Description**: Cập nhật comment
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:

```json
{
  "content": "string"
}
```

#### DELETE /comment/users/comment/{commentId}

- **Description**: Xóa comment
- **Headers**: `Authorization: Bearer <token>`

### 2. Comment Interaction

#### POST /comment/users/comment/{commentId}/interact

- **Description**: Tương tác với comment (like/dislike)
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:

```json
{
  "action": "LIKE|DISLIKE|REMOVE_LIKE|REMOVE_DISLIKE"
}
```

#### GET /comment/users/comment/{commentId}

- **Description**: Lấy thông tin chi tiết comment
- **Response**: Comment details với replies

### 3. Comment Analytics

#### GET /comment/users/video/{videoId}/stats

- **Description**: Lấy thống kê comments của video
- **Response**:

```json
{
  "totalComments": 0,
  "totalLikes": 0,
  "totalDislikes": 0,
  "topComments": []
}
```

## Database Schema

### Comment Entity

```sql
CREATE TABLE comments (
    id VARCHAR(255) PRIMARY KEY,
    content TEXT NOT NULL,
    video_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    parent_comment_id VARCHAR(255),
    like_count INT DEFAULT 0,
    dislike_count INT DEFAULT 0,
    reply_count INT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (video_id) REFERENCES videos(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (parent_comment_id) REFERENCES comments(id)
);
```

### Comment Interaction Entity

```sql
CREATE TABLE comment_interactions (
    id VARCHAR(255) PRIMARY KEY,
    comment_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    action ENUM('LIKE', 'DISLIKE') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (comment_id) REFERENCES comments(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY unique_user_comment (user_id, comment_id)
);
```

## Cách chạy

```bash
cd comment-service
mvn spring-boot:run
```

## Health Check

- **Health**: `http://localhost:8083/comment/actuator/health`
- **Info**: `http://localhost:8083/comment/actuator/info`

## Dependencies

- Spring Boot Web
- Spring Data JPA
- Spring Cloud Eureka Client
- Spring Cloud Config Client
- MySQL Connector
- Spring Kafka
