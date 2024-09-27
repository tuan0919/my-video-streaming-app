package com.nlu.app.common.axon.event;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Builder
@Value
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProfileCreatedEvent {
    String profileId;
    String userId;
    String fullName;
    String country;
    String bio;
    String avatarId;
}
