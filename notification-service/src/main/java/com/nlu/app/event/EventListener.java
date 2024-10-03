package com.nlu.app.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.event.CommentReplyEvent;
import com.nlu.app.event.handler.UserReplyHandler;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EventListener {
    ObjectMapper objectMapper;
    UserReplyHandler USER_REPLY_EVENT_HANDLER;

    @KafkaListener(topics = "comment.topics", groupId = "notification-service")
    public void handleComment(@Payload String payload, @Header("sagaAction") String sagaAction, Acknowledgment ack) throws JsonProcessingException {
        try {
            switch (sagaAction) {
                // send notification to replied user
                case SagaAction.USER_REPLY_COMMENT -> {
                    var event = objectMapper.readValue(payload, CommentReplyEvent.class);
                    USER_REPLY_EVENT_HANDLER.consumeEvent(event);
                    // If everything work without exception throwing, consider at this state the message is consumed.
                    ack.acknowledge();
                }
            }
        }
        catch (Exception e) {
            ack.nack(Duration.ofSeconds(5)); // Retry after 5 seconds
        }
    }
}