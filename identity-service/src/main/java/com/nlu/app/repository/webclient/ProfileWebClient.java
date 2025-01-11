package com.nlu.app.repository.webclient;

import com.nlu.app.common.share.dto.profile_service.request.ProfileCreationRequest;
import com.nlu.app.common.share.dto.CompensationRequest;
import com.nlu.app.common.share.dto.profile_service.response.ProfileCreationResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileResponseDTO;
import com.nlu.app.common.share.dto.saga.SagaAdvancedRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import com.nlu.app.dto.AppResponse;

import reactor.core.publisher.Mono;

public interface ProfileWebClient {
    @PostExchange(url = "profile/users", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<AppResponse<String>> createProfile(@RequestBody ProfileCreationRequest request);
    @PostExchange(url = "profile/users/internal/saga", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<String> sagaRequest(@RequestBody SagaAdvancedRequest request);
    @PostExchange(url = "profile/users/internal/compensation", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<String> compensation(@RequestBody CompensationRequest request);
}
