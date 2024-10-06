package com.nlu.app.saga.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.SagaCompensationStep;
import com.nlu.app.common.share.dto.CompensationRequest;
import com.nlu.app.common.share.dto.notification_service.request.NotificationCreationRequest;
import com.nlu.app.common.share.dto.profile_service.request.ProfileCreationRequest;
import com.nlu.app.common.share.dto.saga.SagaAdvancedRequest;
import com.nlu.app.common.share.event.IdentityUpdatedEvent;
import com.nlu.app.common.share.event.NotificationCreatedEvent;
import com.nlu.app.common.share.event.ProfileCreatedEvent;
import com.nlu.app.common.share.event.UserCreatedEvent;
import com.nlu.app.mapper.SagaMapper;
import com.nlu.app.repository.webclient.NotificationWebClient;
import com.nlu.app.repository.webclient.ProfileWebClient;
import com.nlu.app.saga.KafkaMessage;
import com.nlu.app.saga.SagaError;
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
import java.util.Set;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UpdateIdentitySaga2 {
    NotificationWebClient notificationWebClient;
    UserService userService;
    ObjectMapper objectMapper;
    SagaLogService sagaLogService;
    CompensationService compensationService;
    RedisTemplate<String, Object> redisTemplate;
    SagaMapper sagaMapper;

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
    public void consumeMessage(KafkaMessage message, Acknowledgment ack) throws JsonProcessingException {
        String sagaStep = message.sagaStep();
        try {
            switch (sagaStep) {
                case SagaAdvancedStep.IDENTITY_UPDATE -> {
                    requestNotification(message, ack);
                }
                case SagaAdvancedStep.NOTIFICATION_CREATE -> {
                    onSagaSuccess(message, ack);
                }
                case SagaCompensationStep.COMPENSATION_NOTIFICATION_CREATE -> compensationForIdentity(message.sagaId());
                case SagaCompensationStep.COMPENSATION_IDENTITY_UPDATE -> {
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
    void requestNotification(KafkaMessage message, Acknowledgment ack) throws JsonProcessingException {
        IdentityUpdatedEvent event = objectMapper.readValue(message.payload(), IdentityUpdatedEvent.class);
        var createNotification = NotificationCreationRequest.builder()
                .type("WARNING")
                .userId(event.getUserId())
                .content(String.format("Your login information has been changed.", event.getUserId()))
                .build();
        var sagaAdvanced = SagaAdvancedRequest.builder()
                        .sagaId(message.sagaId())
                        .sagaAction(SagaAction.UPDATE_IDENTITY)
                        .sagaStep(SagaAdvancedStep.NOTIFICATION_CREATE)
                        .payload(objectMapper.writeValueAsString(createNotification))
                        .build();
        notificationWebClient.sagaRequest(sagaAdvanced).block();
        var successStep = sagaMapper.mapToSuccessLog(message);
        sagaLogService.addSagaLog(successStep, PROCEED_STEPS, COMPENSATION_MAP);
        ack.acknowledge();
    }

    @Transactional
    void compensation(KafkaMessage message, Acknowledgment ack) throws JsonProcessingException {
        String advancedStep = message.sagaStep();
        String sagaId = message.sagaId();
        switch (advancedStep) {
            case SagaAdvancedStep.NOTIFICATION_CREATE -> compensationForIdentity(sagaId);
        }
        var failedStep = sagaMapper.mapToFailedLog(message);
        // given up
        sagaLogService.addSagaLog(failedStep, PROCEED_STEPS, COMPENSATION_MAP);
        ack.acknowledge();
    }

    private void compensationForIdentity(String sagaId) throws JsonProcessingException {
        compensationService.doCompensation(sagaId);
    }

    private void compensationForNotification(String sagaId) {
        notificationWebClient.compensation(CompensationRequest.builder().sagaId(sagaId).build()).block();
    }
}
