package com.nlu.app.querySide.controller;

import java.util.List;

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

    @GetMapping
    AppResponse<List<UserResponse>> getUsers() {
        return AppResponse.<List<UserResponse>>builder()
                .result(userService.getUsers())
                .build();
    }

    @GetMapping("/{userId}")
    AppResponse<UserResponse> getUser(@PathVariable("userId") String userId) {
        return AppResponse.<UserResponse>builder()
                .result(userService.getUser(userId))
                .build();
    }

    @GetMapping("/my-info")
    AppResponse<UserResponse> getMyInfo() {
        return AppResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
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
