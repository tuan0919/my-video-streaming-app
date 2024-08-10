package com.nlu.app.dto.webclient.identity.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenUserResponse {
    String username;
    List<String> roles;
    String userId;
}
