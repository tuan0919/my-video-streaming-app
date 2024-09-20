package com.nlu.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EntityScan(
        basePackages = {
            "org.axonframework.eventhandling.tokenstore",
            "org.axonframework.modelling.saga.repository.jpa",
            "org.axonframework.eventsourcing.eventstore.jpa",
            "com.nlu.app"
        })
@EnableDiscoveryClient
public class IdentityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
