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
    @Value("${video-streaming-service.domain}")
    private String videoStreamingBaseURI;
    @Value("${notification-service.domain}")
    private String notificationBaseURI;

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean(value = "videoStreamingWebClient")
    public WebClient videoStreamingWebClient(WebClient.Builder builder) {
        return createWebClient(builder, videoStreamingBaseURI);
    }

    @Bean(value = "notificationWebClient")
    public WebClient notificationWebClient(WebClient.Builder builder) {
        return createWebClient(builder, notificationBaseURI);
    }

    private WebClient createWebClient(WebClient.Builder builder, String baseURI) {
        return builder
                .baseUrl(baseURI) // Thiết lập base URL cho WebClient
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
                .build();
    }
}
