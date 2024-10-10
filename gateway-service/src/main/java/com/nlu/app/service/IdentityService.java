package com.nlu.app.service;
import com.nlu.app.common.share.dto.identity_service.request.IntrospectRequest;
import com.nlu.app.common.share.dto.identity_service.response.IntrospectResponse;
import com.nlu.app.repository.IdentityWebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class IdentityService {
    private final IdentityWebClient identityWebClient;

    @Autowired
    public IdentityService(IdentityWebClient identityWebClient) {
        this.identityWebClient = identityWebClient;
    }

    public Mono<IntrospectResponse> introspect(String token) {
        var dto = IntrospectRequest.builder()
                .token(token)
                .build();
        return identityWebClient
                .introspect(dto)
                .map(result -> result.getResult());
    }
}
