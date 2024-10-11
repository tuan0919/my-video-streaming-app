package com.nlu.app.event;

import com.nlu.app.common.share.KafkaMessage;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.event.handler.ViewVideoHandler;
import com.nlu.app.event.handler.VoteActionVideoHandler;
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
    ViewVideoHandler VIEW_VIDEO_HANDLER;
    VoteActionVideoHandler VOTE_ACTION_VIDEO_HANDLER;

    @KafkaListener(topics = {"video.topics"}, groupId = "video-streaming-service")
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
                case SagaAction.MARK_VIEW_VIDEO -> VIEW_VIDEO_HANDLER.consumeEvent(message, ack);
                case SagaAction.VIDEO_UPVOTE, SagaAction.VIDEO_DOWNVOTE -> VOTE_ACTION_VIDEO_HANDLER.consumeEvent(message, ack);
                default -> {
                    // message này không thuộc nhiệm vụ của consumer group này, skip
                    ack.acknowledge();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
