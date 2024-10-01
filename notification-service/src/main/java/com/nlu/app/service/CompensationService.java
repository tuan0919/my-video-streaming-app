package com.nlu.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.SagaCompensationStep;
import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.common.share.event.NotificationRemovedEvent;
import com.nlu.app.common.share.event.UserRemovedEvent;
import com.nlu.app.entity.Outbox;
import com.nlu.app.repository.NotificationRepository;
import com.nlu.app.repository.OutboxRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CompensationService {
    OutboxRepository outboxRepository;
    ObjectMapper objectMapper;
    NotificationRepository notificationRepository;
    @Transactional
    public void doCompensation(String sagaId) throws JsonProcessingException {
        var outboxLogs = outboxRepository.findAllBySagaId(sagaId);
        for (var log : outboxLogs) {
            switch (log.getSagaStep()) {
                case SagaAdvancedStep.NOTIFICATION_CREATE -> forNotificationCreate(log);
            }
        }
    }

    @Transactional
    void forNotificationCreate(Outbox log) throws JsonProcessingException {
        String notificationId = log.getAggregateId();
        if (notificationRepository.findById(notificationId).isPresent()) {
            notificationRepository.deleteById(notificationId);
        }
        var event = NotificationRemovedEvent
                .builder().notificationId(notificationId).build();
        var outbox = Outbox.builder()
                .aggregateType("notification.topics")
                .sagaAction(log.getSagaAction())
                .sagaId(log.getSagaId())
                .sagaStepStatus(SagaStatus.SUCCESS)
                .sagaStep(SagaCompensationStep.COMPENSATION_NOTIFICATION_CREATE)
                .aggregateId(notificationId)
                .payload(objectMapper.writeValueAsString(event))
                .build();
        outboxRepository.save(outbox);
    }
}
