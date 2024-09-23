package com.nlu.app.application.identity.command;

import java.util.Set;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Value
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CreateRoleCommand {
    String name;
    String description;
    Set<String> permissions;
}
