package com.nlu.app.controller;
import com.nlu.app.common.share.dto.comment_service.request.InteractCommentRequest;
import com.nlu.app.common.share.dto.comment_service.response.CommentResponse;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.dto.request.CommentCreationRequestDTO;
import com.nlu.app.service.CommentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CommentController {
    CommentService commentService;

    @PostMapping("/new")
    public AppResponse<CommentResponse> addComment(@RequestBody CommentCreationRequestDTO request,
                                          @RequestHeader("X-UserId") String userId) {
        var response  = commentService.createComment(userId, request);
        return AppResponse.<CommentResponse>builder()
                .result(response).build();
    }

    @GetMapping("/{commentId}")
    public AppResponse<CommentResponse> getComment(@PathVariable("commentId") String videoId) {
        var response = commentService.getComment(videoId);
        return AppResponse.<CommentResponse>builder()
                .result(response)
                .build();
    }

    @GetMapping("/comment/{commentId}")
    public AppResponse<List<CommentResponse>> getCommentsReplied(@PathVariable("commentId") String commentId) {
        var response = commentService.getCommentsReply(commentId);
        return AppResponse.<List<CommentResponse>>builder()
                .result(response)
                .build();
    }

    @GetMapping("/video/{videoId}")
    public AppResponse<List<CommentResponse>> getCommentsByVideoIdWithPagination(
            @PathVariable("videoId") String videoId,
            @RequestParam("page") Integer page,
            @RequestParam("pageSize") Integer pageSize
    ){
        var response = commentService.getCommentsOfVideoWithPagination(videoId, page, pageSize);
        return AppResponse.<List<CommentResponse>>builder()
                .result(response)
                .build();
    }

    @PostMapping("/comment/{commentId}/interact")
    public AppResponse<String> interactWithComment(@PathVariable("commentId") String commentId,
                                                   @RequestBody InteractCommentRequest request,
                                                   @RequestHeader("X-UserId") String userId) {
        var response = commentService.interactComment(userId, commentId, request);
        return AppResponse.<String>builder()
                .result(response)
                .build();
    }

    @PostMapping("/interacts")
    public AppResponse<Map<String, String>> getUserReactionsForComments(@RequestBody List<String> commentIds,
                                                                        @RequestHeader("X-UserId") String userId) {
        var response = commentService.getUserReactionsForComments(userId, commentIds);
        return AppResponse.<Map<String, String>>builder()
                .result(response)
                .build();
    }
}
