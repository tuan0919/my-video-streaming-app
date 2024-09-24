package com.nlu.app.event.consumer;

import com.nlu.app.common.event.UserCreationEvent;
import com.nlu.app.common.event.comment_created.CommentCreationEvent;
import com.nlu.app.constant.NotificationType;
import com.nlu.app.entity.Notification;
import com.nlu.app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import java.time.Instant;
import java.time.ZoneId;
import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {
    private final NotificationService service;

    @Bean
    public Consumer<Message<UserCreationEvent>> userCreationEvent() {
        return message -> {
            var event = message.getPayload();
            String userId = event.getUserCreateDTO().getUserId();
            Notification notification = new Notification();
            Acknowledgment ack = message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, Acknowledgment.class);
            notification.setType(NotificationType.INFO);
            notification.setContent(String.format("Hello user %s, welcome to our services.", event.getUserCreateDTO().getUsername()));
            var dateTime = Instant.ofEpochMilli(event.getUserCreateDTO().getTimestamp())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            notification.setTime(dateTime);
            notification.setUserId(userId);
            try {
                service.insertDB(notification);
                ack.acknowledge();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Bean
    public Consumer<Message<CommentCreationEvent>> commentRepliedEvent() {
        return message -> {
            var event = message.getPayload();
            String userId = event.getDto().getUserId();
            Notification notification = new Notification();
            Acknowledgment ack = message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, Acknowledgment.class);
            notification.setType(NotificationType.INFO);
            notification.setContent(String.format("User with id %s replied your comment.", event.getDto().getUserId()));
            var dateTime = Instant.ofEpochMilli(event.getTimestamp())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            notification.setTime(dateTime);
            notification.setUserId(userId);
            try {
                service.insertDB(notification);
                ack.acknowledge();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}