package com.nlu.app.common.axon.dto.notification_service.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Builder
@Value
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NotificationCreationRequest {
    String notificationId;
    String userId;
    String content;
    Boolean isRead = false;
    String type;
}
