package com.nlu.app.route;
import com.nlu.app.filter.AuthenticationFilter;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class Routes {
    @Value("${identity-service.domain}")
    String identityDomain;
    @Value("${video-streaming-service.domain}")
    String videoStreamingDomain;
    @Value("${comment-service.domain}")
    String commentDomain;
    @Value("${gateway-service./prefix}")
    String prefix;
    @Value("${gateway-service.strip}")
    int PREFIX_STRIP;

    AuthenticationFilter authenticationFilter;
    @Autowired
    public void setAuthenticationFilter(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        var authFilter = authenticationFilter
                .apply(new AuthenticationFilter.Config());
        return builder.routes()
                .route(r -> r.path(prefix+"/identity/**")
                        .filters(f -> f.stripPrefix(PREFIX_STRIP).filter(authFilter))
                        .uri(identityDomain)
                )
                .route(r -> r.path(prefix+"/comment/**")
                        .filters(f -> f.stripPrefix(PREFIX_STRIP).filter(authFilter))
                        .uri(commentDomain)
                )
                .route(r -> r.path(prefix+"/video-streaming/**")
                        .filters(f -> f.stripPrefix(PREFIX_STRIP).filter(authFilter))
                        .uri(videoStreamingDomain)
                ).build();
    }
}
