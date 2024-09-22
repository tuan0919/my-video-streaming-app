package com.nlu.app.domain.events;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Value
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DataSynchronizedEvent {
    String aggregateId;
    String eventClassName;
    LocalDateTime timestamp;
}
