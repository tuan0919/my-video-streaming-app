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
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final OutboxRepository outboxRepository;
    private final IdentityWebClient identityWebClient;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @Transactional
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
                .map(_ -> commentRepository.findById(comment.getParentId()).get())
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
                    outbox.setAggregateType("replied");
                    try {
                        outbox.setPayload(objectMapper.writeValueAsString(event));
                        outboxRepository.save(outbox);
                        commentRepository.save(comment);
                        return Mono.just(comment);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return Mono.error(e);
                    }
                }).subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Boolean> checkIsReplyComment(Comment comment) {
        String otherComment = comment.getParentId();
        if (otherComment == null) return Mono.just(false);
        return Mono.fromCallable(() -> commentRepository.findById(otherComment).isPresent())
                .subscribeOn(Schedulers.boundedElastic());
    }
}
