package com.nlu.app.repository;

import com.nlu.app.dto.AppResponse;
import com.nlu.app.dto.webclient.identity.request.TokenUserRequest;
import com.nlu.app.dto.webclient.identity.response.TokenUserResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface IdentityWebClient {
    @PostExchange(url = "identity/auth/userinfo", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<AppResponse<TokenUserResponse>> userInfo(@RequestBody TokenUserRequest request);
}
