package com.nlu.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.dto.notification.CommentRepliedDTO;
import com.nlu.app.common.event.comment_created.CommentCreationEvent;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.common.share.event.CommentReplyEvent;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final OutboxRepository outboxRepository;
    private final IdentityWebClient identityWebClient;

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Transactional
    public Mono<String> createComment(String token, CommentCreationRequestDTO request) {
        TokenUserRequest requestUserInfo = new TokenUserRequest(token);
        return identityWebClient.userInfo(requestUserInfo)
                .flatMap(info -> {
                    String userId = info.getResult().getUserId();
                    Comment comment = new Comment();
                    comment.setUserId(userId);
                    comment.setContent(request.getContent());
                    comment.setVideoId(request.getVideoId());
                    comment.setCreateAt(LocalDateTime.now());
                    comment.setUpdateAt(LocalDateTime.now());
                    comment.setParentId(request.getParentId());
                    return Mono.fromCallable(() -> _insertToDB_(comment))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .switchIfEmpty(Mono.error(new ApplicationException(ErrorCode.UNAUTHENTICATED)))
                .map(value -> "OK");
    }

    private Comment _insertToDB_(Comment comment) throws JsonProcessingException {
        boolean isNotification = false;
        Comment parentCmt = null;
        String userId = comment.getUserId();
        /* TODO:
         *  Need to check first if this comment is worth for notification.
         *  when a comment need to be notified, its mean it either send USER_REPLY_COMMENT
         *  or USER_COMMENT_VIDEO events.
         *  Then other services will catch the event and handle the rest.
         */
        if (comment.getParentId() != null) {
            var o = commentRepository.findById(comment.getParentId());
            if (o.isEmpty()) throw new ApplicationException(ErrorCode.PARENT_COMMENT_NOT_EXISTED);
            parentCmt = o.get();
            isNotification = !parentCmt.getUserId().equalsIgnoreCase(userId); // Make no sense if notify about yourself
        }
        commentRepository.save(comment);

        if (isNotification) {
//                  In case comment is a reply, we need to publish its DTO to topic, using outbox pattern.
            ObjectMapper objectMapper = new ObjectMapper();
            var event = CommentReplyEvent.builder()
                    .commentId(comment.getId())
                    .content(comment.getContent())
                    .userId(userId)
                    .parentCommentId(parentCmt.getId())
                    .parentUserId(parentCmt.getUserId())
                    .build();
//                    Insert to outbox table
            Outbox outbox = Outbox.builder()
                            .payload(objectMapper.writeValueAsString(event))
                            .sagaId(comment.getId())
                            .sagaAction(SagaAction.USER_REPLY_COMMENT)
                            .sagaStepStatus(SagaStatus.SUCCESS)
                            .sagaStep(SagaAdvancedStep.NOTIFICATION_CREATE)
                            .aggregateId(comment.getId())
                            .aggregateType("comment.topics")
                            .build();
            outboxRepository.save(outbox);
        }
        return comment;
    }
}
