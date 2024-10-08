package com.nlu.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.event.NewVideoCreatedEvent;
import com.nlu.app.dto.request.LikeVideoRequest;
import com.nlu.app.dto.request.SaveFileRequest;
import com.nlu.app.dto.request.VideoCreationRequest;
import com.nlu.app.dto.response.VideoCreationResponse;
import com.nlu.app.dto.webclient.identity.request.TokenUserRequest;
import com.nlu.app.entity.Outbox;
import com.nlu.app.entity.Video;
import com.nlu.app.entity.VideoInteract;
import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import com.nlu.app.mapper.OutboxMapper;
import com.nlu.app.mapper.VideoInteractMapper;
import com.nlu.app.repository.IdentityWebClient;
import com.nlu.app.repository.OutboxRepository;
import com.nlu.app.repository.VideoInteractRepository;
import com.nlu.app.repository.VideoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class InteractVideoService {
    VideoRepository videoRepository;
    VideoInteractRepository videoInteractRepository;
    OutboxRepository outboxRepository;
    VideoInteractMapper videoInteractMapper;
    OutboxMapper outboxMapper;

    @Transactional
    public Mono<String> likeVideo(LikeVideoRequest request) {
        var oVideo = videoRepository.findById(request.getVideoId());
        if (oVideo.isEmpty()) {
            throw new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        var video = oVideo.get();
        var oInteract = videoInteractRepository.findByVideoIdAndUserId(video.getVideoId(), request.getUserId());
        VideoInteract interact = null;
        if (oInteract.isEmpty()) {
            interact = VideoInteract.builder()
                    .video(video)
                    .vote("UP_VOTE")
                    .build();
        }
        video.getInteractions().add(interact);
        videoRepository.save(video);
        var event = videoInteractMapper.toUpVotedEvent(interact);
        var outbox = outboxMapper.toSuccessOutbox(event, interact.getId(), SagaAction.VIDEO_UPVOTE);
        outboxRepository.save(outbox);
        return Mono.just("OK");
    }
}
