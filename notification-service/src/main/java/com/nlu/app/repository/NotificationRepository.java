package com.nlu.app.repository;

import com.nlu.app.entity.Notification;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface NotificationRepository extends ReactiveMongoRepository<Notification, String> {
    Flux<Notification> findByUserId(String userId);
}