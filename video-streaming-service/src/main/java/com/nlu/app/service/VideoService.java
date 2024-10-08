package com.nlu.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.event.NewVideoCreatedEvent;
import com.nlu.app.dto.request.SaveFileRequest;
import com.nlu.app.dto.request.VideoCreationRequest;
import com.nlu.app.dto.response.VideoCreationResponse;
import com.nlu.app.dto.webclient.identity.request.TokenUserRequest;
import com.nlu.app.entity.Outbox;
import com.nlu.app.entity.Video;
import com.nlu.app.mapper.OutboxMapper;
import com.nlu.app.mapper.VideoMapper;
import com.nlu.app.repository.IdentityWebClient;
import com.nlu.app.repository.OutboxRepository;
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
public class VideoService {
    VideoRepository videoRepository;
    IdentityWebClient identityWebClient;
    FileService fileService;
    OutboxRepository outboxRepository;
    OutboxMapper outboxMapper;
    VideoMapper videoMapper;
    public Mono<VideoCreationResponse> createVideo(String token, VideoCreationRequest request) {
        var dto = TokenUserRequest.builder().token(token).build();
        var wrap = new Object() {
            String userId;
        };
        return identityWebClient.userInfo(dto)
                .map(response -> response.getResult().getUserId())
                .flatMap(userId -> {
                    wrap.userId = userId;
                    var requestSaveFile = SaveFileRequest.builder().filename(request.getVideoKey()).build();
                    return fileService.moveToInventory(requestSaveFile, token);
                })
                // Chuyển các thao tác blocking JPA sang boundedElastic để không chặn thread Reactive
                .flatMap(response -> Mono.fromCallable(() -> _insertVideo_(request, wrap.userId, response.getKey()))
                        .subscribeOn(Schedulers.boundedElastic())) // Sử dụng thread pool riêng cho blocking code
                .flatMap(this::_mapToResponse_);  // Tiếp tục xử lý Reactive không blocking
    }

    @Transactional
    Video _insertVideo_(VideoCreationRequest request, String userId, String key) {
        var video = videoMapper.toEntity(request, userId, key);
        videoRepository.save(video);
        var videoEvent = videoMapper.toNewVideoCreatedEvent(video);
        var outbox = outboxMapper.toSuccessOutbox(videoEvent, videoEvent.getVideoId(), SagaAction.CREATE_NEW_VIDEO);
        outboxRepository.save(outbox);
        return video;
    }

    Mono<VideoCreationResponse> _mapToResponse_(Video video) {
        return fileService
                .generateURL(video.getVideoKey())
                .map(response -> {
                    String link = response.getLink();
                    return videoMapper.toVideoCreationResponse(video, link);
                });
    }
}
