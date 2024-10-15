package com.nlu.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.dto.comment_service.response.CommentResponse;
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

import java.util.ArrayList;
import java.util.List;

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
        String parentId = request.getParentId();
        if (parentId != null && !commentRepository.existsById(parentId)) {
            throw new ApplicationException(ErrorCode.PARENT_COMMENT_NOT_EXISTED);
        }
        var comment = commentMapper.mapToEntity(userId, request);
        _insertToDB_(comment, parentId);
        return "OK";
    }

    @Transactional
    public CommentResponse getComment(String id) {
        var oComment = commentRepository.findById(id);
        if (oComment.isEmpty()) {
            throw new ApplicationException(ErrorCode.COMMENT_NOT_EXISTED);
        }
        var comment = oComment.get();
        return commentMapper.mapToDTO(comment, comment.getReply().size());
    }

    /**
     * Lấy tất cả các comment phản hồi một comment khác.
     * @param parentId
     * @return danh sách các comment
     */
    @Transactional
    public List<CommentResponse> getCommentsReply(String parentId) {
        if (!commentRepository.existsById(parentId)) {
            throw new ApplicationException(ErrorCode.COMMENT_NOT_EXISTED);
        }
        var commentList = commentRepository.findCommentsByParent_Id(parentId);
        return commentList.stream().map(comment -> {
            return commentMapper.mapToDTO(comment, comment.getReply().size());
        }).toList();
    }

    /**
     * Lấy tất cả các comment (có parent = null) của một video
     * @param videoId
     * @return danh sách comment
     */
    @Transactional
    public List<CommentResponse> getCommentsOfVideo(String videoId) {
        var commentList = commentRepository.findCommentsByVideoIdAndParentIsNull(videoId);
        return commentList.stream().map(comment -> {
            return commentMapper.mapToDTO(comment, comment.getReply().size());
        }).toList();
    }

    /**
     * Insert database vào trong database, đồng thời bắn ra event {@link CommentReplyEvent} nếu
     * comment này là reply của một comment khác.
     * @param comment
     * @return
     */
    private Comment _insertToDB_(Comment comment, String parentId) {
        boolean isNotification = false;
        String userId = comment.getUserId();
        Comment parent = null;
        /*
         *  Need to check first if this comment is worth for notification.
         *  when a comment need to be notified, its mean it either send USER_REPLY_COMMENT
         *  or USER_COMMENT_VIDEO events.
         *  Then other services will catch the event and handle the rest.
         */
        if (parentId != null) {
            parent = commentRepository.findById(parentId).get();
            isNotification = !parent.getUserId().equalsIgnoreCase(userId); // Make no sense if notify about yourself
            comment.setParent(parent);
        }

        commentRepository.save(comment);

        if (isNotification) {
//                  In case comment is a reply, we need to publish its DTO to topic, using outbox pattern.
            var event = commentMapper.mapToCommentReplyEvent(comment);
//                    Insert to outbox table
            Outbox outbox = outboxMapper.toSuccessOutbox(event, parent.getUserId(), SagaAction.USER_REPLY_COMMENT);
            outboxRepository.save(outbox);
        }
        return comment;
    }
}
