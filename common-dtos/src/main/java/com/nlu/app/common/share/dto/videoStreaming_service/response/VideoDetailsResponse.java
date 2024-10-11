package com.nlu.app.common.share.dto.videoStreaming_service.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VideoDetailsResponse {
    String videoId;
    String ownerId;
    String name;
    String link;
    String description;
    LocalDateTime createAt;
    Integer viewCount;
    @Builder.Default
    Float progress = 0f;
    @Builder.Default
    String action = null;
    Integer downVote;
    Integer upVote;
}
