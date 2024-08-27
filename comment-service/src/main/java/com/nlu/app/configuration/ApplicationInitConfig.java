package com.nlu.app.configuration;

import com.nlu.app.entity.Comment;
import com.nlu.app.repository.CommentRepository;
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
    CommentRepository commentRepository;
    @EventListener(ApplicationReadyEvent.class)
    void applicationRunner() {
        var initValue = Comment.builder()
                .id("#").build();
        log.info("Initializing application.....");
        commentRepository.findById("#")
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Does not found init value, creating new one");
                    return commentRepository.insert(initValue)
                            .map(comment -> comment);
                }))
                .map(value -> {
                    log.info("Found init value");
                    return Mono.just(value);
                })
                .subscribe(_ -> log.info("Initialized"));
    }
}
