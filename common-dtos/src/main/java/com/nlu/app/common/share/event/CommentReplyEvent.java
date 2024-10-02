package com.nlu.app.common.share.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Builder
@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentReplyEvent implements Serializable {
    String commentId;
    String content;
    String userId;
    String parentUserId;
    String parentCommentId;
}
