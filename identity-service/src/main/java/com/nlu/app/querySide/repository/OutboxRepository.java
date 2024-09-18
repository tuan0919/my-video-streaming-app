package com.nlu.app.querySide.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nlu.app.querySide.entity.Outbox;

public interface OutboxRepository extends JpaRepository<Outbox, String> {}
