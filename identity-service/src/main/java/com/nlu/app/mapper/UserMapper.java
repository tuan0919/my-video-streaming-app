package com.nlu.app.mapper;

import com.nlu.app.common.share.event.IdentityUpdatedEvent;
import com.nlu.app.common.share.event.UserCreatedEvent;
import com.nlu.app.entity.Role;
import com.nlu.app.entity.User;
import org.mapstruct.*;

import com.nlu.app.dto.request.UserCreationRequest;
import com.nlu.app.dto.request.UserUpdateRequest;
import com.nlu.app.dto.response.UserResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {RoleMapper.class, PermissionMapper.class},
        builder = @Builder(disableBuilder = true))
public interface UserMapper {
    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);
    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
    @Mapping(target = "userId", source = "user.id")
    UserCreatedEvent toUserCreatedEvent(User user);
    @Mapping(target = "roles", source = "user.roles", qualifiedByName = "mapRoles")
    IdentityUpdatedEvent toIdentityUpdatedEvent(User user);

    @Named("mapRoles")
    default List<String> mapRoles(Set<Role> roles) {
        return roles.stream().map(Role::getName).toList();
    }
}
