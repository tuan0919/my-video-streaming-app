package com.nlu.app.configuration;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;

// @Configuration
public class ObservationConfig {
    //    @Bean
    ObservedAspect observableAspect(ObservationRegistry registry) {
        return new ObservedAspect(registry);
    }
}
