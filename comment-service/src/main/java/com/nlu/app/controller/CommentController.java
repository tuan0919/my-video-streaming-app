package com.nlu.app.controller;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.dto.request.CommentCreationRequestDTO;
import com.nlu.app.service.CommentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CommentController {
    CommentService commentService;

    @PostMapping("/new")
    public AppResponse<String> addComment(@RequestBody CommentCreationRequestDTO request,
                                          @RequestHeader("X-UserId") String userId) {
        var response  = commentService.createComment(userId, request);
        return AppResponse.<String>builder()
                .result(response).build();
    }
}
