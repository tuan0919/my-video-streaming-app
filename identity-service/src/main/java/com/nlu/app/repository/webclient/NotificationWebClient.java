package com.nlu.app.repository.webclient;

import com.nlu.app.common.share.dto.notification_service.request.NotificationCreationRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface NotificationWebClient {
    @PostExchange(url = "notification/users/internal", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<String> createNotification(@RequestBody NotificationCreationRequest request);
}
