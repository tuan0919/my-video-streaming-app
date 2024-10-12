package com.nlu.app.mapper;

import com.nlu.app.common.share.event.CommentReplyEvent;
import com.nlu.app.dto.request.CommentCreationRequestDTO;
import com.nlu.app.entity.Comment;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {
    @Mappings({
            @Mapping(target = "content", source = "request.content"),
            @Mapping(target = "videoId", source = "request.videoId"),
            @Mapping(target = "userId", source = "userId"),
            @Mapping(target = "parent", source = "parent")
    })
    Comment mapToDTO(String userId, CommentCreationRequestDTO request, Comment parent);

    @Mappings({
            @Mapping(target = "commentId", source = "comment.id"),
            @Mapping(target = "parentCommentId", source = "comment.parent.id"),
            @Mapping(target = "parentUserId", source = "comment.parent.userId")
    })
    CommentReplyEvent mapToCommentReplyEvent(Comment comment);
}
