package com.nlu.app.common.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentRepliedDTO {
    private String replierId;
    private String replierCommentId;
    private String userId;
    private String userCommentId;
    private String content;
}
