package com.nlu.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nlu.app.common.share.dto.CompensationRequest;
import com.nlu.app.common.share.dto.notification_service.request.NotificationCreationRequest;
import com.nlu.app.service.CompensationService;
import com.nlu.app.service.NotificationService;
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
public class NotificationController {
    NotificationService service;
    CompensationService compensationService;

    @PostMapping("/internal")
    public String insert(@RequestBody NotificationCreationRequest request) throws JsonProcessingException {
        return service.insert(request);
    }

    @PostMapping("/internal/compensation")
    public String compensation(@RequestBody CompensationRequest request) throws JsonProcessingException {
        compensationService.doCompensation(request.getSagaId());
        return "OK";
    }
}
