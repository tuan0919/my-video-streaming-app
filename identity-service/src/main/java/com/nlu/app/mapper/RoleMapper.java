package com.nlu.app.mapper;

import com.nlu.app.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nlu.app.dto.request.RoleRequest;
import com.nlu.app.common.share.dto.identity_service.response.RoleResponse;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
