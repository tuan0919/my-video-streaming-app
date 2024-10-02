package com.nlu.app.event.handler;

import com.nlu.app.common.share.event.CommentReplyEvent;
import com.nlu.app.constant.NotificationType;
import com.nlu.app.entity.CommentNotification;
import com.nlu.app.entity.Notification;
import com.nlu.app.repository.CommentNotificationRepository;
import com.nlu.app.repository.NotificationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserReplyHandler {
    NotificationRepository notificationRepository;
    CommentNotificationRepository repository;

    public void consumeEvent(CommentReplyEvent event) {
        String userId = event.getUserId();
        String parentCommentId = event.getParentCommentId();
        // TODO: notification for the user with corresponding userId
        var notification = Notification.builder()
                .time(LocalDateTime.now())
                .type(NotificationType.INFO)
                .content("You have someone replied your comment.")
                .isRead(false)
                .userId(userId)
                .build();
        notificationRepository.save(notification); // FIXME: need to send event for this notification's creation as well.

        /* TODO:
         *  link this comment id with corresponding notification for reference them in future.
         *  in case this comment is deleted, notifications which related to it will be deleted,
         *  that's why we need a way to reference the notifications based on commentId.
         */
        var link = CommentNotification.builder()
                .commentId(parentCommentId)
                .notificationId(notification.getNotificationId())
                .build();
        repository.save(link);
    }
}
