package com.nlu.app.querySide.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.nlu.app.querySide.dto.AppResponse;
import com.nlu.app.querySide.dto.request.PermissionRequest;
import com.nlu.app.querySide.dto.response.PermissionResponse;
import com.nlu.app.querySide.service.PermissionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PermissionController {
    PermissionService permissionService;

    @PostMapping
    AppResponse<PermissionResponse> create(@RequestBody PermissionRequest request) {
        return AppResponse.<PermissionResponse>builder()
                .result(permissionService.create(request))
                .build();
    }

    @GetMapping
    AppResponse<List<PermissionResponse>> getAll() {
        return AppResponse.<List<PermissionResponse>>builder()
                .result(permissionService.getAll())
                .build();
    }

    @DeleteMapping("/{permission}")
    AppResponse<Void> delete(@PathVariable String permission) {
        permissionService.delete(permission);
        return AppResponse.<Void>builder().build();
    }
}
