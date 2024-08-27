package com.nlu.app.repository;

import com.nlu.app.entity.Comment;
import com.nlu.app.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import reactor.core.publisher.Flux;

public interface OutboxRepository extends JpaRepository<Outbox, String> {

}