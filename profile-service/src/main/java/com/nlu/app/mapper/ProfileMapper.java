package com.nlu.app.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.common.share.dto.profile_service.request.ProfileCreationRequest;
import com.nlu.app.common.share.dto.profile_service.response.ProfileCreationResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileFollowStatusResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileResponseDTO;
import com.nlu.app.common.share.event.ProfileCreatedEvent;
import com.nlu.app.common.share.event.ProfileRemovedEvent;
import com.nlu.app.common.share.event.ProfileUpdatedEvent;
import com.nlu.app.entity.Outbox;
import com.nlu.app.entity.Profile;
import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import org.mapstruct.*;

import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true))
public interface ProfileMapper {
    Profile toEntity(ProfileCreationRequest request);
    ProfileCreatedEvent toProfileCreatedEvent(Profile profile);
    ProfileRemovedEvent toProfileRemovedEvent(Profile profile);
    ProfileUpdatedEvent toProfileUpdatedEvent(Profile profile);
    ProfileCreationResponse toResponseCreationDTO(Profile profile);
    ProfileResponseDTO toResponseDTO(Profile profile);
    @Mappings({
            @Mapping(target = "followingCounts", expression = "java(getSize(profile.getFollow()))"),
            @Mapping(target = "followersCounts", expression = "java(getSize(profile.getFollowers()))")
    })
    ProfileFollowStatusResponse toResponseFollowStatusDTO(Profile profile);
    default int getSize(Set<?> set) {
        return set != null ? set.size() : 0;
    }
}
