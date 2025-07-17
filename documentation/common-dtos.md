# Common DTOs Documentation

## Tổng quan

Common DTOs là module chứa các Data Transfer Objects được chia sẻ giữa các service trong hệ thống. Nó giúp đảm bảo tính nhất quán trong việc trao đổi dữ liệu giữa các service.

## Chức năng chính

- **Shared DTOs**: Cung cấp DTOs chung cho toàn bộ hệ thống
- **Data Consistency**: Đảm bảo tính nhất quán dữ liệu
- **Type Safety**: Đảm bảo type safety trong inter-service communication
- **Version Management**: Quản lý version của DTOs
- **Documentation**: Tài liệu cho các DTOs

## Cấu trúc DTOs

### 1. User DTOs

#### UserDto

```java
public class UserDto {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private List<String> roles;
    private boolean enabled;
    private LocalDateTime createdAt;
}
```

#### UserRegistrationDto

```java
public class UserRegistrationDto {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
}
```

#### UserLoginDto

```java
public class UserLoginDto {
    private String username;
    private String password;
}
```

### 2. Video DTOs

#### VideoDto

```java
public class VideoDto {
    private String id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String videoUrl;
    private String duration;
    private long viewCount;
    private long likeCount;
    private long dislikeCount;
    private String uploaderId;
    private String category;
    private List<String> tags;
    private String privacy;
    private String status;
    private LocalDateTime uploadDate;
}
```

#### VideoUploadDto

```java
public class VideoUploadDto {
    private String title;
    private String description;
    private String category;
    private List<String> tags;
    private String privacy;
}
```

#### VideoSearchDto

```java
public class VideoSearchDto {
    private String query;
    private String category;
    private String duration;
    private String uploadDate;
    private String sortBy;
    private String sortOrder;
    private int page;
    private int size;
}
```

### 3. Comment DTOs

#### CommentDto

```java
public class CommentDto {
    private String id;
    private String content;
    private String videoId;
    private String userId;
    private String parentCommentId;
    private long likeCount;
    private long dislikeCount;
    private int replyCount;
    private LocalDateTime createdAt;
    private UserDto user;
    private List<CommentDto> replies;
}
```

#### CommentRequestDto

```java
public class CommentRequestDto {
    private String videoId;
    private String content;
    private String parentCommentId;
}
```

### 4. Notification DTOs

#### NotificationDto

```java
public class NotificationDto {
    private String id;
    private String userId;
    private String type;
    private String title;
    private String message;
    private Map<String, Object> data;
    private boolean isRead;
    private LocalDateTime createdAt;
}
```

#### NotificationPreferencesDto

```java
public class NotificationPreferencesDto {
    private String userId;
    private boolean emailEnabled;
    private boolean pushEnabled;
    private boolean inAppEnabled;
    private Map<String, Boolean> types;
}
```

### 5. Profile DTOs

#### ProfileDto

```java
public class ProfileDto {
    private String id;
    private String userId;
    private String firstName;
    private String lastName;
    private String bio;
    private String location;
    private String website;
    private String avatarUrl;
    private String coverUrl;
    private int followerCount;
    private int followingCount;
    private int videoCount;
    private long totalViews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### FollowRequestDto

```java
public class FollowRequestDto {
    private String targetUserId;
    private String action; // FOLLOW, UNFOLLOW
}
```

### 6. Common Response DTOs

#### ApiResponse<T>

```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<String> errors;
    private LocalDateTime timestamp;
}
```

#### PageResponse<T>

```java
public class PageResponse<T> {
    private List<T> content;
    private int totalElements;
    private int totalPages;
    private int currentPage;
    private int size;
    private boolean hasNext;
    private boolean hasPrevious;
}
```

### 7. Error DTOs

#### ErrorDto

```java
public class ErrorDto {
    private String code;
    private String message;
    private String details;
    private LocalDateTime timestamp;
    private String path;
}
```

#### ValidationErrorDto

```java
public class ValidationErrorDto {
    private String field;
    private String message;
    private Object rejectedValue;
}
```

## Sử dụng trong Services

### 1. Maven Dependency:

```xml
<dependency>
    <groupId>com.nlu.app</groupId>
    <artifactId>common-dtos</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Import trong Service:

```java
import com.nlu.app.common.dto.UserDto;
import com.nlu.app.common.dto.ApiResponse;
import com.nlu.app.common.dto.PageResponse;
```

### 3. Sử dụng trong Controller:

```java
@RestController
public class UserController {

    @GetMapping("/users/{id}")
    public ApiResponse<UserDto> getUser(@PathVariable String id) {
        UserDto user = userService.getUser(id);
        return ApiResponse.success(user);
    }

    @GetMapping("/users")
    public ApiResponse<PageResponse<UserDto>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<UserDto> users = userService.getUsers(page, size);
        return ApiResponse.success(users);
    }
}
```

## Version Management

### 1. Versioning Strategy:

- **Major Version**: Breaking changes
- **Minor Version**: New features, backward compatible
- **Patch Version**: Bug fixes, backward compatible

### 2. Migration Guide:

- Document breaking changes
- Provide migration examples
- Maintain backward compatibility when possible

### 3. Deprecation Policy:

- Mark deprecated DTOs
- Provide alternatives
- Remove after deprecation period

## Best Practices

### 1. Naming Conventions:

- Use descriptive names
- Follow Java naming conventions
- Use consistent suffixes (Dto, Request, Response)

### 2. Immutability:

- Make DTOs immutable when possible
- Use builder pattern for complex DTOs
- Provide copy constructors

### 3. Validation:

- Include validation annotations
- Provide clear error messages
- Validate at service boundaries

### 4. Documentation:

- Document all DTOs
- Include usage examples
- Maintain API documentation

## Cách build

```bash
cd common-dtos
mvn clean install
```

## Dependencies

- Spring Boot Starter Validation
- Jackson Annotations
- Lombok
- Spring Boot Starter Test
