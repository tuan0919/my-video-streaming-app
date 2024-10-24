package com.nlu.app.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfiguration {
    @Value("${file-service.domain}")
    private String fileBaseURI;

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean(value = "fileWebClient")
    public WebClient fileWebClient(WebClient.Builder builder) {
        return createWebClient(builder, fileBaseURI);
    }

    private WebClient createWebClient(WebClient.Builder builder, String baseURI) {
        return builder
                .baseUrl(baseURI) // Thiết lập base URL cho WebClient
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
                .build();
    }
}
