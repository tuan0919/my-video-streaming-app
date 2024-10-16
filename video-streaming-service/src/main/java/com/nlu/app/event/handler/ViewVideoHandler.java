package com.nlu.app.event.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.KafkaMessage;
import com.nlu.app.common.share.dto.notification_service.request.SendMessageWsRequest;
import com.nlu.app.common.share.event.ViewedVideoEvent;
import com.nlu.app.configuration.WebClientBuilder;
import com.nlu.app.repository.NotificationWebClient;
import com.nlu.app.repository.VideoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ViewVideoHandler {
    ObjectMapper objectMapper;
    VideoRepository videoRepository;
    @NonFinal
    private WebClient nWebClient;

    @Autowired
    private void setnWebClient(@Qualifier("notificationWebClient") WebClient nWebClient) {
        this.nWebClient = nWebClient;
    }

    /**
     * Consumed sự kiện {@link ViewedVideoEvent}, khi nhận được sự kiện này, nghĩa là video có thêm một view
     * tăng view count của video này lên.
     * @param message message kafka chứa các thông tin cần thiết trong quá trình consume.
     * @param ack Acknowledgment của kafka consumer, đảm bảo message đã được xử lí đúng đắn
     *            thì mới báo cho kafka.
     * @throws JsonProcessingException khi parse thất bại payload của event này.
     */
    @Transactional
    public void consumeEvent(KafkaMessage message, Acknowledgment ack) throws JsonProcessingException {
        var notificationWebClient = WebClientBuilder.createClient(nWebClient, NotificationWebClient.class);
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
        var request = SendMessageWsRequest.builder()
                .action("VIDEO_CHANGE")
                .topic("/topic/video/"+video.getVideoId())
                .payload("change")
                .build();
        notificationWebClient.sendToClient(request).block();
        ack.acknowledge();
    }
}
