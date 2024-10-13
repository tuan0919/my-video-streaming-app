package com.nlu.app.repository.webclient;

import com.nlu.app.common.share.dto.identity_service.response.UserResponse;
import com.nlu.app.dto.AppResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

public interface IdentityWebClient {
    @GetExchange(url = "identity/users/get/{userId}")
    Mono<AppResponse<UserResponse>> getUser(@PathVariable String userId);
}
