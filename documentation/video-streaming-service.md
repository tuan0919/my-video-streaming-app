# Video Streaming Service Documentation

## Tổng quan

Video Streaming Service là service chính quản lý video content trong hệ thống. Nó xử lý việc upload, lưu trữ, streaming và quản lý metadata của video.

## Chức năng chính

- **Video Upload**: Xử lý upload video files
- **Video Streaming**: Cung cấp streaming video content
- **Video Management**: CRUD operations cho video metadata
- **Video Processing**: Transcoding, thumbnail generation
- **Video Analytics**: View count, watch time tracking
- **Video Search**: Tìm kiếm và filter video
- **Video Recommendations**: Gợi ý video liên quan
- **Video Categories**: Phân loại video

## Công nghệ sử dụng

- **Spring Boot** - Framework chính
- **Spring Data JPA** - Database access
- **Spring Cloud Netflix Eureka Client** - Service discovery
- **Spring Cloud Config Client** - Configuration management
- **Spring Kafka** - Event messaging
- **MySQL** - Primary database
- **FFmpeg** - Video processing
- **MinIO/AWS S3** - File storage

## Cấu trúc project

```
video-streaming-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/nlu/app/
│   │   │       ├── VideoStreamingServiceApplication.java
│   │   │       ├── controller/
│   │   │       │   ├── VideoController.java
│   │   │       │   ├── StreamingController.java
│   │   │       │   └── UploadController.java
│   │   │       ├── service/
│   │   │       │   ├── VideoService.java
│   │   │       │   ├── StreamingService.java
│   │   │       │   ├── UploadService.java
│   │   │       │   └── ProcessingService.java
│   │   │       ├── entity/
│   │   │       │   ├── Video.java
│   │   │       │   ├── VideoCategory.java
│   │   │       │   └── VideoAnalytics.java
│   │   │       ├── repository/
│   │   │       │   ├── VideoRepository.java
│   │   │       │   ├── CategoryRepository.java
│   │   │       │   └── AnalyticsRepository.java
│   │   │       ├── dto/
│   │   │       │   ├── VideoRequest.java
│   │   │       │   ├── VideoResponse.java
│   │   │       │   └── UploadResponse.java
│   │   │       ├── mapper/
│   │   │       │   └── VideoMapper.java
│   │   │       ├── configuration/
│   │   │       │   ├── StorageConfig.java
│   │   │       │   ├── KafkaConfig.java
│   │   │       │   └── ProcessingConfig.java
│   │   │       ├── exception/
│   │   │       │   ├── VideoNotFoundException.java
│   │   │       │   └── UploadException.java
│   │   │       └── util/
│   │   │           ├── VideoProcessor.java
│   │   │           └── ThumbnailGenerator.java
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
│       └── java/
│           └── com/nlu/app/
│               └── VideoStreamingServiceApplicationTests.java
├── env/
│   └── mysql.env
├── pom.xml
└── mvnw
```

## API Endpoints

### 1. Video Management Endpoints

#### GET /video-streaming/videos

- **Description**: Lấy danh sách video
- **Query Parameters**:
  - `page` (int): Page number
  - `size` (int): Page size
  - `category` (string): Filter by category
  - `search` (string): Search term
  - `sort` (string): Sort field
  - `order` (string): Sort order (asc/desc)
