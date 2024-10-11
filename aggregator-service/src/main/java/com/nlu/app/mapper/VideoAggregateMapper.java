package com.nlu.app.mapper;

import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_VideoDetailsDTO;
import com.nlu.app.common.share.dto.identity_service.response.UserResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileResponseDTO;
import com.nlu.app.common.share.dto.videoStreaming_service.response.VideoDetailsResponse;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true))
public interface VideoAggregateMapper {
    ClientView_VideoDetailsDTO mapToDTO(UserResponse ownerIdentity,
                                        ProfileResponseDTO ownerProfile,
                                        Boolean isFollowing,
                                        VideoDetailsResponse videoStat);
}
