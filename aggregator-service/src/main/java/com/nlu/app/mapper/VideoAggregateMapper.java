package com.nlu.app.mapper;

import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_VideoDetailsDTO;
import com.nlu.app.common.share.dto.identity_service.response.UserResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileResponseDTO;
import com.nlu.app.common.share.dto.videoStreaming_service.response.VideoDetailsResponse;
import com.nlu.app.util.MyDateFormat;
import org.mapstruct.*;

import java.time.LocalDateTime;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true))
public interface VideoAggregateMapper {
    MyDateFormat formatter = new MyDateFormat();
    @Mapping(target = "isFollowed", source = "isFollowing")
    @Mapping(target = "ownerProfile", expression = "java(toOwnerProfile(ownerIdentity, ownerProfile))")
    @Mapping(target = "stat", expression = "java(toVideoStats(videoStat))")
    ClientView_VideoDetailsDTO mapToDTO(UserResponse ownerIdentity,
                                        ProfileResponseDTO ownerProfile,
                                        Boolean isFollowing,
                                        VideoDetailsResponse videoStat);


    @Mappings({
            @Mapping(target = "userId", source = "identity.id"),
            @Mapping(target = "username", source = "identity.username"),
            @Mapping(target = "fullName", source = "profile.fullName")
    })
    ClientView_VideoDetailsDTO.OwnerProfile toOwnerProfile(UserResponse identity,
                                                     ProfileResponseDTO profile);
    @Mapping(target = "createTime", source = "stat.createAt", qualifiedByName = "mapToTime")
    ClientView_VideoDetailsDTO.VideoStats toVideoStats(VideoDetailsResponse stat);

    @Named("mapToTime")
    default String mapToTime(LocalDateTime dateTime) {
        return formatter.relativeToCurrentTime(dateTime);
    }
}
