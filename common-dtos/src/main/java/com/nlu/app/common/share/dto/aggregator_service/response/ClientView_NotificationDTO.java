package com.nlu.app.common.share.dto.aggregator_service.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientView_NotificationDTO {
    String id;
    String content;
    String createTime;
    String thumbnail;
    String href;
    String relatedObjectId;
    String avatar;
    String relatedEvent;
}
