package com.nlu.app.event.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.KafkaMessage;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.event.NewVideoCreatedEvent;
import com.nlu.app.common.share.event.NotificationCreatedEvent;
import com.nlu.app.configuration.WebClientBuilder;
import com.nlu.app.entity.Notification;
import com.nlu.app.entity.Outbox;
import com.nlu.app.event.EventListener;
import com.nlu.app.mapper.NotificationMapper;
import com.nlu.app.mapper.OutboxMapper;
import com.nlu.app.repository.NotificationRepository;
import com.nlu.app.repository.OutboxRepository;
import com.nlu.app.repository.webclient.IdentityWebClient;
import com.nlu.app.repository.webclient.ProfileWebClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CreateNewVideoHandler {
    NotificationRepository notificationRepository;
    OutboxRepository outboxRepository;
    ObjectMapper objectMapper;
    NotificationMapper notificationMapper;
    OutboxMapper outboxMapper;
    @NonFinal
    WebClient pWebClient;
    @NonFinal
    WebClient iWebClient;

    @Autowired
    public void setpWebClient(@Qualifier("profileWebClient") WebClient pWebClient) {
        this.pWebClient = pWebClient;
    }

    @Autowired
    public void setiWebClient(@Qualifier("identityWebClient") WebClient iWebClient) {
        this.iWebClient = iWebClient;
    }

    /**
     * Consume sự kiện {@link NewVideoCreatedEvent}, sau đó tiến hành lấy toàn bộ user id đang theo dõi người đăng video hiện tại,
     * để tạo một loạt thông báo tương ứng tới họ.
     * @param message
     * @param ack
     * @throws JsonProcessingException
     */
    @Transactional
    public void consumeEvent(KafkaMessage message, Acknowledgment ack) throws JsonProcessingException {
        var event = objectMapper.readValue(message.payload(), NewVideoCreatedEvent.class);
        var profileWebClient = WebClientBuilder.createClient(pWebClient, ProfileWebClient.class);
        var identityWebClient = WebClientBuilder.createClient(iWebClient, IdentityWebClient.class);
        var profileMono = profileWebClient.getFollowerIds(event.getUserId());
        var identityMono = identityWebClient.getUser(event.getUserId());
        var notifications = new ArrayList<Notification>();
        Mono.zip(profileMono, identityMono)
                .map(tuple -> {
                    String username = tuple.getT2().getResult().getUsername();
                    String content = String.format(
                            "Người dùng %s mà bạn đang theo dõi vừa upload video mới!"
                            , username);
                    tuple.getT1().getResult().getFollowers().stream()
                            .map(userId -> notificationMapper
                                    .forNotifyFollower(event, userId, content))
                            .forEach(notification -> notifications.add(notification));
                    return Mono.just("_");
                }).block();
        notificationRepository.saveAll(notifications);
        var events = notifications.stream()
                .map(notify -> notificationMapper.mapToCreatedEvent(notify)).toList();
        List<Outbox> outboxes = new ArrayList<>();
        Outbox outbox = null;
        for (NotificationCreatedEvent ev : events) {
            outbox = outboxMapper.toSuccessOutbox(ev, ev.getUserId(), message.sagaAction());
            outboxes.add(outbox);
        }
        outboxRepository.saveAll(outboxes);
        ack.acknowledge();
    }
}
