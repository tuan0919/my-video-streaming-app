package com.nlu.app.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VideoCreationRequest {
    String videoKey;
    String thumbnailKey;
    String description;
    String videoName;
}
