package com.nlu.app.saga;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaStep;
import com.nlu.app.common.share.dto.notification_service.request.NotificationCreationRequest;
import com.nlu.app.common.share.dto.profile_service.request.ProfileCreationRequest;
import com.nlu.app.common.share.event.NotificationCreatedEvent;
import com.nlu.app.common.share.event.ProfileCreatedEvent;
import com.nlu.app.repository.webclient.NotificationWebClient;
import com.nlu.app.repository.webclient.ProfileWebClient;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import com.nlu.app.common.share.event.UserCreatedEvent;

@Component
@NoArgsConstructor
@Slf4j
public class IdentityManagementSaga {
    ProfileWebClient profileWebClient;
    NotificationWebClient notificationWebClient;
    ObjectMapper objectMapper;

    @Autowired
    public void setProfileWebClient(ProfileWebClient profileWebClient) {
        this.profileWebClient = profileWebClient;
    }

    @Autowired
    public void setNotificationWebClient(NotificationWebClient notificationWebClient) {
        this.notificationWebClient = notificationWebClient;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

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
            profileWebClient.createProfile(profileCreate).block();
            ack.acknowledge();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "notification.created", groupId = "saga.create_new_user")
    public void notificationCreated(@Payload String payload,
                                    @Header ("sagaAction") String sagaAction,
                                    @Header ("sagaStep") String sagaStep,
                                    @Header ("id") String eventId,
                                    @Header ("sagaStepStatus") String sagaStepStatus,
                                    @Header ("sagaId") String sagaId,
                                    Acknowledgment ack ) throws JsonProcessingException {
        // TODO: checking if key is already exists (this event is consumed)
        log.info("sagaAction: {}, sagaStep: {}, sagaId: {}", sagaAction, sagaStep, sagaId);
        NotificationCreatedEvent event = objectMapper.readValue(payload, NotificationCreatedEvent.class);
        log.info("Notification created successfully: {}", payload);
        // TODO: do something to announce about the success
        ack.acknowledge();
    }

    @KafkaListener(topics = "profile.created", groupId = "saga.create_new_user")
    public void profileCreated(@Payload String payload,
                               @Header ("sagaAction") String sagaAction,
                               @Header ("sagaStep") String sagaStep,
                               @Header ("id") String eventId,
                               @Header ("sagaStepStatus") String sagaStepStatus,
                               @Header ("sagaId") String sagaId,
                               Acknowledgment ack ) throws JsonProcessingException {
        log.info("sagaAction: {}, sagaStep: {}, sagaId: {}", sagaAction, sagaStep, sagaId);
        ProfileCreatedEvent event = objectMapper.readValue(payload, ProfileCreatedEvent.class);
        var createNotification = NotificationCreationRequest.builder()
                .type("INFO")
                .userId(event.getUserId())
                .content(String.format("Welcome userId %s to our service", event.getUserId()))
                .sagaId(sagaId)
                .sagaAction(SagaAction.CREATE_NEW_USER)
                .build();
        log.info("profile created: {}", event);
        try {
            notificationWebClient.createNotification(createNotification).block();
            ack.acknowledge();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
