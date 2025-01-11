package com.nlu.app.event.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.KafkaMessage;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.dto.notification_service.request.NotificationCreationRequest;
import com.nlu.app.common.share.event.NewVideoCreatedEvent;
import com.nlu.app.common.share.event.NotificationCreatedEvent;
import com.nlu.app.common.share.event.ProfileCreatedEvent;
import com.nlu.app.configuration.WebClientBuilder;
import com.nlu.app.constant.NotificationType;
import com.nlu.app.entity.Notification;
import com.nlu.app.entity.Outbox;
import com.nlu.app.mapper.NotificationMapper;
import com.nlu.app.mapper.OutboxMapper;
import com.nlu.app.repository.NotificationRepository;
import com.nlu.app.repository.OutboxRepository;
import com.nlu.app.repository.webclient.IdentityWebClient;
import com.nlu.app.repository.webclient.ProfileWebClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CreateNewUserHandler {
    ObjectMapper objectMapper;
    NotificationRepository notificationRepository;
    OutboxRepository outboxRepository;
    NotificationMapper notificationMapper;
    OutboxMapper outboxMapper;

    @Transactional
    public void consumeEvent(KafkaMessage message, Acknowledgment ack) throws JsonProcessingException {
        var event = objectMapper.readValue(message.payload(), ProfileCreatedEvent.class);
        String welcomeMessage = String.format("Xin chào %s đến với hệ thống", event.getFullName());
        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .relatedEvent("NEW_USER_CREATED_EVENT")
                .title("Chào mừng đến với hệ thống")
                .content(welcomeMessage)
                .isRead(false)
                .type(NotificationType.INFO)
                .time(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
        var createdEvent = notificationMapper.mapToCreatedEvent(notification);
        var outbox = outboxMapper.toSuccessOutbox(createdEvent, event.getUserId(), SagaAction.CREATE_NEW_NOTIFICATION);
        outboxRepository.save(outbox);
        log.info("consumed thành công event: {}", message.sagaAction());
        ack.acknowledge();
    }
}
