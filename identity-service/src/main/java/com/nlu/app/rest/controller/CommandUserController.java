package com.nlu.app.rest.controller;

import jakarta.validation.Valid;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.*;

import com.nlu.app.rest.dto.AppResponse;
import com.nlu.app.rest.dto.request.UserCreationRequest;
import com.nlu.app.rest.dto.response.UserResponse;
import com.nlu.app.rest.service.RestUserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CommandUserController {
    RestUserService restUserService;

    @PostMapping("/registration")
    AppResponse<String> createUser(@RequestBody @Valid UserCreationRequest request) {
        return AppResponse.<String>builder()
                .result(restUserService.createUser(request, UUID.randomUUID().toString()))
                .build();
    }
}
