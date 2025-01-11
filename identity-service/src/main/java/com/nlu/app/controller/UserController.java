package com.nlu.app.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.nlu.app.dto.AppResponse;
import com.nlu.app.dto.request.UserCreationRequest;
import com.nlu.app.dto.request.UserUpdateRequest;
import com.nlu.app.common.share.dto.identity_service.response.UserResponse;
import com.nlu.app.service.UserService;

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
    AppResponse<String> createUser(@RequestBody @Valid UserCreationRequest request) {
        return AppResponse.<String>builder()
                .result(userService.createUser(request))
                .build();
    }

    @GetMapping
    AppResponse<List<UserResponse>> getUsers() {
        return AppResponse.<List<UserResponse>>builder()
                .result(userService.getUsers())
                .build();
    }

    @GetMapping("/get/{userId}")
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

    @GetMapping("/search/id")
    AppResponse<List<String>> searchUserIdByUsername(@RequestParam("page") Integer page,
                                                     @RequestParam("pageSize") Integer pageSize,
                                                     String username) {
        return AppResponse.<List<String>>builder()
                .result(userService.searchUserIdByUsername(page, pageSize, username)
                        .stream().toList())
                .build();
    }

    @PostMapping("/query/map-by-ids")
    AppResponse<Map<String, UserResponse>> getUsersAndMapByIds(@RequestBody List<String> userIds) {
        var response = userService.getUsersMapByIds(userIds);
        return AppResponse.<Map<String, UserResponse>>builder()
                .result(response)
                .build();
    }
}
