package com.nlu.app.application.identity.query.entity;

import org.mapstruct.Mapper;

import com.nlu.app.rest.dto.request.ProfileCreationRequest;
import com.nlu.app.rest.dto.request.UserCreationRequest;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileCreationRequest toProfileCreationRequest(UserCreationRequest request);
}
