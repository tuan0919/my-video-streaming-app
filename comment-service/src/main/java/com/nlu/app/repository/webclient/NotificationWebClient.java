package com.nlu.app.repository.webclient;

import com.nlu.app.common.share.dto.notification_service.request.SendMessageWsRequest;
import com.nlu.app.dto.AppResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface NotificationWebClient {
    @PostExchange(url = "notification/ws", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<AppResponse<String>> sendToClient(@RequestBody SendMessageWsRequest request);
}
