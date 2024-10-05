package com.nlu.app.common.share.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Builder
@Value
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CommentReplyEvent implements Serializable {
    String commentId;
    String content;
    String userId;
    String parentUserId;
    String parentCommentId;
}
