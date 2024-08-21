package com.nlu.app.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import org.springframework.web.server.WebFilter;

@Configuration
public class WebfluxConfiguration implements WebFluxConfigurer {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Bean
    public WebFilter contextPathWebFilter() {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            if (path.startsWith(contextPath)) {
                String newPath = path.substring(contextPath.length());
                return chain.filter(exchange.mutate()
                        .request(exchange.getRequest().mutate().path(newPath).build())
                        .build());
            }
            return chain.filter(exchange);
        };
    }
}
