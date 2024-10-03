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
    ObjectMapper objectMapper;
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
    Video _insertVideo_(VideoCreationRequest request, String userId, String key) throws JsonProcessingException {
        var video = Video.builder()
                .videoDescription(request.getDescription())
                .videoKey(key)
                .videoName(request.getVideoName())
                .createAt(LocalDateTime.now())
                .userId(userId)
                .thumbnailKey("TEST") //TODO: temporary consider all videos have no thumbnail.
                .build();
        videoRepository.save(video);
        var videoEvent = NewVideoCreatedEvent.builder()
                .videoDescription(request.getDescription())
                .videoKey(request.getVideoKey())
                .userId(userId)
                .videoName(request.getVideoName())
                .build();
        var outbox = Outbox.builder()
                .sagaAction(SagaAction.CREATE_NEW_VIDEO)
                .sagaId(video.getVideoId())
                .aggregateId(video.getVideoId())
                .sagaStepStatus(SagaAdvancedStep.VIDEO_CREATE)
                .aggregateType("video.topics")
                .payload(objectMapper.writeValueAsString(videoEvent))
                .build();
        outboxRepository.save(outbox);
        return video;
    }

    Mono<VideoCreationResponse> _mapToResponse_(Video video) {
        return fileService
                .generateURL(video.getVideoKey())
                .map(response -> {
                    String link = response.getLink();
                    return VideoCreationResponse.builder()
                            .videoURL(link)
                            .videoName(video.getVideoName())
                            .description(video.getVideoDescription())
                            .createAt(video.getCreateAt())
                            .build();
                });
    }
}
