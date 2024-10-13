package com.nlu.app.mapper;

import com.nlu.app.common.share.dto.comment_service.response.CommentResponse;
import com.nlu.app.common.share.event.CommentReplyEvent;
import com.nlu.app.dto.request.CommentCreationRequestDTO;
import com.nlu.app.entity.Comment;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true))
public interface CommentMapper {
    @Mappings({
            @Mapping(target = "content", source = "request.content"),
            @Mapping(target = "videoId", source = "request.videoId"),
            @Mapping(target = "userId", source = "userId"),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createAt", ignore = true),
            @Mapping(target = "updateAt", ignore = true),
            @Mapping(target = "reply", ignore = true)
    })
    Comment mapToEntity(String userId, CommentCreationRequestDTO request);

    @Mappings({
            @Mapping(target = "commentId", source = "comment.id"),
            @Mapping(target = "parentCommentId", source = "comment.parent.id"),
            @Mapping(target = "parentUserId", source = "comment.parent", qualifiedByName = "mapToId")
    })
    CommentReplyEvent mapToCommentReplyEvent(Comment comment);

    @Mappings({
            @Mapping(target = "parentId", source = "comment.parent", qualifiedByName = "mapToId")
    })
    CommentResponse mapToDTO(Comment comment, Integer replyCounts);

    @Named("mapToId")
    default String mapToId(Comment comment) {
        if (comment != null)
            return comment.getId();
        else
            return null;
    }
}
