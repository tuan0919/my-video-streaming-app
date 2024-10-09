package com.nlu.app.saga;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nlu.app.common.share.KafkaMessage;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.saga.processor.CreateNewUserSaga;
import com.nlu.app.saga.processor.UpdateIdentitySaga;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class IdentitySagaManager {
    CreateNewUserSaga NEW_USER_SAGA;
    UpdateIdentitySaga UPDATE_IDENTITY_SAGA;

    @KafkaListener(topics = {"identity.topics", "notification.topics", "profile.topics"}, groupId = "identity-service.saga")
    public void handleSagaEvent(@Payload String payload,
                                @Header("sagaAction") String sagaAction,
                                @Header("sagaStep") String sagaStep,
                                @Header("id") String eventId,
                                @Header("sagaStepStatus") String sagaStepStatus,
                                @Header("sagaId") String sagaId,
                                Acknowledgment ack) {
        var message = new KafkaMessage(eventId, sagaId, sagaAction, sagaStep, sagaStepStatus, payload);
        switch (sagaAction) {
            case SagaAction.CREATE_NEW_USER -> {
                NEW_USER_SAGA.consumeMessage(message, ack);
            }
            case SagaAction.UPDATE_IDENTITY -> {
                UPDATE_IDENTITY_SAGA.consumeMessage(message, ack);
            }
            case SagaAction.REMOVE_IDENTITY -> {
                // TODO: Saga for this action
            }
        }
    }

}
