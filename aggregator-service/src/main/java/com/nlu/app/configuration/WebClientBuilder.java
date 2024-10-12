package com.nlu.app.configuration;


import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

public class WebClientBuilder {
    public static <T> T createClient(WebClient webClient, Class<T> clientClass) {
        var factory = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return factory.createClient(clientClass);
    }
}
