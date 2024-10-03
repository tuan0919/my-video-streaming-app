package com.nlu.app.common.share.event;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Builder
@Value
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NewVideoCreatedEvent {
    String videoId;
    String userId;
    String videoName;
    String videoDescription;
    String videoKey;
}
