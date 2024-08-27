package com.nlu.app.repository;

import com.nlu.app.entity.Comment;
import com.nlu.app.entity.Outbox;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface OutboxRepository extends ReactiveMongoRepository<Outbox, String> {

}