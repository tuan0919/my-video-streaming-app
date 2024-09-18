package com.nlu.app.querySide.events;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Value
@Builder
@Data
public class UserCreateEvent {
    String username;
    String password;
    String email;
    String firstName;
    String lastName;
    LocalDate dob;
    String city;
}
