package com.nlu.app.commandSide.events;

import com.nlu.app.querySide.entity.Role;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Value
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RoleAddedEvent {
    String userId;
    Role role;
}
