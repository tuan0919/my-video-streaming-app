package com.nlu.app.event.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.KafkaMessage;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.event.VideoDownVotedEvent;
import com.nlu.app.common.share.event.VideoUpvotedEvent;
import com.nlu.app.repository.VideoInteractRepository;
import com.nlu.app.repository.VideoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VoteActionVideoHandler {
    ObjectMapper objectMapper;
    VideoRepository videoRepository;
    VideoInteractRepository videoInteractRepository;
    SimpMessagingTemplate messagingTemplate;

    /**
     * Consumed sự kiện {@link VideoDownVotedEvent} và {@link VideoUpvotedEvent}
     * @param message
     * @param ack
     * @throws JsonProcessingException
     */
    @Transactional
    public void consumeEvent(KafkaMessage message, Acknowledgment ack) throws JsonProcessingException {
        String videoId = null;
        if (message.sagaAction().equalsIgnoreCase(SagaAction.VIDEO_UPVOTE)) {
            videoId = objectMapper.readValue(message.payload(), VideoUpvotedEvent.class).getVideoId();
        }
        else if (message.sagaAction().equalsIgnoreCase(SagaAction.VIDEO_DOWNVOTE)) {
            videoId = objectMapper.readValue(message.payload(), VideoDownVotedEvent.class).getVideoId();
        }
        else {
            log.error("unexpected behavior, why this message is not known?");
            ack.acknowledge();
            return;
        }
        var oVideo = videoRepository.findById(videoId);
        if (oVideo.isEmpty()) {
            log.error("unexpected: video này không tồn tại? Skip message này. - videoId: {}", videoId);
            ack.acknowledge();
            return;
        }
        Integer upVote = videoInteractRepository.countDistinctByVoteAndVideo_VideoId("UP_VOTE", videoId);
        Integer downVote = videoInteractRepository.countDistinctByVoteAndVideo_VideoId("DOWN_VOTE", videoId);
        log.info("consumed event {}, upVote: {}, downVote: {}", message.sagaAction(), upVote, downVote);
        sendToClient(videoId, upVote, downVote);
        ack.acknowledge();
    }

    void sendToClient(String videoId, int upVote, int downVote) {
        Map<String, Object> message = new HashMap<>();
        message.put("action", "VOTE_VIDEO_CHANGE");
        message.put("videoId", videoId);
        message.put("upVotes", upVote);
        message.put("downVotes", downVote);
        log.info("send message to: {}", "/topic/video/"+videoId, message);
        messagingTemplate.convertAndSend("/topic/video/"+videoId, message);
    }
}
