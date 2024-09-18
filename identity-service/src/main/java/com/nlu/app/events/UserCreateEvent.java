package com.nlu.app.events;

import java.time.LocalDate;

import com.nlu.app.entity.Role;

import lombok.Builder;
import lombok.Value;

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
