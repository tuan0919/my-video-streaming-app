package com.nlu.app.common.share.dto.aggregator_service.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientView_VideoDetailsDTO {
    OwnerProfile ownerProfile;
    VideoStats stat;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OwnerProfile {
        String userId;
        String username;
        String fullName;
        Boolean isFollowed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class VideoStats {
        String videoId;
        String videoName;
        String videoLink;
        String videoDescription;
        Integer videoViewCount;
        Integer videoDownVote;
        Integer videoUpVote;
        @Builder.Default
        Float userProgress = 0f;
        @Builder.Default
        String userAction = null;
    }
}
