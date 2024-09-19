package com.nlu.app.commandSide.controller;

import java.util.concurrent.CompletableFuture;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.nlu.app.commandSide.service.UserCommandService;
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
public class UserController {
    UserCommandService userCommandService;

    @PostMapping("/registration")
    CompletableFuture<AppResponse<String>> createUser(@RequestBody @Valid UserCreationRequest request) {
        return userCommandService.createUser(request).thenApply(result -> AppResponse.<String>builder()
                .result(result)
                .build());
    }
}