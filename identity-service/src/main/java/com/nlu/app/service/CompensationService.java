package com.nlu.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.SagaCompensationStep;
import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.common.share.event.IdentityUpdatedEvent;
import com.nlu.app.common.share.event.UserRemovedEvent;
import com.nlu.app.constant.PredefinedRole;
import com.nlu.app.entity.Outbox;
import com.nlu.app.entity.Role;
import com.nlu.app.entity.User;
import com.nlu.app.repository.OutboxRepository;
import com.nlu.app.repository.RoleRepository;
import com.nlu.app.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CompensationService {
    OutboxRepository outboxRepository;
    ObjectMapper objectMapper;
    UserRepository userRepository;
    RoleRepository roleRepository;
    RedisTemplate<String, Object> redisTemplate;

    public void doCompensation(String sagaId) throws JsonProcessingException {
        var outboxLogs = outboxRepository.findAllBySagaId(sagaId);
        for (var log : outboxLogs) {
            switch (log.getSagaStep()) {
                case SagaAdvancedStep.IDENTITY_CREATE -> forIdentityCreate(log);
                case SagaAdvancedStep.IDENTITY_UPDATE -> forIdentityUpdate(log);
            }
        }
    }

    private void forIdentityUpdate(Outbox log) throws JsonProcessingException {
        var cachedEvent = (IdentityUpdatedEvent) redisTemplate.opsForValue().get(log.getSagaId());
        var userId = log.getAggregateId();
        var user = userRepository.findById(userId).get();
        var roles = roleRepository.findAllById(cachedEvent.getRoles());
        String hashPassword = cachedEvent.getPassword();
        // TODO: temporary solution, need to fix this due to lacking of checking which fields are changed.
        user.setRoles(new HashSet<>(roles));
        user.setPassword(hashPassword);
        userRepository.save(user);
        redisTemplate.delete(log.getSagaId());
        var outbox = Outbox.builder()
                .aggregateType("identity.topics")
                .sagaAction(log.getSagaAction())
                .sagaStep(SagaCompensationStep.COMPENSATION_IDENTITY_UPDATE)
                .aggregateId(userId)
                .sagaId(log.getSagaId())
                .sagaStepStatus(SagaStatus.SUCCESS)
                .payload(objectMapper.writeValueAsString(cachedEvent))
                .build();
        outboxRepository.save(outbox);
    }

    private void forIdentityCreate(Outbox log) throws JsonProcessingException {
        String userId = log.getAggregateId();
        if (userRepository.findById(userId).isPresent()) {
            userRepository.deleteById(userId);
            var event = UserRemovedEvent.builder()
                    .userId(userId)
                    .build();
            var outbox = Outbox.builder()
                    .aggregateType("identity.topics")
                    .sagaAction(log.getSagaAction())
                    .sagaStep(SagaCompensationStep.COMPENSATION_IDENTITY_CREATE)
                    .aggregateId(userId)
                    .sagaId(log.getSagaId())
                    .sagaStepStatus(SagaStatus.SUCCESS)
                    .payload(objectMapper.writeValueAsString(event))
                    .build();
            outboxRepository.save(outbox);
        }
    }
}
