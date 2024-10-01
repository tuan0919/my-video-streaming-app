package com.nlu.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.SagaCompensationStep;
import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.common.share.event.ProfileRemovedEvent;
import com.nlu.app.entity.Outbox;
import com.nlu.app.repository.OutboxRepository;
import com.nlu.app.repository.ProfileRepository;
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
    ProfileRepository profileRepository;
    @Transactional
    public void doCompensation(String sagaId) throws JsonProcessingException {
        var outboxLogs = outboxRepository.findAllBySagaId(sagaId);
        for (var log : outboxLogs) {
            switch (log.getSagaStep()) {
                case SagaAdvancedStep.PROFILE_CREATE -> forProfileCreate(log);
            }
        }
    }

    @Transactional
    void forProfileCreate(Outbox log) throws JsonProcessingException {
        String profileId = log.getAggregateId();
        if (profileRepository.findById(profileId).isPresent()) {
            profileRepository.deleteById(profileId);
            var event = ProfileRemovedEvent
                    .builder().profileId(profileId).build();
            var outbox = Outbox.builder()
                    .aggregateType("profile.topics")
                    .sagaAction(SagaAction.CREATE_NEW_USER)
                    .sagaStep(SagaCompensationStep.COMPENSATION_PROFILE_CREATE)
                    .sagaStepStatus(SagaStatus.SUCCESS)
                    .sagaId(log.getSagaId())
                    .aggregateId(profileId)
                    .payload(objectMapper.writeValueAsString(event))
                    .build();
            outboxRepository.save(outbox);
        }
    }
}
