package com.nlu.app.service;

import com.nlu.app.dto.request.VideoCreationRequest;
import com.nlu.app.dto.response.VideoCreationResponse;
import com.nlu.app.dto.webclient.identity.request.TokenUserRequest;
import com.nlu.app.entity.Video;
import com.nlu.app.repository.IdentityWebClient;
import com.nlu.app.repository.VideoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class VideoService {
    VideoRepository videoRepository;
    IdentityWebClient identityWebClient;
    public Mono<VideoCreationResponse> createVideo(String token, VideoCreationRequest request) {
        var dto = TokenUserRequest.builder().token(token).build();
        return identityWebClient.userInfo(dto)
                .map(response -> response.getResult().getUserId())
                .flatMap(userId -> {
                    var model = Video.builder()
                            .createAt(LocalDateTime.now())
                            .tags(request.getTags())
                            .videoKey(request.getVideoKey())
                            .thumbnailKey(request.getThumbnailKey())
                            .userId(userId).build();
                    return Mono.fromCallable(() -> videoRepository.save(model))
                            .subscribeOn(Schedulers.immediate());
                })
                .map(video -> {
                    return VideoCreationResponse.builder()
                            .userId(video.getUserId())
                            .videoId(video.getVideoId())
                            .videoKey(video.getVideoKey())
                            .thumbnailKey(video.getThumbnailKey())
                            .createAt(video.getCreateAt())
                            .tags(video.getTagsString())
                            .build();
                });
    }
}
