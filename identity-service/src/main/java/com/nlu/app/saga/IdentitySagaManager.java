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
import com.nlu.app.common.share.event.ProfileCreatedEvent;
import com.nlu.app.entity.SagaLog;
import com.nlu.app.repository.webclient.NotificationWebClient;
import com.nlu.app.repository.webclient.ProfileWebClient;
import com.nlu.app.service.CompensationService;
import com.nlu.app.service.SagaLogService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
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
public class CreateNewUserSaga {


    @Transactional
    @KafkaListener(topics = {"identity.created", "notification.created", "profile.created"}, groupId = "saga.create_new_user")
    public void handleSagaEvent(@Payload String payload,
                                @Header("sagaAction") String sagaAction,
                                @Header("sagaStep") String sagaStep,
                                @Header("id") String eventId,
                                @Header("sagaStepStatus") String sagaStepStatus,
                                @Header("sagaId") String sagaId,
                                Acknowledgment ack) throws JsonProcessingException {
        log.info("Saga Action: {}, Saga Step: {}, Saga ID: {}", sagaAction, sagaStep, sagaId);

        updateSagaLog(sagaId, eventId, sagaAction, sagaStep, sagaStepStatus);

        switch (sagaStepStatus) {
            case SagaStatus.SUCCESS -> handleSuccessStep(sagaStep, sagaId, payload);
            case SagaStatus.FAILED -> handleFailedStep(sagaStep, sagaId);
        }

        ack.acknowledge();
    }

}
