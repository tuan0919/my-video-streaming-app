package com.nlu.app.controller;

import com.nlu.app.annotation.JwtToken;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.dto.request.SaveFileRequest;
import com.nlu.app.dto.request.VideoCreationRequest;
import com.nlu.app.dto.response.SaveFileResponse;
import com.nlu.app.dto.response.VideoCreationResponse;
import com.nlu.app.service.VideoService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VideoController {
    VideoService videoService;
    @PostMapping
    public Mono<AppResponse<VideoCreationResponse>> upNewVideo(@RequestBody VideoCreationRequest request,
                                                               @JwtToken String token) {
        return videoService.createVideo(token, request)
                .map(response -> {
                    return AppResponse.<VideoCreationResponse>builder()
                            .result(response).build();
                });
    }
}
