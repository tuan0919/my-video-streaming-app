package com.nlu.app.common.axon.event;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Builder
@Value
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserCreatedEvent {
    String username;
    String password;
    String email;
    Boolean verified;
    String userId;
}
