package com.nlu.app.event.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.KafkaMessage;
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
import com.nlu.app.mapper.NotificationMapper;
import com.nlu.app.mapper.OutboxMapper;
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
    NotificationMapper notificationMapper;
    OutboxMapper outboxMapper;

    /**
     * Consume sự kiện {@link NewVideoCreatedEvent}, sau đó tiến hành lấy toàn bộ user id đang theo dõi người đăng video hiện tại,
     * để tạo một loạt thông báo tương ứng tới họ.
     * @param message
     * @param ack
     * @throws JsonProcessingException
     */
    @Transactional
    public void consumeEvent(KafkaMessage message, Acknowledgment ack) throws JsonProcessingException {
        var event = objectMapper.readValue(message.payload(), NewVideoCreatedEvent.class);
        var response = profileWebClient.getFollowerIds(event.getUserId()).block().getResult();
        var ids = response.getFollowers();
        var notifications = ids.stream()
                .map(userId -> notificationMapper.forNotifyFollower(userId, "Your followed user posted new video."))
                .toList();
        notificationRepository.saveAll(notifications);
        var events = notifications.stream()
                .map(notify -> notificationMapper.mapToCreatedEvent(notify)).toList();
        List<Outbox> outboxes = new ArrayList<>();
        Outbox outbox = null;
        for (NotificationCreatedEvent ev : events) {
            outbox = outboxMapper.toSuccessOutbox(ev, ev.getUserId(), SagaAction.NOTIFY_TO_FOLLOWERS);
            outboxes.add(outbox);
        }
        outboxRepository.saveAll(outboxes);
        ack.acknowledge();
    }
}
