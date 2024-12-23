package com.nlu.app.common.share.dto.aggregator_service.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientView_VideoDetailsDTO {
    OwnerProfile ownerProfile;
    VideoStats stat;
    Boolean isFollowed;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OwnerProfile {
        String userId;
        String username;
        String fullName;
        String avatar;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class VideoStats {
        String videoId;
        String name;
        String link;
        String thumbnail;
        String description;
        Integer viewCount;
        Integer downVote;
        Integer upVote;
        @Builder.Default
        Float progress = 0f;
        @Builder.Default
        String action = null;
        String createTime;
    }
}
