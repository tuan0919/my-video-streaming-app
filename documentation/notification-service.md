# Notification Service Documentation

## Tổng quan

Notification Service quản lý hệ thống thông báo trong ứng dụng. Nó xử lý việc gửi và quản lý các loại thông báo khác nhau như email, push notification, in-app notification.

## Chức năng chính

- **Email Notifications**: Gửi email thông báo
- **Push Notifications**: Gửi push notification
- **In-App Notifications**: Thông báo trong ứng dụng
- **Notification Templates**: Template cho các loại thông báo
- **Notification Preferences**: Cài đặt thông báo của user
- **Notification History**: Lịch sử thông báo
- **Bulk Notifications**: Gửi thông báo hàng loạt

## API Endpoints

### 1. Notification Management

#### GET /notification/users

- **Description**: Lấy danh sách notifications của user
- **Headers**: `Authorization: Bearer <token>`
- **Query Parameters**:
  - `page` (int): Page number
  - `size` (int): Page size
  - `type` (string): Filter by notification type
  - `read` (boolean): Filter by read status
- **Response**: Danh sách notifications với pagination

#### POST /notification/users/count

- **Description**: Đếm số notifications chưa đọc
- **Headers**: `Authorization: Bearer <token>`
- **Response**:

```json
{
  "unreadCount": 0,
  "totalCount": 0
}
```

#### PUT /notification/users/read

- **Description**: Đánh dấu notifications đã đọc
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:

```json
{
  "notificationIds": ["string"],
  "markAllAsRead": false
}
```

### 2. Notification Sending

#### POST /notification/send/email

- **Description**: Gửi email notification
- **Request Body**:

```json
{
  "to": "string",
  "subject": "string",
  "template": "string",
  "data": {}
}
```

#### POST /notification/send/push

- **Description**: Gửi push notification
- **Request Body**:

```json
{
  "userId": "string",
  "title": "string",
  "body": "string",
  "data": {}
}
```

#### POST /notification/send/in-app

- **Description**: Gửi in-app notification
- **Request Body**:

```json
{
  "userId": "string",
  "type": "string",
  "title": "string",
  "message": "string",
  "data": {}
}
```

### 3. Notification Preferences

#### GET /notification/users/preferences

- **Description**: Lấy preferences của user
- **Headers**: `Authorization: Bearer <token>`

#### PUT /notification/users/preferences

- **Description**: Cập nhật preferences
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:

```json
{
  "emailEnabled": true,
  "pushEnabled": true,
  "inAppEnabled": true,
  "types": {
    "NEW_VIDEO": true,
    "NEW_COMMENT": true,
    "NEW_FOLLOWER": true
  }
}
```

## Database Schema

### Notification Entity

```sql
CREATE TABLE notifications (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    type ENUM('EMAIL', 'PUSH', 'IN_APP') NOT NULL,
    title VARCHAR(255),
    message TEXT,
    data JSON,
    is_read BOOLEAN DEFAULT FALSE,
    is_sent BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Notification Template Entity

```sql
CREATE TABLE notification_templates (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    type ENUM('EMAIL', 'PUSH', 'IN_APP') NOT NULL,
    subject VARCHAR(255),
    content TEXT,
    variables JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### User Notification Preferences Entity

```sql
CREATE TABLE user_notification_preferences (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) UNIQUE NOT NULL,
    email_enabled BOOLEAN DEFAULT TRUE,
    push_enabled BOOLEAN DEFAULT TRUE,
    in_app_enabled BOOLEAN DEFAULT TRUE,
    preferences JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## Notification Types

### 1. Email Notifications:

- Welcome email
- Password reset
- Video upload confirmation
- New comment notification
- New follower notification

### 2. Push Notifications:

- New video from followed channels
- New comment on user's video
- New follower
- Live stream started
- Video processing completed

### 3. In-App Notifications:

- System announcements
- Feature updates
- Community guidelines
- Copyright notices

## Notification Flow

### 1. Event-Driven Notifications:

1. Event occurs (new comment, new video, etc.)
2. Event published to Kafka
3. Notification Service consumes event
4. Check user preferences
5. Send appropriate notifications
6. Update notification history

### 2. Scheduled Notifications:

1. Scheduled job triggers
2. Query users based on criteria
3. Generate notifications
4. Send notifications
5. Update status

## Cách chạy

```bash
cd notification-service
mvn spring-boot:run
```

## Health Check

- **Health**: `http://localhost:8084/notification/actuator/health`
- **Info**: `http://localhost:8084/notification/actuator/info`

## Dependencies

- Spring Boot Web
- Spring Data JPA
- Spring Cloud Eureka Client
- Spring Cloud Config Client
- Spring Kafka
- Spring Mail
- MySQL Connector
- JavaMail API
