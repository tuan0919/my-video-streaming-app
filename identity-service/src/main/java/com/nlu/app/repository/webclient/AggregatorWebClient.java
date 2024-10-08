package com.nlu.app.repository.webclient;

import com.nlu.app.common.share.dto.CompensationRequest;
import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_UserDetailsDTO;
import com.nlu.app.common.share.dto.identity_service.response.UserResponse;
import com.nlu.app.common.share.dto.notification_service.request.NotificationCreationRequest;
import com.nlu.app.common.share.dto.saga.SagaAdvancedRequest;
import com.nlu.app.dto.AppResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface AggregatorWebClient {
    @GetExchange(url = "aggregator/query/{userId}")
    Mono<AppResponse<ClientView_UserDetailsDTO>> getUser(@PathVariable String userId);
}
