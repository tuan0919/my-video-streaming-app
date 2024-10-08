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
import com.nlu.app.common.share.event.NotificationCreatedEvent;
import com.nlu.app.common.share.event.ProfileCreatedEvent;
import com.nlu.app.common.share.event.SagaCompletedEvent;
import com.nlu.app.common.share.event.UserCreatedEvent;
import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import com.nlu.app.mapper.OutboxMapper;
import com.nlu.app.mapper.SagaMapper;
import com.nlu.app.repository.OutboxRepository;
import com.nlu.app.repository.webclient.AggregatorWebClient;
import com.nlu.app.repository.webclient.NotificationWebClient;
import com.nlu.app.repository.webclient.ProfileWebClient;
import com.nlu.app.saga.KafkaMessage;
import com.nlu.app.saga.SagaError;
import com.nlu.app.service.CompensationService;
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
public class CreateNewUserSaga {
    ProfileWebClient profileWebClient;
    NotificationWebClient notificationWebClient;
    AggregatorWebClient aggregatorWebClient;
    ObjectMapper objectMapper;
    CompensationService compensationService;
    RedisTemplate<String, Object> redisTemplate;
    SagaMapper sagaMapper;
    OutboxRepository outboxRepository;
    OutboxMapper outboxMapper;

    private static final Set<String> PROCEED_STEPS = Set.of(
            SagaAdvancedStep.IDENTITY_CREATE,
            SagaAdvancedStep.PROFILE_CREATE,
            SagaAdvancedStep.NOTIFICATION_CREATE,
            SagaAdvancedStep.ENDING_SAGA
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
    public void consumeMessage(KafkaMessage message, Acknowledgment ack) {
        String sagaStep = message.sagaStep();
        try {
            switch (sagaStep) {
                case SagaAdvancedStep.IDENTITY_CREATE -> {
                    requestProfile(message);
                }
                case SagaAdvancedStep.PROFILE_CREATE -> {
                    requestNotification(message);
                }
                case SagaAdvancedStep.NOTIFICATION_CREATE -> {
                    requestEndingSaga(message);
                }
                case SagaAdvancedStep.ENDING_SAGA -> {
                    onSagaSuccess(message);
                }
                case SagaCompensationStep.COMPENSATION_NOTIFICATION_CREATE,
                     SagaCompensationStep.COMPENSATION_PROFILE_CREATE,
                     SagaCompensationStep.COMPENSATION_IDENTITY_CREATE -> {
                    compensation(message);
                }
                default -> {
                    throw new ApplicationException(ErrorCode.UNEXPECTED_BEHAVIOR);
                }
            }
            ack.acknowledge();
        }
        catch (WebClientResponseException e) {
            e.printStackTrace();
            handleSagaError(message, e);
        } catch (Exception e) {
            e.printStackTrace();
            handleSagaError(message, e);
        }
    }

    private void handleSagaError(KafkaMessage message, Exception e) {
        SagaError sagaError = sagaMapper.mapToSagaError(e, message);
        this.compensation(message);
        redisTemplate.opsForValue().set("SAGA_ABORTED_" + message.sagaId(), sagaError, Duration.ofMinutes(3));
        // Do not acknowledge in case of error
    }

    private void onSagaSuccess(KafkaMessage message) {
        log.info("Saga {} is successfully finish.", message.sagaAction());
    }

    private void requestEndingSaga(KafkaMessage message) {
        try {
            var event = objectMapper.readValue(message.payload(), NotificationCreatedEvent.class);
            var response = aggregatorWebClient
                    .getUser(event.getUserId())
                    .block();
            redisTemplate.opsForValue().set("SAGA_COMPLETED_"+message.sagaId(), response, Duration.ofMinutes(3));
            var completedEvent = new SagaCompletedEvent(message.sagaId(), message.sagaAction());
            var outbox = outboxMapper.toSuccessOutbox(completedEvent, message.sagaId(), message.sagaAction());
            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new ApplicationException(ErrorCode.UNEXPECTED_BEHAVIOR);
        }
    }


    void requestProfile(KafkaMessage message) {
        try {
            String payload = message.payload();
            String sagaId = message.sagaId();
            UserCreatedEvent event = objectMapper.readValue(payload, UserCreatedEvent.class);
            var profileCreate = ProfileCreationRequest.builder()
                    .bio("none")
                    .userId(event.getUserId())
                    .country("vn")
                    .fullName("")
                    .build();
            var sagaRequest = SagaAdvancedRequest.builder()
                    .sagaId(sagaId)
                    .sagaAction(SagaAction.CREATE_NEW_USER)
                    .sagaStep(SagaAdvancedStep.PROFILE_CREATE)
                    .payload(objectMapper.writeValueAsString(profileCreate))
                    .build();
            profileWebClient.sagaRequest(sagaRequest).block();
        } catch (JsonProcessingException e) {
            throw new ApplicationException(ErrorCode.UNEXPECTED_BEHAVIOR);
        }
    }


    void requestNotification(KafkaMessage message) {
        try {
            ProfileCreatedEvent event = objectMapper.readValue(message.payload(), ProfileCreatedEvent.class);
            var createNotification = NotificationCreationRequest.builder()
                    .type("INFO")
                    .userId(event.getUserId())
                    .content(String.format("Chào mừng userId %s đến với hệ thống.", event.getUserId()))
                    .build();
            var sagaRequest = SagaAdvancedRequest.builder()
                    .sagaId(message.sagaId())
                    .sagaAction(SagaAction.CREATE_NEW_USER)
                    .sagaStep(SagaAdvancedStep.NOTIFICATION_CREATE)
                    .payload(objectMapper.writeValueAsString(createNotification))
                    .build();
            notificationWebClient.sagaRequest(sagaRequest)
                    .block();
        } catch (JsonProcessingException e) {
            throw new ApplicationException(ErrorCode.UNEXPECTED_BEHAVIOR);
        }
    }

    void compensation(KafkaMessage message) {
        String advancedStep = message.sagaStep();
        String sagaId = message.sagaId();
        switch (advancedStep) {
            case SagaAdvancedStep.NOTIFICATION_CREATE -> compensationForNotification(sagaId);
            case SagaAdvancedStep.PROFILE_CREATE -> compensationForProfile(sagaId);
            case SagaAdvancedStep.IDENTITY_CREATE -> compensationForIdentity(sagaId);
            case SagaCompensationStep.COMPENSATION_NOTIFICATION_CREATE -> compensationForProfile(sagaId);
            case SagaCompensationStep.COMPENSATION_PROFILE_CREATE -> compensationForIdentity(sagaId);
            case SagaCompensationStep.COMPENSATION_IDENTITY_CREATE -> onSagaAborted(message);
        }
    }

    private void onSagaAborted(KafkaMessage message) {
        log.info("Successfully aborted saga {}", message.sagaAction());
    }

    private void compensationForProfile(String sagaId) {
        profileWebClient.compensation(CompensationRequest.builder().sagaId(sagaId).build()).block();
    }

    private void compensationForIdentity(String sagaId) {
        compensationService.doCompensation(sagaId);
    }

    private void compensationForNotification(String sagaId) {
        notificationWebClient.compensation(CompensationRequest.builder().sagaId(sagaId).build()).block();
    }
}
