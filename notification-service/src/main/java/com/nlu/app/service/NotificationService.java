package com.nlu.app.service;

import com.nlu.app.entity.Notification;
import com.nlu.app.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repository;

    public Mono<Notification> insertDB(Notification notification) {
//        return repository.insert(notification);
        return Mono.error(new Exception("\"Cannot consume message\""));
    }
}
