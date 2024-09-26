package com.nlu.app.controller;

import com.nlu.app.dto.AppResponse;
import com.nlu.app.dto.request.ProfileCreationRequest;
import com.nlu.app.dto.response.ProfileCreationResponse;
import com.nlu.app.service.ProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProfileController {
    ProfileService profileService;

    @PostMapping
    public AppResponse<ProfileCreationResponse> create(@RequestBody ProfileCreationRequest request) {
        return AppResponse.<ProfileCreationResponse>builder()
                .result(profileService.insert(request))
                .build();
    }
}