package com.nlu.app.common.event.comment_created;

import com.nlu.app.common.dto.notification.CommentRepliedDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class CommentCreationEvent {
    private final String eventId = UUID.randomUUID().toString();
    private final Long timestamp = System.currentTimeMillis();
    private CommentRepliedDTO dto;

    public CommentCreationEvent(CommentRepliedDTO dto) {
        this.dto = dto;
    }
}
