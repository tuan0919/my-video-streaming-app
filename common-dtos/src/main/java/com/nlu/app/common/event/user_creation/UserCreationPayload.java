package com.nlu.app.common.event.user_creation;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserCreationPayload {
    String username;
    String userId;
    String email;
}
