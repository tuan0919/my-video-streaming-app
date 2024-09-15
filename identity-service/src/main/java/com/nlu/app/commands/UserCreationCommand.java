package com.nlu.app.commands;

import com.nlu.app.entity.Role;
import lombok.Builder;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.time.LocalDate;

@Value
@Builder
public class UserCreationCommand {

    @TargetAggregateIdentifier
    private final String userId;
    String username;
    String password;
    String email;
    String firstName;
    String lastName;
    LocalDate dob;
    String city;
    Role role;
}
