package com.nlu.app.saga.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.SagaCompensationStep;
import com.nlu.app.common.share.dto.CompensationRequest;
import com.nlu.app.common.share.dto.notification_service.request.NotificationCreationRequest;
import com.nlu.app.common.share.dto.saga.SagaAdvancedRequest;
import com.nlu.app.common.share.event.NotificationCreatedEvent;
import com.nlu.app.common.share.event.ProfileCreatedEvent;
import com.nlu.app.common.share.event.SagaCompletedEvent;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import com.nlu.app.mapper.OutboxMapper;
import com.nlu.app.repository.OutboxRepository;
import com.nlu.app.repository.webclient.AggregatorWebClient;
import com.nlu.app.repository.webclient.NotificationWebClient;
import com.nlu.app.common.share.KafkaMessage;
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

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UpdateIdentitySaga {
    NotificationWebClient notificationWebClient;
    AggregatorWebClient aggregatorWebClient;
    ObjectMapper objectMapper;
    CompensationService compensationService;
    RedisTemplate<String, Object> redisTemplate;
    OutboxRepository outboxRepository;
    OutboxMapper outboxMapper;

    @Transactional
    public void consumeMessage(KafkaMessage message, Acknowledgment ack) {
        String sagaStep = message.sagaStep();
        try {
            switch (sagaStep) {
                case SagaAdvancedStep.IDENTITY_UPDATE -> {
                    requestCreateNotification(message);
                }
                case SagaAdvancedStep.NOTIFICATION_CREATE -> {
                    requestEndingSaga(message);
                }
                case SagaAdvancedStep.ENDING_SAGA -> {
                    onSagaSuccess(message);
                }
                case SagaCompensationStep.COMPENSATION_NOTIFICATION_CREATE,
                     SagaCompensationStep.COMPENSATION_IDENTITY_UPDATE -> {
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

    private AppResponse<Object> convertException(Exception e) {
        try {
            // expected errors
            var webClientException = (WebClientResponseException)e;
            var response = objectMapper.readValue(webClientException.getResponseBodyAsString(), AppResponse.class);
            var responseEntity = AppResponse.builder()
                    .code(response.getCode())
                    .message(response.getMessage())
                    .build();
            return responseEntity;
        } catch (Exception exception) {
            exception.printStackTrace();
            var responseEntity = AppResponse.builder()
                    .code(ErrorCode.UNKNOWN_EXCEPTION.getCode())
                    .message(ErrorCode.UNKNOWN_EXCEPTION.getMessage())
                    .build();
            return responseEntity;
        }
    }

    private void handleSagaError(KafkaMessage message, Exception e) {
        this.compensation(message);
        var responseEntity = convertException(e);
        redisTemplate.opsForValue().set("SAGA_ABORTED_" + message.sagaId(), responseEntity, Duration.ofMinutes(3));
        // Do not acknowledge in case of error
    }

    private void onSagaSuccess(KafkaMessage message) {
        log.info("Saga {} kết thúc thành công.", message.sagaAction());
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

    void requestCreateNotification(KafkaMessage message) {
        try {
            ProfileCreatedEvent event = objectMapper.readValue(message.payload(), ProfileCreatedEvent.class);
            var createNotification = NotificationCreationRequest.builder()
                    .type("WARNING")
                    .userId(event.getUserId())
                    .content(String.format("Thông tin đăng nhập của bạn (%s) đã bị thay đổi.", event.getUserId()))
                    .build();
            var sagaRequest = SagaAdvancedRequest.builder()
                    .sagaId(message.sagaId())
                    .sagaAction(SagaAction.UPDATE_IDENTITY)
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
            case SagaAdvancedStep.NOTIFICATION_CREATE -> compensationForCreateNotification(sagaId);
            case SagaAdvancedStep.IDENTITY_UPDATE -> compensationForUpdateIdentity(sagaId);
            case SagaCompensationStep.COMPENSATION_NOTIFICATION_CREATE -> compensationForUpdateIdentity(sagaId);
            case SagaCompensationStep.COMPENSATION_IDENTITY_UPDATE -> onSagaAborted(message);
        }
    }

    private void onSagaAborted(KafkaMessage message) {
        log.info("Saga {} hủy bỏ thành công.", message.sagaAction());
    }

    private void compensationForUpdateIdentity(String sagaId) {
        compensationService.doCompensation(sagaId);
    }

    private void compensationForCreateNotification(String sagaId) {
        notificationWebClient.compensation(CompensationRequest.builder().sagaId(sagaId).build()).block();
    }
}
