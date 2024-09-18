package com.nlu.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nlu.app.entity.Outbox;

public interface OutboxRepository extends JpaRepository<Outbox, String> {}
