package com.nlu.app.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.configuration.RoutesAuthorizationConfig;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.exception.ErrorCode;
import com.nlu.app.service.IdentityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@Slf4j
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {
    ObjectMapper objectMapper;
    IdentityService service;
    RoutesAuthorizationConfig routesAuthorizationConfig;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Value("${gateway-service.openEndpoints}")
    String[] opens;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setService(IdentityService service) {
        this.service = service;
    }

    @Autowired
    public void setRoutesAuthorizationConfig(RoutesAuthorizationConfig routesAuthorizationConfig) {
        this.routesAuthorizationConfig = routesAuthorizationConfig;
    }

    public AuthorizationFilter() {
        super(AuthorizationFilter.Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            log.info("start AuthorizationFilter");
            var response = exchange.getResponse();
            var request = exchange.getRequest();
            String path = request.getURI().getPath();
            if (Stream.of(opens).anyMatch(path::matches)) {
                log.info("Authorization successfully cuz this path is public path");
                return chain.filter(exchange);
            }
            List<String> roles = exchange.getAttribute("roles");
            String method = exchange.getRequest().getMethod().name();
            if (roles == null) {
                log.warn("Why this user has no role?");
                return monoError(response, ErrorCode.UNAUTHORIZED);
            }

            var oRequired = getRequiredRolesForPath(path);
            if (oRequired.isEmpty()) {
                log.warn("Why authorization for this path: {} is empty?", path);
                return monoError(response, ErrorCode.UNAUTHORIZED);
            }
            var authorRequired = oRequired.get();
            if (!authorRequired.getMethod().stream().anyMatch(m -> method.equalsIgnoreCase(m))) {
                log.info("Path {} does not support this action: {}", path, method);
                return monoError(response, ErrorCode.UNKNOWN_ACTION);
            }
            for (var role : roles) {
                if (authorRequired.getRoles().contains(role)) {
                    log.info(String.format("OK, path: %s is accept role: %s", path, role));
                    log.info("end AuthorizationFilter");
                    return chain.filter(exchange);
                }
            }
            log.warn("Authorization failed, this path: {} author required roles: {}; method: {}",
                    authorRequired.getPath(),
                    authorRequired.getRoles(),
                    authorRequired.getMethod());
            return monoError(response, ErrorCode.UNAUTHORIZED);
        });
    }

    private Optional<RoutesAuthorizationConfig.RouteAuthorization> getRequiredRolesForPath(String path) {
        return routesAuthorizationConfig.getRoutesAuthorization()
                .stream()
                .filter(ra -> {
                    boolean check = antPathMatcher.match(ra.getPath(), path);
//                    log.info("{} matches {}: {}", ra.getPath(), path, check);
                    return check;
                })
                .findFirst();
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
