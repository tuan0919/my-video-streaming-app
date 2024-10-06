package com.nlu.app.mapper;

import com.nlu.app.entity.Permission;
import org.mapstruct.Mapper;

import com.nlu.app.dto.request.PermissionRequest;
import com.nlu.app.common.share.dto.identity_service.response.PermissionResponse;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
