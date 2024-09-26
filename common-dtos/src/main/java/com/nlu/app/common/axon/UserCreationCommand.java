package com.nlu.app.common.axon;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Builder
@Value
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserCreationCommand {
    @TargetAggregateIdentifier
    String id;
    String username;
    String password;
    String email;
    Boolean verified;
}
