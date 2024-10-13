package com.nlu.app.route;
import com.nlu.app.filter.AuthenticationFilter;
import com.nlu.app.filter.AuthorizationFilter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
public class Routes {
    @Value("${identity-service.domain}")
    String identityDomain;
    @Value("${video-streaming-service.domain}")
    String videoStreamingDomain;
    @Value("${comment-service.domain}")
    String commentDomain;
    @Value("${profile-service.domain}")
    String profileDomain;
    @Value("${aggregator-service.domain}")
    String aggregatorDomain;
    @Value("${notification-service.domain}")
    String notificationDomain;
    @Value("${gateway-service./prefix}")
    String prefix;
    @Value("${gateway-service.strip}")
    int PREFIX_STRIP;

    AuthenticationFilter authenticationFilter;
    AuthorizationFilter authorizationFilter;

    @Autowired
    public void setAuthenticationFilter(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Autowired
    public void setAuthorizationFilter(AuthorizationFilter authorizationFilter) {
        this.authorizationFilter = authorizationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        var authFilter = authenticationFilter
                .apply(new AuthenticationFilter.Config());
        var authorFilter = authorizationFilter
                .apply(new AuthorizationFilter.Config());
        return builder.routes()
                .route(r -> r.path(prefix+"/identity/**")
                        .filters(f -> f.stripPrefix(PREFIX_STRIP)
                                .filter(authFilter)
                                .filter(authorFilter)
                        ).uri(identityDomain)
                )
                .route(r -> r.path(prefix+"/comment/**")
                        .filters(f -> f.stripPrefix(PREFIX_STRIP)
                                .filter(authFilter)
                                .filter(authorFilter)
                        )
                        .uri(commentDomain)
                )
                .route(r -> r.path(prefix+"/video-streaming/**")
                        .filters(f -> f.stripPrefix(PREFIX_STRIP)
                                .filter(authFilter)
                                .filter(authorFilter)
                        )
                        .uri(videoStreamingDomain)
                )
                .route(r -> r.path(prefix+"/profile/**")
                        .filters(f -> f.stripPrefix(PREFIX_STRIP)
                                .filter(authFilter)
                                .filter(authorFilter)
                        )
                        .uri(profileDomain)
                )
                .route(r -> r.path(prefix+"/aggregator/**")
                        .filters(f -> f.stripPrefix(PREFIX_STRIP)
                                .filter(authFilter)
                                .filter(authorFilter)
                        )
                        .uri(aggregatorDomain)
                )
                .route(r -> r.path(prefix+"/notification/**")
                        .filters(f -> f.stripPrefix(PREFIX_STRIP)
                                .filter(authFilter)
                                .filter(authorFilter)
                        )
                        .uri(notificationDomain)
                )
                .build();
    }
}
