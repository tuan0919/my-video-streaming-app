package com.nlu.app.controller;

import com.nlu.app.common.share.dto.notification_service.request.NotificationCreationRequest;
import com.nlu.app.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationController {
    NotificationService service;

    @PostMapping("/internal")
    public String insert(@RequestBody NotificationCreationRequest request) {
        return service.insert(request);
    }
}
