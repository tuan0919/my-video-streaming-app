package com.nlu.app.querySide.mapper;

import org.mapstruct.Mapper;

import com.nlu.app.querySide.dto.request.ProfileCreationRequest;
import com.nlu.app.querySide.dto.request.UserCreationRequest;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileCreationRequest toProfileCreationRequest(UserCreationRequest request);
}
