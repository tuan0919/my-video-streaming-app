package com.nlu.app.share.events;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Value
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserCreatedEvent {
    String userId;
    String username;
    String password;
    String email;
    String firstName;
    String lastName;
    LocalDate dob;
    String city;
}
