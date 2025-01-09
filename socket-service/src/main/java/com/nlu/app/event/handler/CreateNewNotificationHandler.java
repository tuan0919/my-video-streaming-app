package com.nlu.app.event.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.KafkaMessage;
import com.nlu.app.common.share.event.NotificationCreatedEvent;
import com.nlu.app.service.NotificationService;
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
    ObjectMapper objectMapper;
    NotificationService notificationService;
    private final SimpMessagingTemplate messageBus;
    @Transactional
    public void consumeEvent(KafkaMessage message, Acknowledgment ack) throws JsonProcessingException {
        var event = objectMapper.readValue(message.payload(), NotificationCreatedEvent.class);
        var unreadMessage = notificationService.countUnreadNotification(event.getUserId());
        // publish event nay vao topic tuong ung
        String topic = String.format("/topic/%s/notification", event.getUserId());
        log.info("NotificationCreatedEvent: {}", event);
        String payload = String.format("""
                {
                    "type": "count_unread"
                    "payload": %s,
                }
                """, objectMapper.writeValueAsString(unreadMessage));
        log.info("Publish message vào topic: {}", topic);
        messageBus.convertAndSend(topic, payload);
        log.info("Consumed message thành công!");
        ack.acknowledge();
    }
}
