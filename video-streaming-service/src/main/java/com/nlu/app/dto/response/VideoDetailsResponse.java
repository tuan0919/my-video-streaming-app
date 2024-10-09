package com.nlu.app.dto.response;

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
    String name;
    String link;
    String description;
    LocalDateTime createAt;
    Integer viewCount;
    @Builder.Default
    Float progress = 0f;
    @Builder.Default
    String action = null;
}
