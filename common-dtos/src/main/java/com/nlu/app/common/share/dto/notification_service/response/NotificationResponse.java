package com.nlu.app.common.share.dto.notification_service.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse implements Serializable {
    String notificationId;
    String userId;
    LocalDateTime time;
    String content;
    String title;
    String relatedObjectId;
    Boolean isRead;
    String type;
    String relatedEvent;
}
