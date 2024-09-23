package com.nlu.app.domain.identity;

import org.mapstruct.Mapper;

import com.nlu.app.rest.dto.request.PermissionRequest;
import com.nlu.app.rest.dto.response.PermissionResponse;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
