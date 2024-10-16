package com.nlu.app.repository.webclient;

import com.nlu.app.common.share.dto.comment_service.response.CommentResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileResponseDTO;
import com.nlu.app.dto.AppResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface CommentWebClient {
    @GetExchange(url = "comment/users/comment/{commentId}")
    Mono<AppResponse<List<CommentResponse>>> getCommentsReplied(@PathVariable("commentId") String commentId,
                                                                @RequestHeader("X-UserId") String userId,
                                                                @RequestHeader("X-Username") String username);
    @GetExchange(url = "comment/users/video/{videoId}")
    Mono<AppResponse<List<CommentResponse>>> getCommentsByVideoId(@PathVariable("videoId") String videoId,
                                                                @RequestHeader("X-UserId") String userId,
                                                                @RequestHeader("X-Username") String username);

    @PostExchange(url = "comment/users/interacts")
    Mono<AppResponse<Map<String, String>>> getUserReactionsForComments(@RequestBody List<String> commentIds,
                                                                       @RequestHeader("X-UserId") String userId,
                                                                       @RequestHeader("X-Username") String username);
}