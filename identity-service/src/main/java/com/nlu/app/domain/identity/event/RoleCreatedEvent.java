package com.nlu.app.domain.identity.event;

import java.util.Set;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Value
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RoleCreatedEvent {
    String name;
    String description;
    Set<String> permissions;
    String requestId;
}
