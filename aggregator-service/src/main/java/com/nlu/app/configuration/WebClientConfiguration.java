package com.nlu.app.configuration;

import com.nlu.app.repository.webclient.IdentityWebClient;
import com.nlu.app.repository.webclient.ProfileWebClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfiguration {
    @Value("${profile-service.domain}")
    private String profileBaseURI;
    @Value("${identity-service.domain}")
    private String identityBaseURI;
    @Value("${video-streaming-service.domain}")
    private String videoStreamingBaseURI;

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean(value = "identityWebClient")
    public WebClient identityWebClient(WebClient.Builder builder) {
        return createWebClient(builder, identityBaseURI);
    }

    @Bean(value = "profileWebClient")
    public WebClient profileWebClient(WebClient.Builder builder) {
        return createWebClient(builder, profileBaseURI);
    }

    @Bean(value = "videoStreamingWebClient")
    public WebClient videoStreamingWebClient(WebClient.Builder builder) {
        return createWebClient(builder, videoStreamingBaseURI);
    }

    private WebClient createWebClient(WebClient.Builder builder, String baseURI) {
        return builder
                .baseUrl(baseURI) // Thiết lập base URL cho WebClient
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
                .build();
    }
}
