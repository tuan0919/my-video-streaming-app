package com.nlu.app.rest.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.nlu.app.rest.service.CommandUserService;
import com.nlu.app.querySide.dto.AppResponse;
import com.nlu.app.querySide.dto.request.UserCreationRequest;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CommandUserController {
    CommandUserService commandUserService;

    @PostMapping("/registration")
    AppResponse<String> createUser(@RequestBody @Valid UserCreationRequest request) {
        return AppResponse.<String>builder()
                .result(commandUserService.createUser(request))
                .build();
    }
}