package com.nlu.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nlu.app.common.share.dto.CompensationRequest;
import com.nlu.app.common.share.dto.profile_service.request.ProfileCreationRequest;
import com.nlu.app.common.share.dto.profile_service.response.ProfileCreationResponse;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.service.CompensationService;
import com.nlu.app.service.ProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.PostExchange;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProfileController {
    ProfileService profileService;
    CompensationService compensationService;

    @PostMapping("/internal/new")
    public AppResponse<ProfileCreationResponse> create(@RequestBody ProfileCreationRequest request) throws JsonProcessingException {
        return AppResponse.<ProfileCreationResponse>builder()
                .result(profileService.insert(request))
                .build();
    }

    @PostMapping("/internal/compensation")
    public String compensation(@RequestBody CompensationRequest request) throws JsonProcessingException {
        compensationService.doCompensation(request.getSagaId());
        return "OK";
    }
}
