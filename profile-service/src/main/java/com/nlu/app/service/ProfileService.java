package com.nlu.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.event.ProfileCreatedEvent;
import com.nlu.app.dto.request.ProfileCreationRequest;
import com.nlu.app.dto.response.ProfileCreationResponse;
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
    public ProfileCreationResponse insert(ProfileCreationRequest request)  {
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
        try {
            Outbox outbox = Outbox.builder()
                    .type("profile")
                    .aggregateType("created")
                    .aggregateId(profile.getProfileId())
                    .payload(objectMapper.writeValueAsString(event))
                    .build();
            outboxRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ProfileCreationResponse.builder()
                .userId(profile.getUserId())
                .bio(profile.getBio())
                .fullName(profile.getFullName())
                .country(profile.getCountry())
                .build();
    }
}
