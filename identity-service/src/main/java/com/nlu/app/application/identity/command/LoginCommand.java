package com.nlu.app.application.identity.command;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class LoginCommand {
    @TargetAggregateIdentifier
    String userId;
    String username;
    String password;
}
