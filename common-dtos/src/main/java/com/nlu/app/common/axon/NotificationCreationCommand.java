package com.nlu.app.common.axon;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.time.LocalDateTime;

@Builder
@Value
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NotificationCreationCommand {
    @TargetAggregateIdentifier
    String notificationId;
    String userId;
    String content;
    Boolean isRead = false;
}
