package com.nlu.app.annotation;

import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class JwtTokenArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // Kiểm tra xem tham số có annotation @JwtToken không
        return parameter.getParameterAnnotation(JwtToken.class) != null;
    }

    @Override
    public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
        // Lấy giá trị từ header của HTTP request
        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Mono.error(new ApplicationException(ErrorCode.UNAUTHENTICATED));
        }

        // Xử lý token
        String token = authorizationHeader.substring(7); // Remove "Bearer " prefix

        return Mono.just(token);
    }
}