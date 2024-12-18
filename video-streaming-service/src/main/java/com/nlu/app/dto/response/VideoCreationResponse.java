package com.nlu.app.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VideoCreationResponse {
    String videoId;
    String description;
    String videoName;
    String videoURL;
    LocalDateTime createAt;
}
