package com.nlu.app.controller;

import com.nlu.app.annotation.JwtToken;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.dto.request.*;
import com.nlu.app.dto.response.SaveFileResponse;
import com.nlu.app.dto.response.VideoCreationResponse;
import com.nlu.app.dto.response.VideoDetailsResponse;
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
    public Mono<AppResponse<VideoCreationResponse>> upNewVideo(@RequestBody VideoCreationRequest request,
                                                               @RequestHeader("X-UserId") String userId,
                                                               @RequestHeader("X-Username") String username) {
        return videoService.createVideo(userId, username, request)
                .map(response -> {
                    return AppResponse.<VideoCreationResponse>builder()
                            .result(response).build();
                });
    }

    @PostMapping("/upvote")
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
    public Mono<AppResponse<VideoDetailsResponse>> getVideoLink(@PathVariable("videoId") String videoId,
                                                                @RequestHeader("X-UserId") String userId,
                                                                @RequestHeader("X-Username") String username) {
        return videoService.getVideoDetails(videoId, userId)
                .map(response -> {
                    return AppResponse.<VideoDetailsResponse>builder()
                            .result(response).build();
                });
    }
}
