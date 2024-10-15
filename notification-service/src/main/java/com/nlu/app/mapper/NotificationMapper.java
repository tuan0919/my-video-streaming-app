package com.nlu.app.mapper;

import com.nlu.app.common.share.dto.notification_service.request.NotificationCreationRequest;
import com.nlu.app.common.share.dto.notification_service.response.NotificationResponse;
import com.nlu.app.common.share.event.comment.CommentReplyEvent;
import com.nlu.app.common.share.event.NewVideoCreatedEvent;
import com.nlu.app.common.share.event.NotificationCreatedEvent;
import com.nlu.app.constant.NotificationType;
import com.nlu.app.entity.Notification;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true))
public interface NotificationMapper {

    @Mapping(target = "type", source = "request.type", qualifiedByName = "getEnum")
    Notification mapToEntity(NotificationCreationRequest request);
    NotificationCreatedEvent mapToCreatedEvent(Notification notification);

    @Mappings({
            @Mapping(target = "relatedObjectId", source = "event.videoId"),
            @Mapping(target = "title", constant = "Bạn có video mới"),
            @Mapping(target = "userId", source = "followerId"),
            @Mapping(target = "content", source = "content"),
            @Mapping(target = "type", expression = "java(getByName(\"INFO\"))"),
            @Mapping(target = "relatedEvent", constant = "NEW_VIDEO_CREATED_EVENT")
    })
    Notification forNotifyFollower(NewVideoCreatedEvent event, String followerId, String content);

    @Mappings({
            @Mapping(target = "relatedObjectId", source = "event.commentId"),
            @Mapping(target = "title", constant = "Comment của bạn được reply"),
            @Mapping(target = "type", expression = "java(getByName(\"INFO\"))"),
            @Mapping(target = "content", source = "content"),
            @Mapping(target = "userId", source = "event.parentUserId"),
            @Mapping(target = "relatedEvent", constant = "COMMENT_REPLY_EVENT")
    })
    Notification forCommentReply(CommentReplyEvent event, String content);

    NotificationResponse mapToDTO(Notification notification);

    @Named("getEnum")
    default NotificationType getByName(String name) {
        return NotificationType.valueOf(name);
    }
}
