package com.nlu.app.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
public class JwtAuthenticationEntryPoint implements ServerAuthenticationEntryPoint, ServerAccessDeniedHandler {
    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        return monoError(exchange.getResponse(), ErrorCode.UNAUTHENTICATED);
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        return monoError(exchange.getResponse(), ErrorCode.UNAUTHORIZED);
    }

    public Mono<Void> monoError(ServerHttpResponse response, ErrorCode errorCode) {
        response.setStatusCode(errorCode.getStatusCode());
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        AppResponse<?> appResponse = AppResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        var bufferFactory = response.bufferFactory();
        String body = null;
        var objectMapper = new ObjectMapper();
        try {
            body = objectMapper.writeValueAsString(appResponse);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException", e);
            response.setStatusCode(ErrorCode.UNKNOWN_EXCEPTION.getStatusCode());
            body = """
                    {
                        "code": "9999",
                        "message": "Something went wrong :(",
                    }
                    """;
        }
        var monoBody = bufferFactory.wrap(body.getBytes());
        return response.writeWith(Mono.just(monoBody));
    }
}
