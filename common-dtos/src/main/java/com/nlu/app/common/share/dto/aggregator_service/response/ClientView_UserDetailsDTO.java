package com.nlu.app.common.share.dto.aggregator_service.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientView_UserDetailsDTO {
    String userId;
    String username;
    String fullName;
    String region;
    String avatarLink;
    List<String> role;
    String email;
    String bio;
}
