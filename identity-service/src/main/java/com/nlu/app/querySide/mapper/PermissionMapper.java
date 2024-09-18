package com.nlu.app.querySide.mapper;

import org.mapstruct.Mapper;

import com.nlu.app.querySide.dto.request.PermissionRequest;
import com.nlu.app.querySide.dto.response.PermissionResponse;
import com.nlu.app.querySide.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
