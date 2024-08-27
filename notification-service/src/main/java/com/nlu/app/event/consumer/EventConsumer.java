package com.nlu.app.event.consumer;

import com.nlu.app.common.dto.UserCreationDTO;
import com.nlu.app.common.event.UserCreationEvent;
import com.nlu.app.common.event.comment_created.CommentCreationEvent;
import com.nlu.app.constant.NotificationType;
import com.nlu.app.entity.Notification;
import com.nlu.app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageHeaderAccessor;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {
    private final NotificationService service;

    @Bean
    public Function<Flux<Message<UserCreationEvent>>, Flux<Notification>> userCreationEvent() {
        return flux -> flux.flatMap(message -> {
            MessageHeaderAccessor accessor = new MessageHeaderAccessor(message);
            log.info("userid in message[{}]'s header: {}", message.getHeaders().getId(), accessor.getHeader("userid"));
            var event = message.getPayload();
            Notification notification = new Notification();
            notification.setType(NotificationType.INFO);
            notification.setContent(String.format("Hello user %s, welcome to our services.", event.getUserCreateDTO().getUsername()));
            notification.setTime(event.getTimestamp());
            notification.setUserId(event.getUserCreateDTO().getUserId());
            return service.insertDB(notification);
        });
    }

    @Bean
    public Function<Flux<Message<CommentCreationEvent>>, Flux<Notification>> commentRepliedEvent() {
        return flux -> flux.flatMap(message -> {
            MessageHeaderAccessor accessor = new MessageHeaderAccessor(message);
            log.info("userid in message[{}]'s header: {}", message.getHeaders().getId(), accessor.getHeader("userid"));
            var event = message.getPayload();
            Notification notification = new Notification();
            notification.setType(NotificationType.INFO);
            notification.setContent(String.format("User with id %s has replied to your comment.", event.getDto().getReplierId()));
            notification.setTime(event.getTimestamp());
            notification.setUserId(event.getDto().getUserId());
            return service.insertDB(notification);
        });
    }
}
