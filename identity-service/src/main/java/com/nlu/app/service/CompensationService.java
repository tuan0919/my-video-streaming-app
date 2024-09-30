package com.nlu.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.SagaCompensationStep;
import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.common.share.event.UserRemovedEvent;
import com.nlu.app.entity.Outbox;
import com.nlu.app.repository.OutboxRepository;
import com.nlu.app.repository.SagaLogRepository;
import com.nlu.app.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CompensationService {
    OutboxRepository outboxRepository;
    ObjectMapper objectMapper;
    public void doCompensation(String sagaId) throws JsonProcessingException {
        var outboxLogs = outboxRepository.findAllBySagaId(sagaId);
        for (var log : outboxLogs) {
            switch (log.getSagaStep()) {
                case SagaAdvancedStep.IDENTITY_CREATE -> forIdentityCreate(log);
            }
        }
    }


    UserRepository userRepository;
    @Transactional
    void forIdentityCreate(Outbox log) throws JsonProcessingException {
        String userId = log.getAggregateId();
        if (userRepository.findById(userId).isPresent()) {
            userRepository.deleteById(userId);
            var event = UserRemovedEvent.builder()
                    .userId(userId)
                    .build();
            var outbox = Outbox.builder()
                    .aggregateType("identity.created")
                    .sagaAction(SagaAction.CREATE_NEW_USER)
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
