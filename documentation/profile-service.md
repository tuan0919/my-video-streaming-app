# Profile Service Documentation

## Tổng quan

Profile Service quản lý thông tin profile của người dùng, bao gồm thông tin cá nhân, avatar, follow/following relationships, saved videos và các cài đặt khác.

## Chức năng chính

- **User Profile Management**: CRUD operations cho user profiles
- **Avatar Management**: Upload và quản lý avatar
- **Follow System**: Quản lý follow/following relationships
- **Saved Videos**: Lưu và quản lý video yêu thích
- **User Preferences**: Cài đặt cá nhân của user
- **User Analytics**: Thống kê hoạt động của user
- **User Search**: Tìm kiếm user

## API Endpoints

### 1. Profile Management

#### GET /profile/users/{userId}

- **Description**: Lấy thông tin profile của user
- **Response**:

```json
{
  "id": "string",
  "username": "string",
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "avatarUrl": "string",
  "bio": "string",
  "location": "string",
  "website": "string",
  "followerCount": 0,
  "followingCount": 0,
  "videoCount": 0,
  "joinDate": "datetime"
}
```

#### PUT /profile/users/info

- **Description**: Cập nhật thông tin profile
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:

```json
{
  "firstName": "string",
  "lastName": "string",
  "bio": "string",
  "location": "string",
  "website": "string"
}
```

#### PUT /profile/users/avatar

- **Description**: Upload avatar
- **Headers**:
  - `Authorization: Bearer <token>`
  - `Content-Type: multipart/form-data`
- **Request Body**: Form data với image file

#### GET /profile/users/get/avatar

- **Description**: Lấy avatar của user
- **Response**: Image file

### 2. Follow System

#### PUT /profile/users/follow

- **Description**: Follow/unfollow user
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:

```json
{
  "targetUserId": "string",
  "action": "FOLLOW|UNFOLLOW"
}
```

#### GET /profile/users/get/follow/{userId}

- **Description**: Lấy danh sách followers/following
- **Query Parameters**:
  - `type` (string): "followers" hoặc "following"
  - `page` (int): Page number
  - `size` (int): Page size

#### GET /profile/users/suggest

- **Description**: Gợi ý user để follow
- **Headers**: `Authorization: Bearer <token>`
- **Query Parameters**:
  - `page` (int): Page number
  - `size` (int): Page size

### 3. Saved Videos

#### POST /profile/users/save/video

- **Description**: Lưu video vào playlist
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:

```json
{
  "videoId": "string",
  "playlistId": "string"
}
```

#### GET /profile/users/saved/videos

- **Description**: Lấy danh sách video đã lưu
- **Headers**: `Authorization: Bearer <token>`
- **Query Parameters**:
  - `page` (int): Page number
  - `size` (int): Page size
  - `playlistId` (string): Filter by playlist

### 4. User Search

#### POST /profile/users/query

- **Description**: Tìm kiếm user
- **Request Body**:

```json
{
  "query": "string",
  "filters": {
    "location": "string",
    "joinDate": "string"
  },
  "page": 0,
  "size": 10
}
```

## Database Schema

### User Profile Entity

```sql
CREATE TABLE user_profiles (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    bio TEXT,
    location VARCHAR(100),
    website VARCHAR(255),
    avatar_url VARCHAR(500),
    cover_url VARCHAR(500),
    follower_count INT DEFAULT 0,
    following_count INT DEFAULT 0,
    video_count INT DEFAULT 0,
    total_views BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Follow Relationship Entity

```sql
CREATE TABLE follow_relationships (
    id VARCHAR(255) PRIMARY KEY,
    follower_id VARCHAR(255) NOT NULL,
    following_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (follower_id) REFERENCES users(id),
    FOREIGN KEY (following_id) REFERENCES users(id),
    UNIQUE KEY unique_follow (follower_id, following_id)
);
```

### Saved Video Entity

```sql
CREATE TABLE saved_videos (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    video_id VARCHAR(255) NOT NULL,
    playlist_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (video_id) REFERENCES videos(id),
    UNIQUE KEY unique_saved_video (user_id, video_id)
);
```

### Playlist Entity

```sql
CREATE TABLE playlists (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_public BOOLEAN DEFAULT TRUE,
    video_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## Follow System Logic

### 1. Follow Process:

1. User A follows User B
2. Create follow relationship record
3. Increment User B's follower count
4. Increment User A's following count
5. Send notification to User B
6. Update feed recommendations

### 2. Unfollow Process:

1. User A unfollows User B
2. Remove follow relationship record
3. Decrement User B's follower count
4. Decrement User A's following count
5. Update feed recommendations

## Avatar Management

### Upload Process:

1. Validate image file (format, size)
2. Generate unique filename
3. Store image in file storage
4. Generate different sizes (thumbnail, medium, large)
5. Update user profile with avatar URL
6. Delete old avatar if exists

### Supported Formats:

- **Input**: JPG, PNG, GIF
- **Output**: JPG, PNG
- **Max Size**: 5MB
- **Dimensions**: Auto-resize to 400x400

## Cách chạy

```bash
cd profile-service
mvn spring-boot:run
```

## Health Check

- **Health**: `http://localhost:8085/profile/actuator/health`
- **Info**: `http://localhost:8085/profile/actuator/info`

## Dependencies

- Spring Boot Web
- Spring Data JPA
- Spring Cloud Eureka Client
- Spring Cloud Config Client
- Spring Kafka
- MySQL Connector
- Spring Cloud OpenFeign
