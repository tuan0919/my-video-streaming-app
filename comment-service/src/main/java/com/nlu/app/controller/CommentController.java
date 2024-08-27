package com.nlu.app.controller;

import com.nlu.app.annotation.JwtToken;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.dto.request.CommentCreationRequestDTO;
import com.nlu.app.service.CommentService;
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
@RequestMapping("/action")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CommentController {
    CommentService commentService;
    @PostMapping
    public Mono<AppResponse<String>> newComment(@JwtToken String token, @RequestBody CommentCreationRequestDTO request) {
        return commentService.createComment(token, request)
                .map(response -> {
                    return AppResponse.<String>builder()
                            .result(response).build();
                });
    }
}
