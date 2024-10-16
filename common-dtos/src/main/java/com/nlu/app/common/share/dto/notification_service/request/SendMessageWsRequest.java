package com.nlu.app.common.share.dto.notification_service.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Value
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SendMessageWsRequest {
    @Builder.Default
    String messageId = UUID.randomUUID().toString();
    String action;
    String topic;
    String payload;
    @Builder.Default
    LocalDateTime issueAt = LocalDateTime.now();
}
