package com.nlu.app.mapper;

import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_CommentDTO;
import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_SearchUserDTO;
import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_UserPageDetailsDTO;
import com.nlu.app.common.share.dto.comment_service.response.CommentResponse;
import com.nlu.app.common.share.dto.identity_service.response.UserResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileFollowStatusResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileResponseDTO;
import com.nlu.app.common.share.dto.videoStreaming_service.response.VideoCountsResponse;
import com.nlu.app.util.MyDateFormat;
import org.mapstruct.*;

import java.time.LocalDateTime;
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true))
public interface UserAggregateMapper {
    @Mappings({
            @Mapping(target = "user", expression = "java(toDetails(ownerIdentity, ownerProfile))"),
            @Mapping(target = "stats", expression = "java(toStats(statusResponse, videoCountsResponse))"),
            @Mapping(target = "myself", source = "myself"),
            @Mapping(target = "followed", source = "followed")
    })
    ClientView_UserPageDetailsDTO mapToDTO(UserResponse ownerIdentity,
                                           ProfileResponseDTO ownerProfile,
                                           ProfileFollowStatusResponse statusResponse,
                                           VideoCountsResponse videoCountsResponse,
                                           boolean myself,
                                           boolean followed);
    @Mappings({
            @Mapping(target = "userId", source = "ownerIdentity.id"),
            @Mapping(target = "username", source = "ownerIdentity.username"),
            @Mapping(target = "fullName", source = "ownerProfile.fullName"),
            @Mapping(target = "avatar", source = "ownerProfile.avatar"),
            @Mapping(target = "gender", source = "ownerProfile.gender"),
            @Mapping(target = "country", source = "ownerProfile.country"),
            @Mapping(target = "address", source = "ownerProfile.address"),
            @Mapping(target = "bio", source = "ownerProfile.bio"),
    })
    ClientView_UserPageDetailsDTO.UserDetails toDetails(UserResponse ownerIdentity,
                                                        ProfileResponseDTO ownerProfile);
    @Mappings({
            @Mapping(target = "videoCounts", source = "videoCountsResponse.videoCounts"),
            @Mapping(target = "followersCounts", source = "statusResponse.followersCounts"),
            @Mapping(target = "followingCounts", source = "statusResponse.followingCounts"),
    })
    ClientView_UserPageDetailsDTO.UserStats toStats(ProfileFollowStatusResponse statusResponse,
                                                    VideoCountsResponse videoCountsResponse);

/////////////////////////////////////////////////////////////////////
    @Mappings({
            @Mapping(target = "user", expression = "java(toUser(user, profile))"),
            @Mapping(target = "stat", expression = "java(toStat(status))"),
    })
    ClientView_SearchUserDTO toDTO(UserResponse user,
                                   ProfileResponseDTO profile,
                                   ProfileFollowStatusResponse status);
    @Mappings({
            @Mapping(target = "userId", source = "userResponse.id"),
            @Mapping(target = "username", source = "userResponse.username"),
            @Mapping(target = "avatar", source = "profileResponse.avatar"),
            @Mapping(target = "bio", source = "profileResponse.bio"),
    })
    ClientView_SearchUserDTO.User toUser(UserResponse userResponse,
                                         ProfileResponseDTO profileResponse);
    @Mapping(target = "followersCounts", source = "status.followersCounts")
    ClientView_SearchUserDTO.Stat toStat(ProfileFollowStatusResponse status);
}
