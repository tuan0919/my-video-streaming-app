package com.nlu.app.common.share.event.comment;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Builder
@Value
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ReactedToCommentEvent {
    String commentId;
    String userId;
    String action; // null ; LIKE ; DISLIKE
}
