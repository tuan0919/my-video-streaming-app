package com.nlu.app.service;

import com.nlu.app.common.share.dto.profile_service.request.ProfileCreationRequest;
import com.nlu.app.common.share.dto.profile_service.response.ProfileCreationResponse;
import com.nlu.app.repository.webclient.ProfileWebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {
    final ProfileWebClient profileWebClient;
    public String createProfile(ProfileCreationRequest request) {
        return profileWebClient.createProfile(request).block().getResult();
    }
}
