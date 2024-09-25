package com.nlu.app.application.identity.query.entity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nlu.app.rest.dto.request.RoleRequest;
import com.nlu.app.rest.dto.response.RoleResponse;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
