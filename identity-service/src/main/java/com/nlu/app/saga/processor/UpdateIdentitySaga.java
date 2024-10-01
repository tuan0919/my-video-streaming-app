package com.nlu.app.saga.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.SagaCompensationStep;
import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.common.share.dto.CompensationRequest;
import com.nlu.app.common.share.dto.notification_service.request.NotificationCreationRequest;
import com.nlu.app.common.share.dto.profile_service.request.ProfileCreationRequest;
import com.nlu.app.common.share.event.ProfileCreatedEvent;
import com.nlu.app.common.share.event.UserCreatedEvent;
import com.nlu.app.entity.SagaLog;
import com.nlu.app.repository.webclient.NotificationWebClient;
import com.nlu.app.repository.webclient.ProfileWebClient;
import com.nlu.app.saga.KafkaMessage;
import com.nlu.app.service.CompensationService;
import com.nlu.app.service.SagaLogService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UpdateIdentitySaga {
    NotificationWebClient notificationWebClient;
    ObjectMapper objectMapper;
    SagaLogService sagaLogService;
    CompensationService compensationService;

    private static final Set<String> PROCEED_STEPS = Set.of(
            SagaAdvancedStep.IDENTITY_UPDATE,
            SagaAdvancedStep.NOTIFICATION_CREATE
    );

    private static final Map<String, List<String>> COMPENSATION_MAP = Map.of(
            SagaAdvancedStep.NOTIFICATION_CREATE, List.of(
                    SagaCompensationStep.COMPENSATION_NOTIFICATION_CREATE,
                    SagaCompensationStep.COMPENSATION_IDENTITY_UPDATE
            ),
            SagaAdvancedStep.IDENTITY_UPDATE, List.of(
                    SagaCompensationStep.COMPENSATION_IDENTITY_UPDATE
            )
    );

    @Transactional
    public void consumeMessage(KafkaMessage message) throws JsonProcessingException {
        switch (message.sagaStepStatus()) {
            case SagaStatus.SUCCESS -> this.handleSuccessStep(message.sagaStep(), message.sagaId(), message.payload());
            case SagaStatus.FAILED -> this.handleFailedStep(message.sagaStep(), message.sagaId());
        }
        this.updateSagaLog(message.sagaId(), message.eventId(), message.sagaAction(), message.sagaStep(), message.sagaStepStatus());
    }

    public void handleSuccessStep(String sagaStep, String sagaId, String payload) throws JsonProcessingException {
        switch (sagaStep) {
            case SagaAdvancedStep.IDENTITY_UPDATE -> {
                requestNotification(sagaId, payload);
            }
            case SagaAdvancedStep.NOTIFICATION_CREATE -> {
                onSagaSuccess(sagaId, payload);
                // TODO: success notification creation
            }
            case SagaCompensationStep.COMPENSATION_NOTIFICATION_CREATE -> {
                // TODO: do something to announce about ending of compensation
                compensationForIdentity(sagaId);
            }
            case SagaCompensationStep.COMPENSATION_IDENTITY_UPDATE -> {
                // TODO: do something to announce about ending of compensation
            }
        }
    }

    public void handleFailedStep(String sagaStep, String sagaId) throws JsonProcessingException {
        switch (sagaStep) {
            case SagaAdvancedStep.IDENTITY_UPDATE -> compensationForIdentity(sagaId);
            case SagaAdvancedStep.NOTIFICATION_CREATE -> compensationForNotification(sagaId);
            case SagaCompensationStep.COMPENSATION_NOTIFICATION_CREATE -> {
                // TODO: solve failed case for compensation
            }
            case SagaCompensationStep.COMPENSATION_IDENTITY_UPDATE -> {
                // TODO: solve failed case for compensation
            }
        }
    }

    private void onSagaSuccess(String sagaId, String payload) {
        log.info("Notification created successfully: {}", payload);
    }

    private void requestNotification(String sagaId, String payload) throws JsonProcessingException {
        ProfileCreatedEvent event = objectMapper.readValue(payload, ProfileCreatedEvent.class);
        var createNotification = NotificationCreationRequest.builder()
                .type("WARNING")
                .userId(event.getUserId())
                .content(String.format("Your login information has been changed.", event.getUserId()))
                .sagaId(sagaId)
                .sagaAction(SagaAction.UPDATE_IDENTITY)
                .build();
        notificationWebClient.createNotification(createNotification).block();
    }

    private void compensationForIdentity(String sagaId) throws JsonProcessingException {
        compensationService.doCompensation(sagaId);
    }

    private void compensationForNotification(String sagaId) {
        notificationWebClient.compensation(CompensationRequest.builder().sagaId(sagaId).build()).block();
    }

    public void updateSagaLog(String sagaId, String eventId, String sagaAction, String sagaStep, String status) {
        var sagaLog = SagaLog.builder()
                .sagaId(sagaId)
                .id(eventId)
                .sagaStep(sagaStep)
                .status(status)
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .sagaAction(sagaAction)
                .build();
        sagaLogService.addSagaLog(sagaLog, PROCEED_STEPS, COMPENSATION_MAP);
    }
}