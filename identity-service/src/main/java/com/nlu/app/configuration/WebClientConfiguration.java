package com.nlu.app.configuration;

import com.nlu.app.repository.webclient.AggregatorWebClient;
import com.nlu.app.repository.webclient.NotificationWebClient;
import com.nlu.app.repository.webclient.ProfileWebClient;
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
    @Value("${notification-service.domain}")
    private String notificationBaseURI;
    @Value("${aggregator-service.domain}")
    private String aggregatorBaseURI;

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public NotificationWebClient identityWebClient(WebClient.Builder builder) {
        var identityWebClient = createWebClient(builder, notificationBaseURI);
        return createClient(identityWebClient, NotificationWebClient.class);
    }

    @Bean
    public ProfileWebClient profileServiceClient(WebClient.Builder builder) {
        var profileWebClient = createWebClient(builder, profileBaseURI);
        return createClient(profileWebClient, ProfileWebClient.class);
    }

    @Bean
    public AggregatorWebClient aggregatorWebClient(WebClient.Builder builder) {
        var aggregatorWebClient = createWebClient(builder, aggregatorBaseURI);
        return createClient(aggregatorWebClient, AggregatorWebClient.class);
    }

    private <T> T createClient(WebClient webClient, Class<T> clientClass) {
        var factory = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return factory.createClient(clientClass);
    }

    private WebClient createWebClient(WebClient.Builder builder, String baseURI) {
        return builder
                .baseUrl(baseURI) // Thiết lập base URL cho WebClient
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
                .build();
    }
}
