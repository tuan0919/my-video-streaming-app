package com.nlu.app.event.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.KafkaMessage;
import com.nlu.app.common.share.event.NewVideoCreatedEvent;
import com.nlu.app.repository.NotificationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CreateNewNotificationHandler {
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
