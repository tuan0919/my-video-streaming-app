package com.nlu.app.repository;

import com.nlu.app.entity.Notification;
import com.nlu.app.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<Outbox, String> {
    public List<Outbox> findAllBySagaId(String sagaId);
}