package com.nlu.app.commandSide.commands;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Value
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CreateUserCommand {
    String username;
    String password;
    String email;
    String firstName;
    String lastName;
    LocalDate dob;
    String city;
}