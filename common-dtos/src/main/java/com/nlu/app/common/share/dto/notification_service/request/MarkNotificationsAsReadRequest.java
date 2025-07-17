package com.nlu.app.common.share.dto.notification_service.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Builder
@Value
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MarkNotificationsAsReadRequest {
    String id;
}
