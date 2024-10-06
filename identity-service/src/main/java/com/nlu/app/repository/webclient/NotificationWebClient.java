package com.nlu.app.repository.webclient;

import com.nlu.app.common.share.dto.CompensationRequest;
import com.nlu.app.common.share.dto.notification_service.request.NotificationCreationRequest;
import com.nlu.app.common.share.dto.saga.SagaAdvancedRequest;
import com.nlu.app.dto.AppResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface NotificationWebClient {
    @PostExchange(url = "notification/users/internal", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<String> createNotification(@RequestBody NotificationCreationRequest request);
    @PostExchange(url = "notification/users/internal/saga", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<String> sagaRequest(@RequestBody SagaAdvancedRequest request);
    @PostExchange(url = "notification/users/internal/compensation", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<String> compensation(@RequestBody CompensationRequest request);
}