- **Response**:

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "string",
        "title": "string",
        "description": "string",
        "thumbnailUrl": "string",
        "duration": "string",
        "viewCount": 0,
        "uploadDate": "datetime",
        "uploader": {
          "id": "string",
          "username": "string"
        }
      }
    ],
    "totalElements": 0,
    "totalPages": 0,
    "currentPage": 0
  }
}
```

#### POST /video-streaming/videos

- **Description**: Tạo video mới
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:

```json
{
  "title": "string",
  "description": "string",
  "category": "string",
  "tags": ["string"],
  "privacy": "PUBLIC|PRIVATE|UNLISTED"
}
```

#### GET /video-streaming/videos/{videoId}

- **Description**: Lấy thông tin chi tiết video
- **Response**:

```json
{
  "success": true,
  "data": {
    "id": "string",
    "title": "string",
    "description": "string",
    "thumbnailUrl": "string",
    "videoUrl": "string",
    "duration": "string",
    "viewCount": 0,
    "likeCount": 0,
    "dislikeCount": 0,
    "uploadDate": "datetime",
    "uploader": {
      "id": "string",
      "username": "string"
    },
    "category": "string",
    "tags": ["string"],
    "privacy": "string"
  }
}
```

#### PUT /video-streaming/videos/{videoId}

- **Description**: Cập nhật thông tin video
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:

```json
{
  "title": "string",
  "description": "string",
  "category": "string",
  "tags": ["string"],
  "privacy": "string"
}
```

#### DELETE /video-streaming/videos/{videoId}

- **Description**: Xóa video
- **Headers**: `Authorization: Bearer <token>`

### 2. Video Upload Endpoints

#### PUT /video-streaming/videos/upload

- **Description**: Upload video file
- **Headers**:
  - `Authorization: Bearer <token>`
  - `Content-Type: multipart/form-data`
- **Request Body**: Form data với video file
- **Response**:

```json
{
  "success": true,
  "data": {
    "uploadId": "string",
    "status": "UPLOADING|PROCESSING|COMPLETED|FAILED",
    "progress": 0.0
  }
}
```

#### GET /video-streaming/videos/upload/{uploadId}/status

- **Description**: Kiểm tra trạng thái upload
- **Headers**: `Authorization: Bearer <token>`

### 3. Video Streaming Endpoints

#### GET /video-streaming/videos/{videoId}/stream

- **Description**: Stream video content
- **Headers**: `Range: bytes=start-end`
- **Response**: Video content stream

#### GET /video-streaming/videos/{videoId}/thumbnail

- **Description**: Lấy thumbnail của video
- **Response**: Image file

### 4. Video Interaction Endpoints

#### POST /video-streaming/videos/up-vote

- **Description**: Upvote video
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:

```json
{
  "videoId": "string"
}
```

#### POST /video-streaming/videos/down-vote

- **Description**: Downvote video
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:

```json
{
  "videoId": "string"
}
```

#### POST /video-streaming/videos/progress

- **Description**: Cập nhật progress xem video
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:

```json
{
  "videoId": "string",
  "progress": 0.0,
  "duration": 0
}
```

#### POST /video-streaming/videos/view

- **Description**: Ghi nhận lượt xem video
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:

```json
{
  "videoId": "string"
}
```

### 5. Video Search Endpoints

#### GET /video-streaming/videos/search

- **Description**: Tìm kiếm video
- **Query Parameters**:
  - `q` (string): Search query
  - `category` (string): Filter by category
  - `duration` (string): Filter by duration
  - `uploadDate` (string): Filter by upload date
  - `sort` (string): Sort by relevance/date/views
- **Response**: Danh sách video matching criteria

### 6. Video Categories Endpoints

#### GET /video-streaming/categories

- **Description**: Lấy danh sách categories
- **Response**:

```json
{
  "success": true,
  "data": [
    {
      "id": "string",
      "name": "string",
      "description": "string",
      "videoCount": 0
    }
  ]
}
```

## Database Schema

### Video Entity

```sql
CREATE TABLE videos (
    id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    file_path VARCHAR(500) NOT NULL,
    thumbnail_path VARCHAR(500),
    duration INT,
    file_size BIGINT,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    dislike_count INT DEFAULT 0,
    uploader_id VARCHAR(255) NOT NULL,
    category_id VARCHAR(255),
    privacy ENUM('PUBLIC', 'PRIVATE', 'UNLISTED') DEFAULT 'PUBLIC',
    status ENUM('PROCESSING', 'READY', 'FAILED') DEFAULT 'PROCESSING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (uploader_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES video_categories(id)
);
```

### Video Category Entity

```sql
CREATE TABLE video_categories (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Video Analytics Entity

```sql
CREATE TABLE video_analytics (
    id VARCHAR(255) PRIMARY KEY,
    video_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    watch_time INT DEFAULT 0,
    progress FLOAT DEFAULT 0.0,
    last_watched TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (video_id) REFERENCES videos(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Video Tags Entity

```sql
CREATE TABLE video_tags (
    video_id VARCHAR(255),
    tag VARCHAR(100),
    PRIMARY KEY (video_id, tag),
    FOREIGN KEY (video_id) REFERENCES videos(id)
);
```

## Video Processing Pipeline

### 1. Upload Process:

1. Client upload video file
2. Validate file format and size
3. Store file temporarily
4. Generate upload ID
5. Start processing in background
6. Return upload status

### 2. Processing Process:

1. Extract video metadata
2. Generate multiple quality versions
3. Create thumbnail
4. Update database with metadata
5. Move files to permanent storage
6. Update status to READY

### 3. Streaming Process:

1. Client request video stream
2. Validate access permissions
3. Determine optimal quality
4. Stream video content
5. Track analytics

## File Storage

### Storage Structure:

```
/videos/
├── {videoId}/
│   ├── original.mp4
│   ├── 1080p.mp4
│   ├── 720p.mp4
│   ├── 480p.mp4
│   ├── 360p.mp4
│   └── thumbnail.jpg
```

### Supported Formats:

- **Input**: MP4, AVI, MOV, MKV, WMV
- **Output**: MP4 (H.264 codec)
- **Thumbnail**: JPG, PNG

## Cách chạy

### Local Development

```bash
cd video-streaming-service
mvn spring-boot:run
```

### Docker

```bash
# Build image
docker build -t video-streaming-service .

# Run container
docker run -p 8082:8082 video-streaming-service
```

## Health Check

### Endpoints:

- **Health**: `http://localhost:8082/video-streaming/actuator/health`
- **Info**: `http://localhost:8082/video-streaming/actuator/info`
- **Metrics**: `http://localhost:8082/video-streaming/actuator/metrics`

## Monitoring

### Metrics:

- Video upload count
- Video processing time
- Streaming bandwidth usage
- View count per video
- Error rates

### Logs:

- Upload events
- Processing events
- Streaming events
- Error logs

## Troubleshooting

### Common Issues:

1. **Upload failed**:

   - Kiểm tra file size limits
   - Kiểm tra supported formats
   - Kiểm tra storage space

2. **Processing failed**:

   - Kiểm tra FFmpeg installation
   - Kiểm tra processing queue
   - Kiểm tra error logs

3. **Streaming issues**:

   - Kiểm tra file permissions
   - Kiểm tra network bandwidth
   - Kiểm tra CDN configuration

4. **Database connection**:
   - Kiểm tra MySQL service
   - Kiểm tra connection pool
   - Kiểm tra query performance

### Debug Mode:

```bash
# Enable debug logging
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.com.nlu.app=DEBUG"
```

## Performance Optimization

1. **CDN Integration**: Use CDN for video delivery
2. **Caching**: Cache frequently accessed videos
3. **Compression**: Optimize video compression
4. **Load Balancing**: Distribute load across instances
5. **Database Optimization**: Index frequently queried fields

## Security Considerations

1. **Access Control**: Validate user permissions
2. **File Validation**: Validate uploaded files
3. **Rate Limiting**: Limit upload/streaming rates
4. **Content Filtering**: Filter inappropriate content
5. **DDoS Protection**: Protect against DDoS attacks

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
        <artifactId>spring-boot-starter-data-jpa</artifactId>
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
        <groupId>io.minio</groupId>
        <artifactId>minio</artifactId>
    </dependency>
    <dependency>
        <groupId>org.bytedeco</groupId>
        <artifactId>javacv-platform</artifactId>
    </dependency>
</dependencies>
```

## Environment Variables

| Variable                  | Default                                         | Description                      |
| ------------------------- | ----------------------------------------------- | -------------------------------- |
| `SERVER_PORT`             | 0                                               | Port của Video Streaming Service |
| `SPRING_DATASOURCE_URL`   | jdbc:mysql://localhost:3306/video_streaming     | Database URL                     |
| `MINIO_ENDPOINT`          | http://localhost:9000                           | MinIO endpoint                   |
| `MINIO_ACCESS_KEY`        | minioadmin                                      | MinIO access key                 |
| `MINIO_SECRET_KEY`        | minioadmin                                      | MinIO secret key                 |
| `MAX_FILE_SIZE`           | 1073741824                                      | Max file size (1GB)              |
| `PROCESSING_THREADS`      | 4                                               | Number of processing threads     |
| `KAFKA_BOOTSTRAP_SERVERS` | localhost:19092,localhost:29092,localhost:39092 | Kafka brokers                    |
