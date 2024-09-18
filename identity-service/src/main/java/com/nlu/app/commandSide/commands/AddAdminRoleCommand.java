package com.nlu.app.commandSide.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Value
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AddAdminRoleCommand {
    @TargetAggregateIdentifier
    String userId;
}
