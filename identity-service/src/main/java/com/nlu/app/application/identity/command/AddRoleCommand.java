package com.nlu.app.application.identity.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Value
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AddRoleCommand {
    @TargetAggregateIdentifier
    String userId;

    String roleName;
}
