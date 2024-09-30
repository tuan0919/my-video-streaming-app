package com.nlu.app.common.share.dto.notification_service.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Builder
@Value
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NotificationCreationRequest {
    String userId;
    String content;
    Boolean isRead = false;
    String type;
    String sagaId;
    String sagaAction;
}
