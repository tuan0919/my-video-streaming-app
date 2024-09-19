package com.nlu.app.commandSide.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.nlu.app.querySide.dto.AppResponse;
import com.nlu.app.querySide.dto.request.RoleRequest;
import com.nlu.app.querySide.dto.response.RoleResponse;
import com.nlu.app.querySide.service.RoleService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleController {
    RoleService roleService;

    @PostMapping
    AppResponse<RoleResponse> create(@RequestBody RoleRequest request) {
        return AppResponse.<RoleResponse>builder()
                .result(roleService.create(request))
                .build();
    }

    @GetMapping
    AppResponse<List<RoleResponse>> getAll() {
        return AppResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll())
                .build();
    }

    @DeleteMapping("/{role}")
    AppResponse<Void> delete(@PathVariable String role) {
        roleService.delete(role);
        return AppResponse.<Void>builder().build();
    }
}
