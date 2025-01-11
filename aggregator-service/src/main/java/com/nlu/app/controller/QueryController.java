package com.nlu.app.controller;

import com.nlu.app.common.share.dto.aggregator_service.response.*;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.service.CommentAggregateQuery;
import com.nlu.app.service.UserAggregateQuery;
import com.nlu.app.service.VideoAggregateQuery;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/query")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QueryController {
    UserAggregateQuery userAggregateQuery;
    VideoAggregateQuery videoAggregateQuery;
    CommentAggregateQuery commentAggregateQuery;

    @GetMapping("/{userId}")
    public Mono<AppResponse<ClientView_UserDetailsDTO>> query(@PathVariable String userId) {
        return userAggregateQuery.queryUserDetails(userId)
                .map(response -> AppResponse.<ClientView_UserDetailsDTO>builder()
                        .result(response)
                        .build());
    }

    @GetMapping("/video/{videoId}")
    public Mono<AppResponse<ClientView_VideoDetailsDTO>> queryVideoDetails(@PathVariable String videoId,
                                                              @RequestHeader("X-UserId") String userId,
                                                              @RequestHeader("X-Username") String username) {
        return videoAggregateQuery.getVideoDetails(videoId, userId, username)
                .map(response -> AppResponse.<ClientView_VideoDetailsDTO>builder()
                        .result(response)
                        .build());
    }

    @GetMapping("/video/new-feed")
    public Mono<AppResponse<List<ClientView_VideoDetailsDTO>>> queryNewFeed(@RequestParam("page") Integer page,
                                                                            @RequestParam("pageSize") Integer pageSize,
                                                                            @RequestParam(value = "exclude", required = false) String exclude,
                                                                            @RequestHeader("X-UserId") String userId,
                                                                            @RequestHeader("X-Username") String username) {
        System.out.println("exclude: "+exclude);
        if (exclude != null) {
            System.out.println("getVideoFeedExcludeId");
            return videoAggregateQuery.getVideoFeedExcludeId(userId, username, page, pageSize, exclude)
                    .map(response -> AppResponse.<List<ClientView_VideoDetailsDTO>>builder()
                            .result(response)
                            .build());
        }
        System.out.println("getVideoFeed");
        return videoAggregateQuery.getVideoFeed(userId, username, page, pageSize)
                .map(response -> AppResponse.<List<ClientView_VideoDetailsDTO>>builder()
                        .result(response)
                        .build());
    }

    @GetMapping("/video/{videoId}/comments")
    public Mono<AppResponse<List<ClientView_CommentDTO>>> queryCommentOfVideo(@PathVariable String videoId,
                                                                        @RequestParam("page") Integer page,
                                                                        @RequestParam("pageSize") Integer pageSize,
                                                                        @RequestParam(value = "exclude", required = false) String exclude,
                                                                        @RequestHeader("X-UserId") String userId,
                                                                        @RequestHeader("X-Username") String username) {
        return commentAggregateQuery.getCommentsByVideoId(videoId, userId, username, page, pageSize)
                .map(response -> AppResponse.<List<ClientView_CommentDTO>>builder()
                        .result(response)
                        .build());
    }

    @GetMapping("/comment/{commentId}/reply")
    public Mono<AppResponse<List<ClientView_CommentDTO>>> queryCommentRepiedOfComment(@PathVariable String commentId,
                                                                              @RequestHeader("X-UserId") String userId,
                                                                              @RequestHeader("X-Username") String username) {
        return commentAggregateQuery.getReplyComments(commentId, userId, username)
                .map(response -> AppResponse.<List<ClientView_CommentDTO>>builder()
                        .result(response)
                        .build());
    }

    @GetMapping("/notifications")
    public Mono<AppResponse<List<ClientView_NotificationDTO>>> queryUserNotifications(@RequestHeader("X-UserId") String userId,
                                                                                      @RequestHeader("X-Username") String username,
                                                                                      @RequestParam("page") Integer page,
                                                                                      @RequestParam("pageSize") Integer pageSize) {
        return userAggregateQuery.queryNotifications(userId, username, page, pageSize)
                .map(response -> AppResponse.<List<ClientView_NotificationDTO>>builder()
                        .result(response)
                        .build());
    }

    @GetMapping("/user/page/{userId}")
    public Mono<AppResponse<ClientView_UserPageDetailsDTO>> queryUserPageDetails(
            @RequestHeader("X-UserId") String userId,
            @RequestHeader("X-Username") String username,
            @PathVariable("userId") String targetUserId) {
        return userAggregateQuery.queryUserPageDetails(targetUserId, userId.equals(targetUserId), userId, username)
                .map(response -> AppResponse.<ClientView_UserPageDetailsDTO>builder()
                        .result(response)
                        .build());
    }
}
