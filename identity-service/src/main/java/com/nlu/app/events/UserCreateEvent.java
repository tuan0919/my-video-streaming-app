package com.nlu.app.events;

import com.nlu.app.entity.Role;
import lombok.Builder;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.time.LocalDate;

@Value
@Builder
public class UserCreateEvent {
    String userId;
    String username;
    String password;
    String email;
    String firstName;
    String lastName;
    LocalDate dob;
    Role role;
    String city;
}
