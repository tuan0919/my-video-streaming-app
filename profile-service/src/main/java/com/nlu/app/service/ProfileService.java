package com.nlu.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.common.share.dto.profile_service.request.ProfileCreationRequest;
import com.nlu.app.common.share.dto.profile_service.response.ProfileCreationResponse;
import com.nlu.app.common.share.event.ProfileCreatedEvent;
import com.nlu.app.entity.Outbox;
import com.nlu.app.entity.Profile;
import com.nlu.app.repository.OutboxRepository;
import com.nlu.app.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final OutboxRepository outboxRepository;
    @Transactional
    public ProfileCreationResponse insert(ProfileCreationRequest request) throws JsonProcessingException {
        var profile = Profile.builder()
            .userId(request.getUserId())
            .avatarId(null)
            .country(request.getCountry())
            .fullName(request.getFullName())
            .bio(request.getBio())
            .build();
        profileRepository.save(profile);
        var event = ProfileCreatedEvent.builder()
                        .userId(request.getUserId())
                        .bio(request.getBio())
                        .fullName(request.getFullName())
                        .avatarId(null)
                        .country(request.getCountry())
                        .build();
        ObjectMapper objectMapper = new ObjectMapper();
        // TODO: will move this logic to separate command handler folder later.
        try {
            Outbox outbox = Outbox.builder()
                    .aggregateType("profile.created")
                    .sagaId(null)
                    .sagaStep(SagaAdvancedStep.PROFILE_CREATE)
                    .sagaAction(SagaAction.CREATE_NEW_USER)
                    .sagaId(request.getSagaId())
                    .sagaAction(request.getSagaAction())
                    .sagaStepStatus(SagaStatus.SUCCESS)
                    .aggregateId(profile.getProfileId())
                    .payload(objectMapper.writeValueAsString(event))
                    .build();
            throw new Exception("My exception");
        } catch (Exception e) {
            // tell other services to roll back the saga
            Outbox outbox = Outbox.builder()
                    .aggregateType("profile.created")
                    .sagaId(null)
                    .sagaStep(SagaAdvancedStep.PROFILE_CREATE)
                    .sagaAction(SagaAction.CREATE_NEW_USER)
                    .sagaId(request.getSagaId())
                    .sagaAction(request.getSagaAction())
                    .sagaStepStatus(SagaStatus.FAILED)
                    .aggregateId(profile.getProfileId())
                    .payload(objectMapper.writeValueAsString(event))
                    .build();
            outboxRepository.save(outbox);
        }
        return ProfileCreationResponse.builder()
                .userId(profile.getUserId())
                .bio(profile.getBio())
                .fullName(profile.getFullName())
                .country(profile.getCountry())
                .build();
    }
}
