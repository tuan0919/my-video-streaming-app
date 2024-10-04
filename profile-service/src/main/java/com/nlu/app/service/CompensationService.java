package com.nlu.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.SagaCompensationStep;
import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.common.share.event.ProfileRemovedEvent;
import com.nlu.app.entity.Outbox;
import com.nlu.app.mapper.OutboxMapper;
import com.nlu.app.mapper.ProfileMapper;
import com.nlu.app.repository.OutboxRepository;
import com.nlu.app.repository.ProfileRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Service
public class CompensationService implements ICompensationService {
    OutboxRepository outboxRepository;
    OutboxMapper outboxMapper;
    ProfileMapper profileMapper;
    ProfileRepository profileRepository;

    @Transactional
    public void doCompensation(String sagaId) {
        var outboxLogs = outboxRepository.findAllBySagaId(sagaId);
        for (var log : outboxLogs) {
            switch (log.getSagaStep()) {
                case SagaAdvancedStep.PROFILE_CREATE -> forProfileCreate(log);
            }
        }
    }

    @Transactional
    void forProfileCreate(Outbox log) {
        String profileId = log.getAggregateId();
        var oProfile = profileRepository.findById(profileId);
        if (oProfile.isPresent()) {
            profileRepository.deleteById(profileId);
            var event = profileMapper.toProfileRemovedEvent(oProfile.get());
            var outbox = outboxMapper.toCompenstationOutbox(event);
            outboxRepository.save(outbox);
        }
    }
}
