package com.nlu.app.commandSide.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.nlu.app.querySide.dto.AppResponse;
import com.nlu.app.querySide.dto.request.UserCreationRequest;
import com.nlu.app.querySide.dto.request.UserUpdateRequest;
import com.nlu.app.querySide.dto.response.UserResponse;
import com.nlu.app.querySide.service.UserService;

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
    UserService userService;

    @PostMapping("/registration")
    AppResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return AppResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @DeleteMapping("/{userId}")
    AppResponse<String> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return AppResponse.<String>builder().result("User has been deleted").build();
    }

    @PutMapping("/{userId}")
    AppResponse<UserResponse> updateUser(@PathVariable String userId, @RequestBody UserUpdateRequest request) {
        return AppResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }
}
