package com.nlu.app.mapper;

import com.nlu.app.common.share.event.IdentityUpdatedEvent;
import com.nlu.app.common.share.event.UserCreatedEvent;
import com.nlu.app.entity.Role;
import com.nlu.app.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.nlu.app.dto.request.UserCreationRequest;
import com.nlu.app.dto.request.UserUpdateRequest;
import com.nlu.app.dto.response.UserResponse;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);
    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
    UserCreatedEvent toUserCreatedEvent(User user);
    @Mapping(target = "roles", source = "user.roles", qualifiedByName = "mapRoles")
    IdentityUpdatedEvent toIdentityUpdatedEvent(User user);
    @Named("mapRoles")
    default List<String> mapRoles(Set<Role> roles) {
        return roles.stream().map(Role::getName).toList();
    }
}
