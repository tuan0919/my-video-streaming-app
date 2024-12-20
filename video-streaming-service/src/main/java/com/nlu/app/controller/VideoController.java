package com.nlu.app.controller;

import com.nlu.app.common.share.dto.file_service.response.SignedURLResponse;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.dto.request.*;
import com.nlu.app.dto.response.VideoCreationResponse;
import com.nlu.app.common.share.dto.videoStreaming_service.response.VideoDetailsResponse;
import com.nlu.app.service.FileService;
import com.nlu.app.service.InteractVideoService;
import com.nlu.app.service.VideoService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VideoController {
    VideoService videoService;
    InteractVideoService interactVideoService;

    @PutMapping("/upload")
    public AppResponse<SignedURLResponse> getTempUploadUrl(@RequestBody PutFileRequest request,
                                                           @RequestHeader("X-UserId") String userId,
                                                           @RequestHeader("X-Username") String username) {
        var response = videoService.getUrlUploadToTemp(request, userId, username);
        return AppResponse.<SignedURLResponse>builder()
                .result(response)
                .build();
    }

    @PostMapping
    public AppResponse<VideoCreationResponse> upNewVideo(@RequestBody VideoCreationRequest request,
                                                               @RequestHeader("X-UserId") String userId,
                                                               @RequestHeader("X-Username") String username) {
        var response = videoService.createVideo(userId, username, request);
        return AppResponse.<VideoCreationResponse>builder()
                .result(response).build();
    }

    @PostMapping("/test")
    public AppResponse<VideoCreationResponse> upNewVideoWithoutThumbnail(@RequestBody VideoCreationRequest request,
                                                         @RequestHeader("X-UserId") String userId,
                                                         @RequestHeader("X-Username") String username) {
        var response = videoService.createVideoWithoutThumbnail(userId, username, request);
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

    @GetMapping("new-feed")
    public AppResponse<List<String>> getVideoIdsFromStart(@RequestParam("page") Integer page,
                                                          @RequestParam("pageSize") Integer pageSize,
                                                          @RequestParam(value = "exclude", required = false) String excludeId) {
        var response = (excludeId == null)
                ? videoService.videoIdsFromStart(page, pageSize)
                : videoService.videoIdsFromStart(page, pageSize, excludeId);
        return AppResponse.<List<String>>builder()
                .result(response.stream().toList())
                .build();
    }

    @GetMapping("/link/{videoId}")
    public AppResponse<VideoDetailsResponse> getVideoLink(@PathVariable("videoId") String videoId,
                                                                @RequestHeader("X-UserId") String userId,
                                                                @RequestHeader("X-Username") String username) {
        var response = videoService.getVideoDetails(videoId, userId);
        return AppResponse.<VideoDetailsResponse>builder()
                .result(response).build();
    }

    @GetMapping("/is-existed/{videoId}")
    public AppResponse<Boolean> checkExists(@PathVariable("videoId") String videoId) {
        var response = videoService.checkExists(videoId);
        return AppResponse.<Boolean>builder()
                .result(response).build();
    }
}
