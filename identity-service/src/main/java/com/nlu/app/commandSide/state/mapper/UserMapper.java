package com.nlu.app.commandSide.state.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.nlu.app.commandSide.state.entity.User;
import com.nlu.app.rest.dto.request.UserCreationRequest;
import com.nlu.app.rest.dto.request.UserUpdateRequest;
import com.nlu.app.rest.dto.response.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
