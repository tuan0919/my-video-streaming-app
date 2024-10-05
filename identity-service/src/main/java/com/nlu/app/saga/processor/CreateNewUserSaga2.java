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
import com.nlu.app.common.share.event.NotificationCreatedEvent;
import com.nlu.app.common.share.event.ProfileCreatedEvent;
import com.nlu.app.common.share.event.UserCreatedEvent;
import com.nlu.app.mapper.SagaMapper;
import com.nlu.app.repository.webclient.NotificationWebClient;
import com.nlu.app.repository.webclient.ProfileWebClient;
import com.nlu.app.saga.KafkaMessage;
import com.nlu.app.saga.SagaError;
import com.nlu.app.saga.SagaLog;
import com.nlu.app.service.CompensationService;
import com.nlu.app.service.SagaLogService;
import com.nlu.app.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CreateNewUserSaga2 {
    ProfileWebClient profileWebClient;
    NotificationWebClient notificationWebClient;
    UserService userService;
    ObjectMapper objectMapper;
    SagaLogService sagaLogService;
    CompensationService compensationService;
    RedisTemplate<String, Object> redisTemplate;
    SagaMapper sagaMapper;

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

    public void consumeMessage(KafkaMessage message, Acknowledgment ack) throws JsonProcessingException {
        String sagaStep = message.sagaStep();
        try {
            switch (sagaStep) {
                case SagaAdvancedStep.IDENTITY_CREATE -> {
                    requestProfile(message, ack);
                }
                case SagaAdvancedStep.PROFILE_CREATE -> {
                    requestNotification(message, ack);
                }
                case SagaAdvancedStep.NOTIFICATION_CREATE -> {
                    onSagaSuccess(message, ack);
                }
                case SagaCompensationStep.COMPENSATION_NOTIFICATION_CREATE -> compensationForProfile(message.sagaId());
                case SagaCompensationStep.COMPENSATION_PROFILE_CREATE -> compensationForIdentity(message.sagaId());
                case SagaCompensationStep.COMPENSATION_IDENTITY_CREATE -> {
                    // TODO: do something to announce about ending of compensation
                }
            }
        }
        catch (WebClientResponseException e) {
            e.printStackTrace();
            SagaError sagaError = sagaMapper.mapToSagaError(e, message);
            this.compensation(message, ack);
            redisTemplate.opsForValue().set("SAGA_ABORTED_"+message.sagaId(), sagaError, Duration.ofMinutes(3));
        }
    }

    private void onSagaSuccess(KafkaMessage message, Acknowledgment ack) throws JsonProcessingException {
        var event = objectMapper.readValue(message.payload(), NotificationCreatedEvent.class);
        var response = userService.getUser(event.getUserId());
        redisTemplate.opsForValue().set("SAGA_COMPLETED_"+message.sagaId(), response, Duration.ofMinutes(3));
        var successStep = sagaMapper.mapToSuccessLog(message);
        sagaLogService.addSagaLog(successStep, PROCEED_STEPS, COMPENSATION_MAP);
        ack.acknowledge();
    }

    @Transactional
    void requestProfile(KafkaMessage message, Acknowledgment ack) throws JsonProcessingException {
        String payload = message.payload();
        String sagaId = message.sagaId();
        UserCreatedEvent event = objectMapper.readValue(payload, UserCreatedEvent.class);
        var profileCreate = ProfileCreationRequest.builder()
                    .bio("none")
                    .userId(event.getUserId())
                    .sagaId(sagaId)
                    .sagaAction(SagaAction.CREATE_NEW_USER)
                    .country("vn")
                    .fullName("")
                    .build();
        profileWebClient.createProfile(profileCreate).block();
        var successStep = sagaMapper.mapToSuccessLog(message);
        sagaLogService.addSagaLog(successStep, PROCEED_STEPS, COMPENSATION_MAP);
        ack.acknowledge();
    }

    @Transactional
    void requestNotification(KafkaMessage message, Acknowledgment ack) throws JsonProcessingException {
        ProfileCreatedEvent event = objectMapper.readValue(message.payload(), ProfileCreatedEvent.class);
        var createNotification = NotificationCreationRequest.builder()
                .type("INFO")
                .userId(event.getUserId())
                .content(String.format("Welcome userId %s to our service", event.getUserId()))
                .sagaId(message.sagaId())
                .sagaAction(SagaAction.CREATE_NEW_USER)
                .build();
        notificationWebClient.createNotification(createNotification).block();
        var successStep = sagaMapper.mapToSuccessLog(message);
        sagaLogService.addSagaLog(successStep, PROCEED_STEPS, COMPENSATION_MAP);
        ack.acknowledge();
    }

    @Transactional
    void compensation(KafkaMessage message, Acknowledgment ack) throws JsonProcessingException {
        String advancedStep = message.sagaStep();
        String sagaId = message.sagaId();
        switch (advancedStep) {
            case SagaAdvancedStep.NOTIFICATION_CREATE -> compensationForProfile(sagaId);
            case SagaAdvancedStep.PROFILE_CREATE -> compensationForIdentity(sagaId);
        }
        var failedStep = sagaMapper.mapToFailedLog(message);
        // given up
        sagaLogService.addSagaLog(failedStep, PROCEED_STEPS, COMPENSATION_MAP);
        ack.acknowledge();
    }

    private void compensationForProfile(String sagaId) {
        profileWebClient.compensation(CompensationRequest.builder().sagaId(sagaId).build()).block();
    }

    private void compensationForIdentity(String sagaId) throws JsonProcessingException {
        compensationService.doCompensation(sagaId);
    }

    private void compensationForNotification(String sagaId) {
        notificationWebClient.compensation(CompensationRequest.builder().sagaId(sagaId).build()).block();
    }
}
