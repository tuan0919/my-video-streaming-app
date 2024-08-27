package com.nlu.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.dto.notification.CommentRepliedDTO;
import com.nlu.app.common.event.comment_created.CommentCreationEvent;
import com.nlu.app.dto.identity.request.TokenUserRequest;
import com.nlu.app.dto.request.CommentCreationRequestDTO;
import com.nlu.app.entity.Comment;
import com.nlu.app.entity.Outbox;
import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import com.nlu.app.repository.CommentRepository;
import com.nlu.app.repository.IdentityWebClient;
import com.nlu.app.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final OutboxRepository outboxRepository;
    private final IdentityWebClient identityWebClient;

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Mono<String> createComment(String token, CommentCreationRequestDTO request) {
        TokenUserRequest requestUserInfo = new TokenUserRequest(token);
        var wrap = new Object() {
            String userId;
        };
        return identityWebClient.userInfo(requestUserInfo)
                .map(info -> wrap.userId = info.getResult().getUserId())
                .then(Mono.defer(() -> {
                    Comment comment = new Comment();
                    comment.setUserId(wrap.userId);
                    comment.setContent(request.getContent());
                    comment.setVideoId(request.getVideoId());
                    comment.setTimestamp(LocalDateTime.now());
                    comment.setParentId(request.getParentId());
                    return insertToDB(comment);
                }))
                .map(value -> "OK")
                .switchIfEmpty(Mono.error(new ApplicationException(ErrorCode.UNKNOWN_EXCEPTION)));
    }


    private Mono<Comment> insertToDB(Comment comment) {
        return checkIsReplyComment(comment)
                .filter(isReply -> isReply)
                .flatMap(_ -> commentRepository.findById(comment.getParentId()))
                .flatMap(parentCmt -> {
//                  In case comment is a reply, we need to publish its DTO to topic, using outbox pattern.
                    ObjectMapper objectMapper = new ObjectMapper();
//                    Create DTO
                    CommentRepliedDTO dto = new CommentRepliedDTO();
                    dto.setUserId(parentCmt.getUserId());
                    dto.setUserCommentId(parentCmt.getId());
                    dto.setReplierCommentId(comment.getId());
                    dto.setReplierId(comment.getUserId());
                    dto.setContent(comment.getContent());

//                    Create Event object and warp DTO into it
                    CommentCreationEvent event = new CommentCreationEvent(dto);
//                    Insert to outbox table
                    Outbox outbox = new Outbox();
                    outbox.setAggregateId(parentCmt.getId());
                    outbox.setType("insert");
                    outbox.setAggregateType("created");
                    try {
                        outbox.setPayload(objectMapper.writeValueAsString(event));
                        return outboxRepository.save(outbox);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return Mono.error(e);
                    }
                })
                .then(commentRepository.save(comment));
    }

    private Mono<Boolean> checkIsReplyComment(Comment comment) {
        String otherComment = comment.getParentId();
        if (otherComment == null) return Mono.just(false);
        return commentRepository.findById(otherComment)
                .map(_ -> true)
                .defaultIfEmpty(false);
    }
}
