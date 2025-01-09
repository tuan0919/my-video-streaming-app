package com.nlu.app.service;

import com.nlu.app.common.share.dto.notification_service.response.UnreadCountResponse;
import com.nlu.app.common.share.webclient.NotificationWebClient;
import com.nlu.app.configuration.WebClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    @NonFinal
    NotificationWebClient notificationWebClient;

    @Autowired
    private void setNotificationWebClient(@Qualifier("notificationWebClient") WebClient webClient) {
        this.notificationWebClient = WebClientBuilder.createClient(webClient, NotificationWebClient.class);
    }

    public UnreadCountResponse countUnreadNotification(String userId) {
        return notificationWebClient.countUnread(userId).block()
                .getResult();
    }
}
