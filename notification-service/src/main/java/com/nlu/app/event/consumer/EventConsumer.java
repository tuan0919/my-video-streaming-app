package com.nlu.app.event.consumer;

import com.nlu.app.common.event.UserCreationEvent;
import com.nlu.app.common.event.comment_created.CommentCreationEvent;
import com.nlu.app.constant.NotificationType;
import com.nlu.app.entity.Notification;
import com.nlu.app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {
    private final StreamBridge streamBridge;
    private final NotificationService service;

    @Bean
    public Function<Flux<Message<UserCreationEvent>>, Flux<Message<Notification>>> userCreationEvent() {
        return flux -> flux.flatMap(message -> {
            MessageHeaderAccessor accessor = new MessageHeaderAccessor(message);
            log.info("userid in message[{}]'s header: {}", message.getHeaders().getId(), accessor.getHeader("userid"));
            var event = message.getPayload();
            String userId = event.getUserCreateDTO().getUserId();
            Notification notification = new Notification();
            ReceiverOffset ack = message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, ReceiverOffset.class);
            notification.setType(NotificationType.INFO);
            notification.setContent(String.format("Hello user %s, welcome to our services.", event.getUserCreateDTO().getUsername()));
            notification.setTime(event.getTimestamp());
            notification.setUserId(userId);
            UUID messageId = message.getHeaders().getId();
            return service.insertDB(notification)
                    .retryWhen(Retry.backoff(5, Duration.ofSeconds(1))
                            .onRetryExhaustedThrow((_, retrySignal) -> {
                                // Log retry information
                                long retryCount = retrySignal.totalRetries(); // Tổng số lần retry đã thực hiện
                                log.warn("Retry attempt: {} for message [{}]" ,retryCount, messageId);
                                return retrySignal.failure();
                            }).maxBackoff(Duration.ofSeconds(10)))
                    .map(msg ->  {
                        ack.acknowledge();
                        return MessageBuilder.withPayload(msg)
                                .setHeader("partitonKey", userId)
                                .build();
                    })
                    .onErrorResume(error -> {
                        error.printStackTrace();
                        log.error("given up on message [{}]", messageId);
                        streamBridge.send("dlq_user_created", MessageBuilder.withPayload(event)
                                .setHeader("partitionKey", userId)
                                .setHeader(BinderHeaders.PARTITION_OVERRIDE, userId)
                                .build());
                        ack.acknowledge();
                        return Mono.empty();
                    });
        });
    }

    @Bean
    public Function<Flux<Message<CommentCreationEvent>>, Flux<Message<Notification>>> commentRepliedEvent() {
        return flux -> flux.flatMap(message -> {
            MessageHeaderAccessor accessor = new MessageHeaderAccessor(message);
            log.info("userid in message[{}]'s header: {}", message.getHeaders().getId(), accessor.getHeader("userid"));
            var event = message.getPayload();
            String userId = event.getDto().getUserId();
            Notification notification = new Notification();
            notification.setType(NotificationType.INFO);
            ReceiverOffset ack = message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, ReceiverOffset.class);
            notification.setContent(String.format("User with id %s has replied to your comment.", event.getDto().getReplierId()));
            notification.setTime(event.getTimestamp());
            notification.setUserId(userId);
            return service.insertDB(notification)
                    .map(msg ->  {
                        ack.acknowledge();
                        return MessageBuilder.withPayload(msg)
                                .setHeader("partitonKey", userId)
                                .build();
                    });
        });
    }
}
