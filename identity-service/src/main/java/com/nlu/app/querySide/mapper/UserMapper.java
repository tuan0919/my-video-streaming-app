package com.nlu.app.querySide.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.nlu.app.querySide.dto.request.UserCreationRequest;
import com.nlu.app.querySide.dto.request.UserUpdateRequest;
import com.nlu.app.querySide.dto.response.UserResponse;
import com.nlu.app.querySide.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
