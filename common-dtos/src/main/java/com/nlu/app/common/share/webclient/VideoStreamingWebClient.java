package com.nlu.app.common.share.webclient;

import com.nlu.app.common.share.dto.AppResponse;
import com.nlu.app.common.share.dto.videoStreaming_service.response.VideoCountsResponse;
import com.nlu.app.common.share.dto.videoStreaming_service.response.VideoDetailsResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

import java.util.List;

public interface VideoStreamingWebClient {
    @GetExchange(url = "video-streaming/videos/link/{videoId}")
    Mono<AppResponse<VideoDetailsResponse>> getVideoDetails(@PathVariable("videoId") String videoId,
                                                            @RequestHeader("X-UserId") String userId,
                                                            @RequestHeader("X-Username") String username);
    @GetExchange(url = "video-streaming/videos/count/{userId}")
    Mono<AppResponse<VideoCountsResponse>> getVideoCounts(@PathVariable("userId") String userId);
    @GetExchange(url = "video-streaming/videos/new-feed")
    Mono<AppResponse<List<String>>> getIds_SortByPoints(@RequestParam("page") Integer page,
                                                @RequestParam("pageSize") Integer pageSize);
    @GetExchange(url = "video-streaming/videos/new-feed")
    Mono<AppResponse<List<String>>> getIds_SortByPointsExcludeId(@RequestParam("page") Integer page,
                                                        @RequestParam("pageSize") Integer pageSize,
                                                         @RequestParam("exclude") String videoId);
    @GetExchange(url = "video-streaming/videos/search")
    Mono<AppResponse<List<String>>> getIds_SearchByTitle(@RequestParam("page") Integer page,
                                                                 @RequestParam("pageSize") Integer pageSize,
                                                                 @RequestParam("title") String title);
}
