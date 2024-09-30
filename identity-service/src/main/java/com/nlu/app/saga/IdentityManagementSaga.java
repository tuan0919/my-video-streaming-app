package com.nlu.app.saga;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.SagaCompensationStep;
import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.common.share.dto.notification_service.request.NotificationCreationRequest;
import com.nlu.app.common.share.dto.profile_service.request.ProfileCreationRequest;
import com.nlu.app.common.share.dto.CompensationRequest;
import com.nlu.app.common.share.event.NotificationCreatedEvent;
import com.nlu.app.common.share.event.ProfileCreatedEvent;
import com.nlu.app.entity.SagaLog;
import com.nlu.app.repository.SagaLogRepository;
import com.nlu.app.repository.webclient.NotificationWebClient;
import com.nlu.app.repository.webclient.ProfileWebClient;
import com.nlu.app.service.CompensationService;
import com.nlu.app.service.SagaLogService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import com.nlu.app.common.share.event.UserCreatedEvent;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class IdentityManagementSaga {
    ProfileWebClient profileWebClient;
    NotificationWebClient notificationWebClient;
    ObjectMapper objectMapper;
    SagaLogService sagaLogService;
    CompensationService compensationService;

    private static final Set<String> PROCEED_STEPS = Set.of(
            SagaAdvancedStep.IDENTITY_CREATE,
            SagaAdvancedStep.PROFILE_CREATE,
            SagaAdvancedStep.NOTIFICATION_CREATE
    );

    private static final Map<String, List<String>> COMPENSATION_MAP = Map.of(
            SagaAdvancedStep.NOTIFICATION_CREATE, List.of(
                    SagaCompensationStep.COMPENSATION_NOTIFICATION_CREATE,
                    SagaCompensationStep.COMPENSATION_PROFILE_CREATE,
                    SagaCompensationStep.COMPENSATION_IDENTITY_CREATE
            ),
            SagaAdvancedStep.PROFILE_CREATE, List.of(
                    SagaCompensationStep.COMPENSATION_PROFILE_CREATE,
                    SagaCompensationStep.COMPENSATION_IDENTITY_CREATE
            ),
            SagaAdvancedStep.IDENTITY_CREATE, List.of(
                    SagaCompensationStep.COMPENSATION_IDENTITY_CREATE
            )
    );

    @Transactional
    @KafkaListener(topics = "identity.created", groupId = "saga.create_new_user")
    public void userCreated(@Payload String payload,
                            @Header ("sagaAction") String sagaAction,
                            @Header ("sagaStep") String sagaStep,
                            @Header ("id") String eventId,
                            @Header ("sagaStepStatus") String sagaStepStatus,
                            @Header ("sagaId") String sagaId,
                            Acknowledgment ack ) throws JsonProcessingException {
        log.info("sagaAction: {}, sagaStep: {}, sagaId: {}", sagaAction, sagaStep, sagaId);
        // TODO: checking if eventId is consumed or not
        log.info("Saga user start: {}", payload);
        // update saga log
        updateSagaLog(sagaId, eventId, sagaAction, sagaStep, sagaStepStatus);
        if (!sagaStep.equals(SagaAdvancedStep.IDENTITY_CREATE)) {
            ack.acknowledge();
            return;
        }
        switch (sagaStepStatus) {
            case SagaStatus.SUCCESS -> {
                UserCreatedEvent event = objectMapper.readValue(payload, UserCreatedEvent.class);
                var profileCreate = ProfileCreationRequest.builder()
                        .bio("none")
                        .userId(event.getUserId())
                        .sagaId(sagaId)
                        .sagaAction(SagaAction.CREATE_NEW_USER)
                        .country("vn")
                        .fullName("")
                        .build();
                try {
                    profileWebClient
                            .createProfile(profileCreate).block();
                    ack.acknowledge();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            case SagaStatus.FAILED -> {
                handleCompensation(sagaStep, sagaId);
                ack.acknowledge();
            }
        }
    }

    @KafkaListener(topics = "notification.created", groupId = "saga.create_new_user")
    @Transactional
    public void notificationCreated(@Payload String payload,
                                    @Header ("sagaAction") String sagaAction,
                                    @Header ("sagaStep") String sagaStep,
                                    @Header ("id") String eventId,
                                    @Header ("sagaStepStatus") String sagaStepStatus,
                                    @Header ("sagaId") String sagaId,
                                    Acknowledgment ack ) throws JsonProcessingException {
        // TODO: checking if key is already exists (this event is consumed)
        log.info("sagaAction: {}, sagaStep: {}, sagaId: {}", sagaAction, sagaStep, sagaId);
        // update saga log
        updateSagaLog(sagaId, eventId, sagaAction, sagaStep, sagaStepStatus);
        if (!sagaStep.equals(SagaAdvancedStep.NOTIFICATION_CREATE)) {
            ack.acknowledge();
            return;
        }
        switch (sagaStepStatus) {
            case SagaStatus.SUCCESS -> {
                NotificationCreatedEvent event = objectMapper.readValue(payload, NotificationCreatedEvent.class);
                log.info("Notification created successfully: {}", payload);
                ack.acknowledge();
            }
            case SagaStatus.FAILED -> {
                try {
                    handleCompensation(sagaStep, sagaId);
                    ack.acknowledge();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Transactional
    @KafkaListener(topics = "profile.created", groupId = "saga.create_new_user")
    public void profileCreated(@Payload String payload,
                               @Header ("sagaAction") String sagaAction,
                               @Header ("sagaStep") String sagaStep,
                               @Header ("id") String eventId,
                               @Header ("sagaStepStatus") String sagaStepStatus,
                               @Header ("sagaId") String sagaId,
                               Acknowledgment ack ) throws JsonProcessingException {
        log.info("sagaAction: {}, sagaStep: {}, sagaId: {}", sagaAction, sagaStep, sagaId);
        // update saga log
        updateSagaLog(sagaId, eventId, sagaAction, sagaStep, sagaStepStatus);
        if (!sagaStep.equals(SagaAdvancedStep.PROFILE_CREATE)) {
            ack.acknowledge();
            return;
        }
        switch (sagaStepStatus) {
            case SagaStatus.SUCCESS -> {
                ProfileCreatedEvent event = objectMapper.readValue(payload, ProfileCreatedEvent.class);
                log.info("profile created: {}", event);
                try {
                    var createNotification = NotificationCreationRequest.builder()
                            .type("INFO")
                            .userId(event.getUserId())
                            .content(String.format("Welcome userId %s to our service", event.getUserId()))
                            .sagaId(sagaId)
                            .sagaAction(SagaAction.CREATE_NEW_USER)
                            .build();
                    notificationWebClient.createNotification(createNotification).block();
                    ack.acknowledge();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            case SagaStatus.FAILED -> {
                handleCompensation(sagaStep, sagaId);
                ack.acknowledge();
            }
        }
    }

    public void handleCompensation(String sagaStep, String sagaId) throws JsonProcessingException {
        List<String> compensationSteps = COMPENSATION_MAP.get(sagaStep);
        if (compensationSteps != null) {
            for (String step : compensationSteps) {
                // Gọi các service thực hiện từng bước bồi thường
                executeCompensationStep(step, sagaId);
            }
        }
    }

    private void executeCompensationStep(String step, String sagaId) throws JsonProcessingException {
        // Thực hiện bồi thường cho từng step
        switch (step) {
            case SagaCompensationStep.COMPENSATION_NOTIFICATION_CREATE -> {
                compensationForNotification(sagaId);
            }
            case SagaCompensationStep.COMPENSATION_PROFILE_CREATE -> {
                compensationForProfile(sagaId);
            }
            case SagaCompensationStep.COMPENSATION_IDENTITY_CREATE -> {
                compensationForIdentity(sagaId);
            }
        }
    }

    private void compensationForProfile(String sagaId) {
        var rollbackRequest = CompensationRequest.builder()
                .sagaId(sagaId)
                .build();
        profileWebClient.compensation(rollbackRequest).block();
    }

    private void compensationForIdentity(String sagaId) throws JsonProcessingException {
        compensationService.doCompensation(sagaId);
    }

    private void compensationForNotification(String sagaId) {
        var rollbackRequest = CompensationRequest.builder()
                .sagaId(sagaId)
                .build();
        notificationWebClient.compensation(rollbackRequest).block();
    }

    private void updateSagaLog(String sagaId, String eventId, String sagaAction, String sagaStep, String status) {
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
