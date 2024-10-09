package com.nlu.app.controller;

import com.nlu.app.annotation.JwtToken;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.dto.request.LikeVideoRequest;
import com.nlu.app.dto.request.SaveFileRequest;
import com.nlu.app.dto.request.SaveProcessVideoRequest;
import com.nlu.app.dto.request.VideoCreationRequest;
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
                                                               @JwtToken String token) {
        return videoService.createVideo(token, request)
                .map(response -> {
                    return AppResponse.<VideoCreationResponse>builder()
                            .result(response).build();
                });
    }

    @PostMapping("/upvote")
    public Mono<AppResponse<String>> upVote(@RequestBody LikeVideoRequest request) {
        return interactVideoService.upVoteVideo(request)
                .map(response -> {
                    return AppResponse.<String>builder()
                            .result(response)
                            .build();
                });
    }

    @PostMapping("/progress")
    public Mono<AppResponse<String>> progress(@RequestBody SaveProcessVideoRequest request) {
        return interactVideoService.saveProgress(request)
                .map(response -> {
                    return AppResponse.<String>builder().result(response)
                            .build();
                });
    }

    @GetMapping("/link/{videoId}")
    public Mono<AppResponse<VideoDetailsResponse>> getVideoLink(@PathVariable("videoId") String videoId,
                                                                @RequestParam String userId) {
        return videoService.getVideoDetails(videoId, userId)
                .map(response -> {
                    return AppResponse.<VideoDetailsResponse>builder()
                            .result(response).build();
                });
    }
}
