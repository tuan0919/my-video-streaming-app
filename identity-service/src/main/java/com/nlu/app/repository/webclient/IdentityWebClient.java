package com.nlu.app.repository.webclient;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import com.nlu.app.dto.AppResponse;
import com.nlu.app.dto.webclient.profile.request.ProfileCreationRequest;
import com.nlu.app.dto.webclient.profile.response.ProfileCreationResponse;

import reactor.core.publisher.Mono;

public interface IdentityWebClient {
    @PostExchange(url = "/profile", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<AppResponse<ProfileCreationResponse>> createProfile(@RequestBody ProfileCreationRequest request);
}
