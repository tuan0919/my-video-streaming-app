package com.nlu.app.commandSide.events;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Value
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserCreatedEvent {
    String username;
    String password;
    String email;
    String firstName;
    String lastName;
    LocalDate dob;
    String city;
}
