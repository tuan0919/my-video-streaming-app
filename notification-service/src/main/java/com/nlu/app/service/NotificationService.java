package com.nlu.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.common.share.dto.notification_service.request.NotificationCreationRequest;
import com.nlu.app.common.share.event.NotificationCreatedEvent;
import com.nlu.app.constant.NotificationType;
import com.nlu.app.entity.Notification;
import com.nlu.app.entity.Outbox;
import com.nlu.app.mapper.NotificationMapper;
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
    private final NotificationMapper notificationMapper;

    @Transactional
    public String insert(NotificationCreationRequest request) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        var notification = notificationMapper.mapToEntity(request);
        NotificationCreatedEvent event = notificationMapper.mapToCreatedEvent(notification);
        Outbox outbox = null;
        try {
            notification = repository.save(notification);
            outbox = Outbox.builder()
                    .payload(objectMapper.writeValueAsString(event))
                    .aggregateType("notification.topics")
                    .sagaId(request.getSagaId())
                    .sagaAction(request.getSagaAction())
                    .aggregateId(notification.getNotificationId())
                    .sagaAction(request.getSagaAction())
                    .sagaStep(SagaAdvancedStep.NOTIFICATION_CREATE)
                    .sagaStepStatus(SagaStatus.SUCCESS)
                    .build();
//            throw new Exception ("My exception");
        } catch (Exception e) {
            //TODO: thêm các event compensation tại đây
            outbox = Outbox.builder()
                    .payload(objectMapper.writeValueAsString(event))
                    .aggregateType("notification.topics")
                    .sagaId(request.getSagaId())
                    .sagaAction(request.getSagaAction())
                    .aggregateId(notification.getNotificationId())
                    .sagaAction(request.getSagaAction())
                    .sagaStep(SagaAdvancedStep.NOTIFICATION_CREATE)
                    .sagaStepStatus(SagaStatus.FAILED)
                    .build();
        } finally {
            outboxRepository.save(outbox);
        }
        return "OK";
    }
}
