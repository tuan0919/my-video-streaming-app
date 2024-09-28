package com.nlu.app.saga;
import java.time.Duration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @KafkaListener(topics = "user_created", groupId = "identity-service")
    public void userCreated(@Payload String payload, Acknowledgment ack) throws JsonProcessingException {
        log.info("Saga user start: {}", payload);
        UserCreatedEvent event = objectMapper.readValue(payload, UserCreatedEvent.class);
        var profileCreate = ProfileCreationRequest.builder()
                .bio("none")
                .userId(event.getUserId())
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

    @KafkaListener(topics = "notification_created", groupId = "identity-service")
    public void notificationCreated(@Payload String payload, Acknowledgment ack) throws JsonProcessingException {
        NotificationCreatedEvent event = objectMapper.readValue(payload, NotificationCreatedEvent.class);
        try {
            // TODO: do something to announce end this saga
            log.info("notification created: {}", event);
            ack.acknowledge();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "profile_created", groupId = "identity-service")
    public void profileCreated(@Payload String payload, Acknowledgment ack) throws JsonProcessingException {
        ProfileCreatedEvent event = objectMapper.readValue(payload, ProfileCreatedEvent.class);
        var createNotification = NotificationCreationRequest.builder()
                .type("INFO")
                .userId(event.getUserId())
                .content(String.format("Welcome userId %s to our service", event.getUserId()))
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
