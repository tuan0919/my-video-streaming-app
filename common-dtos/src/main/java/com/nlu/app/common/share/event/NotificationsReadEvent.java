package com.nlu.app.common.share.event;

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
public class NotificationsReadEvent {
    List<String> ids;
}
