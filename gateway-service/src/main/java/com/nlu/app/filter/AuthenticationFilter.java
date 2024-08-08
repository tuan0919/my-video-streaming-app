package com.nlu.app.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.exception.ErrorCode;
import com.nlu.app.route.Routes;
import com.nlu.app.service.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    ObjectMapper objectMapper;
    IdentityService service;
    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setService(IdentityService service) {
        this.service = service;
    }

    public AuthenticationFilter() {
        super(AuthenticationFilter.Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            var response = exchange.getResponse();
            var request = exchange.getRequest();
            String path = request.getURI().getPath();
            if (Routes.openEndpoints.stream().anyMatch(path::matches)) {
                return chain.filter(exchange);
            }
            var header = exchange.getRequest()
                    .getHeaders().get(HttpHeaders.AUTHORIZATION);
            if (header == null || header.isEmpty()) {
                return unauthorized(response);
            }
            String token = header.getFirst().replace("Bearer ", "");
            return service.introspect(token)
                    .flatMap(result -> {
                        if (result) return chain.filter(exchange);
                        else
                            return unauthorized(response);
                    })
                    .onErrorResume(_ -> unauthorized(response));
        });
    }

    public Mono<Void> unauthorized(ServerHttpResponse response) {
        response.setStatusCode(ErrorCode.UNAUTHENTICATED.getStatusCode());
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        AppResponse<?> appResponse = AppResponse.builder()
                .code(ErrorCode.UNAUTHENTICATED.getCode())
                .message("Unauthenticated!")
                .build();
        var bufferFactory = response.bufferFactory();
        String body = null;
        try {
            body = objectMapper.writeValueAsString(appResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        var monoBody = bufferFactory.wrap(body.getBytes());
        return response.writeWith(Mono.just(monoBody));
    }

    public static class Config {

    }

}
