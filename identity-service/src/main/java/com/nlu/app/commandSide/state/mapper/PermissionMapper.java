package com.nlu.app.commandSide.state.mapper;

import org.mapstruct.Mapper;

import com.nlu.app.commandSide.state.entity.Permission;
import com.nlu.app.rest.dto.request.PermissionRequest;
import com.nlu.app.rest.dto.response.PermissionResponse;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
