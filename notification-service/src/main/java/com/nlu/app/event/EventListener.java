package com.nlu.app.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.KafkaMessage;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.event.handler.CreateNewVideoHandler;
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
    CreateNewVideoHandler CREATE_NEW_VIDEO_HANDLER;

    @KafkaListener(topics = {"comment.topics", "video.topics"}, groupId = "notification-service")
    public void handleComment(@Payload String payload,
                              @Header("sagaAction") String sagaAction,
                              @Header("sagaStep") String sagaStep,
                              @Header("id") String eventId,
                              @Header("sagaStepStatus") String sagaStepStatus,
                              @Header("sagaId") String sagaId,
                              Acknowledgment ack) {
        var message = new KafkaMessage(eventId, sagaId, sagaAction, sagaStep, sagaStepStatus, payload);
        try {
            switch (sagaAction) {
                // send notification to replied user
                case SagaAction.USER_REPLY_COMMENT -> {
                    USER_REPLY_EVENT_HANDLER.consumeEvent(message, ack);
                }
                // send notification to followers of video's owner.
                case SagaAction.CREATE_NEW_VIDEO -> {
                    CREATE_NEW_VIDEO_HANDLER.consumeEvent(message, ack);
                }
                default -> {
                    // message này không thuộc nhiệm vụ của consumer group này, skip
                    ack.acknowledge();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            ack.nack(Duration.ofSeconds(5)); // Retry after 5 seconds
        }
    }
}
