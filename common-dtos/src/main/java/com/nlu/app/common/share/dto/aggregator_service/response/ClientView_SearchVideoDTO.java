package com.nlu.app.common.share.dto.aggregator_service.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientView_SearchVideoDTO {
    String videoId;
    String thumbnail;
}
