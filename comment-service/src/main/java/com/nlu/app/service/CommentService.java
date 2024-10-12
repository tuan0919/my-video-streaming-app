package com.nlu.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.event.CommentReplyEvent;
import com.nlu.app.configuration.WebClientBuilder;
import com.nlu.app.dto.request.CommentCreationRequestDTO;
import com.nlu.app.entity.Comment;
import com.nlu.app.entity.Outbox;
import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import com.nlu.app.mapper.CommentMapper;
import com.nlu.app.mapper.OutboxMapper;
import com.nlu.app.repository.CommentRepository;
import com.nlu.app.repository.OutboxRepository;
import com.nlu.app.repository.webclient.VideoStreamingWebClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final OutboxRepository outboxRepository;
    private WebClient vWebClient;
    private final CommentMapper commentMapper;
    private final OutboxMapper outboxMapper;

    @Autowired
    public void setvWebClient(@Qualifier("videoStreamingWebClient") WebClient vWebClient) {
        this.vWebClient = vWebClient;
    }

    @Transactional
    public String createComment(String userId, CommentCreationRequestDTO request) {
        var videoStreamingWebClient = WebClientBuilder.createClient(vWebClient, VideoStreamingWebClient.class);
        boolean isVideoExisted = videoStreamingWebClient
                .checkExisted(request.getVideoId())
                .block()
                .getResult();
        if (!isVideoExisted) {
            throw new ApplicationException(ErrorCode.TARGET_VIDEO_NOT_EXISTED);
        }
        Comment parent = null;
        if (request.getParentId() != null) {
            if (!commentRepository.existsById(request.getParentId())) {
                throw new ApplicationException(ErrorCode.PARENT_COMMENT_NOT_EXISTED);
            }
            parent = commentRepository.findById(request.getParentId()).get();
        } else {
            parent = null;
        }
        var comment = commentMapper.mapToDTO(userId, request, parent);
        _insertToDB_(comment);
        return "OK";
    }

    /**
     * Insert database vào trong database, đồng thời bắn ra event {@link CommentReplyEvent} nếu
     * comment này là reply của một comment khác.
     * @param comment
     * @return
     */
    private Comment _insertToDB_(Comment comment) {
        boolean isNotification = false;
        String userId = comment.getUserId();
        /*
         *  Need to check first if this comment is worth for notification.
         *  when a comment need to be notified, its mean it either send USER_REPLY_COMMENT
         *  or USER_COMMENT_VIDEO events.
         *  Then other services will catch the event and handle the rest.
         */
        if (comment.getParent() != null) {
            isNotification = !comment.getParent().getUserId().equalsIgnoreCase(userId); // Make no sense if notify about yourself
        }
        commentRepository.save(comment);

        if (isNotification) {
//                  In case comment is a reply, we need to publish its DTO to topic, using outbox pattern.
            ObjectMapper objectMapper = new ObjectMapper();
            var event = commentMapper.mapToCommentReplyEvent(comment);
//                    Insert to outbox table
            Outbox outbox = outboxMapper.toSuccessOutbox(event, userId, SagaAction.USER_REPLY_COMMENT);
            outboxRepository.save(outbox);
        }
        return comment;
    }
}
