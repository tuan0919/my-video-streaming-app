package com.nlu.app.controller;

import com.nlu.app.dto.AppResponse;
import com.nlu.app.dto.request.*;
import com.nlu.app.dto.response.VideoCreationResponse;
import com.nlu.app.common.share.dto.videoStreaming_service.response.VideoDetailsResponse;
import com.nlu.app.service.InteractVideoService;
import com.nlu.app.service.VideoService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VideoController {
    VideoService videoService;
    InteractVideoService interactVideoService;

    @PostMapping
    public AppResponse<VideoCreationResponse> upNewVideo(@RequestBody VideoCreationRequest request,
                                                               @RequestHeader("X-UserId") String userId,
                                                               @RequestHeader("X-Username") String username) {
        var response = videoService.createVideo(userId, username, request);
        return AppResponse.<VideoCreationResponse>builder()
                .result(response).build();
    }

    @PostMapping("/up-vote")
    public Mono<AppResponse<String>> upVote(@RequestBody LikeVideoRequest request,
                                            @RequestHeader("X-UserId") String userId,
                                            @RequestHeader("X-Username") String username) {
        return interactVideoService.upVoteVideo(request, userId)
                .map(response -> {
                    return AppResponse.<String>builder()
                            .result(response)
                            .build();
                });
    }

    @PostMapping("/down-vote")
    public Mono<AppResponse<String>> downVote(@RequestBody DisLikeVideoRequest request,
                                            @RequestHeader("X-UserId") String userId,
                                            @RequestHeader("X-Username") String username) {
        return interactVideoService.downVoteVideo(request, userId)
                .map(response -> {
                    return AppResponse.<String>builder()
                            .result(response)
                            .build();
                });
    }

    @PostMapping("/view")
    public Mono<AppResponse<Boolean>> newView(@RequestBody NewViewRequest request,
                                            @RequestHeader("X-UserId") String userId,
                                            @RequestHeader("X-Username") String username) {
        return interactVideoService.newViewCount(request, userId)
                .map(response -> {
                    return AppResponse.<Boolean>builder()
                            .result(response)
                            .build();
                });
    }

    @PostMapping("/progress")
    public Mono<AppResponse<String>> progress(@RequestBody SaveProcessVideoRequest request,
                                              @RequestHeader("X-UserId") String userId,
                                              @RequestHeader("X-Username") String username) {
        return interactVideoService.saveProgress(request, userId)
                .map(response -> {
                    return AppResponse.<String>builder().result(response)
                            .build();
                });
    }

    @GetMapping("/link/{videoId}")
    public AppResponse<VideoDetailsResponse> getVideoLink(@PathVariable("videoId") String videoId,
                                                                @RequestHeader("X-UserId") String userId,
                                                                @RequestHeader("X-Username") String username) {
        var response = videoService.getVideoDetails(videoId, userId);
        return AppResponse.<VideoDetailsResponse>builder()
                .result(response).build();
    }
}
