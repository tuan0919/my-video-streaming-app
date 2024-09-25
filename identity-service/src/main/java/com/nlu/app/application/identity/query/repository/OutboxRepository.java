package com.nlu.app.application.identity.query.repository;

import com.nlu.app.application.identity.query.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxRepository extends JpaRepository<Outbox, String> {}
