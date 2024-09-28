package com.nlu.app.mapper;

import com.nlu.app.entity.Permission;
import org.mapstruct.Mapper;

import com.nlu.app.dto.request.PermissionRequest;
import com.nlu.app.dto.response.PermissionResponse;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
