package com.nlu.app.common.share.dto.comment_service.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse implements Serializable {
    String id;
    String videoId;
    String userId;
    String content;
    String parentId;
    Integer replyCounts;
}
