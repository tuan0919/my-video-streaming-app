package com.nlu.app.event.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.KafkaMessage;
import com.nlu.app.common.share.event.ViewedVideoEvent;
import com.nlu.app.repository.VideoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ViewVideoHandler {
    ObjectMapper objectMapper;
    VideoRepository videoRepository;

    /**
     * Consumed sự kiện {@link ViewedVideoEvent}, khi nhận được sự kiện này, nghĩa là video có thêm một người tương tác với nó
     * tăng view count của video này lên.
     * @param message message kafka chứa các thông tin cần thiết trong quá trình consume.
     * @param ack Acknowledgment của kafka consumer, đảm bảo message đã được xử lí đúng đắn
     *            thì mới báo cho kafka.
     * @throws JsonProcessingException khi parse thất bại payload của event này.
     */
    @Transactional
    public void consumeEvent(KafkaMessage message, Acknowledgment ack) throws JsonProcessingException {
        var event = objectMapper.readValue(message.payload(), ViewedVideoEvent.class);
        var oVideo = videoRepository.findById(event.getVideoId());
        if (oVideo.isEmpty()) {
            log.error("unexpected: video này không tồn tại? Skip message này. - videoId: {}", event.getVideoId());
            ack.acknowledge();
            return;
        }
        var video = oVideo.get();
        int currentCount = video.getViewCount();
        video.setViewCount(++currentCount);
        videoRepository.save(video);
        log.info("consumed event ViewedVideoEvent, +1 view cho video có id {}", video.getVideoId());
        ack.acknowledge();
    }
}
