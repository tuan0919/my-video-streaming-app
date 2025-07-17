# Socket Service Documentation

## Tổng quan

Socket Service cung cấp real-time communication thông qua WebSocket. Nó xử lý các kết nối real-time như chat, live comments, notifications và các tính năng real-time khác.

## Chức năng chính

- **WebSocket Connections**: Quản lý kết nối WebSocket
- **Real-time Chat**: Chat real-time giữa users
- **Live Comments**: Comments real-time cho video
- **Real-time Notifications**: Thông báo real-time
- **Live Streaming Chat**: Chat cho live streams
- **Connection Management**: Quản lý connection state

## API Endpoints

### 1. WebSocket Connection

#### WebSocket /socket/websocket

- **Description**: WebSocket endpoint cho real-time communication
- **Headers**: `Authorization: Bearer <token>`

### 2. Connection Management

#### GET /socket/connections

- **Description**: Lấy danh sách active connections
- **Headers**: `Authorization: Bearer <token>`

#### POST /socket/broadcast

- **Description**: Gửi message broadcast
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:

```json
{
  "type": "string",
  "message": "string",
  "recipients": ["string"]
}
```

## WebSocket Message Types

### 1. Chat Messages:

```json
{
  "type": "CHAT",
  "data": {
    "senderId": "string",
    "receiverId": "string",
    "message": "string",
    "timestamp": "datetime"
  }
}
```

### 2. Live Comments:

```json
{
  "type": "LIVE_COMMENT",
  "data": {
    "videoId": "string",
    "commentId": "string",
    "userId": "string",
    "content": "string",
    "timestamp": "datetime"
  }
}
```

### 3. Notifications:

```json
{
  "type": "NOTIFICATION",
  "data": {
    "userId": "string",
    "title": "string",
    "message": "string",
    "type": "string"
  }
}
```

### 4. Live Stream Events:

```json
{
  "type": "LIVE_STREAM",
  "data": {
    "videoId": "string",
    "event": "STARTED|ENDED|PAUSED",
    "timestamp": "datetime"
  }
}
```

## Connection Management

### 1. Connection Flow:

1. Client connects với WebSocket
2. Authenticate với JWT token
3. Register user session
4. Join relevant channels (chat rooms, video rooms)
5. Start receiving real-time messages

### 2. Disconnection Flow:

1. Client disconnects
2. Clean up user session
3. Leave all channels
4. Notify other users if needed

## Channel Management

### 1. Chat Channels:

- Private chat: `chat:{userId1}:{userId2}`
- Group chat: `group:{groupId}`

### 2. Video Channels:

- Video comments: `video:{videoId}:comments`
- Live stream: `video:{videoId}:live`

### 3. Notification Channels:

- User notifications: `user:{userId}:notifications`

## Cách chạy

```bash
cd socket-service
mvn spring-boot:run
```

## Health Check

- **Health**: `http://localhost:8087/socket/actuator/health`

## Dependencies

- Spring Boot Web
- Spring WebSocket
- Spring Cloud Eureka Client
- Spring Cloud Config Client
- Spring Security
