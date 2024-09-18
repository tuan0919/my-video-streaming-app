package com.nlu.app.commands;

import java.time.LocalDate;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import com.nlu.app.entity.Role;

import lombok.Builder;
import lombok.Value;

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
