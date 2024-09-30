package com.nlu.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaStep;
import com.nlu.app.common.share.dto.notification_service.request.NotificationCreationRequest;
import com.nlu.app.common.share.event.NotificationCreatedEvent;
import com.nlu.app.constant.NotificationType;
import com.nlu.app.entity.Notification;
import com.nlu.app.entity.Outbox;
import com.nlu.app.repository.NotificationRepository;
import com.nlu.app.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repository;
    private final OutboxRepository outboxRepository;

    @Transactional
    public String insert(NotificationCreationRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        var notification = Notification.builder()
                .content(request.getContent())
                .time(LocalDateTime.now())
                .type(NotificationType.valueOf(request.getType()))
                .isRead(false)
                .userId(request.getUserId())
                .build();
        var entity = repository.save(notification);
        NotificationCreatedEvent event = NotificationCreatedEvent.builder()
                .userId(notification.getUserId())
                .content(notification.getContent())
                .time(notification.getTime())
                .notificationId(notification.getNotificationId())
                .build();
        try {
            Outbox outbox = Outbox.builder()
                    .payload(objectMapper.writeValueAsString(event))
                    .aggregateType("notification.created")
                    .sagaId(request.getSagaId())
                    .sagaAction(request.getSagaAction())
                    .aggregateId(entity.getUserId())
                    .sagaAction(SagaAction.CREATE_NEW_USER)
                    .sagaStep(SagaStep.NOTIFICATION_CREATE)
                    .sagaStepStatus(true)
                    .build();
            outboxRepository.save(outbox);
        } catch (Exception e) {
            //TODO: thêm các event compensation tại đây
            throw new RuntimeException(e);
        }
        return "OK";
    }
}
