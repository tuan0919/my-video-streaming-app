package com.nlu.app.configuration;

import com.nlu.app.repository.IdentityWebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class WebClientConfiguration {
    @Value("${identity-service.domain}")
    String identityBaseURI;
    @Bean
    WebClient webClient() {
        return WebClient.builder()
                .baseUrl(identityBaseURI)
                .build();
    }
    @Bean
    IdentityWebClient identityWebClient(WebClient webClient) {
        var factory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient)).build();
        return factory.createClient(IdentityWebClient.class);
    }
}
