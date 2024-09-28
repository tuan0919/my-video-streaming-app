package com.nlu.app.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.nlu.app.service.PermissionService;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.dto.request.PermissionRequest;
import com.nlu.app.dto.response.PermissionResponse;

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
