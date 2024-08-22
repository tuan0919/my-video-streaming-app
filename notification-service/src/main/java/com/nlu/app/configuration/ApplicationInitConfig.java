package com.nlu.app.configuration;

import com.nlu.app.entity.Notification;
import com.nlu.app.repository.NotificationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    NotificationRepository repository;
    @EventListener(ApplicationReadyEvent.class)
    void applicationRunner() {
        var initValue = Notification.builder()
                        .id("#").timestamp(System.currentTimeMillis()).userId("#")
                        .build();
        log.info("Initializing application.....");
        repository.findById("#")
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Does not found init value, creating new one");
                    return repository.insert(initValue)
                            .map(comment -> comment);
                }))
                .map(value -> {
                    log.info("Found init value");
                    return Mono.just(value);
                })
                .subscribe(_ -> log.info("Initialized"));
    }
}
