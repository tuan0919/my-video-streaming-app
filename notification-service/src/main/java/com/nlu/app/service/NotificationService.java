package com.nlu.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nlu.app.entity.Notification;
import com.nlu.app.entity.Outbox;
import com.nlu.app.repository.NotificationRepository;
import com.nlu.app.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repository;
    private final OutboxRepository outboxRepository;

    @Transactional
    public Notification insertDB(Notification notification) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        var entity = repository.save(notification);
        var outbox = new Outbox();
        try {
            outbox.setPayload(objectMapper.writeValueAsString(entity));
            outbox.setType("insert");
            outbox.setAggregateType("created");
            outbox.setAggregateId(entity.getUserId());
            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return entity;
    }
}
