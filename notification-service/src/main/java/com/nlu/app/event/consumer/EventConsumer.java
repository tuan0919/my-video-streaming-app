package com.nlu.app.event.consumer;

import com.nlu.app.common.event.UserCreationEvent;
import com.nlu.app.constant.NotificationType;
import com.nlu.app.entity.Notification;
import com.nlu.app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {
    private final NotificationService service;
//    @Bean
//    public Function<Flux<UserCreationEvent>, Flux<Notification>> userCreationEvent() {
//        return fluxEvent -> fluxEvent.flatMap(event -> {
//            log.info("Consumed user-created event");
//            Notification notification = new Notification();
//            notification.setType(NotificationType.INFO);
//            notification.setContent(String.format("Hello user %s, welcome to our services.", event.getUserCreateDTO().getUsername()));
//            notification.setTime(event.getTimestamp());
//            notification.setUserId(event.getUserCreateDTO().getUserId());
//            return service.insertDB(notification);
//        });
//    }

    @Bean
    public Consumer<Message<String>> userCreationEvent() {
        return str -> {
            log.info("Consumed: {}", str);
        };
    }
}
