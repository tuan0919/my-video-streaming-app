package com.nlu.app.repository.webclient;

import com.nlu.app.common.share.dto.profile_service.response.ProfileResponseDTO;
import com.nlu.app.common.share.dto.videoStreaming_service.response.VideoDetailsResponse;
import com.nlu.app.dto.AppResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

public interface VideoStreamingWebClient {
    @GetExchange(url = "video-streaming/videos/link/{videoId}")
    Mono<AppResponse<VideoDetailsResponse>> getVideoDetails(@PathVariable("videoId") String videoId,
                                                       @RequestHeader("X-UserId") String userId,
                                                       @RequestHeader("X-Username") String username);
}
