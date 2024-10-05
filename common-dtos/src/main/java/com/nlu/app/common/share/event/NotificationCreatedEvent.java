package com.nlu.app.common.share.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Builder
@Value
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class NotificationCreatedEvent {
    String notificationId;
    String userId;
    LocalDateTime time;
    String content;
    Boolean isRead = false;
}
