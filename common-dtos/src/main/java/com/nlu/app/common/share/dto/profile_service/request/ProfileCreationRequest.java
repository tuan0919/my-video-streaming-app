package com.nlu.app.common.share.dto.profile_service.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileCreationRequest {
    String userId;
    String fullName;
    String country;
    String bio;
    String sagaId;
    String sagaAction;
}
