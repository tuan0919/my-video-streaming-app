package com.nlu.app.querySide.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nlu.app.querySide.dto.request.RoleRequest;
import com.nlu.app.querySide.dto.response.RoleResponse;
import com.nlu.app.querySide.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}