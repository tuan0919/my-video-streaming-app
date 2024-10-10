package com.nlu.app.repository;

import com.nlu.app.common.share.dto.identity_service.request.IntrospectRequest;
import com.nlu.app.common.share.dto.identity_service.response.IntrospectResponse;
import com.nlu.app.dto.AppResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface IdentityWebClient {
    @PostExchange(url = "identity/auth/introspect", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<AppResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest request);
}
