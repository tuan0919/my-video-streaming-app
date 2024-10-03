package com.nlu.app.event.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.common.share.event.CommentReplyEvent;
import com.nlu.app.common.share.event.NewVideoCreatedEvent;
import com.nlu.app.common.share.event.NotificationCreatedEvent;
import com.nlu.app.constant.NotificationType;
import com.nlu.app.entity.CommentNotification;
import com.nlu.app.entity.Notification;
import com.nlu.app.entity.Outbox;
import com.nlu.app.repository.CommentNotificationRepository;
import com.nlu.app.repository.NotificationRepository;
import com.nlu.app.repository.OutboxRepository;
import com.nlu.app.repository.webclient.ProfileWebClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CreateNewVideoHandler {
    NotificationRepository notificationRepository;
    ProfileWebClient profileWebClient;
    OutboxRepository outboxRepository;
    ObjectMapper objectMapper;

    @Transactional
    public void consumeEvent(NewVideoCreatedEvent event, Acknowledgment ack) throws JsonProcessingException {
        /* TODO:
         *  1. Send request to user's profile service for retrieving follower ids.
         *  2. Batch insert all notifications for every one of them to this server database.
         */
        log.info("Consumed event: {}", event);
        var response = profileWebClient.getFollowerIds(event.getUserId()).block().getResult();
        var ids = response.getFollowers();
        var notifications = ids.stream()
                .map(userId -> Notification.builder()
                        .type(NotificationType.INFO)
                        .userId(userId)
                        .content("Your followed user posted new video.")
                        .isRead(false)
                        .time(LocalDateTime.now())
                        .build()
                ).toList();
        notificationRepository.saveAll(notifications); //TODO: temporary solution, need to do this in batching way.
        var events = notifications.stream()
                .map(notify -> NotificationCreatedEvent.builder()
                        .time(notify.getTime())
                        .content(notify.getContent())
                        .notificationId(notify.getNotificationId())
                        .userId(notify.getUserId())
                        .build()
                ).toList();
        List<Outbox> outboxes = new ArrayList<>();
        Outbox outbox = null;
        for (NotificationCreatedEvent ev : events) {
            outbox = Outbox.builder()
                    .payload(objectMapper.writeValueAsString(ev))
                    .aggregateId(ev.getNotificationId())
                    .sagaAction(SagaAction.NOTIFY_TO_FOLLOWERS)
                    .sagaId(ev.getUserId())
                    .sagaStep(SagaAdvancedStep.NOTIFICATION_CREATE)
                    .sagaStepStatus(SagaStatus.SUCCESS)
                    .build();
            outboxes.add(outbox);
        }
        outboxRepository.saveAll(outboxes); //TODO: temporary solution, need to do this in batching way.
        // successfully consumed this message, ack for kafka to know
        ack.acknowledge();
    }
}
