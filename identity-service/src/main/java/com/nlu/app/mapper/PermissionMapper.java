package com.nlu.app.mapper;

import org.mapstruct.Mapper;

import com.nlu.app.dto.request.PermissionRequest;
import com.nlu.app.dto.response.PermissionResponse;
import com.nlu.app.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
