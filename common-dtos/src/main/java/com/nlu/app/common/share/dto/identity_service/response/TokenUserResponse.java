package com.nlu.app.common.share.dto.identity_service.response;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenUserResponse {
    String userId;
    String username;
    List<String> roles;
}
