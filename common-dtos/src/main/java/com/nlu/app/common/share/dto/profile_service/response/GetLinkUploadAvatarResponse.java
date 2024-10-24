package com.nlu.app.common.share.dto.profile_service.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GetLinkUploadAvatarResponse {
    String key;
}
