package com.nlu.app.querySide.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nlu.app.commandSide.state.entity.Role;
import com.nlu.app.rest.dto.request.RoleRequest;
import com.nlu.app.rest.dto.response.RoleResponse;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
