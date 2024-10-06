package com.nlu.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.common.share.dto.notification_service.request.NotificationCreationRequest;
import com.nlu.app.common.share.dto.saga.SagaAdvancedRequest;
import com.nlu.app.common.share.event.NotificationCreatedEvent;
import com.nlu.app.constant.NotificationType;
import com.nlu.app.entity.Notification;
import com.nlu.app.entity.Outbox;
import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import com.nlu.app.mapper.NotificationMapper;
import com.nlu.app.mapper.OutboxMapper;
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
    private final OutboxMapper outboxMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public String insert(NotificationCreationRequest request) {
        String sagaId = request.getSagaId();
        String sagaAction = request.getSagaAction();
        var notification = notificationMapper.mapToEntity(request);
        notification = repository.save(notification);
        NotificationCreatedEvent event = notificationMapper.mapToCreatedEvent(notification);
        Outbox outbox = outboxMapper.toSuccessOutbox(event, sagaId, sagaAction);
        outboxRepository.save(outbox);
        return "OK";
    }

    public String insert(SagaAdvancedRequest sagaRequest) {
        String sagaStep = sagaRequest.getSagaStep();
        try {
            switch (sagaStep) {
                case SagaAdvancedStep.NOTIFICATION_CREATE -> {
                    return sagaIdentityCreate(sagaRequest);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationException(ErrorCode.UNKNOWN_EXCEPTION);
        }
        throw new ApplicationException(ErrorCode.UNKNOWN_ACTION);
    }

    @Transactional
    String sagaIdentityCreate(SagaAdvancedRequest sagaRequest) throws JsonProcessingException {
        NotificationCreationRequest request = objectMapper.readValue(sagaRequest.getPayload(), NotificationCreationRequest.class);
        String sagaId = sagaRequest.getSagaId();
        String sagaAction = sagaRequest.getSagaAction();
        var notification = notificationMapper.mapToEntity(request);
        notification = repository.save(notification);
        NotificationCreatedEvent event = notificationMapper.mapToCreatedEvent(notification);
        Outbox outbox = outboxMapper.toSuccessOutbox(event, sagaId, sagaAction);
        outboxRepository.save(outbox);
        return "OK";
    }
}
