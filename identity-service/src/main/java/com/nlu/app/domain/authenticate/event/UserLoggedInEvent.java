package com.nlu.app.domain.authenticate.event;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;

@Value
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserLoggedInEvent {
    String userId;
    String username;
    ArrayList<String> roles;
}
