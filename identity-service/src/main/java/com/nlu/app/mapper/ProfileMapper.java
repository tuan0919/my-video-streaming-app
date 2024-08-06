package com.nlu.app.mapper;

import org.mapstruct.Mapper;

import com.nlu.app.dto.request.ProfileCreationRequest;
import com.nlu.app.dto.request.UserCreationRequest;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileCreationRequest toProfileCreationRequest(UserCreationRequest request);
}
