package com.nlu.app.controller;

import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_UserDetailsDTO;
import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_VideoDetailsDTO;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.service.UserAggregateQuery;
import com.nlu.app.service.VideoAggregateQuery;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/query")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QueryController {
    UserAggregateQuery userAggregateQuery;
    VideoAggregateQuery videoAggregateQuery;

    @GetMapping("/{userId}")
    public Mono<AppResponse<ClientView_UserDetailsDTO>> query(@PathVariable String userId) {
        return userAggregateQuery.queryUserDetails(userId)
                .map(response -> AppResponse.<ClientView_UserDetailsDTO>builder()
                        .result(response)
                        .build());
    }

    @GetMapping("/video/{videoId}")
    public Mono<AppResponse<ClientView_VideoDetailsDTO>> query(@PathVariable String videoId,
                                                              @RequestHeader("X-UserId") String userId,
                                                              @RequestHeader("X-Username") String username) {
        return videoAggregateQuery.getVideoDetails(videoId, userId, username)
                .map(response -> AppResponse.<ClientView_VideoDetailsDTO>builder()
                        .result(response)
                        .build());
    }
}
