package com.nlu.app.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.exception.ErrorCode;
import com.nlu.app.route.Routes;
import com.nlu.app.service.IdentityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
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
                log.info("Failed to authenticate due to missing token.");
                return monoError(response, ErrorCode.UNAUTHENTICATED);
            }
            String token = header.getFirst().replace("Bearer ", "");
            log.info("Token: {}", token);
            return service.introspect(token)
                    .flatMap(result -> {
                        if (result) {
                            log.info("Successfully authenticated.");
                            return chain.filter(exchange);
                        }
                        else {
                            log.info("Failed to authenticate.");
                            return monoError(response, ErrorCode.UNAUTHENTICATED);
                        }
                    })
                    .doOnError(error -> log.error(error.getMessage(), error))
                    .onErrorResume(_ -> monoError(response, ErrorCode.UNKNOWN_EXCEPTION));
        });
    }

    public Mono<Void> monoError(ServerHttpResponse response, ErrorCode errorCode) {
        response.setStatusCode(ErrorCode.UNAUTHENTICATED.getStatusCode());
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        AppResponse<?> appResponse = AppResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
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
