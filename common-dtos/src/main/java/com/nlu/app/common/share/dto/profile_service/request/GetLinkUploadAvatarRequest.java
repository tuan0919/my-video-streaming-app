package com.nlu.app.common.share.dto.profile_service.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GetLinkUploadAvatarRequest {
    String fileName;
}
