package com.nlu.app.event.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.KafkaMessage;
import com.nlu.app.common.share.event.NewVideoCreatedEvent;
import com.nlu.app.common.share.event.NotificationCreatedEvent;
import com.nlu.app.configuration.WebClientBuilder;
import com.nlu.app.entity.Notification;
import com.nlu.app.entity.Outbox;
import com.nlu.app.mapper.NotificationMapper;
import com.nlu.app.mapper.OutboxMapper;
import com.nlu.app.repository.NotificationRepository;
import com.nlu.app.repository.OutboxRepository;
import com.nlu.app.repository.webclient.IdentityWebClient;
import com.nlu.app.repository.webclient.ProfileWebClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CreateNewNotification {
    NotificationRepository notificationRepository;
    ObjectMapper objectMapper;
    private final SimpMessagingTemplate messageBus;
    @Transactional
    public void consumeEvent(KafkaMessage message, Acknowledgment ack) throws JsonProcessingException {
        var event = objectMapper.readValue(message.payload(), NewVideoCreatedEvent.class);
        int unreadMessage = notificationRepository.countAllByUserIdAndAndIsRead(event.getUserId(), false);
        System.out.println("consumed message for userId: " + event.getUserId());
        messageBus.convertAndSend(String.format("/topic/%s/notification", event.getUserId()),
                String.format("""
                {
                    "message_count": %d
                }
                """, unreadMessage));
        ack.acknowledge();
    }
}
