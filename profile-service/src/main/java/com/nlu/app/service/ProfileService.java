package com.nlu.app.service;

import com.nlu.app.dto.request.ProfileCreationRequest;
import com.nlu.app.dto.response.ProfileCreationResponse;
import com.nlu.app.entity.Profile;
import com.nlu.app.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;

    public ProfileCreationResponse insert(ProfileCreationRequest request)  {
        var profile = Profile.builder()
            .userId(request.getUserId())
            .avatarId(null)
            .country(request.getCountry())
            .fullName(request.getFullName())
            .bio(request.getBio())
            .build();
        profileRepository.save(profile);
        return ProfileCreationResponse.builder()
                .userId(profile.getUserId())
                .bio(profile.getBio())
                .fullName(profile.getFullName())
                .country(profile.getCountry())
                .build();
    }
}
