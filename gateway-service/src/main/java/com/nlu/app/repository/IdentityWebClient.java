package com.nlu.app.repository;

import com.nlu.app.dto.AppResponse;
import com.nlu.app.dto.request.IntrospectRequest;
import com.nlu.app.dto.response.IntrospectResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface IdentityWebClient {
    @PostExchange(url = "/auth/introspect", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<AppResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest request);
}
