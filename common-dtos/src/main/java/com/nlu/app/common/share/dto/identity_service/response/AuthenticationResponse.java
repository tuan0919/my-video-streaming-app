package com.nlu.app.common.share.dto.identity_service.response;

import java.io.Serializable;
import java.util.Date;

import lombok.*;
import lombok.experimental.FieldDefaults;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse implements Serializable {
    String token;
    Date expiryTime;
}
