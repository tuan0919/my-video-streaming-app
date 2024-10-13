package com.nlu.app.event.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.KafkaMessage;
import com.nlu.app.common.share.event.CommentReplyEvent;
import com.nlu.app.configuration.WebClientBuilder;
import com.nlu.app.mapper.NotificationMapper;
import com.nlu.app.mapper.OutboxMapper;
import com.nlu.app.repository.NotificationRepository;
import com.nlu.app.repository.OutboxRepository;
import com.nlu.app.repository.webclient.IdentityWebClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserReplyHandler {
    NotificationRepository notificationRepository;
    ObjectMapper objectMapper;
    NotificationMapper notificationMapper;
    OutboxRepository outboxRepository;
    OutboxMapper outboxMapper;
    @NonFinal
    private WebClient webClient;

    @Autowired
    public void setWebClient(@Qualifier("identityWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public void consumeEvent(KafkaMessage message, Acknowledgment ack) throws JsonProcessingException {
        var event = objectMapper.readValue(message.payload(), CommentReplyEvent.class);
        String userId = event.getUserId();
        var identityWebClient = WebClientBuilder.createClient(webClient, IdentityWebClient.class);
        var userIdentity = identityWebClient.getUser(userId).block().getResult();
        String content = String.format(
                "Người dùng %s đã phản hồi bình luận của bạn: \"%s\""
                , userIdentity.getUsername(), event.getContent());
        var notification = notificationMapper.forCommentReply(event, content);
        notificationRepository.save(notification);
        var createdEvent = notificationMapper.mapToCreatedEvent(notification);
        var outbox = outboxMapper.toSuccessOutbox(createdEvent, userId, message.sagaAction());
        outboxRepository.save(outbox);
        log.info("consumed thành công event: {}", message.sagaAction());
        ack.acknowledge();
    }
}
