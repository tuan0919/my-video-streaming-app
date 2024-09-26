package com.nlu.app.dto.webclient.profile.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileCreationResponse {
    String userId;
    String fullName;
    String country;
    String bio;
}
