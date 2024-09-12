package com.nlu.app.service;

import com.nlu.app.entity.Profile;
import com.nlu.app.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;

    public boolean insert(Profile profile) {
        profileRepository.save(profile);
        return true;
    }
}
