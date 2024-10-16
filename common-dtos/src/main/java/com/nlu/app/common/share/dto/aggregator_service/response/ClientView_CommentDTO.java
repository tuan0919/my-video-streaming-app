package com.nlu.app.common.share.dto.aggregator_service.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientView_CommentDTO {
    OwnerProfile ownerProfile;
    Comment comment;
    String action;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OwnerProfile {
        String userId;
        String username;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Comment {
        String id;
        String videoId;
        String content;
        String parentId;
        Integer replyCounts;
        Integer likeCounts;
        Integer dislikeCounts;
        LocalDateTime createAt;
    }
}
