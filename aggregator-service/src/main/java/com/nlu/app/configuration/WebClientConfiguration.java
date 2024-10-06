package com.nlu.app.configuration;

import com.nlu.app.repository.webclient.IdentityWebClient;
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
    @Value("${identity-service.domain}")
    private String identityBaseURI;

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public IdentityWebClient identityWebClient(WebClient.Builder builder) {
        var identityWebClient = createWebClient(builder, identityBaseURI);
        return createClient(identityWebClient, IdentityWebClient.class);
    }

    @Bean
    public ProfileWebClient profileServiceClient(WebClient.Builder builder) {
        var profileWebClient = createWebClient(builder, profileBaseURI);
        return createClient(profileWebClient, ProfileWebClient.class);
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
