package com.nlu.app.commandSide.state.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nlu.app.commandSide.state.entity.Outbox;

public interface OutboxRepository extends JpaRepository<Outbox, String> {}
