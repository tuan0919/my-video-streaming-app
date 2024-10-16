package com.nlu.app.mapper;

import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_CommentDTO;
import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_VideoDetailsDTO;
import com.nlu.app.common.share.dto.comment_service.response.CommentResponse;
import com.nlu.app.common.share.dto.identity_service.response.UserResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileResponseDTO;
import com.nlu.app.common.share.dto.videoStreaming_service.response.VideoDetailsResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true))
public interface CommentAggregateMapper {
    @Mappings({
            @Mapping(target = "action", source = "action"),
            @Mapping(target = "comment", expression = "java(toComment(comment))"),
            @Mapping(target = "ownerProfile", expression = "java(toOwnerProfile(ownerIdentity))")
    })
    ClientView_CommentDTO toDTO(CommentResponse comment, UserResponse ownerIdentity, String action);
    ClientView_CommentDTO.Comment toComment(CommentResponse comment);
    @Mapping(target = "userId", source = "id")
    ClientView_CommentDTO.OwnerProfile toOwnerProfile(UserResponse ownerIdentity);
}
