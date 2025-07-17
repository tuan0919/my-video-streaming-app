# File Service Documentation

## Tổng quan

File Service quản lý việc upload, download và lưu trữ file trong hệ thống. Nó hỗ trợ nhiều loại file khác nhau như video, image, document.

## Chức năng chính

- **File Upload**: Xử lý upload file
- **File Download**: Cung cấp download file
- **File Storage**: Quản lý lưu trữ file
- **File Validation**: Kiểm tra file format và size
- **File Processing**: Xử lý file (resize, compress)
- **File Security**: Bảo mật file access

## API Endpoints

### 1. File Upload

#### POST /file/upload

- **Description**: Upload file
- **Headers**:
  - `Authorization: Bearer <token>`
  - `Content-Type: multipart/form-data`
- **Request Body**: Form data với file
- **Response**:

```json
{
  "fileId": "string",
  "fileName": "string",
  "fileSize": 0,
  "fileUrl": "string",
  "fileType": "string"
}
```

### 2. File Download

#### GET /file/download/{fileId}

- **Description**: Download file
- **Headers**: `Authorization: Bearer <token>`
- **Response**: File content

#### GET /file/info/{fileId}

- **Description**: Lấy thông tin file
- **Response**:

```json
{
  "fileId": "string",
  "fileName": "string",
  "fileSize": 0,
  "fileType": "string",
  "uploadDate": "datetime",
  "uploaderId": "string"
}
```

### 3. File Management

#### DELETE /file/{fileId}

- **Description**: Xóa file
- **Headers**: `Authorization: Bearer <token>`

## Database Schema

### File Entity

```sql
CREATE TABLE files (
    id VARCHAR(255) PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(100),
    mime_type VARCHAR(100),
    uploader_id VARCHAR(255) NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (uploader_id) REFERENCES users(id)
);
```

## Cách chạy

```bash
cd file-service
mvn spring-boot:run
```

## Health Check

- **Health**: `http://localhost:8086/file/actuator/health`

## Dependencies

- Spring Boot Web
- Spring Cloud Eureka Client
- Spring Cloud Config Client
- MinIO Client
