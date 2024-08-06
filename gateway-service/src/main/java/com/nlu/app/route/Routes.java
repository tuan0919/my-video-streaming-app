package com.nlu.app.route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class Routes {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/api/identity/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8080")
                )
                .route(r -> r.path("/api/file/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://wjbu.online:8888")
                ).build();
    }
}
